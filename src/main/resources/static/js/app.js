const botoesNavegacao = document.querySelectorAll("nav button");
const titulo = document.querySelector("main h2");
const descricao = document.querySelector("main p");
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
const mensagemEmprestimos = document.querySelector("#mensagem-emprestimos");
const botaoHistoricoEmprestimos = document.querySelector("#botao-historico-emprestimos");
const listaHistoricoEmprestimos = document.querySelector("#lista-historico-emprestimos");
const filtroHistoricoEmprestimos = document.querySelector("#filtro-historico-emprestimos");
const filtroSituacaoEmprestimos = document.querySelector("#filtro-situacao-emprestimos");
let historicoEmprestimos = [];

const secaoMultas = document.querySelector("#secao-multas");
const campoUsuarioMultas = document.querySelector("#usuario-multas");
const saldoMultas = document.querySelector("#saldo-multas");
const listaMultas = document.querySelector("#lista-multas");
const listaPagamentos = document.querySelector("#lista-pagamentos");
const mensagemMultas = document.querySelector("#mensagem-multas");
const listaEmprestimosAtrasados = document.querySelector("#lista-emprestimos-atrasados");

const descricoesPorSecao = {
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

botoesNavegacao.forEach((botao) => {
    botao.addEventListener("click", () => {
        const nomeSecao = botao.textContent.trim();

        titulo.textContent = nomeSecao;
        descricao.textContent = descricoesPorSecao[nomeSecao];

        botoesNavegacao.forEach((item) => item.classList.remove("ativo"));
        botao.classList.add("ativo");

        secaoUsuarios.hidden = nomeSecao !== "Usuários";
        secaoLivros.hidden = nomeSecao !== "Livros e exemplares";
        secaoEmprestimos.hidden = nomeSecao !== "Empréstimos";
        secaoMultas.hidden = nomeSecao !== "Multas e pagamentos";

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
        campoEditarCpf.value = usuario.cpf;
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
        const [respostaAtivos, respostaDisponiveis] = await Promise.all([
            fetch(`/livros/${livroId}/exemplares/ativos`),
            fetch(`/livros/${livroId}/exemplares/disponiveis`)
        ]);

        for (const resposta of [respostaAtivos, respostaDisponiveis]) {
            if (!resposta.ok) {
                throw new Error(await mensagemDeErroDaResposta(resposta));
            }
        }

        const [exemplares, disponiveis] = await Promise.all([
            respostaAtivos.json(),
            respostaDisponiveis.json()
        ]);
        const idsDisponiveis = new Set(
            disponiveis.map((exemplar) => String(exemplar.id))
        );

        if (exemplares.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Este livro não possui exemplares ativos.";
            lista.append(item);
            return;
        }

        exemplares.forEach((exemplar) => {
            const item = document.createElement("li");
            const disponivel = idsDisponiveis.has(String(exemplar.id));
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

async function carregarEmprestimos() {
    mensagemEmprestimos.hidden = true;
    listaEmprestimos.replaceChildren();

    try {
        const [respostaUsuarios, respostaLivros, respostaEmprestimos] =
            await Promise.all([
                fetch("/usuarios"),
                fetch("/livros"),
                fetch("/emprestimos/ativos")
            ]);

        for (const resposta of [
            respostaUsuarios,
            respostaLivros,
            respostaEmprestimos
        ]) {
            if (!resposta.ok) {
                throw new Error(await mensagemDeErroDaResposta(resposta));
            }
        }

        const [usuarios, livros, emprestimos] = await Promise.all([
            respostaUsuarios.json(),
            respostaLivros.json(),
            respostaEmprestimos.json()
        ]);

        const exemplaresPorLivro = await Promise.all(
            livros.map(async (livro) => {
                const resposta = await fetch(
                    `/livros/${livro.id}/exemplares/disponiveis`
                );

                if (!resposta.ok) {
                    throw new Error(await mensagemDeErroDaResposta(resposta));
                }

                return resposta.json();
            })
        );

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
        exemplaresPorLivro.flat().forEach((exemplar) => {
            campoExemplarEmprestimo.add(new Option(
                `${exemplar.livro.nome} — patrimônio ${exemplar.codigoPatrimonio}`,
                exemplar.id
            ));
        });

        if (emprestimos.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Nenhum empréstimo ativo.";
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
    botaoHistoricoEmprestimos.textContent = listaHistoricoEmprestimos.hidden
        ? "Consultar histórico"
        : "Ocultar histórico";

    if (!listaHistoricoEmprestimos.hidden) {
        await carregarHistoricoEmprestimos();
    }
});

filtroHistoricoEmprestimos.addEventListener("change", renderizarHistoricoEmprestimos);
filtroSituacaoEmprestimos.addEventListener("change", renderizarHistoricoEmprestimos);

async function carregarHistoricoEmprestimos() {
    listaHistoricoEmprestimos.replaceChildren();

    try {
        const resposta = await fetch("/emprestimos");
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        historicoEmprestimos = await resposta.json();
        const usuarios = new Map();
        historicoEmprestimos.forEach((emprestimo) => {
            usuarios.set(String(emprestimo.usuario.id), emprestimo.usuario.nome);
        });

        const usuarioSelecionado = filtroHistoricoEmprestimos.value;
        filtroHistoricoEmprestimos.replaceChildren(
            new Option("Todos os usuários", "")
        );
        [...usuarios.entries()]
            .sort(([, nomeA], [, nomeB]) => nomeA.localeCompare(nomeB, "pt-BR"))
            .forEach(([usuarioId, nome]) => {
                filtroHistoricoEmprestimos.add(
                    new Option(`${nome} (ID ${usuarioId})`, usuarioId)
                );
            });
        filtroHistoricoEmprestimos.value = usuarios.has(usuarioSelecionado)
            ? usuarioSelecionado
            : "";
        filtroHistoricoEmprestimos.disabled = usuarios.size === 0;
        const situacoesDisponiveis = new Set(
            historicoEmprestimos.map((emprestimo) => emprestimo.situacao)
        );
        [...filtroSituacaoEmprestimos.options].forEach((opcao) => {
            opcao.disabled = Boolean(opcao.value) &&
                !situacoesDisponiveis.has(opcao.value);
        });
        filtroSituacaoEmprestimos.disabled = situacoesDisponiveis.size === 0;

        renderizarHistoricoEmprestimos();
    } catch (erro) {
        mostrarMensagemEmprestimos(
            `Falha ao carregar histórico de empréstimos: ${erro.message}`
        );
    }
}

function renderizarHistoricoEmprestimos() {
    listaHistoricoEmprestimos.replaceChildren();
    const usuarioId = filtroHistoricoEmprestimos.value;
    const emprestimos = usuarioId
        ? historicoEmprestimos.filter(
            (emprestimo) => String(emprestimo.usuario.id) === usuarioId
        )
        : historicoEmprestimos;
    const situacaoSelecionada = filtroSituacaoEmprestimos.value;
    const emprestimosFiltrados = situacaoSelecionada
        ? emprestimos.filter(
            (emprestimo) => emprestimo.situacao === situacaoSelecionada
        )
        : emprestimos;

    if (emprestimosFiltrados.length === 0) {
        const item = document.createElement("li");
        item.textContent = historicoEmprestimos.length === 0
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

    emprestimosFiltrados.forEach((emprestimo) => {
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

async function carregarEmprestimosAtrasados() {
    listaEmprestimosAtrasados.replaceChildren();

    try {
        const resposta = await fetch("/emprestimos/atrasados");
        if (!resposta.ok) {
            throw new Error(await mensagemDeErroDaResposta(resposta));
        }

        const emprestimos = await resposta.json();
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

campoUsuarioMultas.addEventListener("change", carregarMultasDoUsuario);

async function carregarMultasDoUsuario() {
    const usuarioId = campoUsuarioMultas.value;
    listaMultas.replaceChildren();
    listaPagamentos.replaceChildren();
    saldoMultas.textContent = "";
    mensagemMultas.hidden = true;

    if (!usuarioId) {
        return true;
    }

    try {
        const [respostaSaldo, respostaEmprestimos, respostaPagamentos] =
            await Promise.all([
                fetch(`/usuarios/${usuarioId}/multas/saldo-pendente`),
                fetch(`/emprestimos/usuario/${usuarioId}`),
                fetch(`/usuarios/${usuarioId}/pagamentos`)
            ]);

        for (const resposta of [
            respostaSaldo,
            respostaEmprestimos,
            respostaPagamentos
        ]) {
            if (!resposta.ok) {
                throw new Error(await mensagemDeErroDaResposta(resposta));
            }
        }

        const [saldo, emprestimos, pagamentos] = await Promise.all([
            respostaSaldo.json(),
            respostaEmprestimos.json(),
            respostaPagamentos.json()
        ]);
        saldoMultas.textContent = `Saldo total de multas pendentes: ${formatarMoeda(saldo)}`;

        const saldosPorEmprestimo = await Promise.all(
            emprestimos.map(async (emprestimo) => {
                const resposta = await fetch(
                    `/emprestimos/${emprestimo.id}/pagamentos/saldo`
                );
                if (!resposta.ok) {
                    throw new Error(await mensagemDeErroDaResposta(resposta));
                }

                return {
                    emprestimo,
                    saldo: Number(await resposta.json())
                };
            })
        );

        const multasPendentes = saldosPorEmprestimo.filter(
            ({ saldo: saldoEmprestimo }) => saldoEmprestimo > 0
        );
        if (multasPendentes.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Este usuário não possui multas pendentes.";
            listaMultas.append(item);
        }

        multasPendentes.forEach(({ emprestimo, saldo: saldoEmprestimo }) => {
            const item = document.createElement("li");
            item.append(document.createTextNode(
                `Empréstimo ${emprestimo.id} — ${emprestimo.exemplar.livro.nome}; ` +
                `patrimônio ${emprestimo.exemplar.codigoPatrimonio}; ` +
                `multa pendente: ${formatarMoeda(saldoEmprestimo)} `
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
            campoValor.max = saldoEmprestimo.toFixed(2);
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
                    emprestimo.id,
                    campoValor.value,
                    campoData.value
                );
            });
            item.append(formulario);
            listaMultas.append(item);
        });

        if (pagamentos.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Nenhum pagamento registrado.";
            listaPagamentos.append(item);
            return true;
        }

        pagamentos.forEach((pagamento) => {
            const item = document.createElement("li");
            item.textContent =
                `${pagamento.dataPagamento} — ${formatarMoeda(pagamento.valor)} ` +
                `(empréstimo ${pagamento.emprestimo.id})`;
            listaPagamentos.append(item);
        });
        return true;
    } catch (erro) {
        mostrarMensagemMultas(`Falha ao consultar multas: ${erro.message}`);
        return false;
    }
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