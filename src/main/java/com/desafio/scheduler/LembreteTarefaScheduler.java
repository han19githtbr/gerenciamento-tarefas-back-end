package com.desafio.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.desafio.model.Tarefa;
import com.desafio.repository.TarefaRepository;
import com.desafio.service.NotificacaoService;

@Component
public class LembreteTarefaScheduler {

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    // Executa a cada 2 horas
    @Scheduled(fixedRate = 7200000)
    public void enviarLembretes() {
        LocalDate amanha = LocalDate.now().plusDays(1);

        // Busca tarefas cujo prazo é amanhã, não finalizadas e com pessoa alocada
        List<Tarefa> tarefas = tarefaRepository.findTarefasParaLembrete(amanha);

        for (Tarefa tarefa : tarefas) {
            if (tarefa.getPessoa() != null && tarefa.getPessoa().getEmail() != null) {
                String email = tarefa.getPessoa().getEmail();
                notificacaoService.criarNotificacao(email, tarefa.getId(),
                        "⏰ Lembrete: A tarefa '" + tarefa.getTitulo() + "' vence amanhã! Prazo: " + tarefa.getPrazo());
            }
        }
    }

}
