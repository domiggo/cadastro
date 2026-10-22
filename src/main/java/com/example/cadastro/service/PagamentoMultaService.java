package com.example.cadastro.service;

import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.PagamentoMulta;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.PagamentoMultaRepository;
import com.example.cadastro.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@AllArgsConstructor
public class PagamentoMultaService {

    private final PagamentoMultaRepository pagamentoMultaRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public BigDecimal saldoDaMulta(Long emprestimoId) {
         Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                 .orElseThrow(() -> new EntityNotFoundException("Empréstimo não encontrado"));

         BigDecimal multaTotal = emprestimo.getMulta();
         BigDecimal valorPago = pagamentoMultaRepository.somarPorEmprestimoId(emprestimoId);

         return multaTotal.subtract(valorPago);
    }

    @Transactional
    public PagamentoMulta registrarPagamento(Long emprestimoId, BigDecimal valorPago, LocalDate dataDePagamento) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new EntityNotFoundException("Empréstimo não encontrado"));

        if (valorPago == null){
            throw new IllegalArgumentException("É necessário um valor inicial para realizar o pagamento");
        }

        if (valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor inválido para o pagamento");

        }

        BigDecimal saldoAtual = saldoDaMulta(emprestimoId);

        if (valorPago.compareTo(saldoAtual) > 0) {
            throw new IllegalArgumentException("Valor desejado é maior que o valor da multa");
        }

        PagamentoMulta pagamentoMulta = new PagamentoMulta();

        if (dataDePagamento == null) {
            throw new IllegalArgumentException("É obrigatório possuir uma data de efetuamento de pagamento");
        }

        pagamentoMulta.setEmprestimo(emprestimo);
        pagamentoMulta.setValor(valorPago);
        pagamentoMulta.setDataPagamento(dataDePagamento);

        return pagamentoMultaRepository.save(pagamentoMulta);
    }
}