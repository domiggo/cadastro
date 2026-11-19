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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void deveAceitarCpfNoCadastroSemRetornaLoNaResposta() throws Exception {
        when(usuarioService.criar(any(Usuario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva",
                                  "email": "ana@example.com",
                                  "cpf": "12345678900"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.cpf").doesNotExist());

        verify(usuarioService).criar(argThat(usuario ->
                "12345678900".equals(usuario.getCpf())
        ));
    }

    @Test
    void naoDeveRetornarCpfAoConsultarUsuario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Ana Silva");
        usuario.setEmail("ana@example.com");
        usuario.setCpf("12345678900");
        when(usuarioService.buscarPorId(1L)).thenReturn(java.util.Optional.of(usuario));

        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.cpf").doesNotExist());
    }
}
