CREATE INDEX idx_tb_emprestimos_situacao_data_emprestimo_id
    ON tb_emprestimos (situacao, data_emprestimo DESC, id DESC);
