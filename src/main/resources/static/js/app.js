const botoesNavegacao = document.querySelectorAll("nav button");
const titulo = document.querySelector("main h2");
const descricao = document.querySelector("main p");
const secaoUsuarios = document.querySelector("#secao-usuarios");

const formUsuario = document.querySelector("#form-usuario");
const listaUsuarios = document.querySelector("#lista-usuarios");
const mensagemForm = document.querySelector("#mensagem-form");

const botaoInativos = document.querySelector("#botao-inativos");
const listaInativos = document.querySelector("#lista-inativos");

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

const descricoesPorSecao = {
    "Usuários": "Aqui você poderá consultar e cadastrar usuários.",
    "Livros e exemplares": "Aqui você poderá consultar livros e exemplares.",
    "Empréstimos": "Aqui você poderá acompanhar os empréstimos.",
    "Multas e pagamentos": "Aqui você poderá consultar multas e pagamentos."
};

function mostrarMensagem(texto) {
    mensagemForm.textContent = texto;
    mensagemForm.hidden = false;
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

        if (!secaoUsuarios.hidden) {
            carregarUsuarios();
        }

        if (!secaoLivros.hidden) {
            carregarLivros();
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

        const usuarios = await resposta.json();

        if (usuarios.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Nenhum usuário cadastrado.";
            listaUsuarios.append(item);
            return;
        }

        usuarios.forEach((usuario) => {
            const item = document.createElement("li");
            item.append(document.createTextNode(
                `${usuario.id} — ${usuario.nome} (${usuario.email}) `
            ));

            const botaoEditar = document.createElement("button");
            botaoEditar.type = "button";
            botaoEditar.textContent = "Editar";
            botaoEditar.addEventListener("click", () => editarUsuario(usuario.id));

            const botaoDesativar = document.createElement("button");
            botaoDesativar.type = "button";
            botaoDesativar.textContent = "Desativar";
            botaoDesativar.addEventListener("click", () => desativarUsuario(usuario));

            item.append(botaoEditar, " ", botaoDesativar);
            listaUsuarios.append(item);
        });
    } catch (erro) {
        mostrarMensagem(`Falha ao carregar usuários: ${erro.message}`);
    }
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

        const usuarios = await resposta.json();

        if (usuarios.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Nenhum usuário inativo.";
            listaInativos.append(item);
            return;
        }

        usuarios.forEach((usuario) => {
            const item = document.createElement("li");
            item.append(document.createTextNode(
                `${usuario.id} — ${usuario.nome} (${usuario.email}) `
            ));

            const botaoReativar = document.createElement("button");
            botaoReativar.type = "button";
            botaoReativar.textContent = "Reativar";
            botaoReativar.addEventListener("click", () => reativarUsuario(usuario));

            item.append(botaoReativar);
            listaInativos.append(item);
        });
    } catch (erro) {
        mostrarMensagem(`Falha ao carregar usuários inativos: ${erro.message}`);
    }
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
    mensagemLivros.textContent = texto;
    mensagemLivros.hidden = false;
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

        if (livros.length === 0) {
            const item = document.createElement("li");
            item.textContent = "Nenhum livro cadastrado.";
            listaLivros.append(item);
            return;
        }

        livros.forEach((livro) => {
            const item = document.createElement("li");
            const tituloLivro = document.createElement("h4");
            tituloLivro.textContent = `${livro.id} — ${livro.nome}`;

            const formularioExemplar = document.createElement("form");
            const rotulo = document.createElement("label");
            const campoCodigo = document.createElement("input");
            const botaoCadastrar = document.createElement("button");
            const listaExemplares = document.createElement("ul");

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

            item.append(
                tituloLivro,
                formularioExemplar,
                botaoVerExemplares,
                listaExemplares
            );
            listaLivros.append(item);
        });
    } catch (erro) {
        mostrarMensagemLivros(`Falha ao carregar livros: ${erro.message}`);
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
        const resposta = await fetch(`/livros/${livroId}/exemplares/ativos`);

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
            item.textContent =
                `Exemplar ${exemplar.id} — patrimônio: ${exemplar.codigoPatrimonio}`;
            lista.append(item);
        });
    } catch (erro) {
        mostrarMensagemLivros(`Falha ao carregar exemplares: ${erro.message}`);
    }
}