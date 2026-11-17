package com.example.cadastro.service;

import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.entities.Usuario;
import com.example.cadastro.entities.SituacaoEmprestimo;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.ExemplarRepository;
import com.example.cadastro.repository.LivrosRepository;
import com.example.cadastro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class EmprestimoConcorrenciaTest {

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
    private EmprestimoService emprestimoService;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private ExemplarRepository exemplarRepository;

    @Autowired
    private LivrosRepository livrosRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void devePermitirApenasUmEmprestimoConcorrenteDoMesmoExemplar() throws Exception {
        String sufixo = UUID.randomUUID().toString();
        Usuario primeiroUsuario = criarUsuario("primeiro-" + sufixo);
        Usuario segundoUsuario = criarUsuario("segundo-" + sufixo);

        LivrosModel livro = new LivrosModel();
        livro.setNome("Livro de teste " + sufixo);
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("TESTE-" + sufixo);
        exemplar = exemplarRepository.save(exemplar);

        Long exemplarId = exemplar.getId();
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> primeiro = executor.submit(
                    tentarEmprestar(inicio, exemplarId, primeiroUsuario.getId())
            );
            Future<Boolean> segundo = executor.submit(
                    tentarEmprestar(inicio, exemplarId, segundoUsuario.getId())
            );
            inicio.countDown();

            int emprestimosCriados = (primeiro.get() ? 1 : 0) + (segundo.get() ? 1 : 0);
            assertEquals(1, emprestimosCriados);
            assertEquals(
                    1L,
                    emprestimoRepository.countByExemplarIdAndSituacao(
                            exemplarId,
                            SituacaoEmprestimo.ATIVO
                    )
            );
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void devePermitirApenasUmaRenovacaoConcorrenteDoMesmoEmprestimo() throws Exception {
        String sufixo = UUID.randomUUID().toString();
        Usuario usuario = criarUsuario("renovacao-" + sufixo);

        LivrosModel livro = new LivrosModel();
        livro.setNome("Livro para renovação " + sufixo);
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("RENOVACAO-" + sufixo);
        exemplar = exemplarRepository.save(exemplar);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(exemplar, usuario, LocalDate.now().minusDays(7));
        emprestimo = emprestimoRepository.save(emprestimo);

        Long emprestimoId = emprestimo.getId();
        Long exemplarId = exemplar.getId();
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> primeira = executor.submit(tentarRenovar(inicio, emprestimoId));
            Future<Boolean> segunda = executor.submit(tentarRenovar(inicio, emprestimoId));
            inicio.countDown();

            int renovacoesAceitas = (primeira.get() ? 1 : 0) + (segunda.get() ? 1 : 0);
            assertEquals(1, renovacoesAceitas);
            assertEquals(
                    1L,
                    emprestimoRepository.countByExemplarIdAndSituacao(
                            exemplarId,
                            SituacaoEmprestimo.ATIVO
                    )
            );
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void deveSerializarRenovacaoEDevolucaoConcorrentes() throws Exception {
        String sufixo = UUID.randomUUID().toString();
        Usuario usuario = criarUsuario("renovacao-devolucao-" + sufixo);

        LivrosModel livro = new LivrosModel();
        livro.setNome("Livro para disputa de estado " + sufixo);
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("DISPUTA-" + sufixo);
        exemplar = exemplarRepository.save(exemplar);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(exemplar, usuario, LocalDate.now().minusDays(7));
        emprestimo = emprestimoRepository.save(emprestimo);

        Long emprestimoId = emprestimo.getId();
        Long exemplarId = exemplar.getId();
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> renovacao = executor.submit(
                    tentarRenovar(inicio, emprestimoId)
            );
            Future<Boolean> devolucao = executor.submit(
                    tentarDevolver(inicio, emprestimoId)
            );
            inicio.countDown();

            int operacoesAceitas = (renovacao.get() ? 1 : 0) + (devolucao.get() ? 1 : 0);
            assertEquals(1, operacoesAceitas);

            Emprestimo estadoFinal = emprestimoRepository.findById(emprestimoId)
                    .orElseThrow();
            assertTrue(
                    estadoFinal.getSituacao() == SituacaoEmprestimo.RENOVADO
                            || estadoFinal.getSituacao() == SituacaoEmprestimo.DEVOLVIDO
            );

            long emprestimosAtivos = emprestimoRepository.countByExemplarIdAndSituacao(
                    exemplarId,
                    SituacaoEmprestimo.ATIVO
            );
            assertEquals(renovacao.get() ? 1L : 0L, emprestimosAtivos);
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<Boolean> tentarEmprestar(
            CountDownLatch inicio,
            Long exemplarId,
            Long usuarioId
    ) {
        return () -> {
            inicio.await();
            try {
                emprestimoService.emprestar(exemplarId, usuarioId);
                return true;
            } catch (IllegalStateException exemplarJaEmprestado) {
                return false;
            }
        };
    }

    private Callable<Boolean> tentarRenovar(CountDownLatch inicio, Long emprestimoId) {
        return () -> {
            inicio.await();
            try {
                emprestimoService.renovar(emprestimoId);
                return true;
            } catch (IllegalStateException emprestimoJaRenovado) {
                return false;
            }
        };
    }

    private Callable<Boolean> tentarDevolver(CountDownLatch inicio, Long emprestimoId) {
        return () -> {
            inicio.await();
            try {
                emprestimoService.devolver(emprestimoId);
                return true;
            } catch (IllegalStateException emprestimoJaEncerrado) {
                return false;
            }
        };
    }

    private Usuario criarUsuario(String identificador) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário " + identificador);
        usuario.setEmail(identificador + "@example.com");
        usuario.setCpf(String.format(
                "%011d",
                ThreadLocalRandom.current().nextLong(100_000_000_000L)
        ));
        return usuarioRepository.save(usuario);
    }
}
