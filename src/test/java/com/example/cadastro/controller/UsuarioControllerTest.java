package com.example.cadastro.controller;

import com.example.cadastro.entities.Usuario;
import com.example.cadastro.service.PagamentoMultaService;
import com.example.cadastro.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private PagamentoMultaService pagamentoMultaService;

    @Test
    void deveRetornar400QuandoCpfForInvalido() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva",
                                  "email": "ana@example.com",
                                  "cpf": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(
                        "O CPF deve conter exatamente 11 dígitos"
                ));

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRetornar409QuandoBancoRejeitarCadastro() throws Exception {
        when(usuarioService.criar(any(Usuario.class)))
                .thenThrow(new DataIntegrityViolationException("CPF duplicado"));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva",
                                  "email": "ana@example.com",
                                  "cpf": "12345678900"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").exists());
    }
}
