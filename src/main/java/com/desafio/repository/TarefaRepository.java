package com.desafio.repository;

import java.time.LocalDate;
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

	// @Query(nativeQuery = true, value = "SELECT * FROM tarefa ORDER BY id ASC")
	@Query("SELECT t FROM Tarefa t LEFT JOIN FETCH t.departamento ORDER BY t.id ASC")
	List<Tarefa> getAllTarefa();

	// Feature 1 - Em Aandamento
	@Query("SELECT t FROM Tarefa t WHERE t.pessoa IS NOT NULL AND t.finalizado = false AND t.emAndamento = true")
	List<Tarefa> findTarefasEmAndamento();

	@Query("SELECT COUNT(t) FROM Tarefa t WHERE t.pessoa IS NOT NULL AND t.finalizado = false AND t.emAndamento = false")
	long countTarefasEmAndamento();

	// Feature 2 - Admin dashboard
	@Query("SELECT COUNT(t) FROM Tarefa t WHERE t.pessoa IS NULL")
	long countPendentes();

	@Query("SELECT COUNT(t) FROM Tarefa t WHERE t.finalizado = true")
	long countConcluidas();

	// Feature 3 - Tarefas por pessoa
	@Query("SELECT t FROM Tarefa t WHERE t.pessoa.id = :pessoaId ORDER BY t.prazo ASC")
	List<Tarefa> findByPessoaId(@Param("pessoaId") Long pessoaId);

	// Feature 4 - Lembretes
	@Query("SELECT t FROM Tarefa t " +
			"WHERE (t.prazo = :amanha OR t.prazo = :doisDiasAtras) " +
			"AND t.finalizado = false " +
			"AND t.pessoa IS NOT NULL")
	List<Tarefa> findTarefasParaLembrete(
			@Param("amanha") LocalDate amanha,
			@Param("doisDiasAtras") LocalDate doisDiasAtras);

}
