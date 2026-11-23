package com.example.cadastro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagamentoMultaResumo(
        Long id,
        BigDecimal valor,
        LocalDate dataPagamento,
        Long emprestimoId
) {
}
