package com.desafio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.desafio.model.Departamento;
import com.desafio.model.Pessoa;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
	
    @Query(nativeQuery = true, value = "SELECT * FROM departamento as d \n" +
			"where d.titulo = :titulo ")
	Departamento checkTituloDepartamento(@Param("titulo") String titulo);

    @Query(nativeQuery = true, value = "SELECT * FROM departamento ORDER BY id ASC")
	List<Departamento> getAllDepartamento();

    @Query(nativeQuery = true, value = "SELECT * FROM departamento ORDER BY ordem_apresentacao DESC")
    List<Departamento> ordemApresentacaoDesc();
	
	@Query(nativeQuery = true, value = "SELECT * FROM departamento ORDER BY ordem_apresentacao ASC")
    List<Departamento> ordemApresentacaoAsc();

}
