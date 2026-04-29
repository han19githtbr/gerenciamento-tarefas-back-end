package com.desafio.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.desafio.model.Pessoa;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude
public class TarefaDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private LocalDate prazo;
    private String departamento;
    private Long departamentoId;
    private Long pessoaId;
    private Long duracao;
    private Long ordem_apresentacao;
    private boolean finalizado;
    private boolean emAndamento;
    private Pessoa pessoa;
    private String mensagem;
    private Boolean success;

    // USANDO Map (compatível com seu código existente)
    private List<Map<String, Object>> mensagens;
    private List<Map<String, Object>> pessoasAlocadas;
    private boolean vencida;

}