package com.example.cadastro.repository;

import java.math.BigDecimal;

public interface TotalPagoPorEmprestimo {
    Long getEmprestimoId();

    BigDecimal getTotalPago();
}
