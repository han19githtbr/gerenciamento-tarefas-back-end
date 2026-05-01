package com.desafio.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.model.Mensagem;
import com.desafio.repository.MensagemRepository;
import com.desafio.service.DepartamentoService;
import com.desafio.service.PessoaService;
import com.desafio.service.TarefaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final TarefaService tarefaService;

    private final PessoaService pessoaService;

    private final DepartamentoService departamentoService;

    private final MensagemRepository mensagemRepository; // ← ADICIONAR

    // Endpoint existente — sem alteração
    @PutMapping("/mensagem/{mensagemId}/responder")
    public ResponseEntity<?> responderMensagem(
            @PathVariable Long mensagemId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String adminEmail = auth.getName();
        String resposta = body.get("resposta");
        if (resposta == null || resposta.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O campo 'resposta' é obrigatório."));
        }
        Object resultado = tarefaService.responderMensagem(mensagemId, adminEmail, resposta);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/tarefas/vencidas")
    public List<com.desafio.view.TarefaDTO> getTarefasVencidas() {
        return tarefaService.listarVencidas();
    }

    @GetMapping("/mensagens/pendentes")
    public List<Map<String, Object>> getMensagensPendentes() {
        List<Mensagem> mensagens = mensagemRepository.findByRespondidaFalse();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Mensagem m : mensagens) {
            Map<String, Object> dto = new java.util.LinkedHashMap<>();
            dto.put("id", m.getId());
            dto.put("remetenteEmail", m.getRemetenteEmail());
            dto.put("texto", m.getTexto());
            dto.put("dataCriacao", m.getDataCriacao());
            dto.put("respondida", m.isRespondida());
            dto.put("tarefaId", m.getTarefa() != null ? m.getTarefa().getId() : null);
            dto.put("tarefaTitulo", m.getTarefa() != null ? m.getTarefa().getTitulo() : null);
            result.add(dto);
        }
        return result;
    }

    // Endpoint existente — sem alteração
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

    @DeleteMapping("/mensagem/{mensagemId}")
    public ResponseEntity<?> excluirMensagem(@PathVariable Long mensagemId) {
        if (!mensagemRepository.existsById(mensagemId)) {
            return ResponseEntity.notFound().build();
        }
        mensagemRepository.deleteById(mensagemId);
        return ResponseEntity.ok(Map.of("excluido", true));
    }

    @PutMapping("/tarefa/{tarefaId}/prorrogar")
    public ResponseEntity<?> prorrogarTarefa(
            @PathVariable Long tarefaId,
            @RequestBody Map<String, String> body) {

        String novoPrazoStr = body.get("novoPrazo");
        if (novoPrazoStr == null || novoPrazoStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Campo 'novoPrazo' obrigatório."));
        }

        java.time.LocalDate novoPrazo = java.time.LocalDate.parse(novoPrazoStr);
        Object resultado = tarefaService.prorrogarTarefa(tarefaId, novoPrazo);
        return ResponseEntity.ok(resultado);
    }

    @PutMapping("/tarefa/{tarefaId}/encerrar")
    public ResponseEntity<?> encerrarTarefa(@PathVariable Long tarefaId) {
        Object resultado = tarefaService.encerrarTarefaVencida(tarefaId);
        return ResponseEntity.ok(resultado);
    }

}