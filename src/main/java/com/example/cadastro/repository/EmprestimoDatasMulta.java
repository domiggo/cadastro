package com.example.cadastro.repository;

import java.time.LocalDate;

public interface EmprestimoDatasMulta {
    LocalDate getDataPrevistaDevolucao();

    LocalDate getDataDevolucao();

    LocalDate getDataEncerramento();
}
