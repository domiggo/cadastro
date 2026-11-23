package com.example.cadastro.service;

import com.example.cadastro.dto.ResumoMultasUsuario;
import com.example.cadastro.dto.PagamentoMultaResumo;
import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.PagamentoMulta;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.PagamentoMultaRepository;
import com.example.cadastro.repository.TotalPagoPorEmprestimo;
import com.example.cadastro.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class PagamentoMultaService {

    private final PagamentoMultaRepository pagamentoMultaRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<PagamentoMulta> listarPagamentosPorEmprestimo(Long emprestimoId) {
        if (!emprestimoRepository.existsById(emprestimoId)) {
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
        Emprestimo emprestimo = emprestimoRepository
                .buscarPorIdComBloqueio((emprestimoId))
                .orElseThrow(() ->
                        new EntityNotFoundException("Empréstimo não encontrado"));


        if (dataDePagamento == null) {
            throw new IllegalArgumentException("É obrigatório possuir uma data de pagamento.");
        }

        if (dataDePagamento.isBefore(emprestimo.getDataEmprestimo())) {
            throw new IllegalArgumentException("A data de pagamento não pode ser anterior ao empréstimo");
        }

        if (dataDePagamento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de pagamento não pode ser futura.");
        }

        if (valorPago == null) {
            throw new IllegalArgumentException("É necessário um valor inicial para realizar o pagamento.");
        }

        if (valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor inválido para o pagamento.");

        }

        if (valorPago.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("O valor do pagamento deve ter no máximo duas casas decimais.");
        }

        BigDecimal totalPago = pagamentoMultaRepository.somarPorEmprestimoId(emprestimoId);
        BigDecimal saldoAtual = emprestimo.getMulta().subtract(totalPago);

        if (saldoAtual.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Não há multa pendente para este empréstimo.");
        }

        if (valorPago.compareTo(saldoAtual) > 0) {
            throw new IllegalArgumentException("Valor desejado é maior que o valor da multa.");
        }

        PagamentoMulta pagamentoMulta = new PagamentoMulta();

        pagamentoMulta.setEmprestimo(emprestimo);
        pagamentoMulta.setValor(valorPago);
        pagamentoMulta.setDataPagamento(dataDePagamento);

        return pagamentoMultaRepository.save(pagamentoMulta);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoMultasPendentesPorUsuario(Long usuarioId) {
        return consultarResumoMultasPendentesPorUsuario(usuarioId).saldoTotal();
    }

    @Transactional(readOnly = true)
    public ResumoMultasUsuario consultarResumoMultasPendentesPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new EntityNotFoundException("Usuário não encontrado.");
        }

        List<Emprestimo> emprestimos =
                emprestimoRepository.buscarComDadosParaMultasPorUsuario(usuarioId);
        if (emprestimos.isEmpty()) {
            return new ResumoMultasUsuario(BigDecimal.ZERO, List.of());
        }

        Map<Long, BigDecimal> totaisPagos = pagamentoMultaRepository
                .somarPorUsuarioAgrupadoPorEmprestimo(usuarioId)
                .stream()
                .collect(Collectors.toMap(
                        TotalPagoPorEmprestimo::getEmprestimoId,
                        TotalPagoPorEmprestimo::getTotalPago
                ));

        List<ResumoMultasUsuario.SaldoMultaEmprestimo> saldos = emprestimos.stream()
                .map(emprestimo -> new ResumoMultasUsuario.SaldoMultaEmprestimo(
                        emprestimo.getId(),
                        emprestimo.getExemplar().getLivro().getNome(),
                        emprestimo.getExemplar().getCodigoPatrimonio(),
                        emprestimo.getMulta().subtract(
                                totaisPagos.getOrDefault(emprestimo.getId(), BigDecimal.ZERO)
                        )
                ))
                .toList();
        BigDecimal saldoTotal = saldos.stream()
                .map(ResumoMultasUsuario.SaldoMultaEmprestimo::saldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumoMultasUsuario(saldoTotal, saldos);
    }

    @Transactional(readOnly = true)
    public List<PagamentoMulta> listarPagamentosPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new EntityNotFoundException("Usuário não encontrado.");
        }

        return pagamentoMultaRepository.findByEmprestimoUsuarioIdOrderByDataPagamentoAsc(usuarioId);
    }

    @Transactional(readOnly = true)
    public Page<PagamentoMultaResumo> listarResumoPagamentosPorUsuario(
            Long usuarioId,
            Pageable pageable
    ) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new EntityNotFoundException("Usuário não encontrado.");
        }

        return pagamentoMultaRepository.buscarResumoPorUsuario(usuarioId, pageable);
    }
}
