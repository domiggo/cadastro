package com.example.cadastro.controller;

import com.example.cadastro.dto.ResumoBiblioteca;
import com.example.cadastro.service.ResumoBibliotecaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResumoBibliotecaController.class)
class ResumoBibliotecaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumoBibliotecaService resumoBibliotecaService;

    @Test
    void deveRetornarContagensDoPainel() throws Exception {
        when(resumoBibliotecaService.consultar())
                .thenReturn(new ResumoBiblioteca(12, 8, 5, 2));

        mockMvc.perform(get("/biblioteca/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuariosAtivos").value(12))
                .andExpect(jsonPath("$.livrosAtivos").value(8))
                .andExpect(jsonPath("$.emprestimosAtivos").value(5))
                .andExpect(jsonPath("$.emprestimosAtrasados").value(2));
    }
}
