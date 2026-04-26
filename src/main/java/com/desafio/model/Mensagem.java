package com.desafio.model;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "mensagem")

public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_tarefa")
    @JsonBackReference("tarefa-mensagens")
    private Tarefa tarefa;

    private String remetenteEmail;

    private String texto;

    private String resposta;

    private String adminEmail;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataResposta;

    private boolean respondida;

}
