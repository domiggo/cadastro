package com.example.cadastro.controller;

import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.entities.PagamentoMulta;
import com.example.cadastro.entities.Usuario;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.ExemplarRepository;
import com.example.cadastro.repository.LivrosRepository;
import com.example.cadastro.repository.PagamentoMultaRepository;
import com.example.cadastro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PagamentoMultaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LivrosRepository livrosRepository;

    @Autowired
    private ExemplarRepository exemplarRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private PagamentoMultaRepository pagamentoMultaRepository;

    @Test
    void deveSerializarPagamentoERelacionamentosComOpenSessionInViewDesativado()
            throws Exception {
        String sufixo = Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );
        String nomeUsuario = "Ana Silva " + sufixo;

        Usuario usuario = new Usuario();
        usuario.setNome(nomeUsuario);
        usuario.setEmail("ana-" + sufixo + "@example.com");
        usuario.setCpf(sufixo);
        usuario = usuarioRepository.save(usuario);

        LivrosModel livro = new LivrosModel();
        livro.setNome("O Hobbit " + sufixo);
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("TESTE-" + sufixo);
        exemplar = exemplarRepository.save(exemplar);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(exemplar, usuario, LocalDate.now().minusDays(20));
        emprestimo = emprestimoRepository.save(emprestimo);

        PagamentoMulta pagamento = new PagamentoMulta();
        pagamento.setEmprestimo(emprestimo);
        pagamento.setValor(new BigDecimal("5.00"));
        pagamento.setDataPagamento(LocalDate.now());
        pagamento = pagamentoMultaRepository.save(pagamento);

        mockMvc.perform(get("/emprestimos/{emprestimoId}/pagamentos", emprestimo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pagamento.getId()))
                .andExpect(jsonPath("$[0].valor").value(5.00))
                .andExpect(jsonPath("$[0].dataPagamento").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$[0].emprestimo.id").value(emprestimo.getId()))
                .andExpect(jsonPath("$[0].emprestimo.usuario.nome").value(nomeUsuario));
    }

    @Test
    void deveConsultarResumoDeMultasComSaldosAgrupadosPorEmprestimo() throws Exception {
        String sufixo = Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );
        Usuario usuario = new Usuario();
        usuario.setNome("Bruno " + sufixo);
        usuario.setEmail("bruno-" + sufixo + "@example.com");
        usuario.setCpf(sufixo);
        usuario = usuarioRepository.save(usuario);

        Emprestimo primeiro = criarEmprestimo(usuario, sufixo, "A", 20);
        Emprestimo segundo = criarEmprestimo(usuario, sufixo, "B", 15);
        registrarPagamento(primeiro, "5.00");
        registrarPagamento(segundo, "3.00");

        mockMvc.perform(get("/usuarios/{usuarioId}/multas/resumo", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.saldoTotal").value(34.00))
                .andExpect(jsonPath(
                        "$.saldosPorEmprestimo[?(@.emprestimoId == " +
                                primeiro.getId() + ")].saldo"
                ).value(21.00))
                .andExpect(jsonPath(
                        "$.saldosPorEmprestimo[?(@.emprestimoId == " +
                                segundo.getId() + ")].saldo"
                ).value(13.00))
                .andExpect(jsonPath(
                        "$.saldosPorEmprestimo[?(@.emprestimoId == " +
                                primeiro.getId() + ")].livroNome"
                ).value("Livro A " + sufixo))
                .andExpect(jsonPath(
                        "$.saldosPorEmprestimo[?(@.emprestimoId == " +
                                segundo.getId() + ")].codigoPatrimonio"
                ).value("RESUMO-B-" + sufixo));
    }

    @Test
    void devePaginarHistoricoDePagamentosPorUsuarioEmOrdemCronologica() throws Exception {
        String sufixo = Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );
        Usuario usuario = new Usuario();
        usuario.setNome("Carla " + sufixo);
        usuario.setEmail("carla-" + sufixo + "@example.com");
        usuario.setCpf(sufixo);
        usuario = usuarioRepository.save(usuario);

        Emprestimo emprestimo = criarEmprestimo(usuario, sufixo, "PAGINADO", 30);
        registrarPagamento(emprestimo, "1.00", LocalDate.now().minusDays(3));
        registrarPagamento(emprestimo, "2.00", LocalDate.now().minusDays(2));
        registrarPagamento(emprestimo, "3.00", LocalDate.now().minusDays(1));

        mockMvc.perform(get(
                        "/usuarios/{usuarioId}/pagamentos/historico",
                        usuario.getId()
                )
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].valor").value(1.00))
                .andExpect(jsonPath("$.content[0].dataPagamento")
                        .value(LocalDate.now().minusDays(3).toString()))
                .andExpect(jsonPath("$.content[0].emprestimoId")
                        .value(emprestimo.getId()))
                .andExpect(jsonPath("$.content[1].valor").value(2.00))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get(
                        "/usuarios/{usuarioId}/pagamentos/historico",
                        usuario.getId()
                )
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].valor").value(3.00))
                .andExpect(jsonPath("$.last").value(true));
    }

    private Emprestimo criarEmprestimo(
            Usuario usuario,
            String sufixo,
            String identificador,
            int diasDesdeEmprestimo
    ) {
        LivrosModel livro = new LivrosModel();
        livro.setNome("Livro " + identificador + " " + sufixo);
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("RESUMO-" + identificador + "-" + sufixo);
        exemplar = exemplarRepository.save(exemplar);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(
                exemplar,
                usuario,
                LocalDate.now().minusDays(diasDesdeEmprestimo)
        );
        return emprestimoRepository.save(emprestimo);
    }

    private void registrarPagamento(Emprestimo emprestimo, String valor) {
        registrarPagamento(emprestimo, valor, LocalDate.now());
    }

    private void registrarPagamento(
            Emprestimo emprestimo,
            String valor,
            LocalDate dataPagamento
    ) {
        PagamentoMulta pagamento = new PagamentoMulta();
        pagamento.setEmprestimo(emprestimo);
        pagamento.setValor(new BigDecimal(valor));
        pagamento.setDataPagamento(dataPagamento);
        pagamentoMultaRepository.save(pagamento);
    }
}
