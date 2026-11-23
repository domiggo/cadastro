package com.example.cadastro.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EmprestimoIndexIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    private void assertPlanoUsaIndice(String explainQuery, String indexName) {
        String plano = jdbcTemplate.queryForObject(explainQuery, String.class);
        assertTrue(plano.contains(indexName), () -> "Índice ausente do plano: " + plano);
    }
}
