package com.desafio.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.model.Pessoa;
import com.desafio.service.PessoaService;
import com.desafio.view.PessoaDTO;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/pessoas")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost" }, maxAge = 3600)
public class PessoaController {

	@Autowired
    private PessoaService pessoaService;

	@PostMapping("/salvarPessoa")
	@Transactional(rollbackFor = Exception.class)
	public PessoaDTO salvarPessoa(@RequestBody Pessoa pessoa, HttpServletRequest request) throws IOException, ParseException {
		PessoaDTO pessoaDTO = pessoaService.salvarPessoa(pessoa);
		return pessoaDTO;
	}

	@GetMapping("/getAllPessoa")
    public List<PessoaDTO> getAllPessoa() {
        return pessoaService.getAllPessoa();
    }
	
	@DeleteMapping("/removerPessoa/{id}")
	@Transactional(rollbackFor = Exception.class)
	public PessoaDTO removerPessoa(@PathVariable Long id, HttpServletRequest request) throws IOException, ParseException {
		PessoaDTO pessoaDTO = pessoaService.removerPessoa(id);
		return pessoaDTO;
	}

	@PutMapping("/alterarPessoa/{nome}")
	@Transactional(rollbackFor = Exception.class)
	public PessoaDTO alterarPessoa(@PathVariable String nome, @RequestBody Pessoa pessoa, HttpServletRequest request) throws IOException {
		PessoaDTO pessoaDTO = pessoaService.alterarPessoa(nome, pessoa);
		return pessoaDTO;
	}
	
	@GetMapping("/gastos")
    public PessoaDTO buscarPorNome(
        @RequestParam String nome,
        @RequestParam String dataCriacao,
        @RequestParam long duracao) throws IOException {

		LocalDateTime dataCriacaoConvertida = LocalDateTime.parse(dataCriacao, DateTimeFormatter.ISO_DATE_TIME);
		PessoaDTO pessoaDTO = pessoaService.buscarPorNome(nome, dataCriacaoConvertida, duracao);

        return pessoaDTO;
    }
}
