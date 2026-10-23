package com.example.cadastro.controller;


import com.example.cadastro.entities.PagamentoMulta;
import com.example.cadastro.service.PagamentoMultaService;
import lombok.AllArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/emprestimos/{emprestimoId}/pagamentos")
public class PagamentoMultaController {

    private final PagamentoMultaService pagamentoMultaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PagamentoMulta registrarPagamento(
            @PathVariable Long emprestimoId,
            @RequestParam BigDecimal valorPago,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataDePagamento
    ) {
        return pagamentoMultaService.registrarPagamento(emprestimoId, valorPago, dataDePagamento);
    }

    @GetMapping("/saldo")
    public BigDecimal consultarSaldoMulta(@PathVariable Long emprestimoId) {
        return pagamentoMultaService.saldoDaMulta(emprestimoId);
    }

    @GetMapping
    public List<PagamentoMulta> listarPagamentosPorEmprestimo(
            @PathVariable Long emprestimoId
    ) {
        return pagamentoMultaService.listarPagamentosPorEmprestimo(emprestimoId );
    }


}
