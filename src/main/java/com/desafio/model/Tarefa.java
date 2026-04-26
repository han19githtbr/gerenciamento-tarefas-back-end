package com.desafio.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.desafio.view.PessoaDTO;
import com.desafio.view.TarefaDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tarefa")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String descricao;

    private LocalDate prazo;

    @ManyToOne
    @JoinColumn(name = "id_departamento")
    @JsonBackReference("departamento-tarefas")
    private Departamento departamento;

    private long duracao;

    private boolean finalizado;

    private boolean emAndamento = false;

    @ManyToOne
    @JoinColumn(name = "id_pessoa")
    private Pessoa pessoa;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Transient
    public long getDuracao() {
        if (dataCriacao == null)
            return 0;
        return Duration.between(dataCriacao, LocalDateTime.now()).toHours();
    }

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ordem_apresentacao")
    private Long ordem_apresentacao;

    /**
     * Relacionamento com mensagens da tarefa (dúvidas do usuário).
     */
    @OneToMany(mappedBy = "tarefa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("tarefa-mensagens")
    private List<Mensagem> mensagens;

    public TarefaDTO toDTO() {
        TarefaDTO tarefaDTO = new TarefaDTO();
        tarefaDTO.setId(this.id);
        tarefaDTO.setTitulo(this.titulo);
        tarefaDTO.setDescricao(this.descricao);
        tarefaDTO.setPrazo(this.prazo);

        // VERIFICA SE DEPARTAMENTO NÃO É NULL
        if (this.departamento != null) {
            tarefaDTO.setDepartamento(this.departamento.getTitulo());
            tarefaDTO.setDepartamentoId(this.departamento.getId());
        } else {
            tarefaDTO.setDepartamento("Sem departamento");
            tarefaDTO.setDepartamentoId(null);
        }

        if (this.pessoa != null) {
            tarefaDTO.setPessoa(this.pessoa);
            tarefaDTO.setPessoaId(this.pessoa.getId());
        } else {
            tarefaDTO.setPessoa(null);
            tarefaDTO.setPessoaId(null);
        }

        tarefaDTO.setFinalizado(this.finalizado);
        tarefaDTO.setEmAndamento(this.emAndamento);
        tarefaDTO.setDuracao(this.getDuracao());
        tarefaDTO.setOrdem_apresentacao(this.ordem_apresentacao);

        // Inclui mensagens no DTO para o painel do usuário
        if (this.mensagens != null) {
            tarefaDTO.setMensagens(this.mensagens.stream()
                    .map(m -> {
                        java.util.Map<String, Object> msgMap = new java.util.LinkedHashMap<>();
                        msgMap.put("id", m.getId());
                        msgMap.put("texto", m.getTexto());
                        msgMap.put("remetenteEmail", m.getRemetenteEmail());
                        msgMap.put("dataCriacao", m.getDataCriacao() != null ? m.getDataCriacao().toString() : null);
                        msgMap.put("respondida", m.isRespondida());
                        msgMap.put("resposta", m.getResposta());
                        msgMap.put("dataResposta", m.getDataResposta() != null ? m.getDataResposta().toString() : null);
                        return msgMap;
                    })
                    .collect(java.util.stream.Collectors.toList()));
        }

        return tarefaDTO;
    }

}
