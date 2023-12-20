package com.desafio.view;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.desafio.model.Pessoa;
import com.desafio.model.Tarefa;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude
public class TarefaDTO {

	Long id;

    String titulo;

    String descricao;

    LocalDate prazo;

    String departamento;

    Long duracao;

    Long ordem_apresentacao;

    boolean finalizado;
    
    Pessoa pessoa;
    
    String mensagem;
    
    Boolean success;
}
