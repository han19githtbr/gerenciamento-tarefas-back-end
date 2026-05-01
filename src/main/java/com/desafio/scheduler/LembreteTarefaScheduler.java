package com.desafio.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.desafio.model.Tarefa;
import com.desafio.model.TarefaAlocacao;
import com.desafio.repository.TarefaRepository;
import com.desafio.service.NotificacaoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LembreteTarefaScheduler {

    private final TarefaRepository tarefaRepository;
    private final NotificacaoService notificacaoService;

    /** Lembrete D-1: executa a cada 2 horas */
    @Scheduled(fixedRate = 7_200_000)
    public void enviarLembretes() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        List<Tarefa> tarefas = tarefaRepository.findTarefasParaLembrete(amanha);

        for (Tarefa tarefa : tarefas) {
            // Notifica todas as pessoas alocadas (multi-alocação)
            if (tarefa.getAlocacoes() != null) {
                for (TarefaAlocacao al : tarefa.getAlocacoes()) {
                    if (al.getPessoa() != null && al.getPessoa().getEmail() != null) {
                        notificacaoService.criarNotificacao(
                                al.getPessoa().getEmail(),
                                tarefa.getId(),
                                "⏰ Lembrete: A tarefa '" + tarefa.getTitulo()
                                        + "' vence amanhã! Prazo: " + tarefa.getPrazo());
                    }
                }
            }
        }
    }

    /**
     * Feature 3 — Notificação automática de prazo vencido.
     * Executa a cada hora. Para cada tarefa vencida e não finalizada,
     * envia mensagem automática do admin para todas as pessoas alocadas
     * (apenas uma vez: evita reenvio via campo notificacaoVencimentoEnviada).
     *
     * ATENÇÃO: Para o "apenas uma vez", adicione o campo boolean
     * `notificacaoVencimentoEnviada` na entidade Tarefa.
     */
    @Scheduled(fixedRate = 3_600_000)
    public void notificarVencidas() {
        LocalDate hoje = LocalDate.now();
        List<Tarefa> vencidas = tarefaRepository.findVencidas(hoje);

        for (Tarefa tarefa : vencidas) {
            // Evita reenvio (adicione o campo e getter/setter na entidade Tarefa)
            if (tarefa.isNotificacaoVencimentoEnviada())
                continue;

            List<TarefaAlocacao> alocacoes = tarefa.getAlocacoes();
            if (alocacoes == null || alocacoes.isEmpty())
                continue;

            for (TarefaAlocacao al : alocacoes) {
                if (al.getPessoa() == null || al.getPessoa().getEmail() == null)
                    continue;

                String email = al.getPessoa().getEmail();
                String nomePessoa = al.getPessoa().getNome();

                // Notificação para o colaborador com as duas opções
                notificacaoService.criarNotificacaoPrazoVencido(email, tarefa.getId(), tarefa.getTitulo());

                // Notificação para o admin
                notificacaoService.criarNotificacaoParaAdmin(
                        tarefa.getId(),
                        "🚨 Tarefa \"" + tarefa.getTitulo() + "\" venceu. "
                                + "Colaborador: " + nomePessoa + " (" + email + ")");
            }

            // Marca como notificada
            tarefa.setNotificacaoVencimentoEnviada(true);
            tarefaRepository.save(tarefa);
        }
    }
}
