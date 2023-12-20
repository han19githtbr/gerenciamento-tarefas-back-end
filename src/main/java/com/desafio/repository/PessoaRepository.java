package com.desafio.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.desafio.model.Pessoa;
import com.desafio.model.Tarefa;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
	
	@Query("SELECT p, t.duracao " +
	           "FROM Pessoa p LEFT JOIN p.tarefas t " +
	           "GROUP BY p")
	List<Object[]> obterTodasPessoas();
	
	
	@Query(nativeQuery = true, value = "SELECT * FROM pessoa as p \n" +
            " where p.id = :id ")
	Pessoa checkId(@Param("id") Long id);
	
	
	@Query(nativeQuery = true, value = "SELECT * FROM pessoa ORDER BY id ASC")
	List<Pessoa> getAllPessoa();
	
	
	@Query("SELECT p FROM Pessoa p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Pessoa> findByName(@Param("nome") String nome);
	
	
	@Query("SELECT t FROM Tarefa t " +
		       "JOIN t.pessoa p " +
		       "WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')) " +
		       "AND t.dataCriacao = :dataCriacao " +
		       "AND t.duracao = :duracao")
	List<Tarefa> findByNameAndPeriod(@Param("nome") String nome, @Param("dataCriacao") LocalDateTime dataCriacao, @Param("duracao") long duracao);
	

	@Query(nativeQuery = true, value = "SELECT * FROM Pessoa as p \n" +
			" where p.nome = :nome ")
	Pessoa checkNomePessoa(@Param("nome") String nome);


	@Query(nativeQuery = true, value = "SELECT * FROM pessoa ORDER BY ordem_apresentacao DESC")
	List<Pessoa> ordemApresentacaoDesc();

	
	@Query(nativeQuery = true, value = "SELECT * FROM pessoa ORDER BY ordem_apresentacao ASC")
	List<Pessoa> ordemApresentacaoAsc();
}
