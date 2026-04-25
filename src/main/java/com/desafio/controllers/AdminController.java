package com.desafio.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.model.Tarefa;
import com.desafio.service.DepartamentoService;
import com.desafio.service.PessoaService;
import com.desafio.service.TarefaService;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")

public class AdminController {

    @Autowired
    private TarefaService tarefaService;

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private DepartamentoService departamentoService;

    // Admin responde mensagem de um usuário em uma tarefa
    @PutMapping("/mensagem/{mensagemId}/responder")
    public ResponseEntity<?> responderMensagem(
            @PathVariable Long mensagemId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String adminEmail = auth.getName();
        String resposta = body.get("resposta");
        if (resposta == null || resposta.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O campo \'resposta\' é obrigatório."));
        }
        Object resultado = tarefaService.responderMensagem(mensagemId, adminEmail, resposta);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalPessoas", pessoaService.count());
        result.put("totalTarefas", tarefaService.count());
        result.put("totalDepartamentos", departamentoService.count());
        result.put("emAndamento", tarefaService.contarTarefasEmAndamento());
        result.put("pendentes", tarefaService.contarPendentes());
        result.put("concluidas", tarefaService.contarConcluidas());
        return result;
    }

}
