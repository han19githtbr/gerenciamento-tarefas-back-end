package com.desafio.controllers;

import java.io.IOException;
import java.text.ParseException;
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
import org.springframework.web.bind.annotation.RestController;

import com.desafio.model.Departamento;
import com.desafio.model.Pessoa;
import com.desafio.service.DepartamentoService;
import com.desafio.view.DepartamentoDTO;
import com.desafio.view.PessoaDTO;
import com.desafio.view.TarefaDTO;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/departamentos")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost" }, maxAge = 3600)
public class DepartamentoController {

	@Autowired
	private DepartamentoService departamentoService;

	@PostMapping("/salvarDepartamento")
	@Transactional(rollbackFor = Exception.class)
	public DepartamentoDTO salvarDepartamento(@RequestBody Departamento departamento, HttpServletRequest request) throws IOException, ParseException {
		DepartamentoDTO departamentoDTO = departamentoService.salvarDepartamento(departamento);
		
		return departamentoDTO;
	}
	
	@GetMapping
    public List<DepartamentoDTO> listarDepartamentosComQuantidade() {
        return departamentoService.listarDepartamentosComQuantidade();
    }


	@GetMapping("/getAllDepartamento")
    public List<DepartamentoDTO> getAllDepartamento() {
        return departamentoService.getAllDepartamento();
    }


	@DeleteMapping("/removerDepartamento/{id}")
	@Transactional(rollbackFor = Exception.class)
	public DepartamentoDTO removerDepartamento(@PathVariable Long id, HttpServletRequest request) throws IOException {
		DepartamentoDTO departamentoDTO = departamentoService.removerDepartamento(id);
		return departamentoDTO;
	}

	@PutMapping("/alterarDepartamento/{titulo}")
	@Transactional(rollbackFor = Exception.class)
	public DepartamentoDTO alterarDepartamento(@PathVariable String titulo, @RequestBody Departamento departamento, HttpServletRequest request) throws IOException {
		DepartamentoDTO departamentoDTO = departamentoService.alterarDepartamento(titulo, departamento);
		return departamentoDTO;
	}


	@PutMapping("/salvarDepartamentoOrder")
	@Transactional(rollbackFor = Exception.class)
	public DepartamentoDTO salvarDepartamentoOrder(@RequestBody List<Departamento> departamentoList) throws ParseException{
	    DepartamentoDTO departamentoDTO = departamentoService.salvarDepartamentoOrder(departamentoList);
	    return departamentoDTO;
	}

}
