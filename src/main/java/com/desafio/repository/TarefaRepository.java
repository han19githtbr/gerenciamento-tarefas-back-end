package com.desafio.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.desafio.model.Pessoa;
import com.desafio.model.Tarefa;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
	
	@Query("SELECT t FROM Tarefa t " +
		       "LEFT JOIN t.pessoa p " +
		       "WHERE p IS NULL " +
		       "ORDER BY t.prazo ASC")
	List<Tarefa> findTarefasSemAlocacao(Pageable pageable);


	@Query(nativeQuery = true, value = "SELECT * FROM tarefa as t \n" +
			"where t.titulo = :titulo ")
	Tarefa checkTituloTarefa(@Param("titulo") String titulo);


	@Query(nativeQuery = true, value = "SELECT * FROM tarefa ORDER BY ordem_apresentacao DESC")
	List<Tarefa> ordemApresentacaoDesc();

	
	@Query(nativeQuery = true, value = "SELECT * FROM tarefa ORDER BY ordem_apresentacao ASC")
	List<Tarefa> ordemApresentacaoAsc();


	@Query(nativeQuery = true, value = "SELECT * FROM tarefa ORDER BY id ASC")
	List<Tarefa> getAllTarefa();

}
