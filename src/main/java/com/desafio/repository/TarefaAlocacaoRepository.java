package com.desafio.repository;

import com.desafio.model.TarefaAlocacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TarefaAlocacaoRepository extends JpaRepository<TarefaAlocacao, Long> {

    @Query("SELECT ta FROM TarefaAlocacao ta JOIN FETCH ta.pessoa WHERE ta.tarefa.id = :tarefaId")
    List<TarefaAlocacao> findByTarefaId(@Param("tarefaId") Long tarefaId);

    boolean existsByTarefaIdAndPessoaId(Long tarefaId, Long pessoaId);

    void deleteByTarefaIdAndPessoaId(Long tarefaId, Long pessoaId);

    @Query("SELECT ta.tarefa.id, COUNT(ta) FROM TarefaAlocacao ta GROUP BY ta.tarefa.id")
    List<Object[]> countByTarefaIdGrouped();
}
