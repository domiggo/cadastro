package com.example.cadastro.service;

import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.entities.PagamentoMulta;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.PagamentoMultaRepository;
import com.example.cadastro.repository.TotalPagoPorEmprestimo;
import com.example.cadastro.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoMultaServiceTest {

    @Mock
    private PagamentoMultaRepository pagamentoMultaRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PagamentoMultaService pagamentoMultaService;

    private Emprestimo emprestimo;
    private Long emprestimoId;
    private LocalDate hoje;

    @BeforeEach
    void prepararDados() {
        emprestimoId = 1L;
        hoje = LocalDate.now();

        emprestimo = new Emprestimo();
        emprestimo.setDataEmprestimo(hoje.minusDays(20));
        emprestimo.setDataPrevistaDevolucao(hoje.minusDays(13));
    }

    @Test
    void deveRegistrarPagamentoQuandoDadosForemValidos() {
        configurarCenario();
        configurarTotalJaPago(BigDecimal.ZERO);

        when(pagamentoMultaRepository.save(any(PagamentoMulta.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        BigDecimal valor = new BigDecimal("1.00");

        PagamentoMulta resultado = pagamentoMultaService.registrarPagamento(
                emprestimoId, valor, hoje
        );

        assertEquals(valor, resultado.getValor());
        assertEquals(hoje, resultado.getDataPagamento());
        assertSame(emprestimo, resultado.getEmprestimo());

        verify(pagamentoMultaRepository).save(any(PagamentoMulta.class));
    }

    @Test
    void deveFalharQuandoEmprestimoNaoExistir() {
        when(emprestimoRepository.buscarPorIdComBloqueio(emprestimoId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                pagamentoMultaService.registrarPagamento(
                        emprestimoId, new BigDecimal("1.00"), hoje
                )
        );

        verificarQueNaoSalvou();
    }

    @Test
    void deveFalharQuandoDataForNula() {
        configurarCenario();

        assertThrows(IllegalArgumentException.class, () ->
                pagamentoMultaService.registrarPagamento(
                        emprestimoId, new BigDecimal("1.00"), null
                )
        );

        verificarQueNaoSalvou();
    }

    @Test
    void deveFalharQuandoDataForAnteriorAoEmprestimo() {
        configurarCenario();

        LocalDate dataAnterior = emprestimo.getDataEmprestimo().minusDays(1);

        assertThrows(IllegalArgumentException.class, () ->
                pagamentoMultaService.registrarPagamento(
                        emprestimoId, new BigDecimal("1.00"), dataAnterior
                )
        );

        verificarQueNaoSalvou();
    }

    @Test
    void deveFalharQuandoDataForFutura() {
        configurarCenario();

        assertThrows(IllegalArgumentException.class, () ->
                pagamentoMultaService.registrarPagamento(
                        emprestimoId, new BigDecimal("1.00"), hoje.plusDays(1)
                )
        );

        verificarQueNaoSalvou();
    }

    @Test
    void deveFalharQuandoValorForNulo() {
        configurarCenario();

        assertThrows(IllegalArgumentException.class, () ->
                pagamentoMultaService.registrarPagamento(
                        emprestimoId, null, hoje
                )
        );

        verificarQueNaoSalvou();
    }

    @Test
    void deveFalharQuandoValorForZero() {
        configurarCenario();

        assertThrows(IllegalArgumentException.class, () ->
                pagamentoMultaService.registrarPagamento(
                        emprestimoId, BigDecimal.ZERO, hoje
                )
        );

        verificarQueNaoSalvou();
    }

    @Test
    void deveFalharQuandoValorForNegativo() {
        configurarCenario();

        assertThrows(IllegalArgumentException.class, () ->
                pagamentoMultaService.registrarPagamento(
                        emprestimoId, new BigDecimal("-1.00"), hoje
                )
        );

        verificarQueNaoSalvou();
    }

    @Test
    void deveFalharQuandoValorTiverMaisDeDuasCasasDecimais() {
        configurarCenario();

        assertThrows(IllegalArgumentException.class, () ->
                pagamentoMultaService.registrarPagamento(
                        emprestimoId, new BigDecimal("1.005"), hoje
                )
        );

        verificarQueNaoSalvou();
    }

    @Test
    void deveFalharQuandoNaoHouverMultaPendente() {
        configurarCenario();
        configurarTotalJaPago(emprestimo.getMulta());

        assertThrows(IllegalStateException.class, () ->
                pagamentoMultaService.registrarPagamento(
                        emprestimoId, new BigDecimal("1.00"), hoje
                )
        );

        verificarQueNaoSalvou();
    }

    @Test
    void deveFalharQuandoValorForMaiorQueSaldo() {
        BigDecimal saldoRestante = new BigDecimal("1.00");
        BigDecimal totalJaPago = emprestimo.getMulta().subtract(saldoRestante);
        configurarCenario();
        configurarTotalJaPago(totalJaPago);

        assertThrows(IllegalArgumentException.class, () ->
                pagamentoMultaService.registrarPagamento(
                        emprestimoId, new BigDecimal("1.01"), hoje
                )
        );

        verificarQueNaoSalvou();
    }

    private void configurarCenario() {
        when(emprestimoRepository.buscarPorIdComBloqueio(emprestimoId))
                .thenReturn(Optional.of(emprestimo));
    }

    private void configurarTotalJaPago(BigDecimal totalJaPago) {
        when(pagamentoMultaRepository.somarPorEmprestimoId(emprestimoId))
                .thenReturn(totalJaPago);
    }

    private void verificarQueNaoSalvou() {
        verify(pagamentoMultaRepository, never())
                .save(any(PagamentoMulta.class));
    }

    @Test
    void deveRetornarSaldoDaMulta() {
        when(emprestimoRepository.findById(emprestimoId))
                .thenReturn(Optional.of(emprestimo));
        when(pagamentoMultaRepository.somarPorEmprestimoId(emprestimoId))
                .thenReturn(new BigDecimal("5.00"));

        BigDecimal multaTotal = emprestimo.getMulta();
        BigDecimal saldoEsperado = multaTotal.subtract(new BigDecimal("5.00"));

        BigDecimal saldoRetornado =
                pagamentoMultaService.saldoDaMulta(emprestimoId);

        assertEquals(saldoEsperado, saldoRetornado);
    }

    @Test
    void deveFalharAoConsultarSaldoDeEmprestimoInexistente() {
        when(emprestimoRepository.findById(emprestimoId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                pagamentoMultaService.saldoDaMulta(emprestimoId)
        );

        verify(pagamentoMultaRepository, never())
                .somarPorEmprestimoId(emprestimoId);
    }

    @Test
    void deveListarPagamentosDoEmprestimo() {
        when(emprestimoRepository.existsById(emprestimoId))
                .thenReturn(true);

        List<PagamentoMulta> pagamentos = List.of(
                new PagamentoMulta(),
                new PagamentoMulta()
        );

        when(pagamentoMultaRepository
                .findByEmprestimoIdOrderByDataPagamentoAsc(emprestimoId))
                .thenReturn(pagamentos);

        List<PagamentoMulta> resultado =
                pagamentoMultaService.listarPagamentosPorEmprestimo(emprestimoId);

        assertSame(pagamentos, resultado);
        verify(pagamentoMultaRepository)
                .findByEmprestimoIdOrderByDataPagamentoAsc(emprestimoId);
    }

    @Test
    void deveFalharAoListarPagamentosDeEmprestimoInexistente() {
        when(emprestimoRepository.existsById(emprestimoId))
                .thenReturn(false);

        assertThrows(EntityNotFoundException.class, () ->
                pagamentoMultaService.listarPagamentosPorEmprestimo(emprestimoId)
        );

        verify(pagamentoMultaRepository, never())
                .findByEmprestimoIdOrderByDataPagamentoAsc(emprestimoId);
    }

    @Test
    void deveSomarSaldosDeMultaDosEmprestimosDoUsuario() {
        Long usuarioId = 1L;

        Emprestimo primeiro = new Emprestimo();
        primeiro.setId(11L);
        primeiro.setDataPrevistaDevolucao(hoje.minusDays(2));
        primeiro.setExemplar(criarExemplar("Livro A", "PAT-11"));

        Emprestimo segundo = new Emprestimo();
        segundo.setId(12L);
        segundo.setDataPrevistaDevolucao(hoje.minusDays(5));
        segundo.setExemplar(criarExemplar("Livro B", "PAT-12"));

        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(emprestimoRepository.buscarComDadosParaMultasPorUsuario(usuarioId))
                .thenReturn(List.of(primeiro, segundo));
        TotalPagoPorEmprestimo totalPagoPrimeiro = mock(TotalPagoPorEmprestimo.class);
        when(totalPagoPrimeiro.getEmprestimoId()).thenReturn(11L);
        when(totalPagoPrimeiro.getTotalPago()).thenReturn(new BigDecimal("1.00"));
        when(pagamentoMultaRepository.somarPorUsuarioAgrupadoPorEmprestimo(usuarioId))
                .thenReturn(List.of(totalPagoPrimeiro));

        BigDecimal saldoEsperado =
                primeiro.getMulta().subtract(new BigDecimal("1.00"))
                        .add(segundo.getMulta());

        BigDecimal resultado =
                pagamentoMultaService.calcularSaldoMultasPendentesPorUsuario(usuarioId);

        assertEquals(saldoEsperado, resultado);
        verify(pagamentoMultaRepository)
                .somarPorUsuarioAgrupadoPorEmprestimo(usuarioId);
        verify(pagamentoMultaRepository, never()).somarPorEmprestimoId(anyLong());
    }

    @Test
    void deveRetornarSaldoPorEmprestimoEInformacoesDoExemplar() {
        Long usuarioId = 1L;
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(11L);
        emprestimo.setDataPrevistaDevolucao(hoje.minusDays(2));
        emprestimo.setExemplar(criarExemplar("Livro A", "PAT-11"));

        TotalPagoPorEmprestimo totalPago = mock(TotalPagoPorEmprestimo.class);
        when(totalPago.getEmprestimoId()).thenReturn(11L);
        when(totalPago.getTotalPago()).thenReturn(new BigDecimal("1.00"));
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(emprestimoRepository.buscarComDadosParaMultasPorUsuario(usuarioId))
                .thenReturn(List.of(emprestimo));
        when(pagamentoMultaRepository.somarPorUsuarioAgrupadoPorEmprestimo(usuarioId))
                .thenReturn(List.of(totalPago));

        var resumo =
                pagamentoMultaService.consultarResumoMultasPendentesPorUsuario(usuarioId);

        assertEquals(new BigDecimal("3.00"), resumo.saldoTotal());
        assertEquals(
                new BigDecimal("3.00"),
                resumo.saldosPorEmprestimo().getFirst().saldo()
        );
        assertEquals("Livro A", resumo.saldosPorEmprestimo().getFirst().livroNome());
        assertEquals("PAT-11", resumo.saldosPorEmprestimo().getFirst().codigoPatrimonio());
        verify(pagamentoMultaRepository, times(1))
                .somarPorUsuarioAgrupadoPorEmprestimo(usuarioId);
        verify(pagamentoMultaRepository, never()).somarPorEmprestimoId(anyLong());
    }

    private Exemplar criarExemplar(String nomeLivro, String patrimonio) {
        LivrosModel livro = new LivrosModel();
        livro.setNome(nomeLivro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio(patrimonio);
        return exemplar;
    }

    @Test
    void deveFalharAoCalcularSaldoDeUsuarioInexistente() {
        Long usuarioId = 1L;

        when(usuarioRepository.existsById(usuarioId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () ->
                pagamentoMultaService.calcularSaldoMultasPendentesPorUsuario(usuarioId)
        );

        verify(emprestimoRepository, never())
                .buscarComDadosParaMultasPorUsuario(usuarioId);
        verifyNoInteractions(pagamentoMultaRepository);
    }

    @Test
    void deveRetornarResumoVazioQuandoUsuarioNaoPossuirEmprestimos() {
        Long usuarioId = 1L;
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(emprestimoRepository.buscarComDadosParaMultasPorUsuario(usuarioId))
                .thenReturn(List.of());

        var resumo =
                pagamentoMultaService.consultarResumoMultasPendentesPorUsuario(usuarioId);

        assertEquals(BigDecimal.ZERO, resumo.saldoTotal());
        assertTrue(resumo.saldosPorEmprestimo().isEmpty());
        verifyNoInteractions(pagamentoMultaRepository);
    }

    @Test
    void deveListarPagamentosDoUsuario() {
        Long usuarioId = 1L;

        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);

        List<PagamentoMulta> pagamentos = List.of(
                new PagamentoMulta(),
                new PagamentoMulta()
        );

        when(pagamentoMultaRepository
                .findByEmprestimoUsuarioIdOrderByDataPagamentoAsc(usuarioId))
                .thenReturn(pagamentos);

        List<PagamentoMulta> resultado =
                pagamentoMultaService.listarPagamentosPorUsuario(usuarioId);

        assertSame(pagamentos, resultado);
        verify(pagamentoMultaRepository)
                .findByEmprestimoUsuarioIdOrderByDataPagamentoAsc(usuarioId);
    }

    @Test
    void deveFalharAoListarPagamentosDeUsuarioInexistente() {
        Long usuarioId = 1L;

        when(usuarioRepository.existsById(usuarioId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () ->
                pagamentoMultaService.listarPagamentosPorUsuario(usuarioId)
        );

        verify(pagamentoMultaRepository, never())
                .findByEmprestimoUsuarioIdOrderByDataPagamentoAsc(usuarioId);
    }
}
