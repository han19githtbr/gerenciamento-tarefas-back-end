package com.desafio.controllers;

import com.desafio.service.AnthropicService;
import com.desafio.service.AnthropicService.PrazoSugerido;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Controller que expõe os três endpoints de IA para o front-end Angular.
 *
 * Rotas:
 * POST /ia/sugerir-prazo → Feature 2: retorna dias, complexidade e
 * justificativa sugeridos
 * POST /ia/gerar-descricao → Feature 3: retorna descrição gerada + flag
 * indicando se a IA de fato gerou o conteúdo
 * POST /ia/responder-mensagem → Feature 1: resposta contextual (usada
 * internamente)
 *
 * Obs.: a Feature 1 também é disparada automaticamente pelo TarefaService
 * ao salvar uma mensagem, já enriquecida com histórico da conversa e equipe
 * alocada. Este endpoint é exposto opcionalmente para testes manuais, sem
 * esse enriquecimento automático.
 */
@RestController
@RequestMapping("/ia")
@RequiredArgsConstructor
public class AiController {

    private final AnthropicService anthropicService;

    // ─── Feature 2: Sugestão de prazo (com complexidade) ─────────────────────

    /**
     * Recebe título, descrição e departamento de uma tarefa e retorna dias,
     * complexidade e justificativa sugeridos pela IA para o prazo.
     *
     * Body: { "titulo": "...", "descricao": "...", "departamento": "..." }
     * Response: { "diasSugeridos": 12, "prazoSugerido": "2026-09-20",
     * "complexidade": "Complexa", "justificativa": "..." }
     */
    @PostMapping("/sugerir-prazo")
    public ResponseEntity<?> sugerirPrazo(@RequestBody Map<String, String> body) {
        String titulo = body.getOrDefault("titulo", "").trim();
        String descricao = body.getOrDefault("descricao", "").trim();
        String departamento = body.getOrDefault("departamento", "").trim();

        if (titulo.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "O campo 'titulo' é obrigatório."));
        }

        PrazoSugerido resultado = anthropicService.sugerirPrazoDetalhado(titulo, descricao, departamento);
        LocalDate prazoSugerido = LocalDate.now().plusDays(resultado.getDias());

        return ResponseEntity.ok(Map.of(
                "diasSugeridos", resultado.getDias(),
                "prazoSugerido", prazoSugerido.toString(),
                "complexidade", resultado.getComplexidade(),
                "justificativa", resultado.getJustificativa()));
    }

    // ─── Feature 3: Geração de descrição (contextual) ────────────────────────

    /**
     * Recebe o título e o departamento de uma tarefa e retorna uma descrição
     * gerada pela IA, contextualizada pelo domínio do departamento.
     *
     * Body: { "titulo": "...", "departamento": "..." }
     * Response: { "descricao": "...", "gerado": true|false }
     *
     * Quando "gerado" vem false, a chamada à IA falhou, não estava
     * configurada, ou a resposta era praticamente idêntica ao título — nesses
     * casos "descricao" traz um texto de fallback e o front-end deve avisar
     * o usuário em vez de tratar como sucesso.
     */
    @PostMapping("/gerar-descricao")
    public ResponseEntity<?> gerarDescricao(@RequestBody Map<String, String> body) {
        String titulo = body.getOrDefault("titulo", "").trim();
        String departamento = body.getOrDefault("departamento", "").trim();

        if (titulo.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "O campo 'titulo' é obrigatório."));
        }

        String descricao = anthropicService.gerarDescricao(titulo, departamento);
        boolean gerado = descricao != null && !descricao.isBlank();

        return ResponseEntity.ok(Map.of(
                "descricao", gerado ? descricao
                        : "Não foi possível gerar uma descrição no momento. "
                                + "Tente novamente ou escreva manualmente.",
                "gerado", gerado));
    }

    // ─── Feature 1: Resposta a mensagem (endpoint avulso para testes) ─────────

    /**
     * Endpoint auxiliar para testar a geração de resposta a mensagens sem o
     * enriquecimento automático de histórico/equipe feito pelo
     * TarefaService. Na prática, a Feature 1 é disparada automaticamente
     * pelo TarefaService.
     *
     * Body: { "titulo": "...", "descricao": "...", "prazo": "...",
     * "departamento": "...", "status": "...", "mensagem": "...",
     * "historico": "..." (opcional), "equipe": "..." (opcional) }
     */
    @PostMapping("/responder-mensagem")
    public ResponseEntity<?> responderMensagem(@RequestBody Map<String, String> body) {
        String resposta = anthropicService.gerarRespostaParaMensagem(
                body.getOrDefault("titulo", ""),
                body.getOrDefault("descricao", ""),
                body.getOrDefault("prazo", ""),
                body.getOrDefault("departamento", ""),
                body.getOrDefault("status", ""),
                body.getOrDefault("mensagem", ""),
                body.getOrDefault("historico", ""),
                body.getOrDefault("equipe", ""));
        return ResponseEntity.ok(Map.of("resposta", resposta));
    }
}
