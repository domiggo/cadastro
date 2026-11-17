package com.example.cadastro.controller;

import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.entities.Usuario;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.ExemplarRepository;
import com.example.cadastro.repository.LivrosRepository;
import com.example.cadastro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmprestimoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LivrosRepository livrosRepository;

    @Autowired
    private ExemplarRepository exemplarRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Test
    void deveSerializarDadosRelacionadosComOpenSessionInViewDesativado() throws Exception {
        String sufixo = Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );
        String nomeUsuario = "Ana Silva " + sufixo;
        String nomeLivro = "O Hobbit " + sufixo;

        Usuario usuario = new Usuario();
        usuario.setNome(nomeUsuario);
        usuario.setEmail("ana-" + sufixo + "@example.com");
        usuario.setCpf(sufixo);
        usuario = usuarioRepository.save(usuario);

        LivrosModel livro = new LivrosModel();
        livro.setNome(nomeLivro);
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("TESTE-" + sufixo);
        exemplar = exemplarRepository.save(exemplar);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(exemplar, usuario, LocalDate.now());
        emprestimo = emprestimoRepository.save(emprestimo);

        String corpo = mockMvc.perform(get("/emprestimos/ativos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andReturn().getResponse().getContentAsString();

        assertTrue(corpo.contains("\"usuario\":{"));
        assertTrue(corpo.contains("\"nome\":\"" + nomeUsuario + "\""));
        assertTrue(corpo.contains("\"exemplar\":{"));
        assertTrue(corpo.contains("\"livro\":{"));
        assertTrue(corpo.contains("\"nome\":\"" + nomeLivro + "\""));
        assertTrue(corpo.contains("\"codigoPatrimonio\":\"TESTE-" + sufixo + "\""));
        assertTrue(corpo.contains("\"id\":" + emprestimo.getId()));
    }
}
