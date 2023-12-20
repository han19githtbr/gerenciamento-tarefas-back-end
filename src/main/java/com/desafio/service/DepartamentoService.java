package com.desafio.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.desafio.model.Departamento;
import com.desafio.model.Pessoa;
import com.desafio.repository.DepartamentoRepository;
import com.desafio.view.DepartamentoDTO;
import com.desafio.view.PessoaDTO;

@Service
public class DepartamentoService {

	@Autowired
	private DepartamentoRepository departamentoRepository;

	public DepartamentoDTO salvarDepartamento(Departamento departamento) throws ParseException{
		DepartamentoDTO departamentoDTO = new DepartamentoDTO();
		if(departamento.getTitulo() == null) {
			departamentoDTO.setSuccess(Boolean.FALSE);
			departamentoDTO.setMensagem("O título do departamento não pode ser nulo");
			return departamentoDTO;
		}
	

		if(departamentoRepository.checkTituloDepartamento(departamento.getTitulo()) != null ) {
			departamentoDTO.setSuccess(Boolean.FALSE);
			departamentoDTO.setMensagem("Já existe um departamento com esse nome.");
			return departamentoDTO;
		}

		Long nextOrdem = this.nextOrdem();

		departamento.setOrdem_apresentacao(nextOrdem);

		departamentoRepository.save(departamento);
		departamentoDTO.setId(departamento.getId());
		departamentoDTO.setTitulo(departamento.getTitulo());
		departamentoDTO.setOrdem_apresentacao(departamento.getOrdem_apresentacao());
		departamentoDTO.setMensagem(departamento.getTitulo() + " Foi salvo com sucesso");
		
		departamentoDTO.setSuccess(Boolean.TRUE);
		return departamentoDTO;
	}
	

	public List<DepartamentoDTO> getAllDepartamento() {
		List<Departamento> departamentos = departamentoRepository.getAllDepartamento();
	    List<DepartamentoDTO> departamentosDTO = new ArrayList<>();

	    for (Departamento departamento : departamentos) {
	        departamentosDTO.add(departamento.toDTO());
	    }

	    return departamentosDTO;
	}


	public DepartamentoDTO removerDepartamento(Long id) {
    	DepartamentoDTO departamentoDTO = new DepartamentoDTO();
    	Departamento departamento = departamentoRepository.getById(id);
		if(Objects.nonNull(departamento)) {
			departamentoRepository.delete(departamento);
			departamentoDTO.setMensagem("O departamento foi removido com sucesso");
			departamentoDTO.setSuccess(Boolean.TRUE);
		} else {
			departamentoDTO.setMensagem("O departamento não foi removido");
			departamentoDTO.setSuccess(Boolean.FALSE);
		}
		return departamentoDTO;
	}


	public DepartamentoDTO alterarDepartamento(String titulo, Departamento departamento) {
		DepartamentoDTO departamentoDTO = new DepartamentoDTO();
		Departamento departamentoModel = departamentoRepository.checkTituloDepartamento(titulo);
		if(Objects.nonNull(departamentoModel)) {
			if(departamento.getTitulo() == null) {
				departamentoDTO.setSuccess(Boolean.FALSE);
				departamentoDTO.setMensagem("O campo Titulo é obrigatório");
				return departamentoDTO;
			}
			
			if(!titulo.equals(departamento.getTitulo())) {
				Departamento novoDepartamentoModel = departamentoRepository.checkTituloDepartamento(departamento.getTitulo());
				if(Objects.isNull(novoDepartamentoModel)) {
					departamentoModel.setTitulo(departamento.getTitulo());
					
					departamentoRepository.save(departamentoModel);
					
					departamentoDTO.setMensagem("O departamento " + departamento.getTitulo() + " foi salvo com sucesso.");
					departamentoDTO.setSuccess(Boolean.TRUE);
				} else {
					departamentoDTO.setSuccess(Boolean.FALSE);
					departamentoDTO.setMensagem("Já existe um departamento com esse nome.");
					return departamentoDTO;
				}
			}else{
				departamentoModel.setTitulo(departamento.getTitulo());
				
				departamentoRepository.save(departamentoModel);
				
				departamentoDTO.setMensagem("O departamento " + departamento.getTitulo() + " foi salvo com sucesso.");
				departamentoDTO.setSuccess(Boolean.TRUE);
			}
			
			departamentoDTO.setId(departamento.getId());
			departamentoDTO.setTitulo(departamento.getTitulo());
			
			return departamentoDTO;
	
		}else {
			departamentoDTO.setMensagem("O departamento não foi alterado.");
			departamentoDTO.setSuccess(Boolean.FALSE);
			return departamentoDTO;
		}
	}


	public List<DepartamentoDTO> listarDepartamentosComQuantidade() {
        List<Departamento> departamentos = departamentoRepository.findAll();
        return departamentos.stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }

    	
	private Long nextOrdem() {
		List<Departamento> departamentoDTO = departamentoRepository.ordemApresentacaoDesc();
	
		if (!departamentoDTO.isEmpty() && departamentoDTO.get(0).getOrdem_apresentacao() != null) {
			Long maiorNumber = departamentoDTO.get(0).getOrdem_apresentacao();
			return maiorNumber + 1;
		} else {
			return 1L;
		}
	}


	private DepartamentoDTO converterParaDTO(Departamento departamento) {
        DepartamentoDTO departamentoDTO = new DepartamentoDTO();
        departamentoDTO.setId(departamento.getId());
        departamentoDTO.setTitulo(departamento.getTitulo());
        departamentoDTO.setQuantidadePessoas(departamento.getPessoas().size());
        departamentoDTO.setQuantidadeTarefas(departamento.getTarefas().size());
        return departamentoDTO;
    }


	public DepartamentoDTO salvarDepartamentoOrder(List<Departamento> departamentoList) throws ParseException {
		DepartamentoDTO departamentoDTO = new DepartamentoDTO();
		try {
			for(Departamento itemDepartamento : departamentoList) {
				Departamento departamento = departamentoRepository.checkTituloDepartamento(itemDepartamento.getTitulo());
				if(itemDepartamento.getOrdem_apresentacao() != departamento.getOrdem_apresentacao()) {
					departamento.setOrdem_apresentacao(itemDepartamento.getOrdem_apresentacao());
					departamentoRepository.save(departamento);
				}
			}
			departamentoDTO.setSuccess(Boolean.TRUE);
			departamentoDTO.setMensagem("A ordem dos departamentos foi salva com sucesso.");
			return departamentoDTO;
		} catch (Exception e) {
			departamentoDTO.setSuccess(Boolean.FALSE);
			departamentoDTO.setMensagem("Houve um erro ao salvar a ordem dos departamentos.");
			return departamentoDTO;
		}
	}


}
