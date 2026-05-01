package com.desafio.controllers;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.desafio.service.NotificacaoService;
import com.desafio.service.TarefaService;
import com.desafio.view.TarefaDTO;
import com.desafio.repository.NotificacaoRepository;
import com.desafio.repository.TarefaRepository;

import lombok.RequiredArgsConstructor;

import com.desafio.repository.MensagemRepository;

@RestController
@RequestMapping("/usuario")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final TarefaService tarefaService;

    private final PessoaRepository pessoaRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final TarefaRepository tarefaRepository;
    private final MensagemRepository mensagemRepository;
    private final NotificacaoService notificacaoService;

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
        try {
            Object resultado = tarefaService.enviarMensagem(id, email, texto);
            return ResponseEntity.ok(resultado);
        } catch (javax.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("erro", "Erro interno ao enviar mensagem."));
        }
    }

    @PostMapping("/notificacao/{notifId}/responder-vencimento")
    public ResponseEntity<?> responderVencimento(
            @PathVariable Long notifId,
            @RequestBody Map<String, String> body,
            Authentication auth) {

        String email = auth.getName();
        String opcao = body.get("opcao"); // "A" ou "B"

        if (opcao == null || (!opcao.equals("A") && !opcao.equals("B"))) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Opção inválida. Envie 'A' ou 'B'."));
        }

        // Busca a notificação
        com.desafio.model.Notificacao notif = notificacaoRepository.findById(notifId).orElse(null);
        if (notif == null || !notif.getDestinatarioEmail().equals(email)) {
            return ResponseEntity.status(403).body(Map.of("erro", "Notificação não encontrada."));
        }

        // Marca como lida (usuário respondeu)
        notif.setLida(true);
        notificacaoRepository.save(notif);

        // Busca a tarefa para o título
        Long tarefaId = notif.getTarefaId();
        com.desafio.model.Tarefa tarefa = tarefaRepository.findById(tarefaId).orElse(null);
        String titulo = tarefa != null ? tarefa.getTitulo() : "ID " + tarefaId;

        // Monta mensagem para o admin
        String msgAdmin = opcao.equals("A")
                ? "📩 Usuário " + email + " respondeu na tarefa \"" + titulo + "\": A) Preciso de um prazo maior."
                : "📩 Usuário " + email + " respondeu na tarefa \"" + titulo
                        + "\": B) Não estou conseguindo executar a tarefa.";

        notificacaoService.criarNotificacaoParaAdmin(tarefaId, msgAdmin);

        return ResponseEntity.ok(Map.of("success", true, "mensagem", "Resposta enviada ao administrador."));
    }

    // Usuário inicia uma tarefa (muda status para em andamento)
    @PutMapping("/iniciar-tarefa/{tarefaId}")
    public TarefaDTO iniciarTarefa(@PathVariable Long tarefaId, Authentication auth) {
        String email = auth.getName();
        return tarefaService.iniciarTarefa(tarefaId, email);
    }

    @DeleteMapping("/mensagem/{msgId}")
    public ResponseEntity<?> excluirMensagem(
            @PathVariable Long msgId,
            Authentication auth) {
        String email = auth.getName();
        com.desafio.model.Mensagem mensagem = mensagemRepository.findById(msgId)
                .orElse(null);
        if (mensagem == null)
            return ResponseEntity.notFound().build();
        if (!mensagem.getRemetenteEmail().equals(email)) {
            return ResponseEntity.status(403).body(Map.of("erro", "Sem permissão para excluir esta mensagem."));
        }
        mensagemRepository.deleteById(msgId);
        return ResponseEntity.ok(Map.of("excluido", true));
    }

}
