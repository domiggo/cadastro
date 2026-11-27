ALTER TABLE tb_emprestimos
    ADD COLUMN exemplar_id_ativo BIGINT;

UPDATE tb_emprestimos
SET exemplar_id_ativo = exemplar_id
WHERE situacao = 'ATIVO';

ALTER TABLE tb_emprestimos
    ADD CONSTRAINT uk_tb_emprestimos_exemplar_ativo
    UNIQUE (exemplar_id_ativo);
