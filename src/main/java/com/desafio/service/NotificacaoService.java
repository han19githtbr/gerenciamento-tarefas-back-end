package com.desafio.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.desafio.model.Notificacao;
import com.desafio.repository.NotificacaoRepository;

@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    public void criarNotificacao(String email, Long tarefaId, String msg) {
        Notificacao n = new Notificacao();
        n.setDestinatarioEmail(email);
        n.setMensagem(msg);
        n.setLida(false);
        n.setDataCriacao(LocalDateTime.now());
        notificacaoRepository.save(n);
    }

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
