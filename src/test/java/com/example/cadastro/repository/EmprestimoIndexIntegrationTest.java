package com.example.cadastro.repository;

import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.entities.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class EmprestimoIndexIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private ExemplarRepository exemplarRepository;

    @Autowired
    private LivrosRepository livrosRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void planosDeConsultaUsamIndicesDeBuscaDeEmprestimos() {
        assertPlanoUsaIndice(
                """
                        EXPLAIN SELECT id FROM tb_emprestimos
                        WHERE usuario_id = 1 AND situacao = 'ATIVO'
                          AND data_prevista_devolucao < DATE '2026-11-23'
                        """,
                "IDX_TB_EMPRESTIMOS_USUARIO_SITUACAO_DATA"
        );
        assertPlanoUsaIndice(
                """
                        EXPLAIN SELECT id FROM tb_emprestimos
                        WHERE situacao = 'ATIVO'
                          AND data_prevista_devolucao < DATE '2026-11-23'
                        """,
                "IDX_TB_EMPRESTIMOS_SITUACAO_DATA"
        );
        assertPlanoUsaIndice(
                """
                        EXPLAIN SELECT id FROM tb_emprestimos
                        WHERE situacao = 'ATIVO'
                        ORDER BY data_emprestimo DESC, id DESC
                        """,
                "IDX_TB_EMPRESTIMOS_SITUACAO_DATA_EMPRESTIMO_ID"
        );
        assertPlanoUsaIndice(
                """
                        EXPLAIN SELECT id FROM tb_emprestimos
                        WHERE exemplar_id = 1 AND situacao = 'ATIVO'
                        """,
                "IDX_TB_EMPRESTIMOS_EXEMPLAR_SITUACAO"
        );
        assertPlanoUsaIndice(
                """
                        EXPLAIN SELECT pagamento.id FROM tb_pagamentos pagamento
                        JOIN tb_emprestimos emprestimo
                          ON emprestimo.id = pagamento.emprestimo_id
                        WHERE emprestimo.usuario_id = 1
                        ORDER BY pagamento.data_pagamento, pagamento.id
                        """,
                "IDX_TB_PAGAMENTOS_EMPRESTIMO_DATA_ID"
        );
    }

    @Test
    void bancoImpedeSegundoEmprestimoAtivoDoMesmoExemplar() {
        String sufixo = UUID.randomUUID().toString();

        Usuario primeiroUsuario = criarUsuario("h2-primeiro-" + sufixo);
        Usuario segundoUsuario = criarUsuario("h2-segundo-" + sufixo);

        LivrosModel livro = new LivrosModel();
        livro.setNome("Livro para índice ativo " + sufixo);
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("H2-AUTO-" + sufixo);
        exemplar = exemplarRepository.save(exemplar);

        Emprestimo primeiro = new Emprestimo();
        primeiro.iniciar(exemplar, primeiroUsuario, LocalDate.now());
        emprestimoRepository.saveAndFlush(primeiro);

        Emprestimo segundo = new Emprestimo();
        segundo.iniciar(exemplar, segundoUsuario, LocalDate.now());

        assertThrows(
                DataIntegrityViolationException.class,
                () -> emprestimoRepository.saveAndFlush(segundo)
        );
    }

    private Usuario criarUsuario(String identificador) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário " + identificador);
        usuario.setEmail(identificador + "@example.com");
        usuario.setCpf(String.format(
                "%011d",
                java.util.concurrent.ThreadLocalRandom.current().nextLong(100_000_000_000L)
        ));
        return usuarioRepository.save(usuario);
    }

    private void assertPlanoUsaIndice(String explainQuery, String indexName) {
        String plano = jdbcTemplate.queryForObject(explainQuery, String.class);
        assertTrue(plano.contains(indexName), () -> "Índice ausente do plano: " + plano);
    }
}
