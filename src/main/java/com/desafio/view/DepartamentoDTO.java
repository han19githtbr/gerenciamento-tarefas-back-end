package com.desafio.view;

import java.util.List;
import com.desafio.model.Pessoa;
import com.desafio.model.Tarefa;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude
public class DepartamentoDTO {

	Long id;

    String titulo;
       
    Long ordem_apresentacao;
    
    List<Pessoa> pessoas;
      
    List<Tarefa> tarefas;

    String mensagem;
    
    Boolean success;
    
    Integer quantidadePessoas;
    
    Integer quantidadeTarefas;

}
