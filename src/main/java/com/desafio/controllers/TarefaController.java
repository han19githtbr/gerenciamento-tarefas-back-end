package com.desafio.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.model.Pessoa;
import com.desafio.model.Tarefa;
import com.desafio.service.TarefaService;
import com.desafio.view.PessoaDTO;
import com.desafio.view.TarefaDTO;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/tarefas")
// @CrossOrigin(origins = { "http://localhost:4200", "http://localhost" },
// maxAge = 3600)
public class TarefaController {

	@Autowired
	private TarefaService tarefaService;

	@PostMapping("/salvarTarefa")
	@Transactional(rollbackFor = Exception.class)
	public TarefaDTO salvarTarefa(@RequestBody Tarefa tarefa, HttpServletRequest request)
			throws IOException, ParseException {
		// TarefaDTO tarefaDTO = tarefaService.salvarTarefa(tarefa);

		// return tarefaDTO;
		return tarefaService.salvarTarefa(tarefa);
	}

	@GetMapping("/getAllTarefa")
	public List<TarefaDTO> getAllTarefa() {
		return tarefaService.getAllTarefa();
	}

	@PutMapping("/alocar/{tarefaId}/{pessoaId}")
	public ResponseEntity<TarefaDTO> alocarPessoaNaTarefa(
			@PathVariable Long tarefaId,
			@PathVariable Long pessoaId,
			@RequestBody(required = false) Map<String, String> body) { // ← ADICIONAR body

		String emailPessoa = (body != null) ? body.get("emailPessoa") : null;
		TarefaDTO tarefaDTO = tarefaService.alocarPessoaNaTarefa(tarefaId, pessoaId, emailPessoa);
		return ResponseEntity.ok(tarefaDTO);
	}

	@PutMapping("/finalizar/{tarefaId}")
	public TarefaDTO finalizarTarefa(@PathVariable Long tarefaId) {
		// TarefaDTO tarefaDTO = tarefaService.finalizarTarefa(tarefaId);
		// return tarefaDTO;
		return tarefaService.finalizarTarefa(tarefaId);
	}

	@DeleteMapping("/removerTarefa/{id}")
	@Transactional(rollbackFor = Exception.class)
	public TarefaDTO removerTarefa(@PathVariable Long id, HttpServletRequest request) throws IOException {
		// TarefaDTO tarefaDTO = tarefaService.removerTarefa(id);
		// return tarefaDTO;
		return tarefaService.removerTarefa(id);
	}

	@GetMapping("/pendentes")
	public List<TarefaDTO> listarTarefasPendentes() {
		return tarefaService.listarTarefasPendentes();
	}

	@PutMapping("/alterarTarefa/{titulo}")
	@Transactional(rollbackFor = Exception.class)
	public TarefaDTO alterarTarefa(@PathVariable String titulo, @RequestBody Tarefa tarefa, HttpServletRequest request)
			throws IOException {
		// TarefaDTO tarefaDTO = tarefaService.alterarTarefa(titulo, tarefa);
		// return tarefaDTO;
		return tarefaService.alterarTarefa(titulo, tarefa);
	}

	@PutMapping("/salvarTarefaOrder")
	@Transactional(rollbackFor = Exception.class)
	public TarefaDTO salvarTarefaOrder(@RequestBody List<Tarefa> tarefaList) throws ParseException {
		// TarefaDTO tarefaDTO = tarefaService.salvarTarefaOrder(tarefaList);
		// return tarefaDTO;
		return tarefaService.salvarTarefaOrder(tarefaList);
	}

	// Feature 1 - Em Andamento
	@GetMapping("/emAndamento")
	public List<TarefaDTO> listarTarefasEmAndamento() {
		return tarefaService.listarTarefasEmAndamento();
	}

	@GetMapping("/contagemEmAndamento")
	public Map<String, Long> contagemEmAndamento() {
		long count = tarefaService.contarTarefasEmAndamento();
		return Collections.singletonMap("total", count);
	}

}
