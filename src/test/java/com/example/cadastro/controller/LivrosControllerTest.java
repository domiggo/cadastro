package com.example.cadastro.controller;

import com.example.cadastro.service.LivrosService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LivrosController.class)
class LivrosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LivrosService livrosService;

    @Test
    void deveRetornar204AoExcluirLivroSemHistorico() throws Exception {
        doNothing().when(livrosService).excluir(10L);

        mockMvc.perform(delete("/livros/10"))
                .andExpect(status().isNoContent());

        verify(livrosService).excluir(10L);
    }

    @Test
    void deveRetornar404QuandoLivroNaoExiste() throws Exception {
        doThrow(new EntityNotFoundException("Livro não encontrado"))
                .when(livrosService).excluir(999L);

        mockMvc.perform(delete("/livros/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Livro não encontrado"));
    }

    @Test
    void deveRetornar409QuandoLivroPossuiHistorico() throws Exception {
        doThrow(new IllegalStateException(
                "Não é possível excluir um livro com histórico de empréstimos"
        )).when(livrosService).excluir(11L);

        mockMvc.perform(delete("/livros/11"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(
                        "Não é possível excluir um livro com histórico de empréstimos"
                ));
    }
}
