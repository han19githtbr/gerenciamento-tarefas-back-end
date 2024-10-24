package com.desafio.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.desafio.model.Departamento;
import com.desafio.model.Pessoa;
import com.desafio.model.Tarefa;
import com.desafio.repository.DepartamentoRepository;
import com.desafio.repository.PessoaRepository;
import com.desafio.view.DepartamentoDTO;
import com.desafio.view.PessoaDTO;

@Service
public class PessoaService {

	@Autowired
    private PessoaRepository pessoaRepository;
	
	@Autowired
    private DepartamentoRepository departamentoRepository;
		
	public PessoaDTO salvarPessoa(Pessoa pessoa) throws ParseException {
	    PessoaDTO pessoaDTO = new PessoaDTO();
	    
	    if (pessoa.getNome() == null || pessoa.getDepartamento() == null || pessoa.getDepartamento().getId() == null) {
	        pessoaDTO.setSuccess(Boolean.FALSE);
	        pessoaDTO.setMensagem("O nome e o departamento não podem ser nulos");
	        return pessoaDTO;
	    }
	    
		if (pessoaRepository.checkNomePessoa(pessoa.getNome()) != null) {
			pessoaDTO.setSuccess(Boolean.FALSE);
			pessoaDTO.setMensagem("Já existe uma pessoa com esse nome.");
			return pessoaDTO;
		}

	    Departamento departamento = departamentoRepository.findById(pessoa.getDepartamento().getId()).orElse(null);
	    if (departamento == null) {
	        pessoaDTO.setSuccess(Boolean.FALSE);
	        pessoaDTO.setMensagem("Esse departamento não existe");
	        return pessoaDTO;
	    }
	    
		Long nextOrdem = this.nextOrdem();

		pessoa.setOrdem_apresentacao(nextOrdem);

	    pessoaRepository.save(pessoa);
	    pessoaDTO.setId(pessoa.getId());
		pessoaDTO.setNome(pessoa.getNome());
		pessoaDTO.setOrdem_apresentacao(pessoa.getOrdem_apresentacao());
		pessoaDTO.setMensagem("A pessoa " + pessoa.getNome() + " foi salvo(a) com sucesso");
	    pessoaDTO.setSuccess(Boolean.TRUE);
	    
		return pessoaDTO;
	}	


	private Long nextOrdem() {
		List<Pessoa> pessoaDTO = pessoaRepository.ordemApresentacaoDesc();
	
		if (!pessoaDTO.isEmpty() && pessoaDTO.get(0).getOrdem_apresentacao() != null) {
			Long maiorNumber = pessoaDTO.get(0).getOrdem_apresentacao();
			return maiorNumber + 1;
		} else {
			return 1L;
		}
	}


	public PessoaDTO removerPessoa(Long id) throws ParseException{
		PessoaDTO pessoaDTO = new PessoaDTO();
		Pessoa pessoa = pessoaRepository.getById(id);
		if(Objects.nonNull(pessoa)) {
			pessoaRepository.delete(pessoa);
			pessoaDTO.setMensagem("A pessoa foi removida com sucesso");
			pessoaDTO.setSuccess(Boolean.TRUE);
		} else {
			pessoaDTO.setMensagem("A pessoa não foi removida");
			pessoaDTO.setSuccess(Boolean.FALSE);
		}
		return pessoaDTO;
	}

	
	public PessoaDTO alterarPessoa(String nome, Pessoa pessoa) {
		PessoaDTO pessoaDTO = new PessoaDTO();
		Pessoa pessoaModel = pessoaRepository.checkNomePessoa(nome);
		if(Objects.nonNull(pessoaModel)) {
			if(pessoa.getNome() == null) {
				pessoaDTO.setSuccess(Boolean.FALSE);
				pessoaDTO.setMensagem("O campo Nome é obrigatório");
				return pessoaDTO;
			}
			
			if(!nome.equals(pessoa.getNome())) {
				Pessoa novaPessoaModel = pessoaRepository.checkNomePessoa(pessoa.getNome());
				if(Objects.isNull(novaPessoaModel)) {
					pessoaModel.setNome(pessoa.getNome());
					
					pessoaRepository.save(pessoaModel);
					
					pessoaDTO.setMensagem("A pessoa " + pessoa.getNome() + " foi salva com sucesso.");
					pessoaDTO.setSuccess(Boolean.TRUE);
				} else {
					pessoaDTO.setSuccess(Boolean.FALSE);
					pessoaDTO.setMensagem("Já existe uma pessoa com esse nome.");
					return pessoaDTO;
				}
			}else{
				pessoaModel.setNome(pessoa.getNome());
				
				pessoaRepository.save(pessoaModel);
				
				pessoaDTO.setMensagem("A pessoa " + pessoa.getNome() + " foi salva com sucesso.");
				pessoaDTO.setSuccess(Boolean.TRUE);
			}
			
			pessoaDTO.setId(pessoa.getId());
			pessoaDTO.setNome(pessoa.getNome());
			
			return pessoaDTO;
	
		}else {
			pessoaDTO.setMensagem("A pessoa não foi editada.");
			pessoaDTO.setSuccess(Boolean.FALSE);
			return pessoaDTO;
		}
	}

	

	public List<PessoaDTO> getAllPessoa() {
		List<Pessoa> pessoas = pessoaRepository.getAllPessoa();
	    List<PessoaDTO> pessoasDTO = new ArrayList<>();

	    for (Pessoa pessoa : pessoas) {
	        pessoasDTO.add(pessoa.toDTO());
	    }

	    return pessoasDTO;
	}
	
	public PessoaDTO buscarPorNome(String nome, LocalDateTime dataCriacao, long duracao) {
	    PessoaDTO pessoaDTO = new PessoaDTO();
	    List<Pessoa> pessoas = pessoaRepository.findByName(nome);

	    if (pessoas.isEmpty()) {
	        pessoaDTO.setMensagem("A pessoa não existe");
	        pessoaDTO.setSuccess(Boolean.FALSE);
	        return pessoaDTO;
	    }

	    List<Tarefa> tarefas = pessoaRepository.findByNameAndPeriod(nome, dataCriacao, duracao);
	    
	    if (tarefas.isEmpty()) {
	        pessoaDTO.setMensagem("A tarefa não existe");
	        pessoaDTO.setSuccess(Boolean.FALSE);
	        return pessoaDTO;
	    }

	    double mediaHorasPorTarefa = tarefas.stream().mapToLong(Tarefa::getDuracao).average().orElse(0.0);
	    pessoaDTO.setMensagem("A média de horas gastas por tarefa é: " + mediaHorasPorTarefa);
	    pessoaDTO.setSuccess(Boolean.TRUE);
	    return pessoaDTO;
	}


	public PessoaDTO salvarPessoaOrder(List<Pessoa> pessoaList) throws ParseException {
		PessoaDTO pessoaDTO = new PessoaDTO();
		try {
			for(Pessoa itemPessoa : pessoaList) {
				Pessoa pessoa = pessoaRepository.checkNomePessoa(itemPessoa.getNome());
				if(!Objects.equals(itemPessoa.getOrdem_apresentacao(), pessoa.getOrdem_apresentacao())) {
					pessoa.setOrdem_apresentacao(itemPessoa.getOrdem_apresentacao());
					pessoaRepository.save(pessoa);
				}
			}
			
			pessoaDTO.setSuccess(Boolean.TRUE);
			pessoaDTO.setMensagem("A ordem das pessoas foi salva com sucesso.");
			return pessoaDTO;
		} catch (Exception e) {
			pessoaDTO.setSuccess(Boolean.FALSE);
			pessoaDTO.setMensagem("Houve um erro ao salvar a ordem das pessoas.");
			return pessoaDTO;
		}
	}

}
