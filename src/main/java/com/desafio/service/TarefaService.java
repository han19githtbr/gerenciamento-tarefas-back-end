package com.desafio.service;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import com.desafio.model.Departamento;
import com.desafio.model.Pessoa;
import com.desafio.model.Tarefa;
import com.desafio.repository.PessoaRepository;
import com.desafio.repository.TarefaRepository;
import com.desafio.view.DepartamentoDTO;
import com.desafio.view.PessoaDTO;
import com.desafio.view.TarefaDTO;

@Service
public class TarefaService {

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private TarefaRepository tarefaRepository;

	public TarefaDTO salvarTarefa(Tarefa tarefa) throws ParseException {

		System.out.println("Salvando tarefa: " + tarefa.getTitulo());
		System.out.println(
				"Departamento: " + (tarefa.getDepartamento() != null ? tarefa.getDepartamento().getTitulo() : "NULL"));

		tarefa.setDataCriacao(LocalDateTime.now());
		TarefaDTO tarefaDTO = new TarefaDTO();
		if (tarefa.getTitulo() == null || tarefa.getDescricao() == null || tarefa.getPrazo() == null) {
			tarefaDTO.setSuccess(Boolean.FALSE);
			tarefaDTO.setMensagem("Os campos da tarefa não podem ser nulos");
			return tarefaDTO;
		}

		if (tarefaRepository.checkTituloTarefa(tarefa.getTitulo()) != null) {
			tarefaDTO.setSuccess(Boolean.FALSE);
			tarefaDTO.setMensagem("Já existe uma tarefa com esse nome.");
			return tarefaDTO;
		}

		Long nextOrdem = this.nextOrdem();

		tarefa.setOrdem_apresentacao(nextOrdem);

		tarefaRepository.save(tarefa);
		tarefaDTO.setId(tarefa.getId());
		tarefaDTO.setTitulo(tarefa.getTitulo());
		tarefaDTO.setDescricao(tarefa.getDescricao());
		tarefaDTO.setPrazo(tarefa.getPrazo());

		if (tarefa.getDepartamento() != null) {
			tarefaDTO.setDepartamento(tarefa.getDepartamento().getTitulo());
			tarefaDTO.setDepartamentoId(tarefa.getDepartamento().getId());
		}

		tarefaDTO.setMensagem(tarefa.getTitulo() + " Foi salva com sucesso");

		tarefaDTO.setSuccess(Boolean.TRUE);
		return tarefaDTO;

	}

	private Long nextOrdem() {
		List<Tarefa> tarefaDTO = tarefaRepository.ordemApresentacaoDesc();

		if (!tarefaDTO.isEmpty() && tarefaDTO.get(0).getOrdem_apresentacao() != null) {
			Long maiorNumber = tarefaDTO.get(0).getOrdem_apresentacao();
			return maiorNumber + 1;
		} else {
			return 1L;
		}
	}

	public TarefaDTO alocarPessoaNaTarefa(Long tarefaId, Long pessoaId) {
		// ADICIONE LOGS PARA DEBUG - isso ajudará a identificar o problema
		System.out.println("=== INÍCIO ALOCAÇÃO ===");
		System.out.println("Tarefa ID: " + tarefaId);
		System.out.println("Pessoa ID: " + pessoaId);

		Tarefa tarefa = tarefaRepository.findById(tarefaId)
				.orElseThrow(() -> {
					System.out.println("ERRO: Tarefa não encontrada com ID: " + tarefaId);
					return new EntityNotFoundException("Tarefa não encontrada.");
				});
		Pessoa pessoa = pessoaRepository.findById(pessoaId)
				.orElseThrow(() -> {
					System.out.println("ERRO: Pessoa não encontrada com ID: " + pessoaId);
					return new EntityNotFoundException("Pessoa não encontrada.");
				});

		TarefaDTO tarefaDTO = new TarefaDTO();

		// LOGS DE DEBUG
		System.out.println("Tarefa encontrada: " + tarefa.getTitulo());
		System.out.println("Pessoa encontrada: " + pessoa.getNome());
		System.out.println("Tarefa Departamento: " +
				(tarefa.getDepartamento() != null
						? tarefa.getDepartamento().getId() + " - " + tarefa.getDepartamento().getTitulo()
						: "NULL"));
		System.out.println("Pessoa Departamento: " +
				(pessoa.getDepartamento() != null
						? pessoa.getDepartamento().getId() + " - " + pessoa.getDepartamento().getTitulo()
						: "NULL"));

		// Verificação mais robusta de departamentos
		if (tarefa.getDepartamento() == null) {
			System.out.println("ERRO: Tarefa não possui departamento");
			tarefaDTO.setSuccess(Boolean.FALSE);
			tarefaDTO.setMensagem("A tarefa não possui departamento.");
			return tarefaDTO;
		}

		if (pessoa.getDepartamento() == null) {
			System.out.println("ERRO: Pessoa não possui departamento");
			tarefaDTO.setSuccess(Boolean.FALSE);
			tarefaDTO.setMensagem("A pessoa não possui departamento.");
			return tarefaDTO;
		}

		// Comparar IDs dos departamentos em vez de objetos inteiros
		Long tarefaDeptId = tarefa.getDepartamento().getId();
		Long pessoaDeptId = pessoa.getDepartamento().getId();

		System.out.println("Comparando departamentos: Tarefa=" + tarefaDeptId + ", Pessoa=" + pessoaDeptId);

		if (!tarefaDeptId.equals(pessoaDeptId)) {
			System.out.println("ERRO: Departamentos diferentes");
			tarefaDTO.setSuccess(Boolean.FALSE);
			tarefaDTO.setMensagem("A pessoa não pertence ao mesmo departamento da tarefa.");
			return tarefaDTO;
		}

		// Verificar se a tarefa já está finalizada
		if (tarefa.isFinalizado()) {
			System.out.println("ERRO: Tarefa já finalizada");
			tarefaDTO.setSuccess(Boolean.FALSE);
			tarefaDTO.setMensagem("Não é possível alocar pessoa em tarefa finalizada.");
			return tarefaDTO;
		}

		// ADICIONE ESTA LINHA - atualiza a data de modificação
		tarefa.setDataCriacao(LocalDateTime.now());

		tarefa.setPessoa(pessoa);
		tarefaRepository.save(tarefa);

		System.out.println("SUCESSO: Pessoa alocada na tarefa");

		tarefaDTO.setSuccess(Boolean.TRUE);
		tarefaDTO.setMensagem("A pessoa foi alocada com sucesso");

		// Preencha mais dados no DTO se necessário
		tarefaDTO.setId(tarefa.getId());
		tarefaDTO.setTitulo(tarefa.getTitulo());
		tarefaDTO.setDescricao(tarefa.getDescricao());
		if (tarefa.getPessoa() != null) {
			tarefaDTO.setPessoaId(tarefa.getPessoa().getId());
		}

		return tarefaDTO;
	}

	public List<TarefaDTO> getAllTarefa() {
		List<Tarefa> tarefas = tarefaRepository.getAllTarefa();
		List<TarefaDTO> tarefasDTO = new ArrayList<>();

		for (Tarefa tarefa : tarefas) {
			tarefasDTO.add(tarefa.toDTO());
		}

		return tarefasDTO;
	}

	public TarefaDTO finalizarTarefa(Long tarefaId) {
		Tarefa tarefa = tarefaRepository.findById(tarefaId)
				.orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada."));
		TarefaDTO tarefaDTO = new TarefaDTO();

		if (tarefa.isFinalizado()) {
			tarefaDTO.setSuccess(Boolean.TRUE);
			tarefaDTO.setMensagem("A tarefa já foi finalizada anteriormente.");
			return tarefaDTO;
		}

		tarefa.setFinalizado(true);
		tarefa.setDuracao(tarefa.getDuracao());
		tarefaRepository.save(tarefa);
		tarefaDTO.setDuracao(tarefa.getDuracao());
		tarefaDTO.setFinalizado(true);
		tarefaDTO.setSuccess(Boolean.TRUE);

		return tarefaDTO;
	}

	public TarefaDTO removerTarefa(Long id) {
		TarefaDTO tarefaDTO = new TarefaDTO();
		Tarefa tarefa = tarefaRepository.getById(id);
		if (Objects.nonNull(tarefa)) {
			tarefaRepository.delete(tarefa);
			tarefaDTO.setMensagem("A tarefa foi removida com sucesso");
			tarefaDTO.setSuccess(Boolean.TRUE);
		} else {
			tarefaDTO.setMensagem("A tarefa não foi removida");
			tarefaDTO.setSuccess(Boolean.FALSE);
		}
		return tarefaDTO;
	}

	public TarefaDTO alterarTarefa(String titulo, Tarefa tarefa) {
		TarefaDTO tarefaDTO = new TarefaDTO();
		Tarefa tarefaModel = tarefaRepository.checkTituloTarefa(titulo);
		if (Objects.nonNull(tarefaModel)) {
			if (tarefa.getTitulo() == null) {
				tarefaDTO.setSuccess(Boolean.FALSE);
				tarefaDTO.setMensagem("O campo Titulo é obrigatório");
				return tarefaDTO;
			}

			if (!titulo.equals(tarefa.getTitulo())) {
				Tarefa novaTarefaModel = tarefaRepository.checkTituloTarefa(tarefa.getTitulo());
				if (Objects.isNull(novaTarefaModel)) {
					tarefaModel.setTitulo(tarefa.getTitulo());
					tarefaModel.setDescricao(tarefa.getDescricao());
					tarefaModel.setPrazo(tarefa.getPrazo());

					tarefaRepository.save(tarefaModel);

					tarefaDTO.setMensagem("A tarefa " + tarefa.getTitulo() + " foi salva com sucesso.");
					tarefaDTO.setSuccess(Boolean.TRUE);
				} else {
					tarefaDTO.setSuccess(Boolean.FALSE);
					tarefaDTO.setMensagem("Já existe uma tarefa com esse nome.");
					return tarefaDTO;
				}
			} else {
				tarefaModel.setTitulo(tarefa.getTitulo());
				tarefaModel.setDescricao(tarefa.getDescricao());
				tarefaModel.setPrazo(tarefa.getPrazo());

				tarefaRepository.save(tarefaModel);

				tarefaDTO.setMensagem("A tarefa " + tarefa.getTitulo() + " foi salva com sucesso.");
				tarefaDTO.setSuccess(Boolean.TRUE);
			}

			if (tarefa.getDepartamento() != null) {
				tarefaModel.setDepartamento(tarefa.getDepartamento());
			}

			if (tarefaModel.getDepartamento() != null) {
				tarefaDTO.setDepartamento(tarefaModel.getDepartamento().getTitulo());
				tarefaDTO.setDepartamentoId(tarefaModel.getDepartamento().getId());
			}

			tarefaDTO.setId(tarefa.getId());
			tarefaDTO.setTitulo(tarefa.getTitulo());
			tarefaDTO.setDescricao(tarefa.getDescricao());
			tarefaDTO.setPrazo(tarefa.getPrazo());
			tarefaDTO.setOrdem_apresentacao(tarefa.getOrdem_apresentacao());

			return tarefaDTO;

		} else {
			tarefaDTO.setMensagem("A tarefa não foi alterada.");
			tarefaDTO.setSuccess(Boolean.FALSE);
			return tarefaDTO;
		}
	}

	public List<TarefaDTO> listarTarefasPendentes() {
		List<TarefaDTO> tarefasPendentes = new ArrayList<>();
		Pageable pageable = PageRequest.of(0, 3);
		List<Tarefa> tarefas = tarefaRepository.findTarefasSemAlocacao(pageable);

		for (Tarefa tarefa : tarefas) {
			TarefaDTO tarefaDTO = new TarefaDTO();
			tarefaDTO.setId(tarefa.getId());
			tarefaDTO.setDescricao(tarefa.getDescricao());
			// VERIFICA SE DEPARTAMENTO NÃO É NULL
			if (tarefa.getDepartamento() != null) {
				tarefaDTO.setDepartamento(tarefa.getDepartamento().getTitulo());
			} else {
				tarefaDTO.setDepartamento("Sem departamento");
			}
			tarefaDTO.setDuracao(tarefa.getDuracao());
			tarefaDTO.setPrazo(tarefa.getPrazo());
			tarefaDTO.setTitulo(tarefa.getTitulo());
			tarefasPendentes.add(tarefaDTO);
		}

		return tarefasPendentes;
	}

	public TarefaDTO salvarTarefaOrder(List<Tarefa> tarefaList) throws ParseException {
		TarefaDTO tarefaDTO = new TarefaDTO();
		try {
			for (Tarefa itemTarefa : tarefaList) {
				Tarefa tarefa = tarefaRepository.checkTituloTarefa(itemTarefa.getTitulo());
				if (!Objects.equals(itemTarefa.getOrdem_apresentacao(), tarefa.getOrdem_apresentacao())) {
					tarefa.setOrdem_apresentacao(itemTarefa.getOrdem_apresentacao());
					tarefaRepository.save(tarefa);
				}
			}
			tarefaDTO.setSuccess(Boolean.TRUE);
			tarefaDTO.setMensagem("A ordem das tarefas foi salva com sucesso.");
			return tarefaDTO;
		} catch (Exception e) {
			tarefaDTO.setSuccess(Boolean.FALSE);
			tarefaDTO.setMensagem("Houve um erro ao salvar a ordem das tarefas.");
			return tarefaDTO;
		}
	}
}
