package com.example.cadastro.controller;

import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.service.ExemplarService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExemplarController.class)
class ExemplarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExemplarService exemplarService;

    @Test
    void deveCadastrarExemplarERetornar201() throws Exception {
        LivrosModel livro = new LivrosModel();
        livro.setId(10L);
        livro.setNome("O Hobbit");

        Exemplar exemplar = new Exemplar();
        exemplar.setId(20L);
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("EX-001");

        when(exemplarService.cadastrar(10L, "EX-001")).thenReturn(exemplar);

        mockMvc.perform(post("/livros/10/exemplares")
                        .param("codigoPatrimonio", "EX-001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.codigoPatrimonio").value("EX-001"))
                .andExpect(jsonPath("$.livro.id").value(10));

        verify(exemplarService).cadastrar(10L, "EX-001");
    }

    @Test
    void deveRetornar400QuandoCodigoPatrimonioForVazio() throws Exception {
        when(exemplarService.cadastrar(10L, " "))
                .thenThrow(new IllegalArgumentException("Código de patrimônio é obrigatório"));

        mockMvc.perform(post("/livros/10/exemplares")
                        .param("codigoPatrimonio", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Código de patrimônio é obrigatório"));
    }

    @Test
    void deveRetornar404QuandoLivroNaoExiste() throws Exception {
        when(exemplarService.cadastrar(999L, "EX-999"))
                .thenThrow(new EntityNotFoundException("Livro não encontrado"));

        mockMvc.perform(post("/livros/999/exemplares")
                        .param("codigoPatrimonio", "EX-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Livro não encontrado"));
    }

    @Test
    void deveRetornar400QuandoCodigoPatrimonioNaoForInformado() throws Exception {
        mockMvc.perform(post("/livros/10/exemplares"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(
                        "Parâmetro obrigatório ausente: codigoPatrimonio"
                ));

        verifyNoInteractions(exemplarService);
    }

    @Test
    void deveRetornar204AoDesativarExemplar() throws Exception {
        when(exemplarService.desativar(10L, 20L)).thenReturn(true);

        mockMvc.perform(patch("/livros/10/exemplares/20/desativacao"))
                .andExpect(status().isNoContent());

        verify(exemplarService).desativar(10L, 20L);
    }

    @Test
    void deveRetornar404AoDesativarExemplarDeOutroLivro() throws Exception {
        when(exemplarService.desativar(10L, 20L)).thenReturn(false);

        mockMvc.perform(patch("/livros/10/exemplares/20/desativacao"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409AoDesativarExemplarEmprestado() throws Exception {
        when(exemplarService.desativar(10L, 20L))
                .thenThrow(new IllegalStateException(
                        "Não é possível desativar um exemplar emprestado"
                ));

        mockMvc.perform(patch("/livros/10/exemplares/20/desativacao"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(
                        "Não é possível desativar um exemplar emprestado"
                ));
    }

    @Test
    void deveRetornar204AoAtivarExemplar() throws Exception {
        when(exemplarService.ativar(10L, 20L)).thenReturn(true);

        mockMvc.perform(patch("/livros/10/exemplares/20/ativacao"))
                .andExpect(status().isNoContent());

        verify(exemplarService).ativar(10L, 20L);
    }

    @Test
    void deveRetornar409AoAtivarExemplarDeLivroInativo() throws Exception {
        when(exemplarService.ativar(10L, 20L))
                .thenThrow(new IllegalStateException(
                        "Não é possível reativar um exemplar de um livro inativo"
                ));

        mockMvc.perform(patch("/livros/10/exemplares/20/ativacao"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(
                        "Não é possível reativar um exemplar de um livro inativo"
                ));
    }
}
