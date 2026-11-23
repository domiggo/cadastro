package com.example.cadastro.service;

import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.entities.Usuario;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.ExemplarRepository;
import com.example.cadastro.repository.LivrosRepository;
import com.example.cadastro.repository.PagamentoMultaRepository;
import com.example.cadastro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PagamentoMultaConcorrenciaTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configurarBancoDeTeste(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    private PagamentoMultaService pagamentoMultaService;
    @Autowired
    private PagamentoMultaRepository pagamentoMultaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private LivrosRepository livrosRepository;
    @Autowired
    private ExemplarRepository exemplarRepository;
    @Autowired
    private EmprestimoRepository emprestimoRepository;

    private Emprestimo criarEmprestimoComMultaDe26Reais() {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário do teste");
        usuario.setEmail("teste@mail.com");
        usuario.setCpf("12345678900");
        usuario = usuarioRepository.save(usuario);

        LivrosModel livro = new LivrosModel();
        livro.setNome("Livro do teste");
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("TESTE-" + UUID.randomUUID());
        exemplar = exemplarRepository.save(exemplar);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(exemplar, usuario, LocalDate.now().minusDays(20));

        return emprestimoRepository.save(emprestimo);
    }

    @Test
    void naoDevePermitirPagamentosConcorrentesAcimaDoSaldo() throws Exception {
        Emprestimo emprestimo = criarEmprestimoComMultaDe26Reais();
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Callable<Boolean> tentarPagar = () -> {
                inicio.await();
                try {
                    pagamentoMultaService.registrarPagamento(
                            emprestimo.getId(),
                            new BigDecimal("20.00"),
                            LocalDate.now()
                    );
                    return true;
                } catch (IllegalArgumentException saldoInsuficiente) {
                    return false;
                }
            };

            Future<Boolean> primeiro = executor.submit(tentarPagar);
            Future<Boolean> segundo = executor.submit(tentarPagar);
            inicio.countDown();

            assertEquals(1, (primeiro.get() ? 1 : 0) + (segundo.get() ? 1 : 0));

            BigDecimal totalPago =
                    pagamentoMultaRepository.somarPorEmprestimoId(emprestimo.getId());
            assertTrue(totalPago.compareTo(new BigDecimal("26.00")) <= 0);
        } finally {
            executor.shutdownNow();
        }
    }
}