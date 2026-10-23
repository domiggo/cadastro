package com.example.cadastro.service;

import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.PagamentoMulta;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.PagamentoMultaRepository;
import com.example.cadastro.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@AllArgsConstructor
public class PagamentoMultaService {

    private final PagamentoMultaRepository pagamentoMultaRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<PagamentoMulta> listarPagamentosPorEmprestimo(Long emprestimoId) {
        if(!emprestimoRepository.existsById(emprestimoId)) {
            throw new EntityNotFoundException("Empréstimo não encontrado");
        }

        return pagamentoMultaRepository.findByEmprestimoIdOrderByDataPagamentoAsc(emprestimoId);
    }

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
            throw new IllegalArgumentException("É obrigatório possuir uma data de pagamento");
        }

        pagamentoMulta.setEmprestimo(emprestimo);
        pagamentoMulta.setValor(valorPago);
        pagamentoMulta.setDataPagamento(dataDePagamento);

        return pagamentoMultaRepository.save(pagamentoMulta);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoMultasPendentesPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new EntityNotFoundException("Usuário não encontrado");
        }

        List<Emprestimo> emprestimos = emprestimoRepository.findByUsuarioId(usuarioId);
        BigDecimal saldoMultas = BigDecimal.ZERO;

        for (Emprestimo emprestimo : emprestimos) {
           BigDecimal valorPagoNoEmprestimo =
                   pagamentoMultaRepository.somarPorEmprestimoId(emprestimo.getId());

           BigDecimal saldoEmprestimo =
           emprestimo.getMulta().subtract(valorPagoNoEmprestimo);

           saldoMultas = saldoMultas.add(saldoEmprestimo);
        }

        return saldoMultas;
    }

    @Transactional(readOnly = true)
    public List<PagamentoMulta> listarPagamentosPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new EntityNotFoundException("Usuário não encontrado");
        }

        return pagamentoMultaRepository.findByEmprestimoUsuarioIdOrderByDataPagamentoAsc(usuarioId);
    }
}