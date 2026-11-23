package com.example.cadastro.controller;

import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.entities.SituacaoEmprestimo;
import com.example.cadastro.entities.Usuario;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.ExemplarRepository;
import com.example.cadastro.repository.LivrosRepository;
import com.example.cadastro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmprestimoControllerTest {

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

    @Test
    void deveSerializarDadosRelacionadosComOpenSessionInViewDesativado() throws Exception {
        String sufixo = Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );
        String nomeUsuario = "Ana Silva " + sufixo;
        String nomeLivro = "O Hobbit " + sufixo;

        Usuario usuario = new Usuario();
        usuario.setNome(nomeUsuario);
        usuario.setEmail("ana-" + sufixo + "@example.com");
        usuario.setCpf(sufixo);
        usuario = usuarioRepository.save(usuario);

        LivrosModel livro = new LivrosModel();
        livro.setNome(nomeLivro);
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("TESTE-" + sufixo);
        exemplar = exemplarRepository.save(exemplar);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(exemplar, usuario, LocalDate.now());
        emprestimo = emprestimoRepository.save(emprestimo);

        mockMvc.perform(get("/emprestimos/ativos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath(
                        "$[?(@.id == " + emprestimo.getId() + ")].usuario.nome"
                ).value(nomeUsuario))
                .andExpect(jsonPath(
                        "$[?(@.id == " + emprestimo.getId() + ")].usuario.cpf"
                ).doesNotExist())
                .andExpect(jsonPath(
                        "$[?(@.id == " + emprestimo.getId() + ")].exemplar.livro.nome"
                ).value(nomeLivro))
                .andExpect(jsonPath(
                        "$[?(@.id == " + emprestimo.getId() + ")].exemplar.codigoPatrimonio"
                ).value("TESTE-" + sufixo));
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExisteAoConsultarAtrasados() throws Exception {
        long usuarioId = 999_999_999L;

        mockMvc.perform(get("/emprestimos/usuario/{usuarioId}/atrasados", usuarioId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.erro").value("Usuário não encontrado"));

        mockMvc.perform(get(
                        "/emprestimos/usuario/{usuarioId}/atrasados/pagina",
                        usuarioId
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Usuário não encontrado"));
    }

    @Test
    void deveSerializarRelacionamentosNosListadosDeEmprestimos() throws Exception {
        String sufixo = Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );
        Usuario usuario = criarUsuario("atrasado-" + sufixo, sufixo);
        LivrosModel livro = new LivrosModel();
        livro.setNome("Livro atrasado " + sufixo);
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("ATRASADO-" + sufixo);
        exemplar = exemplarRepository.save(exemplar);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(exemplar, usuario, LocalDate.now().minusDays(20));
        emprestimo = emprestimoRepository.save(emprestimo);

        mockMvc.perform(get("/emprestimos/atrasados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + emprestimo.getId() +
                        ")].usuario.nome").value("atrasado-" + sufixo))
                .andExpect(jsonPath("$[?(@.id == " + emprestimo.getId() +
                        ")].exemplar.livro.nome").value("Livro atrasado " + sufixo));

        mockMvc.perform(get("/emprestimos/usuario/{usuarioId}/atrasados", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(emprestimo.getId()))
                .andExpect(jsonPath("$[0].exemplar.codigoPatrimonio")
                        .value("ATRASADO-" + sufixo));

        mockMvc.perform(get(
                        "/emprestimos/usuario/{usuarioId}/atrasados/pagina",
                        usuario.getId()
                ).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(emprestimo.getId()))
                .andExpect(jsonPath("$.content[0].usuario.nome")
                        .value("atrasado-" + sufixo))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        mockMvc.perform(get("/emprestimos/usuario/{usuarioId}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuario.nome").value("atrasado-" + sufixo))
                .andExpect(jsonPath("$[0].exemplar.livro.nome")
                        .value("Livro atrasado " + sufixo));

        mockMvc.perform(get(
                        "/emprestimos/usuario/{usuarioId}/pagina",
                        usuario.getId()
                ).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(emprestimo.getId()))
                .andExpect(jsonPath("$.content[0].exemplar.livro.nome")
                        .value("Livro atrasado " + sufixo))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void deveRetornar404NaConsultaPaginadaQuandoUsuarioNaoExiste() throws Exception {
        mockMvc.perform(get("/emprestimos/usuario/{usuarioId}/pagina", 999_999_999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.erro").value("Usuário não encontrado"));
    }

    @Test
    void deveCalcularMultaTotalSemCarregarRelacionamentosDosEmprestimos() throws Exception {
        String sufixo = Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );
        Usuario usuario = criarUsuario("multa-total-" + sufixo, sufixo);
        criarEmprestimoAtrasado(usuario, sufixo, "ATRASADO", 20);
        criarEmprestimo(usuario, sufixo, "NO-PRAZO");

        mockMvc.perform(get(
                        "/emprestimos/usuario/{usuarioId}/multa-total",
                        usuario.getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(26.0));
    }

    @Test
    void devePaginarEmprestimosAtrasadosDoMaisAntigoAoMaisRecente() throws Exception {
        String sufixo = Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );
        Usuario usuario = criarUsuario("paginas-atraso-" + sufixo, sufixo);
        Emprestimo maisAntigo = criarEmprestimoAtrasado(usuario, sufixo, "A", 30);
        Emprestimo intermediario = criarEmprestimoAtrasado(usuario, sufixo, "B", 20);
        Emprestimo maisRecente = criarEmprestimoAtrasado(usuario, sufixo, "C", 10);

        mockMvc.perform(get("/emprestimos/atrasados/pagina")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(maisAntigo.getId()))
                .andExpect(jsonPath("$.content[1].id").value(intermediario.getId()))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/emprestimos/atrasados/pagina")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(maisRecente.getId()))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void deveRetornar409QuandoExemplarJaEstaEmprestado() throws Exception {
        String sufixo = Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );

        Usuario usuario = new Usuario();
        usuario.setNome("Bruno " + sufixo);
        usuario.setEmail("bruno-" + sufixo + "@example.com");
        usuario.setCpf(sufixo);
        usuario = usuarioRepository.save(usuario);

        LivrosModel livro = new LivrosModel();
        livro.setNome("Fundação " + sufixo);
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("CONFLITO-" + sufixo);
        exemplar = exemplarRepository.save(exemplar);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(exemplar, usuario, LocalDate.now());
        emprestimoRepository.save(emprestimo);

        mockMvc.perform(post("/emprestimos")
                        .param("exemplarId", exemplar.getId().toString())
                        .param("usuarioId", usuario.getId().toString()))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.erro").value("Este exemplar já está emprestado"));
    }

    @Test
    void devePaginarHistoricoOrdenadoEAplicarFiltrosNoBanco() throws Exception {
        String sufixo = Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );
        Usuario usuario = criarUsuario("historico-" + sufixo, sufixo);
        String cpfOutroUsuario = sufixo.substring(0, sufixo.length() - 1) +
                ((Character.digit(sufixo.charAt(sufixo.length() - 1), 10) + 1) % 10);
        Usuario outroUsuario = criarUsuario("outro-" + sufixo, cpfOutroUsuario);

        Emprestimo devolvido = criarEmprestimo(usuario, sufixo, "DEVOLVIDO");
        devolvido.registrarDevolucao(LocalDate.now());
        emprestimoRepository.save(devolvido);
        criarEmprestimo(usuario, sufixo, "ATIVO");
        criarEmprestimo(outroUsuario, sufixo, "OUTRO");

        mockMvc.perform(get("/emprestimos/historico")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id")
                        .value(emprestimoRepository.findAll().stream()
                                .mapToLong(Emprestimo::getId)
                                .max()
                                .orElseThrow()))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/emprestimos/historico")
                        .param("usuarioId", usuario.getId().toString())
                        .param("situacao", SituacaoEmprestimo.DEVOLVIDO.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(devolvido.getId()))
                .andExpect(jsonPath("$.content[0].situacao").value("DEVOLVIDO"))
                .andExpect(jsonPath("$.content[0].usuario.nome")
                        .value("historico-" + sufixo));

        mockMvc.perform(get("/emprestimos/historico")
                        .param("usuarioId", usuario.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/emprestimos/historico")
                        .param("situacao", SituacaoEmprestimo.ATIVO.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void devePaginarSomenteEmprestimosAtivosDoMaisRecenteParaOMaisAntigo() throws Exception {
        String sufixo = Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );
        Usuario usuario = criarUsuario("ativos-" + sufixo, sufixo);

        Emprestimo primeiro = criarEmprestimo(usuario, sufixo, "ATIVO-1");
        Emprestimo segundo = criarEmprestimo(usuario, sufixo, "ATIVO-2");
        Emprestimo terceiro = criarEmprestimo(usuario, sufixo, "ATIVO-3");
        Emprestimo devolvido = criarEmprestimo(usuario, sufixo, "DEVOLVIDO-ATIVO");
        devolvido.registrarDevolucao(LocalDate.now());
        emprestimoRepository.save(devolvido);

        mockMvc.perform(get("/emprestimos/ativos/pagina")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(terceiro.getId()))
                .andExpect(jsonPath("$.content[1].id").value(segundo.getId()))
                .andExpect(jsonPath("$.content[0].situacao").value("ATIVO"))
                .andExpect(jsonPath("$.content[0].usuario.nome")
                        .value("ativos-" + sufixo))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/emprestimos/ativos/pagina")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(primeiro.getId()))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void deveRejeitarTamanhoDePaginaForaDoLimite() throws Exception {
        mockMvc.perform(get("/emprestimos/historico").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(
                        "A página deve ser não negativa e o tamanho deve estar entre 1 e 100"
                ));
    }

    @Test
    void deveRejeitarPaginaAtivaComIndiceNegativo() throws Exception {
        mockMvc.perform(get("/emprestimos/ativos/pagina").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(
                        "A página deve ser não negativa e o tamanho deve estar entre 1 e 100"
                ));
    }

    private Usuario criarUsuario(String nome, String cpf) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(nome + "@example.com");
        usuario.setCpf(cpf);
        return usuarioRepository.save(usuario);
    }

    private Emprestimo criarEmprestimo(Usuario usuario, String sufixo, String identificador) {
        LivrosModel livro = new LivrosModel();
        livro.setNome("Livro " + identificador + " " + sufixo);
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("HIST-" + identificador + "-" + sufixo);
        exemplar = exemplarRepository.save(exemplar);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(exemplar, usuario, LocalDate.now());
        return emprestimoRepository.save(emprestimo);
    }

    private Emprestimo criarEmprestimoAtrasado(
            Usuario usuario,
            String sufixo,
            String identificador,
            int diasDesdeEmprestimo
    ) {
        LivrosModel livro = new LivrosModel();
        livro.setNome("Atrasado " + identificador + " " + sufixo);
        livro = livrosRepository.save(livro);

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio("PAGINA-ATRASO-" + identificador + "-" + sufixo);
        exemplar = exemplarRepository.save(exemplar);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(exemplar, usuario, LocalDate.now().minusDays(diasDesdeEmprestimo));
        return emprestimoRepository.save(emprestimo);
    }
}