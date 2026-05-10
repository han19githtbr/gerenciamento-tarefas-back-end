package com.desafio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.desafio.model.Mensagem;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByTarefaId(Long tarefaId);

    List<Mensagem> findByRespondidaFalse();

    // Busca mensagens respondidas apenas pela IA ou não respondidas
    List<Mensagem> findByRespondidaFalseOrAdminEmailEquals(String iaEmail);
}
