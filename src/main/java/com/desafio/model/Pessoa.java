/**
 * 
 */
package com.desafio.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.desafio.view.PessoaDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "pessoa")
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToOne
    @JoinColumn(name = "id_departamento")
    private Departamento departamento;

    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Tarefa> tarefas;

    public long calcularTotalHoras() {
        return getTarefas().stream().mapToLong(Tarefa::getDuracao).sum();
    }
    

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ordem_apresentacao")
    private Long ordem_apresentacao;


    public PessoaDTO toDTO() {
        PessoaDTO pessoaDTO = new PessoaDTO();
        pessoaDTO.setId(this.id);
        pessoaDTO.setNome(this.nome);
        pessoaDTO.setDepartamento(this.departamento.getTitulo());
        pessoaDTO.setTotalHoras(this.calcularTotalHoras());
        return pessoaDTO;
    }
}
