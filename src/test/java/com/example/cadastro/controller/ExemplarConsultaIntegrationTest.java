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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExemplarConsultaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LivrosRepository livrosRepository;

    @Autowired
    private ExemplarRepository exemplarRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveListarEmLoteSomenteExemplaresDisponiveisDeLivrosAtivos() throws Exception {
        String sufixo = novoSufixo();
        Usuario usuario = criarUsuario(sufixo);
        LivrosModel livroAtivo = criarLivro("Livro ativo " + sufixo, true);
        Exemplar disponivel = criarExemplar(livroAtivo, "DISPONIVEL-" + sufixo, true);
        Exemplar emprestado = criarExemplar(livroAtivo, "EMPRESTADO-" + sufixo, true);
        emprestimoRepository.save(criarEmprestimo(emprestado, usuario));

        LivrosModel livroInativo = criarLivro("Livro inativo " + sufixo, false);
        criarExemplar(livroInativo, "LIVRO-INATIVO-" + sufixo, true);
        criarExemplar(livroAtivo, "EXEMPLAR-INATIVO-" + sufixo, false);

        mockMvc.perform(get("/livros/exemplares/disponiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + disponivel.getId() + ")]").exists())
                .andExpect(jsonPath("$[?(@.id == " + emprestado.getId() + ")]").doesNotExist())
                .andExpect(jsonPath(
                        "$[?(@.codigoPatrimonio == 'LIVRO-INATIVO-" + sufixo + "')]"
                ).doesNotExist())
                .andExpect(jsonPath(
                        "$[?(@.codigoPatrimonio == 'EXEMPLAR-INATIVO-" + sufixo + "')]"
                ).doesNotExist())
                .andExpect(jsonPath(
                        "$[?(@.id == " + disponivel.getId() + ")].livroNome"
                ).value("Livro ativo " + sufixo));
    }

    @Test
    void deveRetornarDisponibilidadeDosExemplaresAtivosEmUmaConsulta() throws Exception {
        String sufixo = novoSufixo();
        Usuario usuario = criarUsuario(sufixo);
        LivrosModel livro = criarLivro("Livro resumo " + sufixo, true);
        Exemplar disponivel = criarExemplar(livro, "RESUMO-DISPONIVEL-" + sufixo, true);
        Exemplar emprestado = criarExemplar(livro, "RESUMO-EMPRESTADO-" + sufixo, true);
        emprestimoRepository.save(criarEmprestimo(emprestado, usuario));
        criarExemplar(livro, "RESUMO-INATIVO-" + sufixo, false);

        mockMvc.perform(get("/livros/{livroId}/exemplares/resumo", livro.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + disponivel.getId() + ")].disponivel")
                        .value(true))
                .andExpect(jsonPath("$[?(@.id == " + emprestado.getId() + ")].disponivel")
                        .value(false))
                .andExpect(jsonPath(
                        "$[?(@.codigoPatrimonio == 'RESUMO-INATIVO-" + sufixo + "')]"
                ).doesNotExist());
    }

    private String novoSufixo() {
        return Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );
    }

    private Usuario criarUsuario(String sufixo) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário " + sufixo);
        usuario.setEmail("usuario-" + sufixo + "@example.com");
        usuario.setCpf(sufixo);
        return usuarioRepository.save(usuario);
    }

    private LivrosModel criarLivro(String nome, boolean ativo) {
        LivrosModel livro = new LivrosModel();
        livro.setNome(nome);
        livro.setAtivo(ativo);
        return livrosRepository.save(livro);
    }

    private Exemplar criarExemplar(
            LivrosModel livro,
            String codigoPatrimonio,
            boolean ativo
    ) {
        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio(codigoPatrimonio);
        exemplar.setAtivo(ativo);
        return exemplarRepository.save(exemplar);
    }

    private Emprestimo criarEmprestimo(Exemplar exemplar, Usuario usuario) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(exemplar, usuario, LocalDate.now());
        return emprestimo;
    }
}
