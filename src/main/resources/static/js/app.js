const botoesNavegacao = document.querySelectorAll("nav button");
const titulo = document.querySelector("main h2");
const descricao = document.querySelector("main p");
const secaoVisaoGeral = document.querySelector("#secao-visao-geral");
const mensagemVisaoGeral = document.querySelector("#mensagem-visao-geral");
const secaoUsuarios = document.querySelector("#secao-usuarios");

const formUsuario = document.querySelector("#form-usuario");
const listaUsuarios = document.querySelector("#lista-usuarios");
const botaoMostrarUsuarios = document.querySelector("#botao-mostrar-usuarios");
const mensagemForm = document.querySelector("#mensagem-form");
const LIMITE_USUARIOS_RECOLHIDOS = 3;
let usuariosCadastrados = [];
let mostrarTodosUsuarios = false;
let usuariosInativos = [];
let mostrarTodosUsuariosInativos = false;

const botaoInativos = document.querySelector("#botao-inativos");
const listaInativos = document.querySelector("#lista-inativos");
const botaoMostrarInativos = document.querySelector("#botao-mostrar-inativos");
const formEditar = document.querySelector("#form-editar-usuario");
const campoEditarId = document.querySelector("#editar-id");
const campoEditarNome = document.querySelector("#editar-nome");
const campoEditarEmail = document.querySelector("#editar-email");
const campoEditarCpf = document.querySelector("#editar-cpf");
const botaoCancelarEdicao = document.querySelector("#cancelar-edicao");

const secaoLivros = document.querySelector("#secao-livros");
const formLivro = document.querySelector("#form-livro");
const listaLivros = document.querySelector("#lista-livros");
const mensagemLivros = document.querySelector("#mensagem-livros");
const botaoLivrosInativos = document.querySelector("#botao-livros-inativos");
const listaLivrosInativos = document.querySelector("#lista-livros-inativos");

const secaoEmprestimos = document.querySelector("#secao-emprestimos");
const formEmprestimo = document.querySelector("#form-emprestimo");
const campoUsuarioEmprestimo = document.querySelector("#usuario-emprestimo");
const campoExemplarEmprestimo = document.querySelector("#exemplar-emprestimo");
const listaEmprestimos = document.querySelector("#lista-emprestimos");
const paginacaoEmprestimosAtivos = document.querySelector("#paginacao-emprestimos-ativos");
const paginaAnteriorEmprestimosAtivos =
    document.querySelector("#pagina-anterior-emprestimos-ativos");
const proximaPaginaEmprestimosAtivos =
    document.querySelector("#proxima-pagina-emprestimos-ativos");
const paginaAtualEmprestimosAtivos =
    document.querySelector("#pagina-atual-emprestimos-ativos");
const mensagemEmprestimos = document.querySelector("#mensagem-emprestimos");
const botaoHistoricoEmprestimos = document.querySelector("#botao-historico-emprestimos");
const listaHistoricoEmprestimos = document.querySelector("#lista-historico-emprestimos");
const controlesPaginacaoHistorico = document.querySelector(".paginacao-historico");
const filtroHistoricoEmprestimos = document.querySelector("#filtro-historico-emprestimos");
const filtroSituacaoEmprestimos = document.querySelector("#filtro-situacao-emprestimos");
const botaoPaginaAnteriorHistorico = document.querySelector("#pagina-anterior-historico");
const botaoProximaPaginaHistorico = document.querySelector("#proxima-pagina-historico");
const paginaAtualHistorico = document.querySelector("#pagina-atual-historico");
const TAMANHO_PAGINA_HISTORICO = 20;
const TAMANHO_PAGINA_EMPRESTIMOS_ATIVOS = 20;
let paginaEmprestimosAtivos = 0;
let totalPaginasEmprestimosAtivos = 0;
let historicoEmprestimos = [];
let paginaHistorico = 0;
let totalPaginasHistorico = 0;
let totalEmprestimosHistorico = 0;
let filtrosHistoricoCarregados = false;

const secaoMultas = document.querySelector("#secao-multas");
const campoUsuarioMultas = document.querySelector("#usuario-multas");
const saldoMultas = document.querySelector("#saldo-multas");
const listaMultas = document.querySelector("#lista-multas");
const listaPagamentos = document.querySelector("#lista-pagamentos");
const paginacaoPagamentos = document.querySelector("#paginacao-pagamentos");
const paginaAnteriorPagamentos = document.querySelector("#pagina-anterior-pagamentos");
const proximaPaginaPagamentos = document.querySelector("#proxima-pagina-pagamentos");
const paginaAtualPagamentos = document.querySelector("#pagina-atual-pagamentos");
const TAMANHO_PAGINA_PAGAMENTOS = 20;
let paginaPagamentos = 0;
let totalPaginasPagamentos = 0;
const mensagemMultas = document.querySelector("#mensagem-multas");
const listaEmprestimosAtrasados = document.querySelector("#lista-emprestimos-atrasados");
const paginacaoAtrasados = document.querySelector("#paginacao-atrasados");
const paginaAnteriorAtrasados = document.querySelector("#pagina-anterior-atrasados");
const proximaPaginaAtrasados = document.querySelector("#proxima-pagina-atrasados");
const paginaAtualAtrasados = document.querySelector("#pagina-atual-atrasados");
const TAMANHO_PAGINA_ATRASADOS = 20;
let paginaAtrasados = 0;
let totalPaginasAtrasados = 0;

const descricoesPorSecao = {
    "Visão geral": "Acompanhe os principais indicadores e acesse rapidamente cada fluxo.",
    "Usuários": "Aqui você poderá consultar e cadastrar usuários.",
    "Livros e exemplares": "Aqui você poderá consultar livros e exemplares.",
    "Empréstimos": "Aqui você poderá acompanhar os empréstimos.",
    "Multas e pagamentos": "Aqui você poderá consultar multas e pagamentos."
};

function atualizarMensagemStatus(elemento, texto) {
    elemento.textContent = texto;
    elemento.classList.toggle("mensagem-erro", texto.startsWith("Falha"));
    elemento.classList.toggle("mensagem-sucesso", !texto.startsWith("Falha"));
    elemento.hidden = false;
}

function mostrarMensagem(texto) {
    atualizarMensagemStatus(mensagemForm, texto);
}

function limparMensagem() {
    mensagemForm.textContent = "";
    mensagemForm.hidden = true;
}

async function mensagemDeErroDaResposta(resposta) {
    try {
        const detalhe = await resposta.json();
        return detalhe.erro || `Erro HTTP: ${resposta.status}`;
    } catch {
        return `Erro HTTP: ${resposta.status}`;
    }
}

async function carregarVisaoGeral() {
    const indicadores = [
        document.querySelector("#resumo-usuarios"),
        document.querySelector("#resumo-livros"),
        document.querySelector("#resumo-emprestimos"),
        document.querySelector("#resumo-atrasos")
    ];
    indicadores.forEach((indicador) => {
        indicador.textContent = "—";
    });
    mensagemVisaoGeral.textContent = "Carregando indicadores...";
    mensagemVisaoGeral.hidden = false;
    mensagemVisaoGeral.classList.remove("mensagem-erro", "mensagem-sucesso");

    try {
        const resposta = await fetch("/biblioteca/resumo");
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const resumo = await resposta.json();

        indicadores[0].textContent = resumo.usuariosAtivos;
        indicadores[1].textContent = resumo.livrosAtivos;
        indicadores[2].textContent = resumo.emprestimosAtivos;
        indicadores[3].textContent = resumo.emprestimosAtrasados;
        atualizarMensagemStatus(mensagemVisaoGeral, "Indicadores atualizados.");
    } catch (erro) {
        atualizarMensagemStatus(
            mensagemVisaoGeral,
            `Falha ao carregar visão geral: ${erro.message}`
        );
    }
}

botoesNavegacao.forEach((botao) => {
    botao.addEventListener("click", () => {
        const nomeSecao = botao.textContent.trim();

        titulo.textContent = nomeSecao;
        descricao.textContent = descricoesPorSecao[nomeSecao];

        botoesNavegacao.forEach((item) => {
            const ativo = item === botao;
            item.classList.toggle("ativo", ativo);
            if (ativo) {
                item.setAttribute("aria-current", "page");
            } else {
                item.removeAttribute("aria-current");
            }
        });

        secaoVisaoGeral.hidden = nomeSecao !== "Visão geral";
        secaoUsuarios.hidden = nomeSecao !== "Usuários";
        secaoLivros.hidden = nomeSecao !== "Livros e exemplares";
        secaoEmprestimos.hidden = nomeSecao !== "Empréstimos";
        secaoMultas.hidden = nomeSecao !== "Multas e pagamentos";

        if (!secaoVisaoGeral.hidden) {
            carregarVisaoGeral();
        }

        if (!secaoUsuarios.hidden) {
            carregarUsuarios();
        }

        if (!secaoLivros.hidden) {
            carregarLivros();
        }

        if (!secaoEmprestimos.hidden) {
            carregarEmprestimos();
        }

        if (!secaoMultas.hidden) {
            carregarUsuariosParaMultas();
        }
    });
});

document.querySelectorAll("[data-secao]").forEach((botao) => {
    botao.addEventListener("click", () => {
        const destino = Array.from(botoesNavegacao).find(
            (item) => item.textContent.trim() === botao.dataset.secao
        );
        destino?.click();
    });
});

formUsuario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limparMensagem();

    const dados = Object.fromEntries(new FormData(formUsuario));

    try {
        const resposta = await fetch("/usuarios", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        formUsuario.reset();
        await carregarUsuarios();
        mostrarMensagem("Usuário cadastrado com sucesso.");
    } catch (erro) {
        mostrarMensagem(`Falha ao cadastrar: ${erro.message}`);
    }
});

async function carregarUsuarios() {
    listaUsuarios.replaceChildren();

    try {
        const resposta = await fetch("/usuarios");

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        usuariosCadastrados = await resposta.json();
        if (usuariosCadastrados.length <= LIMITE_USUARIOS_RECOLHIDOS) {
            mostrarTodosUsuarios = false;
        }
        renderizarUsuarios();
    } catch (erro) {
        mostrarMensagem(`Falha ao carregar usuários: ${erro.message}`);
    }
}

botaoMostrarUsuarios.addEventListener("click", () => {
    mostrarTodosUsuarios = !mostrarTodosUsuarios;
    renderizarUsuarios();
});

function renderizarUsuarios() {
    listaUsuarios.replaceChildren();

    if (usuariosCadastrados.length === 0) {
        const item = document.createElement("li");
        item.textContent = "Nenhum usuário cadastrado.";
        listaUsuarios.append(item);
        botaoMostrarUsuarios.hidden = true;
        return;
    }

    const usuariosVisiveis = mostrarTodosUsuarios
        ? usuariosCadastrados
        : usuariosCadastrados.slice(0, LIMITE_USUARIOS_RECOLHIDOS);

    usuariosVisiveis.forEach((usuario) => {
        const item = document.createElement("li");
        item.classList.add("usuario-lista-item");

        const dadosUsuario = document.createElement("span");
        dadosUsuario.classList.add("usuario-dados");
        dadosUsuario.textContent =
            `${usuario.id} — ${usuario.nome} (${usuario.email})`;

        const acoes = document.createElement("div");
        acoes.classList.add("usuario-acoes");

        const botaoEditar = document.createElement("button");
        botaoEditar.type = "button";
        botaoEditar.textContent = "Editar";
        botaoEditar.addEventListener("click", () => editarUsuario(usuario.id));

        const botaoDesativar = document.createElement("button");
        botaoDesativar.type = "button";
        botaoDesativar.classList.add("botao-perigo");
        botaoDesativar.textContent = "Desativar";
        botaoDesativar.addEventListener("click", () => desativarUsuario(usuario));

        acoes.append(botaoEditar, botaoDesativar);
        item.append(dadosUsuario, acoes);
        listaUsuarios.append(item);
    });

    const temMaisUsuarios = usuariosCadastrados.length > LIMITE_USUARIOS_RECOLHIDOS;
    if (temMaisUsuarios && !mostrarTodosUsuarios) {
        const indicador = document.createElement("li");
        indicador.classList.add("indicador-mais-usuarios");
        indicador.textContent = "...";
        listaUsuarios.append(indicador);
    }

    botaoMostrarUsuarios.hidden = !temMaisUsuarios;
    botaoMostrarUsuarios.textContent = mostrarTodosUsuarios
        ? "Mostrar menos"
        : "Mostrar todos os usuários";
}

async function editarUsuario(id) {
    limparMensagem();

    try {
        const resposta = await fetch(`/usuarios/${id}`);

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const usuario = await resposta.json();

        campoEditarId.value = usuario.id;
        campoEditarNome.value = usuario.nome;
        campoEditarEmail.value = usuario.email;
        campoEditarCpf.value = "";
        formEditar.hidden = false;
    } catch (erro) {
        mostrarMensagem(`Falha ao buscar usuário: ${erro.message}`);
    }
}

formEditar.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limparMensagem();

    const id = campoEditarId.value;
    const dados = {
        nome: campoEditarNome.value,
        email: campoEditarEmail.value,
        cpf: campoEditarCpf.value
    };

    try {
        const resposta = await fetch(`/usuarios/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        formEditar.reset();
        formEditar.hidden = true;

        await carregarUsuarios();

        if (!listaInativos.hidden) {
            await carregarUsuariosInativos();
        }

        mostrarMensagem("Usuário atualizado com sucesso.");
    } catch (erro) {
        mostrarMensagem(`Falha ao atualizar: ${erro.message}`);
    }
});

botaoCancelarEdicao.addEventListener("click", () => {
    formEditar.reset();
    formEditar.hidden = true;
    limparMensagem();
});

async function desativarUsuario(usuario) {
    const confirmar = window.confirm(`Desativar o usuário ${usuario.nome}?`);

    if (!confirmar) {
        return;
    }

    limparMensagem();

    try {
        const resposta = await fetch(`/usuarios/${usuario.id}`, {
            method: "DELETE"
        });

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        await carregarUsuarios();

        if (!listaInativos.hidden) {
            await carregarUsuariosInativos();
        }

        mostrarMensagem("Usuário desativado.");
    } catch (erro) {
        mostrarMensagem(`Falha ao desativar: ${erro.message}`);
    }
}

botaoInativos.addEventListener("click", async () => {
    listaInativos.hidden = !listaInativos.hidden;
    botaoInativos.textContent = listaInativos.hidden
        ? "Ver usuários inativos"
        : "Ocultar usuários inativos";

    if (!listaInativos.hidden) {
        await carregarUsuariosInativos();
    }
});

async function carregarUsuariosInativos() {
    listaInativos.replaceChildren();

    try {
        const resposta = await fetch("/usuarios/inativos");

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        usuariosInativos = await resposta.json();
        if (usuariosInativos.length <= LIMITE_USUARIOS_RECOLHIDOS) {
            mostrarTodosUsuariosInativos = false;
        }
        renderizarUsuariosInativos();
    } catch (erro) {
        mostrarMensagem(`Falha ao carregar usuários inativos: ${erro.message}`);
    }
}

botaoMostrarInativos.addEventListener("click", () => {
    mostrarTodosUsuariosInativos = !mostrarTodosUsuariosInativos;
    renderizarUsuariosInativos();
});

function renderizarUsuariosInativos() {
    listaInativos.replaceChildren();

    if (usuariosInativos.length === 0) {
        const item = document.createElement("li");
        item.textContent = "Nenhum usuário inativo.";
        listaInativos.append(item);
        botaoMostrarInativos.hidden = true;
        return;
    }

    const usuariosVisiveis = mostrarTodosUsuariosInativos
        ? usuariosInativos
        : usuariosInativos.slice(0, LIMITE_USUARIOS_RECOLHIDOS);

    usuariosVisiveis.forEach((usuario) => {
        const item = document.createElement("li");
        item.classList.add("usuario-lista-item");

        const dadosUsuario = document.createElement("span");
        dadosUsuario.classList.add("usuario-dados");
        dadosUsuario.textContent =
            `${usuario.id} — ${usuario.nome} (${usuario.email})`;

        const botaoReativar = document.createElement("button");
        botaoReativar.type = "button";
        botaoReativar.textContent = "Reativar";
        botaoReativar.addEventListener("click", () => reativarUsuario(usuario));

        const acoes = document.createElement("div");
        acoes.classList.add("usuario-acoes");
        acoes.append(botaoReativar);

        item.append(dadosUsuario, acoes);
        listaInativos.append(item);
    });

    const temMaisUsuarios =
        usuariosInativos.length > LIMITE_USUARIOS_RECOLHIDOS;
    if (temMaisUsuarios && !mostrarTodosUsuariosInativos) {
        const indicador = document.createElement("li");
        indicador.classList.add("indicador-mais-usuarios");
        indicador.textContent = "...";
        listaInativos.append(indicador);
    }

    botaoMostrarInativos.hidden = !temMaisUsuarios;
    botaoMostrarInativos.textContent = mostrarTodosUsuariosInativos
        ? "Mostrar menos"
        : "Mostrar todos os usuários inativos";
}

async function reativarUsuario(usuario) {
    limparMensagem();

    try {
        const resposta = await fetch(`/usuarios/${usuario.id}/ativacao`, {
            method: "PATCH"
        });

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        await carregarUsuarios();
        await carregarUsuariosInativos();
        mostrarMensagem("Usuário reativado.");
    } catch (erro) {
        mostrarMensagem(`Falha ao reativar: ${erro.message}`);
    }
}

function mostrarMensagemLivros(texto) {
    atualizarMensagemStatus(mensagemLivros, texto);
}

formLivro.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    mensagemLivros.hidden = true;

    const dados = Object.fromEntries(new FormData(formLivro));

    try {
        const resposta = await fetch("/livros", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dados)
        });

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        formLivro.reset();
        await carregarLivros();
        mostrarMensagemLivros("Livro cadastrado com sucesso.");
    } catch (erro) {
        mostrarMensagemLivros(`Falha ao cadastrar livro: ${erro.message}`);
    }
});

async function carregarLivros() {
    listaLivros.replaceChildren();

    try {
        const resposta = await fetch("/livros");

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const livros = await resposta.json();
        listaLivros.hidden = false;

        if (livros.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Nenhum livro cadastrado.";
            listaLivros.append(item);
            return true;
        }

        livros.forEach((livro) => {
            const item = document.createElement("li");
            const tituloLivro = document.createElement("h4");
            tituloLivro.textContent = `${livro.id} — ${livro.nome}`;
            const botaoDesativarLivro = document.createElement("button");
            botaoDesativarLivro.type = "button";
            botaoDesativarLivro.classList.add("botao-perigo");
            botaoDesativarLivro.textContent = "Desativar livro";
            botaoDesativarLivro.addEventListener("click", () => {
                alterarSituacaoLivro(livro, false);
            });

            const formularioExemplar = document.createElement("form");
            const rotulo = document.createElement("label");
            const campoCodigo = document.createElement("input");
            const botaoCadastrar = document.createElement("button");
            const listaExemplares = document.createElement("ul");
            const listaExemplaresInativos = document.createElement("ul");

            rotulo.textContent = "Código de patrimônio";
            campoCodigo.type = "text";
            campoCodigo.required = true;
            botaoCadastrar.type = "submit";
            botaoCadastrar.textContent = "Cadastrar exemplar";

            formularioExemplar.append(rotulo, campoCodigo, botaoCadastrar);
            formularioExemplar.addEventListener("submit", async (evento) => {
                evento.preventDefault();
                await cadastrarExemplar(livro.id, campoCodigo, listaExemplares);
            });

            const botaoVerExemplares = document.createElement("button");
            botaoVerExemplares.type = "button";
            botaoVerExemplares.textContent = "Ver exemplares ativos";
            botaoVerExemplares.addEventListener("click", () => {
                carregarExemplares(livro.id, listaExemplares);
            });

            const botaoVerInativos = document.createElement("button");
            botaoVerInativos.type = "button";
            botaoVerInativos.textContent = "Ver exemplares inativos";
            botaoVerInativos.addEventListener("click", async () => {
                listaExemplaresInativos.hidden = !listaExemplaresInativos.hidden;
                botaoVerInativos.textContent = listaExemplaresInativos.hidden
                    ? "Ver exemplares inativos"
                    : "Ocultar exemplares inativos";

                if (!listaExemplaresInativos.hidden) {
                    await carregarExemplaresInativos(
                        livro.id,
                        listaExemplaresInativos,
                        listaExemplares
                    );
                }
            });
            listaExemplaresInativos.hidden = true;

            item.append(
                tituloLivro,
                botaoDesativarLivro,
                formularioExemplar,
                botaoVerExemplares,
                listaExemplares,
                botaoVerInativos,
                listaExemplaresInativos
            );
            listaLivros.append(item);
        });
        return true;
    } catch (erro) {
        mostrarMensagemLivros(`Falha ao carregar livros: ${erro.message}`);
        return false;
    }
}

botaoLivrosInativos.addEventListener("click", async () => {
    listaLivrosInativos.hidden = !listaLivrosInativos.hidden;
    botaoLivrosInativos.textContent = listaLivrosInativos.hidden
        ? "Ver livros inativos"
        : "Ocultar livros inativos";

    if (!listaLivrosInativos.hidden) {
        await carregarLivrosInativos();
    }
});

async function carregarLivrosInativos() {
    listaLivrosInativos.replaceChildren();

    try {
        const resposta = await fetch("/livros/inativos");
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const livros = await resposta.json();
        if (livros.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Nenhum livro inativo.";
            listaLivrosInativos.append(item);
            return true;
        }

        livros.forEach((livro) => {
            const item = document.createElement("li");
            item.append(document.createTextNode(`${livro.id} — ${livro.nome} `));

            const botaoReativar = document.createElement("button");
            botaoReativar.type = "button";
            botaoReativar.textContent = "Reativar livro";
            botaoReativar.addEventListener("click", () => {
                alterarSituacaoLivro(livro, true);
            });
            item.append(botaoReativar);
            listaLivrosInativos.append(item);
        });
        return true;
    } catch (erro) {
        mostrarMensagemLivros(`Falha ao carregar livros inativos: ${erro.message}`);
        return false;
    }
}

async function alterarSituacaoLivro(livro, ativar) {
    const acao = ativar ? "reativar" : "desativar";
    const participio = ativar ? "reativado" : "desativado";

    if (!window.confirm(`${ativar ? "Reativar" : "Desativar"} o livro "${livro.nome}"?`)) {
        return;
    }

    try {
        const operacao = ativar ? "ativacao" : "desativacao";
        const resposta = await fetch(`/livros/${livro.id}/${operacao}`, {
            method: "PATCH"
        });
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const livrosAtivosCarregados = await carregarLivros();
        const inativosCarregados = listaLivrosInativos.hidden
            ? true
            : await carregarLivrosInativos();

        if (livrosAtivosCarregados && inativosCarregados) {
            mostrarMensagemLivros(`Livro ${participio} com sucesso.`);
        }
    } catch (erro) {
        mostrarMensagemLivros(`Falha ao ${acao} livro: ${erro.message}`);
    }
}

async function cadastrarExemplar(livroId, campoCodigo, listaExemplares) {
    const codigo = campoCodigo.value.trim();

    try {
        const url = `/livros/${livroId}/exemplares?codigoPatrimonio=${encodeURIComponent(codigo)}`;
        const resposta = await fetch(url, { method: "POST" });

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        campoCodigo.value = "";
        await carregarExemplares(livroId, listaExemplares);
        mostrarMensagemLivros("Exemplar cadastrado com sucesso.");
    } catch (erro) {
        mostrarMensagemLivros(`Falha ao cadastrar exemplar: ${erro.message}`);
    }
}

async function carregarExemplares(livroId, lista) {
    lista.replaceChildren();

    try {
        const resposta = await fetch(
            `/livros/${livroId}/exemplares/resumo`
        );
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const exemplares = await resposta.json();

        if (exemplares.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Este livro não possui exemplares ativos.";
            lista.append(item);
            return;
        }

        exemplares.forEach((exemplar) => {
            const item = document.createElement("li");
            const disponivel = exemplar.disponivel;
            item.append(document.createTextNode(
                `Exemplar ${exemplar.id} — patrimônio: ${exemplar.codigoPatrimonio} `
            ));
            const etiquetaDisponibilidade = document.createElement("span");
            etiquetaDisponibilidade.classList.add(
                "etiqueta-status",
                disponivel ? "etiqueta-disponivel" : "etiqueta-emprestado"
            );
            etiquetaDisponibilidade.textContent = disponivel
                ? "Disponível para empréstimo"
                : "Emprestado";

            const botaoDesativar = document.createElement("button");
            botaoDesativar.type = "button";
            botaoDesativar.classList.add("botao-perigo");
            botaoDesativar.textContent = "Desativar exemplar";
            botaoDesativar.disabled = !disponivel;
            if (!disponivel) {
                botaoDesativar.title =
                    "Não é possível desativar um exemplar emprestado.";
            } else {
                botaoDesativar.addEventListener("click", () => {
                    alterarSituacaoExemplar(
                        livroId,
                        exemplar.id,
                        false,
                        lista
                    );
                });
            }
            item.append(etiquetaDisponibilidade, botaoDesativar);
            lista.append(item);
        });
    } catch (erro) {
        mostrarMensagemLivros(`Falha ao carregar exemplares: ${erro.message}`);
    }
}

async function carregarExemplaresInativos(livroId, listaInativos, listaAtivos) {
    listaInativos.replaceChildren();

    try {
        const resposta = await fetch(
            `/livros/${livroId}/exemplares/inativos`
        );
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const exemplares = await resposta.json();
        if (exemplares.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Nenhum exemplar inativo.";
            listaInativos.append(item);
            return;
        }

        exemplares.forEach((exemplar) => {
            const item = document.createElement("li");
            item.append(document.createTextNode(
                `Exemplar ${exemplar.id} — patrimônio: ${exemplar.codigoPatrimonio} `
            ));

            const botaoReativar = document.createElement("button");
            botaoReativar.type = "button";
            botaoReativar.textContent = "Reativar exemplar";
            botaoReativar.addEventListener("click", () => {
                alterarSituacaoExemplar(
                    livroId,
                    exemplar.id,
                    true,
                    listaAtivos,
                    listaInativos
                );
            });
            item.append(botaoReativar);
            listaInativos.append(item);
        });
    } catch (erro) {
        mostrarMensagemLivros(
            `Falha ao carregar exemplares inativos: ${erro.message}`
        );
    }
}

async function alterarSituacaoExemplar(
    livroId,
    exemplarId,
    ativar,
    listaAtivos,
    listaInativos
) {
    const acao = ativar ? "reativar" : "desativar";
    const acaoConcluida = ativar ? "reativado" : "desativado";
    if (!window.confirm(
        `${ativar ? "Reativar" : "Desativar"} o exemplar ${exemplarId}?`
    )) {
        return;
    }

    try {
        const operacao = ativar ? "ativacao" : "desativacao";
        const resposta = await fetch(
            `/livros/${livroId}/exemplares/${exemplarId}/${operacao}`,
            { method: "PATCH" }
        );
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        await carregarExemplares(livroId, listaAtivos);
        if (listaInativos && !listaInativos.hidden) {
            await carregarExemplaresInativos(
                livroId,
                listaInativos,
                listaAtivos
            );
        }
        mostrarMensagemLivros(`Exemplar ${acaoConcluida} com sucesso.`);
    } catch (erro) {
        mostrarMensagemLivros(`Falha ao ${acao} exemplar: ${erro.message}`);
    }
}

function mostrarMensagemEmprestimos(texto) {
    atualizarMensagemStatus(mensagemEmprestimos, texto);
}

async function carregarEmprestimos(numeroPagina = 0, carregarOpcoes = true) {
    mensagemEmprestimos.hidden = true;
    listaEmprestimos.replaceChildren();

    try {
        const parametros = new URLSearchParams({
            page: String(numeroPagina),
            size: String(TAMANHO_PAGINA_EMPRESTIMOS_ATIVOS)
        });
        const requisicoes = [
            fetch(`/emprestimos/ativos/pagina?${parametros}`)
        ];
        if (carregarOpcoes) {
            requisicoes.push(fetch("/usuarios"), fetch("/livros/exemplares/disponiveis"));
        }

        const respostas = await Promise.all(requisicoes);
        for (const resposta of respostas) {
            if (!resposta.ok) {
                throw new Error(await mensagemDeErroDaResposta(resposta));
            }
        }

        const [paginaEmprestimos, usuarios, exemplaresDisponiveis] = await Promise.all([
            respostas[0].json(),
            carregarOpcoes ? respostas[1].json() : Promise.resolve(null),
            carregarOpcoes ? respostas[2].json() : Promise.resolve(null)
        ]);
        if (carregarOpcoes) {
            campoUsuarioEmprestimo.replaceChildren();
            campoUsuarioEmprestimo.add(new Option("Selecione um usuário", ""));
            usuarios.forEach((usuario) => {
                campoUsuarioEmprestimo.add(
                    new Option(`${usuario.nome} (ID ${usuario.id})`, usuario.id)
                );
            });

            campoExemplarEmprestimo.replaceChildren();
            campoExemplarEmprestimo.add(
                new Option("Selecione um exemplar disponível", "")
            );
            exemplaresDisponiveis.forEach((exemplar) => {
                campoExemplarEmprestimo.add(new Option(
                    `${exemplar.livroNome} — patrimônio ${exemplar.codigoPatrimonio}`,
                    exemplar.id
                ));
            });
        }

        paginaEmprestimosAtivos = paginaEmprestimos.number;
        totalPaginasEmprestimosAtivos = paginaEmprestimos.totalPages;
        paginaAnteriorEmprestimosAtivos.disabled = paginaEmprestimos.first;
        proximaPaginaEmprestimosAtivos.disabled = paginaEmprestimos.last;
        paginacaoEmprestimosAtivos.hidden = paginaEmprestimos.totalPages <= 1;
        paginaAtualEmprestimosAtivos.textContent = paginaEmprestimos.totalPages === 0
            ? "Nenhuma página"
            : `Página ${paginaEmprestimos.number + 1} de ${paginaEmprestimos.totalPages} ` +
                `(${paginaEmprestimos.totalElements} empréstimos)`;
        const emprestimos = paginaEmprestimos.content;

        if (emprestimos.length === 0) {
            const item = document.createElement("li");
            item.textContent = paginaEmprestimos.totalElements === 0
                ? "Nenhum empréstimo ativo."
                : "Não há mais empréstimos ativos nesta página.";
            listaEmprestimos.append(item);
            return true;
        }

        emprestimos.forEach((emprestimo) => {
            const item = document.createElement("li");
            const diasAtraso = Number(emprestimo.diasDeAtraso) || 0;
            const situacaoPrazo = diasAtraso > 0
                ? `Atrasado há ${diasAtraso} dia(s)`
                : `Devolução prevista: ${emprestimo.dataPrevistaDevolucao}`;
            item.append(document.createTextNode(
                `Empréstimo ${emprestimo.id} — ${emprestimo.exemplar.livro.nome}, ` +
                `patrimônio ${emprestimo.exemplar.codigoPatrimonio}; ` +
                `${emprestimo.usuario.nome}; `
            ));
            const etiquetaPrazo = document.createElement("span");
            etiquetaPrazo.classList.add(
                "etiqueta-status",
                diasAtraso > 0 ? "etiqueta-atrasado" : "etiqueta-no-prazo"
            );
            etiquetaPrazo.textContent = situacaoPrazo;
            item.append(etiquetaPrazo, " ");

            const botaoDevolver = document.createElement("button");
            botaoDevolver.type = "button";
            botaoDevolver.textContent = "Registrar devolução";
            botaoDevolver.addEventListener("click", () => {
                devolverEmprestimo(emprestimo.id);
            });

            const botaoRenovar = document.createElement("button");
            botaoRenovar.type = "button";
            botaoRenovar.textContent = "Renovar empréstimo";
            const [diaPrevisto, mesPrevisto, anoPrevisto] =
                emprestimo.dataPrevistaDevolucao.split("/");
            const hoje = new Date();
            const dataPrevista = new Date(
                Number(anoPrevisto),
                Number(mesPrevisto) - 1,
                Number(diaPrevisto)
            );
            const venceHoje =
                dataPrevista.getFullYear() === hoje.getFullYear() &&
                dataPrevista.getMonth() === hoje.getMonth() &&
                dataPrevista.getDate() === hoje.getDate();
            botaoRenovar.disabled = !venceHoje;
            if (venceHoje) {
                botaoRenovar.addEventListener("click", () => {
                    renovarEmprestimo(emprestimo.id);
                });
            } else {
                botaoRenovar.title =
                    "A renovação só pode ser feita na data prevista para devolução.";
            }

            item.append(botaoDevolver, " ", botaoRenovar);
            listaEmprestimos.append(item);
        });
        return true;
    } catch (erro) {
        mostrarMensagemEmprestimos(
            `Falha ao carregar empréstimos: ${erro.message}`
        );
        return false;
    }
}

paginaAnteriorEmprestimosAtivos.addEventListener("click", () => {
    if (paginaEmprestimosAtivos > 0) {
        carregarEmprestimos(paginaEmprestimosAtivos - 1, false);
    }
});

proximaPaginaEmprestimosAtivos.addEventListener("click", () => {
    if (paginaEmprestimosAtivos + 1 < totalPaginasEmprestimosAtivos) {
        carregarEmprestimos(paginaEmprestimosAtivos + 1, false);
    }
});

formEmprestimo.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    mensagemEmprestimos.hidden = true;

    const parametros = new URLSearchParams({
        usuarioId: campoUsuarioEmprestimo.value,
        exemplarId: campoExemplarEmprestimo.value
    });

    try {
        const resposta = await fetch(`/emprestimos?${parametros}`, {
            method: "POST"
        });

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        formEmprestimo.reset();
        if (await carregarEmprestimos()) {
            mostrarMensagemEmprestimos("Empréstimo registrado com sucesso.");
        }
    } catch (erro) {
        mostrarMensagemEmprestimos(
            `Falha ao registrar empréstimo: ${erro.message}`
        );
    }
});

async function devolverEmprestimo(emprestimoId) {
    try {
        const resposta = await fetch(
            `/emprestimos/${emprestimoId}/devolucao`,
            { method: "POST" }
        );

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        if (await carregarEmprestimos()) {
            mostrarMensagemEmprestimos("Devolução registrada com sucesso.");
        }
    } catch (erro) {
        mostrarMensagemEmprestimos(
            `Falha ao registrar devolução: ${erro.message}`
        );
    }
}

async function renovarEmprestimo(emprestimoId) {
    try {
        const resposta = await fetch(
            `/emprestimos/${emprestimoId}/renovacao`,
            { method: "POST" }
        );

        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        if (await carregarEmprestimos()) {
            mostrarMensagemEmprestimos("Empréstimo renovado com sucesso.");
        }
    } catch (erro) {
        mostrarMensagemEmprestimos(
            `Falha ao renovar empréstimo: ${erro.message}`
        );
    }
}

botaoHistoricoEmprestimos.addEventListener("click", async () => {
    listaHistoricoEmprestimos.hidden = !listaHistoricoEmprestimos.hidden;
    controlesPaginacaoHistorico.hidden = listaHistoricoEmprestimos.hidden;
    botaoHistoricoEmprestimos.textContent = listaHistoricoEmprestimos.hidden
        ? "Consultar histórico"
        : "Ocultar histórico";

    if (!listaHistoricoEmprestimos.hidden) {
        await carregarHistoricoEmprestimos();
    }
});

filtroHistoricoEmprestimos.addEventListener("change", () => {
    paginaHistorico = 0;
    carregarHistoricoEmprestimos();
});
filtroSituacaoEmprestimos.addEventListener("change", () => {
    paginaHistorico = 0;
    carregarHistoricoEmprestimos();
});
botaoPaginaAnteriorHistorico.addEventListener("click", () => {
    if (paginaHistorico > 0) {
        paginaHistorico -= 1;
        carregarHistoricoEmprestimos();
    }
});
botaoProximaPaginaHistorico.addEventListener("click", () => {
    if (paginaHistorico + 1 < totalPaginasHistorico) {
        paginaHistorico += 1;
        carregarHistoricoEmprestimos();
    }
});

async function carregarHistoricoEmprestimos() {
    listaHistoricoEmprestimos.replaceChildren();

    try {
        if (!filtrosHistoricoCarregados) {
            const [respostaUsuarios, respostaUsuariosInativos] = await Promise.all([
                fetch("/usuarios"),
                fetch("/usuarios/inativos")
            ]);
            for (const resposta of [respostaUsuarios, respostaUsuariosInativos]) {
                if (!resposta.ok) {
                    throw new Error(await mensagemDeErroDaResposta(resposta));
                }
            }

            const [usuarios, usuariosInativos] = await Promise.all([
                respostaUsuarios.json(),
                respostaUsuariosInativos.json()
            ]);
            [...usuarios, ...usuariosInativos]
                .sort((usuarioA, usuarioB) =>
                    usuarioA.nome.localeCompare(usuarioB.nome, "pt-BR")
                )
                .forEach((usuario) => {
                    filtroHistoricoEmprestimos.add(new Option(
                        `${usuario.nome} (ID ${usuario.id})`,
                        usuario.id
                    ));
                });
            filtroHistoricoEmprestimos.disabled = usuarios.length +
                usuariosInativos.length === 0;
            filtroSituacaoEmprestimos.disabled = false;
            filtrosHistoricoCarregados = true;
        }

        const parametros = new URLSearchParams({
            page: String(paginaHistorico),
            size: String(TAMANHO_PAGINA_HISTORICO)
        });
        if (filtroHistoricoEmprestimos.value) {
            parametros.set("usuarioId", filtroHistoricoEmprestimos.value);
        }
        if (filtroSituacaoEmprestimos.value) {
            parametros.set("situacao", filtroSituacaoEmprestimos.value);
        }
        const resposta = await fetch(`/emprestimos/historico?${parametros}`);
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const pagina = await resposta.json();
        historicoEmprestimos = pagina.content;
        paginaHistorico = pagina.number;
        totalPaginasHistorico = pagina.totalPages;
        totalEmprestimosHistorico = pagina.totalElements;
        botaoPaginaAnteriorHistorico.disabled = pagina.first;
        botaoProximaPaginaHistorico.disabled = pagina.last;
        paginaAtualHistorico.textContent = totalPaginasHistorico === 0
            ? "Nenhuma página"
            : `Página ${pagina.number + 1} de ${totalPaginasHistorico} ` +
                `(${totalEmprestimosHistorico} empréstimos)`;
        renderizarHistoricoEmprestimos();
    } catch (erro) {
        mostrarMensagemEmprestimos(
            `Falha ao carregar histórico de empréstimos: ${erro.message}`
        );
    }
}

function renderizarHistoricoEmprestimos() {
    listaHistoricoEmprestimos.replaceChildren();

    if (historicoEmprestimos.length === 0) {
        const item = document.createElement("li");
        item.textContent = totalEmprestimosHistorico === 0 &&
            !filtroHistoricoEmprestimos.value &&
            !filtroSituacaoEmprestimos.value
            ? "Nenhum empréstimo registrado."
            : "Nenhum empréstimo corresponde aos filtros selecionados.";
        listaHistoricoEmprestimos.append(item);
        return;
    }

    const situacoes = {
        ATIVO: "Ativo",
        DEVOLVIDO: "Devolvido",
        RENOVADO: "Renovado"
    };

    historicoEmprestimos.forEach((emprestimo) => {
        const item = document.createElement("li");
        const situacao = situacoes[emprestimo.situacao] ||
            emprestimo.situacao;
        const dataDevolucao = emprestimo.dataDevolucao || "Não devolvido";
        item.textContent =
            `Empréstimo ${emprestimo.id} — ${emprestimo.usuario.nome}; ` +
            `${emprestimo.exemplar.livro.nome} (patrimônio ` +
            `${emprestimo.exemplar.codigoPatrimonio}); situação: ${situacao}; ` +
            `emprestado em ${emprestimo.dataEmprestimo}; devolução prevista ` +
            `${emprestimo.dataPrevistaDevolucao}; devolvido em ${dataDevolucao}; ` +
            `multa atual: ${formatarMoeda(emprestimo.multa)}`;
        listaHistoricoEmprestimos.append(item);
    });
}

function mostrarMensagemMultas(texto) {
    atualizarMensagemStatus(mensagemMultas, texto);
}

function formatarMoeda(valor) {
    return new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL"
    }).format(Number(valor));
}

async function carregarUsuariosParaMultas() {
    mensagemMultas.hidden = true;
    await carregarEmprestimosAtrasados();

    try {
        const resposta = await fetch("/usuarios");
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const usuarios = await resposta.json();
        campoUsuarioMultas.replaceChildren();
        campoUsuarioMultas.add(new Option("Selecione um usuário", ""));
        usuarios.forEach((usuario) => {
            campoUsuarioMultas.add(
                new Option(`${usuario.nome} (ID ${usuario.id})`, usuario.id)
            );
        });

        if (usuarios.length === 0) {
            saldoMultas.textContent = "Não há usuários ativos cadastrados.";
            listaMultas.replaceChildren();
            listaPagamentos.replaceChildren();
        }
    } catch (erro) {
        mostrarMensagemMultas(`Falha ao carregar usuários: ${erro.message}`);
    }
}

paginaAnteriorAtrasados.addEventListener("click", () => {
    if (paginaAtrasados > 0) {
        carregarEmprestimosAtrasados(paginaAtrasados - 1);
    }
});

proximaPaginaAtrasados.addEventListener("click", () => {
    if (paginaAtrasados + 1 < totalPaginasAtrasados) {
        carregarEmprestimosAtrasados(paginaAtrasados + 1);
    }
});

async function carregarEmprestimosAtrasados(numeroPagina = 0) {
    listaEmprestimosAtrasados.replaceChildren();

    try {
        const parametros = new URLSearchParams({
            page: String(numeroPagina),
            size: String(TAMANHO_PAGINA_ATRASADOS)
        });
        const resposta = await fetch(`/emprestimos/atrasados/pagina?${parametros}`);
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const pagina = await resposta.json();
        paginaAtrasados = pagina.number;
        totalPaginasAtrasados = pagina.totalPages;
        paginaAnteriorAtrasados.disabled = pagina.first;
        proximaPaginaAtrasados.disabled = pagina.last;
        paginacaoAtrasados.hidden = pagina.totalPages <= 1;
        paginaAtualAtrasados.textContent = pagina.totalPages === 0
            ? "Nenhuma página"
            : `Página ${pagina.number + 1} de ${pagina.totalPages} ` +
                `(${pagina.totalElements} empréstimos)`;

        const emprestimos = pagina.content;
        if (emprestimos.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Nenhum empréstimo atrasado.";
            listaEmprestimosAtrasados.append(item);
            return true;
        }

        emprestimos.forEach((emprestimo) => {
            const item = document.createElement("li");
            item.append(document.createTextNode(
                `${emprestimo.usuario.nome} — ${emprestimo.exemplar.livro.nome} ` +
                `(patrimônio ${emprestimo.exemplar.codigoPatrimonio}); ` +
                `devolução prevista: ${emprestimo.dataPrevistaDevolucao}; ` +
                `multa atual: ` +
                `${formatarMoeda(emprestimo.multa)} `
            ));
            const etiquetaAtraso = document.createElement("span");
            etiquetaAtraso.classList.add("etiqueta-status", "etiqueta-atrasado");
            etiquetaAtraso.textContent =
                `${emprestimo.diasDeAtraso} dia(s) de atraso`;

            const botaoDevolver = document.createElement("button");
            botaoDevolver.type = "button";
            botaoDevolver.textContent = "Registrar devolução";
            botaoDevolver.addEventListener("click", () => {
                devolverEmprestimoAtrasado(emprestimo);
            });
            item.append(etiquetaAtraso, " ", botaoDevolver);
            listaEmprestimosAtrasados.append(item);
        });
        return true;
    } catch (erro) {
        mostrarMensagemMultas(
            `Falha ao carregar empréstimos atrasados: ${erro.message}`
        );
        return false;
    }
}

async function devolverEmprestimoAtrasado(emprestimo) {
    if (!window.confirm(
        `Registrar a devolução do empréstimo ${emprestimo.id} ` +
        `de ${emprestimo.usuario.nome}?`
    )) {
        return;
    }

    mensagemMultas.hidden = true;

    try {
        const resposta = await fetch(
            `/emprestimos/${emprestimo.id}/devolucao`,
            { method: "POST" }
        );
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const [atrasosAtualizados, emprestimosAtualizados, multasAtualizadas] =
            await Promise.all([
                carregarEmprestimosAtrasados(),
                carregarEmprestimos(),
                carregarMultasDoUsuario()
            ]);
        if (atrasosAtualizados && emprestimosAtualizados && multasAtualizadas) {
            mostrarMensagemMultas("Devolução registrada com sucesso.");
        }
    } catch (erro) {
        mostrarMensagemMultas(
            `Falha ao registrar devolução: ${erro.message}`
        );
    }
}

campoUsuarioMultas.addEventListener("change", () => {
    paginaPagamentos = 0;
    carregarMultasDoUsuario();
});

paginaAnteriorPagamentos.addEventListener("click", () => {
    if (paginaPagamentos > 0) {
        carregarPaginaPagamentos(paginaPagamentos - 1).catch((erro) => {
            mostrarMensagemMultas(
                `Falha ao consultar pagamentos: ${erro.message}`
            );
        });
    }
});

proximaPaginaPagamentos.addEventListener("click", () => {
    if (paginaPagamentos + 1 < totalPaginasPagamentos) {
        carregarPaginaPagamentos(paginaPagamentos + 1).catch((erro) => {
            mostrarMensagemMultas(
                `Falha ao consultar pagamentos: ${erro.message}`
            );
        });
    }
});

async function carregarMultasDoUsuario() {
    const usuarioId = campoUsuarioMultas.value;
    listaMultas.replaceChildren();
    listaPagamentos.replaceChildren();
    paginacaoPagamentos.hidden = true;
    saldoMultas.textContent = "";
    mensagemMultas.hidden = true;

    if (!usuarioId) {
        return true;
    }

    try {
        const [respostaResumo, pagamentosCarregados] =
            await Promise.all([
                fetch(`/usuarios/${usuarioId}/multas/resumo`),
                carregarPaginaPagamentos(0)
            ]);

        if (!respostaResumo.ok) {
            throw new Error(await mensagemDeErroDaResposta(respostaResumo));
        }

        if (!pagamentosCarregados) {
            return false;
        }

        const resumo = await respostaResumo.json();
        saldoMultas.textContent =
            `Saldo total de multas pendentes: ${formatarMoeda(resumo.saldoTotal)}`;
        const multasPendentes = resumo.saldosPorEmprestimo.filter(
            ({ saldo }) => Number(saldo) > 0
        );
        if (multasPendentes.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Este usuário não possui multas pendentes.";
            listaMultas.append(item);
        }

        multasPendentes.forEach((multa) => {
            const item = document.createElement("li");
            item.append(document.createTextNode(
                `Empréstimo ${multa.emprestimoId} — ${multa.livroNome}; ` +
                `patrimônio ${multa.codigoPatrimonio}; ` +
                `multa pendente: ${formatarMoeda(multa.saldo)} `
            ));

            const formulario = document.createElement("form");
            const rotuloValor = document.createElement("label");
            const campoValor = document.createElement("input");
            const rotuloData = document.createElement("label");
            const campoData = document.createElement("input");
            const botaoPagar = document.createElement("button");

            rotuloValor.textContent = "Valor do pagamento";
            campoValor.type = "number";
            campoValor.min = "0.01";
            campoValor.max = Number(multa.saldo).toFixed(2);
            campoValor.step = "0.01";
            campoValor.required = true;

            rotuloData.textContent = "Data do pagamento";
            campoData.type = "date";
            campoData.max = new Date(
                Date.now() - new Date().getTimezoneOffset() * 60000
            ).toISOString().slice(0, 10);
            campoData.value = campoData.max;
            campoData.required = true;

            botaoPagar.type = "submit";
            botaoPagar.textContent = "Registrar pagamento";
            formulario.append(
                rotuloValor,
                campoValor,
                rotuloData,
                campoData,
                botaoPagar
            );
            formulario.addEventListener("submit", (evento) => {
                evento.preventDefault();
                registrarPagamentoMulta(
                    multa.emprestimoId,
                    campoValor.value,
                    campoData.value
                );
            });
            item.append(formulario);
            listaMultas.append(item);
        });

        return true;
    } catch (erro) {
        mostrarMensagemMultas(`Falha ao consultar multas: ${erro.message}`);
        return false;
    }
}

async function carregarPaginaPagamentos(numeroPagina) {
    const usuarioId = campoUsuarioMultas.value;
    const parametros = new URLSearchParams({
        page: String(numeroPagina),
        size: String(TAMANHO_PAGINA_PAGAMENTOS)
    });
    const resposta = await fetch(
        `/usuarios/${usuarioId}/pagamentos/historico?${parametros}`
    );
    if (!resposta.ok) {
        throw new Error(await mensagemDeErroDaResposta(resposta));
    }

    const pagina = await resposta.json();
    paginaPagamentos = pagina.number;
    totalPaginasPagamentos = pagina.totalPages;
    listaPagamentos.replaceChildren();
    paginaAnteriorPagamentos.disabled = pagina.first;
    proximaPaginaPagamentos.disabled = pagina.last;
    paginacaoPagamentos.hidden = pagina.totalPages <= 1;
    paginaAtualPagamentos.textContent = pagina.totalPages === 0
        ? "Nenhuma página"
        : `Página ${pagina.number + 1} de ${pagina.totalPages} ` +
            `(${pagina.totalElements} pagamentos)`;

    if (pagina.content.length === 0) {
        const item = document.createElement("li");
        item.textContent = "Nenhum pagamento registrado.";
        listaPagamentos.append(item);
        return true;
    }

    pagina.content.forEach((pagamento) => {
        const item = document.createElement("li");
        item.textContent =
            `${pagamento.dataPagamento} — ${formatarMoeda(pagamento.valor)} ` +
            `(empréstimo ${pagamento.emprestimoId})`;
        listaPagamentos.append(item);
    });
    return true;
}

async function registrarPagamentoMulta(emprestimoId, valorPago, dataDePagamento) {
    const parametros = new URLSearchParams({ valorPago, dataDePagamento });

    try {
        const resposta = await fetch(
            `/emprestimos/${emprestimoId}/pagamentos?${parametros}`,
            { method: "POST" }
        );
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const [multasAtualizadas, atrasosAtualizados] = await Promise.all([
            carregarMultasDoUsuario(),
            carregarEmprestimosAtrasados()
        ]);
        if (multasAtualizadas && atrasosAtualizados) {
            mostrarMensagemMultas("Pagamento registrado com sucesso.");
        }
    } catch (erro) {
        mostrarMensagemMultas(`Falha ao registrar pagamento: ${erro.message}`);
    }
}

carregarVisaoGeral();