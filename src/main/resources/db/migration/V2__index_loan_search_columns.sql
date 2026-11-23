CREATE INDEX idx_tb_emprestimos_usuario_situacao_data
    ON tb_emprestimos (usuario_id, situacao, data_prevista_devolucao);

CREATE INDEX idx_tb_emprestimos_situacao_data
    ON tb_emprestimos (situacao, data_prevista_devolucao);

CREATE INDEX idx_tb_emprestimos_exemplar_situacao
    ON tb_emprestimos (exemplar_id, situacao);
