package com.desafio.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.model.Departamento;
import com.desafio.service.DepartamentoService;
import com.desafio.view.DepartamentoDTO;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/departamentos")
// CORS handled globally by SecurityConfig.corsConfigurationSource()
// Removido @CrossOrigin para evitar conflito com o filtro CORS do Spring
// Security
public class DepartamentoController {

	@Autowired
	private DepartamentoService departamentoService;

	@PostMapping(value = "/salvarDepartamento", consumes = "application/json", produces = "application/json")
	@Transactional(rollbackFor = { Exception.class })
	public DepartamentoDTO salvarDepartamento(
			@RequestBody Departamento departamento) throws IOException, ParseException {
		return departamentoService.salvarDepartamento(departamento);
	}

	@GetMapping(produces = "application/json")
	public List<DepartamentoDTO> listarDepartamentosComQuantidade() {
		return departamentoService.listarDepartamentosComQuantidade();
	}

	@GetMapping(value = "/getAllDepartamento", produces = "application/json")
	public List<DepartamentoDTO> getAllDepartamento() {
		return departamentoService.getAllDepartamento();
	}

	@DeleteMapping(value = "/removerDepartamento/{id}", produces = "application/json")
	@Transactional(rollbackFor = { Exception.class })
	public DepartamentoDTO removerDepartamento(
			@PathVariable Long id,
			HttpServletRequest request) throws IOException {
		return departamentoService.removerDepartamento(id);
	}

	@PutMapping(value = "/alterarDepartamento/{titulo}", consumes = "application/json", produces = "application/json")
	@Transactional(rollbackFor = { Exception.class })
	public DepartamentoDTO alterarDepartamento(
			@PathVariable String titulo,
			@RequestBody Departamento departamento) throws IOException {
		return departamentoService.alterarDepartamento(titulo, departamento);
	}

	@PutMapping(value = "/salvarDepartamentoOrder", consumes = "application/json", produces = "application/json")
	@Transactional(rollbackFor = { Exception.class })
	public DepartamentoDTO salvarDepartamentoOrder(
			@RequestBody List<Departamento> departamentoList) throws ParseException {
		return departamentoService.salvarDepartamentoOrder(departamentoList);
	}
}
