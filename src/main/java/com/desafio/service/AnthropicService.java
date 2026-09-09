package com.desafio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serviço de integração com a API da Anthropic (Claude).
 *
 * Responsável pelas três funcionalidades de IA:
 * 1. Resposta automática a mensagens de tarefas (assistente contextual, com
 * memória do histórico da conversa e conhecimento da equipe alocada)
 * 2. Sugestão de prazo com base em título, descrição e departamento,
 * incluindo classificação de complexidade e justificativa
 * 3. Geração automática de descrição a partir do título e do departamento
 *
 * A chave de API é lida da variável de ambiente ANTHROPIC_API_KEY.
 */
@Slf4j
@Service
public class AnthropicService {

    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";

    // Modelo Sonnet atual da Anthropic (substitui o snapshot legado
    // "claude-sonnet-4-20250514").
    private static final String MODEL = "claude-sonnet-5";

    private static final String API_VERSION = "2023-06-01";
    private static final int TIMEOUT_SECS = 20;

    // Tarefas objetivas (extração de número/JSON) pedem menos tokens e menos
    // aleatoriedade; texto natural (descrição/mensagem) pede um pouco mais de
    // liberdade para não soar robótico.
    private static final double TEMPERATURE_DETERMINISTICA = 0.2;
    private static final double TEMPERATURE_NATURAL = 0.65;

    private static final int MAX_TOKENS_CURTO = 200; // JSON de prazo
    private static final int MAX_TOKENS_MEDIO = 400; // descrição / mensagem

    @Value("${anthropic.api.key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECS))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─────────────────────────────────────────────────────────────────────────
    // DTO interno — resultado estruturado da Feature 2 (prazo + complexidade)
    // ─────────────────────────────────────────────────────────────────────────

    @Getter
    public static class PrazoSugerido {
        private final int dias;
        private final String complexidade;
        private final String justificativa;

        public PrazoSugerido(int dias, String complexidade, String justificativa) {
            this.dias = dias;
            this.complexidade = complexidade;
            this.justificativa = justificativa;
        }

        public static PrazoSugerido fallback() {
            return new PrazoSugerido(7, "Média",
                    "Não foi possível estimar automaticamente; usando prazo padrão de 7 dias.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Feature 1 – Resposta automática a mensagens de tarefas
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gera uma resposta automática para a mensagem enviada pelo usuário,
     * usando o contexto completo da tarefa (título, descrição, prazo,
     * departamento, status), o histórico de mensagens já respondidas nessa
     * tarefa e a equipe atualmente alocada.
     *
     * @param tarefaTitulo      Título da tarefa
     * @param tarefaDescricao   Descrição da tarefa
     * @param prazo             Prazo no formato ISO (yyyy-MM-dd)
     * @param departamento      Nome do departamento
     * @param status            Status atual ("Pendente", "Em Andamento",
     *                          "Finalizada")
     * @param perguntaUsuario   Texto da mensagem enviada pelo usuário
     * @param historicoConversa Últimas perguntas/respostas já trocadas nesta
     *                          tarefa, formatadas em texto simples (pode ser
     *                          vazio se for a primeira mensagem)
     * @param equipeAlocada     Nomes das pessoas alocadas na tarefa,
     *                          separados por vírgula (pode ser vazio)
     * @return Resposta gerada pela IA, ou mensagem de fallback em caso de erro
     */
    public String gerarRespostaParaMensagem(
            String tarefaTitulo,
            String tarefaDescricao,
            String prazo,
            String departamento,
            String status,
            String perguntaUsuario,
            String historicoConversa,
            String equipeAlocada) {

        String system = "Você é um assistente de gerenciamento de tarefas, atuando como um coordenador " +
                "experiente e objetivo. Responda sempre em português, de forma direta e prática, em no " +
                "máximo 3 parágrafos. Não use markdown, listas ou formatação especial — apenas texto simples. " +
                "Use o histórico da conversa (quando houver) para não repetir informações já dadas e para " +
                "manter continuidade entre as perguntas do colaborador. Quando fizer sentido, você pode " +
                "mencionar os colegas alocados na tarefa (por nome) como referência de com quem o colaborador " +
                "pode alinhar o trabalho. Se a pergunta fugir do escopo da tarefa ou exigir uma decisão que só " +
                "o administrador pode tomar, diga isso claramente e sugira o encaminhamento pelo canal " +
                "\"Enviar ao admin\".";

        StringBuilder contexto = new StringBuilder();
        contexto.append("Contexto da tarefa:\n")
                .append("- Título: ").append(tarefaTitulo).append('\n')
                .append("- Descrição: ").append(tarefaDescricao).append('\n')
                .append("- Prazo: ").append(prazo).append('\n')
                .append("- Departamento: ").append(departamento).append('\n')
                .append("- Status: ").append(status).append('\n');

        if (equipeAlocada != null && !equipeAlocada.isBlank()) {
            contexto.append("- Equipe alocada: ").append(equipeAlocada).append('\n');
        }

        if (historicoConversa != null && !historicoConversa.isBlank()) {
            contexto.append("\nHistórico de mensagens anteriores nesta tarefa:\n")
                    .append(historicoConversa).append('\n');
        }

        contexto.append("\nNova mensagem do colaborador: \"").append(perguntaUsuario).append("\"");

        return chamarAPI(system, contexto.toString(), TEMPERATURE_NATURAL, MAX_TOKENS_MEDIO,
                "Obrigado pela sua mensagem. O administrador irá analisá-la em breve.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Feature 2 – Sugestão de prazo com classificação de complexidade
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Estima um prazo adequado (em dias corridos a partir de hoje) para uma
     * tarefa, com base no título, na descrição e no departamento informados.
     * A IA primeiro classifica a complexidade da tarefa e só então estima os
     * dias, retornando tudo em JSON estruturado — o que torna a sugestão
     * auditável em vez de um número isolado.
     *
     * @param titulo       Título da tarefa
     * @param descricao    Descrição da tarefa
     * @param departamento Nome do departamento (ajuda a IA a entender o
     *                     domínio/tipo de trabalho envolvido)
     * @return {@link PrazoSugerido} com dias, complexidade e justificativa
     */
    public PrazoSugerido sugerirPrazoDetalhado(String titulo, String descricao, String departamento) {
        String system = "Você é um especialista em gerenciamento de projetos. Sua tarefa é analisar o " +
                "título, a descrição e o departamento de uma tarefa e estimar um prazo realista para " +
                "concluí-la. Primeiro raciocine internamente sobre a complexidade da tarefa, considerando " +
                "o tipo de trabalho típico do departamento informado (por exemplo: tarefas de tecnologia " +
                "podem envolver desenvolvimento, testes e revisão; tarefas administrativas costumam ser " +
                "mais rápidas). Depois, responda APENAS com um objeto JSON válido, sem markdown, sem texto " +
                "antes ou depois, no seguinte formato exato:\n" +
                "{\"dias\": <inteiro entre 1 e 90>, \"complexidade\": \"Simples|Média|Complexa|Muito complexa\", " +
                "\"justificativa\": \"<uma frase curta explicando a estimativa>\"}";

        String user = String.format(
                "Título: %s\nDescrição: %s\nDepartamento: %s",
                titulo, descricao, departamento == null || departamento.isBlank() ? "Não informado" : departamento);

        String raw = chamarAPI(system, user, TEMPERATURE_DETERMINISTICA, MAX_TOKENS_CURTO, null);

        if (raw == null || raw.isBlank()) {
            return PrazoSugerido.fallback();
        }

        PrazoSugerido resultado = tentarParsearJsonDePrazo(raw);
        return resultado != null ? resultado : PrazoSugerido.fallback();
    }

    private PrazoSugerido tentarParsearJsonDePrazo(String raw) {
        String jsonCandidato = extrairPrimeiroObjetoJson(raw);
        if (jsonCandidato == null) {
            // Fallback: tenta extrair apenas os primeiros dígitos da resposta.
            String apenasDigitos = raw.replaceAll("[^0-9]", "");
            if (!apenasDigitos.isEmpty()) {
                try {
                    int dias = Math.min(90, Math.max(1, Integer.parseInt(apenasDigitos.substring(0,
                            Math.min(2, apenasDigitos.length())))));
                    return new PrazoSugerido(dias, "Média", "Estimativa aproximada (formato inesperado da IA).");
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
            return null;
        }

        try {
            JsonNode node = objectMapper.readTree(jsonCandidato);
            int dias = Math.min(365, Math.max(1, node.path("dias").asInt(7)));
            String complexidade = node.path("complexidade").asText("Média");
            String justificativa = node.path("justificativa").asText("Estimativa gerada pela IA.");
            return new PrazoSugerido(dias, complexidade, justificativa);
        } catch (Exception e) {
            log.error("[AnthropicService] Falha ao parsear JSON de prazo: {}", e.getMessage());
            return null;
        }
    }

    private String extrairPrimeiroObjetoJson(String texto) {
        Matcher matcher = Pattern.compile("\\{.*\\}", Pattern.DOTALL).matcher(texto);
        return matcher.find() ? matcher.group() : null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Feature 3 – Geração automática de descrição contextual
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gera uma descrição detalhada e profissional para uma tarefa com base no
     * título e no departamento informados. A IA é instruída a interpretar o
     * domínio da tarefa (a partir do departamento) e nunca apenas repetir o
     * título literalmente.
     *
     * @param titulo       Título da tarefa
     * @param departamento Nome do departamento selecionado
     * @return Descrição gerada pela IA, ou {@code null} se a geração falhar
     *         ou resultar em texto idêntico/quase idêntico ao título (o
     *         chamador decide o fallback a ser exibido ao usuário)
     */
    public String gerarDescricao(String titulo, String departamento) {
        String system = "Você é um analista de projetos sênior. Sua tarefa é interpretar o título de uma " +
                "tarefa e, com base no departamento informado, escrever uma descrição profissional e " +
                "realista do que essa tarefa provavelmente envolve. NUNCA repita o título literalmente ou " +
                "quase literalmente — elabore a partir dele. A descrição deve ter de 2 a 3 frases e cobrir: " +
                "(1) o que precisa ser feito, (2) o contexto ou domínio técnico/de negócio envolvido, " +
                "considerando o departamento, e (3) o que se espera como entrega ou critério de conclusão. " +
                "Não invente números, nomes ou prazos específicos. Não use markdown. Texto simples apenas.";

        String user = String.format(
                "Título da tarefa: %s\nDepartamento: %s",
                titulo, departamento == null || departamento.isBlank() ? "Não informado" : departamento);

        String resultado = chamarAPI(system, user, TEMPERATURE_NATURAL, MAX_TOKENS_MEDIO, null);

        if (resultado == null || resultado.isBlank()) {
            return null;
        }

        // Guarda de qualidade: se a IA apenas devolveu o título (ou algo muito
        // próximo disso), trata como falha para o chamador decidir o fallback.
        String tituloNormalizado = normalizar(titulo);
        String resultadoNormalizado = normalizar(resultado);
        if (resultadoNormalizado.equals(tituloNormalizado)
                || resultadoNormalizado.length() <= tituloNormalizado.length() + 5) {
            log.warn("[AnthropicService] Descrição gerada é praticamente igual ao título. Descartando.");
            return null;
        }

        return resultado;
    }

    private String normalizar(String texto) {
        if (texto == null)
            return "";
        return texto.trim().toLowerCase().replaceAll("[.!?]+$", "");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Método interno – chamada HTTP à API da Anthropic
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param system      Instruções de papel/regras para a IA (mensagem de
     *                    sistema). Pode ser {@code null}.
     * @param userContent Conteúdo específico da requisição (dados da
     *                    tarefa/pergunta do usuário).
     * @param temperature Controla a aleatoriedade da resposta.
     * @param maxTokens   Limite de tokens da resposta.
     * @param fallback    Valor devolvido em caso de erro ou chave ausente.
     *                    Pode ser {@code null} — nesse caso o chamador decide
     *                    o que fazer com um retorno nulo/vazio.
     */
    private String chamarAPI(String system, String userContent, double temperature, int maxTokens,
            String fallback) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[AnthropicService] ANTHROPIC_API_KEY não configurada. Retornando fallback.");
            return fallback;
        }

        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", MODEL);
            root.put("max_tokens", maxTokens);
            root.put("temperature", temperature);
            if (system != null && !system.isBlank()) {
                root.put("system", system);
            }

            ArrayNode messages = root.putArray("messages");
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", userContent);

            String body = objectMapper.writeValueAsString(root);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ANTHROPIC_URL))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECS))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", API_VERSION)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("[AnthropicService] Erro HTTP {}: {}", response.statusCode(), response.body());
                return fallback;
            }

            JsonNode responseRoot = objectMapper.readTree(response.body());
            JsonNode content = responseRoot.path("content");
            if (content.isArray() && content.size() > 0) {
                String texto = content.get(0).path("text").asText(fallback);
                return texto == null ? fallback : texto.trim();
            }

            return fallback;

        } catch (Exception e) {
            log.error("[AnthropicService] Falha na chamada à API: {}", e.getMessage());
            return fallback;
        }
    }
}
