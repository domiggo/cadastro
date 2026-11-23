package com.example.cadastro.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumoMultasUsuario(
        BigDecimal saldoTotal,
        List<SaldoMultaEmprestimo> saldosPorEmprestimo
) {
    public record SaldoMultaEmprestimo(
            Long emprestimoId,
            String livroNome,
            String codigoPatrimonio,
            BigDecimal saldo
    ) {
    }
}
