package com.desafio.controllers;

import com.desafio.service.AnthropicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Controller que expõe os três endpoints de IA para o front-end Angular.
 *
 * Rotas:
 * POST /ia/sugerir-prazo → Feature 2: retorna dias sugeridos
 * POST /ia/gerar-descricao → Feature 3: retorna descrição gerada
 * POST /ia/responder-mensagem → Feature 1: resposta contextual (usada
 * internamente)
 *
 * Obs.: a Feature 1 também é disparada automaticamente pelo TarefaService
 * ao salvar uma mensagem. Este endpoint é exposto opcionalmente para testes.
 */
@RestController
@RequestMapping("/ia")
@RequiredArgsConstructor
public class AiController {

    private final AnthropicService anthropicService;

    // ─── Feature 2: Sugestão de prazo ────────────────────────────────────────

    /**
     * Recebe título e descrição de uma tarefa e retorna o número de dias
     * sugerido pela IA para o prazo.
     *
     * Body: { "titulo": "...", "descricao": "..." }
     * Response: { "diasSugeridos": 7, "prazoSugerido": "2026-05-17" }
     */
    @PostMapping("/sugerir-prazo")
    public ResponseEntity<?> sugerirPrazo(@RequestBody Map<String, String> body) {
        String titulo = body.getOrDefault("titulo", "").trim();
        String descricao = body.getOrDefault("descricao", "").trim();

        if (titulo.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "O campo 'titulo' é obrigatório."));
        }

        String diasStr = anthropicService.sugerirPrazoEmDias(titulo, descricao);
        int dias = Integer.parseInt(diasStr);
        LocalDate prazoSugerido = LocalDate.now().plusDays(dias);

        return ResponseEntity.ok(Map.of(
                "diasSugeridos", dias,
                "prazoSugerido", prazoSugerido.toString()));
    }

    // ─── Feature 3: Geração de descrição ─────────────────────────────────────

    /**
     * Recebe o título de uma tarefa e retorna uma descrição gerada pela IA.
     *
     * Body: { "titulo": "..." }
     * Response: { "descricao": "..." }
     */
    @PostMapping("/gerar-descricao")
    public ResponseEntity<?> gerarDescricao(@RequestBody Map<String, String> body) {
        String titulo = body.getOrDefault("titulo", "").trim();

        if (titulo.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "O campo 'titulo' é obrigatório."));
        }

        String descricao = anthropicService.gerarDescricao(titulo);
        return ResponseEntity.ok(Map.of("descricao", descricao));
    }

    // ─── Feature 1: Resposta a mensagem (endpoint avulso para testes) ─────────

    /**
     * Endpoint auxiliar para testar a geração de resposta a mensagens.
     * Na prática, a Feature 1 é disparada automaticamente pelo TarefaService.
     *
     * Body: { "titulo": "...", "descricao": "...", "prazo": "...",
     * "departamento": "...", "status": "...", "mensagem": "..." }
     */
    @PostMapping("/responder-mensagem")
    public ResponseEntity<?> responderMensagem(@RequestBody Map<String, String> body) {
        String resposta = anthropicService.gerarRespostaParaMensagem(
                body.getOrDefault("titulo", ""),
                body.getOrDefault("descricao", ""),
                body.getOrDefault("prazo", ""),
                body.getOrDefault("departamento", ""),
                body.getOrDefault("status", ""),
                body.getOrDefault("mensagem", ""));
        return ResponseEntity.ok(Map.of("resposta", resposta));
    }
}
