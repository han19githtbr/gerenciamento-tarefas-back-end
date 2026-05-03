package com.desafio.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desafio.model.Notificacao;
import com.desafio.repository.NotificacaoRepository;
import com.desafio.repository.TarefaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final TarefaRepository tarefaRepository; // ← ADICIONE ESTA LINHA

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

    public void criarNotificacaoParaAdmin(Long tarefaId, String msg) {
        criarNotificacao(adminEmail, tarefaId, msg);
    }

    public void criarNotificacaoTipada(String email, Long tarefaId, String msg, String tipo) {
        Notificacao n = new Notificacao();
        n.setDestinatarioEmail(email);
        n.setTarefaId(tarefaId);
        n.setMensagem(msg);
        n.setTipo(tipo);
        n.setLida(false);
        n.setDataCriacao(LocalDateTime.now());
        notificacaoRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notificacao> getNotificacoesPendentes(String email) {
        return notificacaoRepository.findByDestinatarioEmailAndLidaFalse(email);
    }

    public List<Notificacao> getNotificacoesConclusaoPendentes(String adminEmail) {
        return notificacaoRepository.findByDestinatarioEmailAndLidaFalse(adminEmail)
                .stream()
                .filter(n -> "CONCLUSAO_PENDENTE".equals(n.getTipo()))
                .collect(Collectors.toList());
    }

    public void marcarComoLida(Long id) {
        notificacaoRepository.findById(id).ifPresent(n -> {
            n.setLida(true);
            notificacaoRepository.save(n);
        });
    }

    public void aprovarConclusao(Long notifId, Long tarefaId) { // ← SEM parâmetros de repository
        notificacaoRepository.findById(notifId).ifPresent(n -> {
            n.setLida(true);
            notificacaoRepository.save(n);
        });
        tarefaRepository.findById(tarefaId).ifPresent(t -> {
            t.setFinalizado(true);
            t.setDataConclusao(LocalDate.now());
            tarefaRepository.save(t);
        });
    }
}
