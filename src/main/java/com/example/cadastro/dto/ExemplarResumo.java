package com.example.cadastro.dto;

public record ExemplarResumo(
        Long id,
        Long livroId,
        String livroNome,
        String codigoPatrimonio,
        boolean disponivel
) {
}
