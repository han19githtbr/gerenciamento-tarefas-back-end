package com.desafio.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.desafio.view.DepartamentoDTO;
import com.desafio.view.PessoaDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "departamento")
public class Departamento {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
   
    @OneToMany(mappedBy = "departamento", cascade = CascadeType.ALL)
    private List<Pessoa> pessoas;
    
    @OneToMany(mappedBy = "departamento", cascade = CascadeType.ALL)
    private List<Tarefa> tarefas;

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ordem_apresentacao")
    private Long ordem_apresentacao;


    public DepartamentoDTO toDTO() {
    	DepartamentoDTO departamentoDTO = new DepartamentoDTO();
        departamentoDTO.setId(this.id);
        departamentoDTO.setTitulo(this.titulo);
        return departamentoDTO;
    }

}
