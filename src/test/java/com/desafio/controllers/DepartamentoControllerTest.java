// File: DepartamentoControllerTest.java

package com.desafio.controllers;

import com.desafio.model.Departamento;
import com.desafio.service.DepartamentoService;
import com.desafio.view.DepartamentoDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import static org.mockito.Mockito.when;

@WebMvcTest(controllers = DepartamentoController.class)
public class DepartamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartamentoService departamentoService;

    @Test
    public void shouldSaveNewDepartamento() throws Exception {

        // Prepare test data and mock service behaviour
        Departamento newDepartamento = new Departamento();
        when(departamentoService.salvarDepartamento(any(Departamento.class))).thenReturn(newDepartamento.toDTO());

        // Defining request body
        String requestBody = "{\"id\":1,\"pessoas\":[],\"tarefas\":[],\"ordem_apresentacao\":1}";

        // Making the POST request to /departamentos/salvarDepartamento
        MvcResult mvcResult = mockMvc.perform(post("/departamentos/salvarDepartamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn(); // Capture the result of the MVC operation

        // Parsing and asserting response
        // Assert response
        DepartamentoDTO responseDTO = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), DepartamentoDTO.class);
        assertThat(responseDTO).usingRecursiveComparison().isEqualTo(newDepartamento.toDTO());
    }

    @Test
    public void shouldListDepartamentosComQuantidade() throws Exception {
        // Mocking service behaviour
        when(departamentoService.listarDepartamentosComQuantidade()).thenReturn(new ArrayList<>());

        // Making the GET request to /departamentos endpoint
        MvcResult mvcResult = mockMvc.perform(get("/departamentos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn(); // Capture the result of the MVC operation

        // Parsing and asserting response
        List<DepartamentoDTO> responseDTOs = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<DepartamentoDTO>>() {
        });
        assertThat((List<DepartamentoDTO>) responseDTOs).isEmpty();
    }

    @Test
    public void shouldAlterDepartamento() throws Exception {

        // Prepare test data and mock service behaviour
        Departamento existingDepartamento = new Departamento();
        String titulo = "teste";

        // Use eq() for the string argument and any() for the Departamento argument
        when(departamentoService.alterarDepartamento(eq(titulo), any(Departamento.class)))
                .thenReturn(existingDepartamento.toDTO());

        when(departamentoService.alterarDepartamento(any(String.class), any(Departamento.class)))
                .thenReturn(existingDepartamento.toDTO());

        // Defining request body
        String requestBody = "{\"id\":1,\"pessoas\":[],\"tarefas\":[],\"ordem_apresentacao\":1}";

        // Making the PUT request to /departamentos/alterarDepartamento
        MvcResult mvcResult = mockMvc.perform(put("/departamentos/alterarDepartamento/" + titulo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn(); // Capture the result of the MVC operation

        // Parsing and asserting response
        DepartamentoDTO responseDTO = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), DepartamentoDTO.class);
        assertThat(responseDTO).usingRecursiveComparison().isEqualTo(existingDepartamento.toDTO());
    }

    @Test
    public void shouldGetAllDepartamento() throws Exception {

        // Mocking service behaviour
        when(departamentoService.getAllDepartamento()).thenReturn(new ArrayList<>());

        // Making the GET request to /departamentos/getAllDepartamento
        MvcResult mvcResult = mockMvc.perform(get("/departamentos/getAllDepartamento")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn(); // Capture the result of the MVC operation

        // Parsing and asserting response
        List<DepartamentoDTO> responseDTOs = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<DepartamentoDTO>>() {
        });
        assertThat((List<DepartamentoDTO>) responseDTOs).isEmpty();
    }

    @Test
    public void shouldRemoveDepartamento() throws Exception {
        // given
        Long idToRemove = 1L;
        Departamento existingDepartamento = new Departamento();
        when(departamentoService.removerDepartamento(idToRemove)).thenReturn(existingDepartamento.toDTO());

        // when
        MvcResult mvcResult = mockMvc.perform(delete("/departamentos/removerDepartamento/" + idToRemove)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn(); // Capture the result of the MVC operation

        // then
        DepartamentoDTO responseDTO = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), DepartamentoDTO.class);
        assertThat(responseDTO).usingRecursiveComparison().isEqualTo(existingDepartamento.toDTO());
    }
}
