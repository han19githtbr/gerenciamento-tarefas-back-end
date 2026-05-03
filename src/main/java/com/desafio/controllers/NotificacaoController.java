package com.desafio.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.model.Notificacao;
import com.desafio.service.NotificacaoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService; // ← só o service, nada mais

    @GetMapping("/pendentes")
    public List<Notificacao> getPendentes(Authentication auth) {
        return notificacaoService.getNotificacoesPendentes(auth.getName());
    }

    @PutMapping("/ler/{id}")
    public void marcarLida(@PathVariable Long id) {
        notificacaoService.marcarComoLida(id);
    }

    @GetMapping("/conclusao-pendentes")
    public List<Notificacao> getConclusaoPendentes(Authentication auth) {
        return notificacaoService.getNotificacoesConclusaoPendentes(auth.getName());
    }

    @PutMapping("/aprovar-conclusao/{id}")
    public ResponseEntity<?> aprovarConclusao(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        Long tarefaId = Long.valueOf(body.get("tarefaId").toString());
        notificacaoService.aprovarConclusao(id, tarefaId); // ← só id e tarefaId, sem repositories
        return ResponseEntity.ok(Map.of("success", true));
    }
}
