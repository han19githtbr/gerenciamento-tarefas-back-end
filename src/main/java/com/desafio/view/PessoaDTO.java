package com.desafio.view;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;

import com.desafio.model.Tarefa;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude
public class PessoaDTO {

	Long id;

    String nome;

    String departamento;

    Long ordem_apresentacao;
       
    List<Tarefa> tarefas;
	
    String mensagem;
    
    Boolean success;
    
    Long totalHoras;
}
