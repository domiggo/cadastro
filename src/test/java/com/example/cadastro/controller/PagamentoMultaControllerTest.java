package com.example.cadastro.controller;

import com.example.cadastro.entities.PagamentoMulta;
import com.example.cadastro.service.PagamentoMultaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(PagamentoMultaController.class)
class PagamentoMultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagamentoMultaService pagamentoMultaService;

    @Test
    void deveConsultarSaldoDaMulta() throws Exception {
        when(pagamentoMultaService.saldoDaMulta(1L))
                .thenReturn(new BigDecimal("3.00"));

        mockMvc.perform(get("/emprestimos/1/pagamentos/saldo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3.00));

        verify(pagamentoMultaService).saldoDaMulta(1L);
    }
    @Test
    void deveListarPagamentosDoEmprestimo() throws Exception {
        when(pagamentoMultaService.listarPagamentosPorEmprestimo(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/emprestimos/1/pagamentos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(pagamentoMultaService)
                .listarPagamentosPorEmprestimo(1L);
    }

    @Test
    void deveRegistrarPagamento() throws Exception {
        BigDecimal valor = new BigDecimal("1.00");
        LocalDate data = LocalDate.of(2026, 11, 3);

        PagamentoMulta pagamento = new PagamentoMulta();
        pagamento.setValor(valor);
        pagamento.setDataPagamento(data);

        when(pagamentoMultaService.registrarPagamento(1L, valor, data))
                .thenReturn(pagamento);

        mockMvc.perform(post("/emprestimos/1/pagamentos")
                        .param("valorPago", "1.00")
                        .param("dataDePagamento", "2026-11-03"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(1.00))
                .andExpect(jsonPath("$.dataPagamento").value("2026-11-03"));

        verify(pagamentoMultaService)
                .registrarPagamento(1L, valor, data);
    }

    @Test
    void deveRetornar400QuandoDataDePagamentoNaoForInformada() throws Exception {
        mockMvc.perform(post("/emprestimos/1/pagamentos")
                        .param("valorPago", "1.00"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pagamentoMultaService);
    }

    @Test
    void deveRetornar400QuandoFormatoDaDataForInvalido() throws Exception {
        mockMvc.perform(post("/emprestimos/1/pagamentos")
                        .param("valorPago", "1.00")
                        .param("dataDePagamento", "03/11/2026"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pagamentoMultaService);
    }

    @Test
    void deveRetornar400QuandoValorPagoNaoForInformado() throws Exception {
        mockMvc.perform(post("/emprestimos/1/pagamentos")
                        .param("dataDePagamento", "2026-11-03"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pagamentoMultaService);
    }

    @Test
    void deveRetornar400QuandoValorPagoNaoForNumerico() throws Exception {
        mockMvc.perform(post("/emprestimos/1/pagamentos")
                        .param("valorPago", "um")
                        .param("dataDePagamento", "2026-11-03"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pagamentoMultaService);
    }

    @Test
    void deveRetornarPagamentoNoCorpoDaResposta() throws Exception {
        PagamentoMulta pagamento = new PagamentoMulta();
        pagamento.setId(1L);
        pagamento.setValor(new BigDecimal("1.00"));
        pagamento.setDataPagamento(LocalDate.of(2026, 11, 3));

        when(pagamentoMultaService.listarPagamentosPorEmprestimo(1L))
                .thenReturn(List.of(pagamento));

        mockMvc.perform(get("/emprestimos/1/pagamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].valor").value(1.00))
                .andExpect(jsonPath("$[0].dataPagamento").value("2026-11-03"));

        verify(pagamentoMultaService)
                .listarPagamentosPorEmprestimo(1L);
    }
}