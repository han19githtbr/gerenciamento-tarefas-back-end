package com.desafio.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.desafio.model.Pessoa;
import com.desafio.service.PessoaService;
import com.desafio.view.PessoaDTO;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/pessoas")
public class PessoaController {

	@Autowired
	private PessoaService pessoaService;

	@PostMapping("/salvarPessoa")
	@Transactional(rollbackFor = Exception.class)
	public PessoaDTO salvarPessoa(@RequestBody Pessoa pessoa, HttpServletRequest request)
			throws IOException, ParseException {
		return pessoaService.salvarPessoa(pessoa);
	}

	@GetMapping("/getAllPessoa")
	public List<PessoaDTO> getAllPessoa() {
		return pessoaService.getAllPessoa();
	}

	@DeleteMapping("/removerPessoa/{id}")
	@Transactional(rollbackFor = Exception.class)
	public PessoaDTO removerPessoa(@PathVariable Long id, HttpServletRequest request)
			throws IOException, ParseException {
		return pessoaService.removerPessoa(id);
	}

	@PutMapping("/alterarPessoa/{nome}")
	@Transactional(rollbackFor = Exception.class)
	public PessoaDTO alterarPessoa(@PathVariable String nome, @RequestBody Pessoa pessoa, HttpServletRequest request)
			throws IOException {
		return pessoaService.alterarPessoa(nome, pessoa);
	}

	@GetMapping("/gastos")
	public PessoaDTO buscarPorNome(
			@RequestParam String nome,
			@RequestParam String dataCriacao,
			@RequestParam long duracao) throws IOException {
		LocalDateTime dataCriacaoConvertida = LocalDateTime.parse(dataCriacao, DateTimeFormatter.ISO_DATE_TIME);
		return pessoaService.buscarPorNome(nome, dataCriacaoConvertida, duracao);
	}

	// ← ADICIONAR: chamado pelo front-end para buscar pessoas de um departamento
	@GetMapping("/getPessoasDepartamentos/{departamentoId}")
	public List<PessoaDTO> getPessoasDepartamentos(@PathVariable Long departamentoId) {
		return pessoaService.getPessoasPorDepartamento(departamentoId);
	}

	// ← ADICIONAR: chamado pelo front-end para salvar ordem de pessoas
	@PutMapping("/salvarPessoaOrder")
	@Transactional(rollbackFor = Exception.class)
	public PessoaDTO salvarPessoaOrder(@RequestBody List<Pessoa> pessoaList) throws ParseException {
		return pessoaService.salvarPessoaOrder(pessoaList);
	}
}
