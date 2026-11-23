package com.example.cadastro.dto;

import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.SituacaoEmprestimo;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmprestimoResposta(
        Long id,
        ExemplarResposta exemplar,
        UsuarioResposta usuario,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate dataEmprestimo,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate dataPrevistaDevolucao,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate dataDevolucao,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate dataEncerramento,
        SituacaoEmprestimo situacao,
        long diasDeAtraso,
        BigDecimal multa
) {
    public static EmprestimoResposta de(Emprestimo emprestimo) {
        var exemplar = emprestimo.getExemplar();
        var livro = exemplar.getLivro();
        var usuario = emprestimo.getUsuario();

        return new EmprestimoResposta(
                emprestimo.getId(),
                new ExemplarResposta(
                        exemplar.getId(),
                        new LivroResposta(livro.getId(), livro.getNome(), livro.isAtivo()),
                        exemplar.getCodigoPatrimonio()
                ),
                new UsuarioResposta(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.isAtivo()
                ),
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataPrevistaDevolucao(),
                emprestimo.getDataDevolucao(),
                emprestimo.getDataEncerramento(),
                emprestimo.getSituacao(),
                emprestimo.getDiasDeAtraso(),
                emprestimo.getMulta()
        );
    }

    public record ExemplarResposta(Long id, LivroResposta livro, String codigoPatrimonio) {
    }

    public record LivroResposta(Long id, String nome, boolean ativo) {
    }

    public record UsuarioResposta(Long id, String nome, String email, boolean ativo) {
    }
}
