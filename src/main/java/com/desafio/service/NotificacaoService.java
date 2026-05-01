package com.desafio.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.desafio.model.Notificacao;
import com.desafio.repository.NotificacaoRepository;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    // ← NOVO: lê o email do admin do application.properties
    @Value("${admin.email}")
    private String adminEmail;

    public void criarNotificacao(String email, Long tarefaId, String msg) {
        Notificacao n = new Notificacao();
        n.setDestinatarioEmail(email);
        n.setTarefaId(tarefaId);
        n.setMensagem(msg);
        n.setLida(false);
        n.setDataCriacao(LocalDateTime.now());
        notificacaoRepository.save(n);
    }

    public void criarNotificacaoPrazoVencido(String email, Long tarefaId, String tituloTarefa) {
        Notificacao n = new Notificacao();
        n.setDestinatarioEmail(email);
        n.setTarefaId(tarefaId);
        n.setMensagem("📋 PRAZO VENCIDO — Tarefa: \"" + tituloTarefa
                + "\". Responda ao administrador: A) Preciso de um prazo maior  B) Não estou conseguindo executar a tarefa");
        n.setTipo("PRAZO_VENCIDO");
        n.setLida(false);
        n.setDataCriacao(LocalDateTime.now());
        notificacaoRepository.save(n);
    }

    // ← NOVO: cria notificação especificamente para o admin
    public void criarNotificacaoParaAdmin(Long tarefaId, String msg) {
        criarNotificacao(adminEmail, tarefaId, msg);
    }

    @Transactional(readOnly = true)
    public List<Notificacao> getNotificacoesPendentes(String email) {
        return notificacaoRepository.findByDestinatarioEmailAndLidaFalse(email);
    }

    public void marcarComoLida(Long id) {
        notificacaoRepository.findById(id).ifPresent(n -> {
            n.setLida(true);
            notificacaoRepository.save(n);
        });
    }
}
