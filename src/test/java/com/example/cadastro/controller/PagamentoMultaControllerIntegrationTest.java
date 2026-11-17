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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
}
