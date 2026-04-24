package com.desafio.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.model.Notificacao;
import com.desafio.service.NotificacaoService;

@RestController
@RequestMapping("/notificacoes")

public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @GetMapping("/pendentes")
    public List<Notificacao> getPendentes(Authentication auth) {

        return notificacaoService.getNotificacoesPendentes(auth.getName());
    }

    @PutMapping("/ler/{id}")
    public void marcarLida(@PathVariable Long id) {
        notificacaoService.marcarComoLida(id);
    }

}
