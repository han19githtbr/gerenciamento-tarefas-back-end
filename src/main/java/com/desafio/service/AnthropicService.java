package com.desafio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Serviço de integração com a API da Anthropic (Claude).
 *
 * Responsável pelas três funcionalidades de IA:
 * 1. Resposta automática a mensagens de tarefas (assistente contextual)
 * 2. Sugestão de prazo com base em título e descrição
 * 3. Geração automática de descrição a partir do título
 *
 * A chave de API é lida da variável de ambiente ANTHROPIC_API_KEY.
 */
@Slf4j
@Service
public class AnthropicService {

    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-20250514";
    private static final String API_VERSION = "2023-06-01";
    private static final int MAX_TOKENS = 400;
    private static final int TIMEOUT_SECS = 20;

    @Value("${anthropic.api.key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECS))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─────────────────────────────────────────────────────────────────────────
    // Feature 1 – Resposta automática a mensagens de tarefas
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gera uma resposta automática para a mensagem enviada pelo usuário,
     * usando o contexto completo da tarefa (título, descrição, prazo, status).
     *
     * @param tarefaTitulo    Título da tarefa
     * @param tarefaDescricao Descrição da tarefa
     * @param prazo           Prazo no formato ISO (yyyy-MM-dd)
     * @param departamento    Nome do departamento
     * @param status          Status atual ("Pendente", "Em Andamento",
     *                        "Finalizada")
     * @param perguntaUsuario Texto da mensagem enviada pelo usuário
     * @return Resposta gerada pela IA, ou mensagem de fallback em caso de erro
     */
    public String gerarRespostaParaMensagem(
            String tarefaTitulo,
            String tarefaDescricao,
            String prazo,
            String departamento,
            String status,
            String perguntaUsuario) {

        String prompt = String.format(
                "Você é um assistente de gerenciamento de tarefas. Responda em português, " +
                        "de forma objetiva e útil, como se fosse um coordenador experiente. " +
                        "Não use markdown, listas ou formatação especial — texto simples apenas.\n\n" +
                        "Contexto da tarefa:\n" +
                        "- Título: %s\n" +
                        "- Descrição: %s\n" +
                        "- Prazo: %s\n" +
                        "- Departamento: %s\n" +
                        "- Status: %s\n\n" +
                        "Mensagem do colaborador: \"%s\"\n\n" +
                        "Responda de forma direta e prática em no máximo 3 parágrafos.",
                tarefaTitulo, tarefaDescricao, prazo, departamento, status, perguntaUsuario);

        return chamarAPI(prompt, "Obrigado pela sua mensagem. O administrador irá analisá-la em breve.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Feature 2 – Sugestão de prazo
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Estima um prazo adequado (em dias a partir de hoje) para uma tarefa,
     * baseando-se no título e na descrição fornecidos.
     *
     * @param titulo    Título da tarefa
     * @param descricao Descrição da tarefa
     * @return Número de dias sugerido como string (ex: "7"), ou "7" como fallback
     */
    public String sugerirPrazoEmDias(String titulo, String descricao) {
        String prompt = String.format(
                "Você é um especialista em gerenciamento de projetos. " +
                        "Com base no título e descrição abaixo, estime quantos dias corridos " +
                        "seriam necessários para concluir essa tarefa. " +
                        "Responda APENAS com o número inteiro de dias, sem texto adicional, " +
                        "sem unidade, sem ponto final. Exemplo de resposta válida: 5\n\n" +
                        "Título: %s\nDescrição: %s",
                titulo, descricao);

        String raw = chamarAPI(prompt, "7").trim().replaceAll("[^0-9]", "");
        if (raw.isEmpty())
            return "7";

        try {
            int dias = Integer.parseInt(raw);
            // Limita entre 1 e 365 dias
            return String.valueOf(Math.min(365, Math.max(1, dias)));
        } catch (NumberFormatException e) {
            return "7";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Feature 3 – Geração automática de descrição
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gera uma descrição detalhada e profissional para uma tarefa
     * com base apenas no título informado.
     *
     * @param titulo Título da tarefa
     * @return Descrição gerada pela IA, ou texto de fallback em caso de erro
     */
    public String gerarDescricao(String titulo) {
        String prompt = String.format(
                "Você é um analista de projetos. Com base no título de tarefa abaixo, " +
                        "escreva uma descrição profissional e objetiva para ela. " +
                        "A descrição deve ter entre 1 e 2 frases, explicar o objetivo da tarefa " +
                        "e o que se espera como entrega. Não use markdown. Texto simples apenas.\n\n" +
                        "Título: %s",
                titulo);

        return chamarAPI(prompt, titulo);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Método interno – chamada HTTP à API da Anthropic
    // ─────────────────────────────────────────────────────────────────────────

    private String chamarAPI(String promptTexto, String fallback) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[AnthropicService] ANTHROPIC_API_KEY não configurada. Retornando fallback.");
            return fallback;
        }

        try {
            // Escapa aspas duplas no prompt para JSON seguro
            String promptEscapado = promptTexto
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");

            String body = String.format(
                    "{\"model\":\"%s\",\"max_tokens\":%d,\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                    MODEL, MAX_TOKENS, promptEscapado);

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

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("content");
            if (content.isArray() && content.size() > 0) {
                return content.get(0).path("text").asText(fallback).trim();
            }

            return fallback;

        } catch (Exception e) {
            log.error("[AnthropicService] Falha na chamada à API: {}", e.getMessage());
            return fallback;
        }
    }
}
