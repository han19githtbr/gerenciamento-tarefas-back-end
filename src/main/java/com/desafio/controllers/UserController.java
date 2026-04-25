package com.desafio.controllers;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.model.Pessoa;
import com.desafio.model.Tarefa;
import com.desafio.repository.PessoaRepository;
import com.desafio.service.TarefaService;
import com.desafio.view.TarefaDTO;

@RestController
@RequestMapping("/usuario")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")

public class UserController {

    @Autowired
    private TarefaService tarefaService;

    @Autowired
    private PessoaRepository pessoaRepository;

    // Busca tarefas do usuário logado pelo email
    @GetMapping("/minhas-tarefas")
    public List<TarefaDTO> getMinhasTarefas(Authentication auth) {
        String email = auth.getName();
        Pessoa pessoa = pessoaRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        return tarefaService.getTarefasByPessoa(pessoa.getId());
    }

    // Usuário envia mensagem em uma tarefa
    @PostMapping("/tarefa/{id}/mensagem")
    public ResponseEntity<?> enviarMensagem(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String email = auth.getName();
        String texto = body.get("texto");
        if (texto == null || texto.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O campo 'texto' é obrigatório."));
        }
        Object resultado = tarefaService.enviarMensagem(id, email, texto);
        return ResponseEntity.ok(resultado);
    }

    // Usuário inicia uma tarefa (muda status para em andamento)
    @PutMapping("/iniciar-tarefa/{tarefaId}")
    public TarefaDTO iniciarTarefa(@PathVariable Long tarefaId, Authentication auth) {
        String email = auth.getName();
        return tarefaService.iniciarTarefa(tarefaId, email);
    }

}
