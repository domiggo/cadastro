package com.example.cadastro.dto;

public record ResumoBiblioteca(
        long usuariosAtivos,
        long livrosAtivos,
        long emprestimosAtivos,
        long emprestimosAtrasados
) {
}
