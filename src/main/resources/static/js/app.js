console.log("JavaScript is loaded");

const botoes = document.querySelectorAll("nav button");
const titulo = document.querySelector("main h2");
const descricao = document.querySelector("main p");
const descricoesPorSecao = {
    "Usuários": "Aqui você poderá consultar e cadastrar usuários.",
    "Livros e exemplares": "Aqui você poderá consultar livros e exemplares.",
    "Empréstimos": "Aqui você poderá acompanhar os empréstimos.",
    "Multas e pagamentos": "Aqui você poderá consultar multas e pagamentos."
};
const secaoUsuarios = document.querySelector("#secao-usuarios");
const listaUsuarios = document.querySelector("#lista-usuarios");
const formUsuario = document.querySelector("#form-usuario");
const mensagemForm = document.querySelector("#mensagem-form");

console.log(botoes.length);
console.log(titulo);

formUsuario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    mensagemForm.textContent = "";
    mensagemForm.hidden = true;

    const dados = Object.fromEntries(new FormData(formUsuario));
    dados.ativo = true;

    try {
        const resposta = await fetch("/usuarios", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        if (!resposta.ok) {
            const detalhe = await resposta.json();
            throw new Error(detalhe.erro || `Erro HTTP: ${resposta.status}`);
        }

        formUsuario.reset();
        await carregarUsuarios();
        mensagemForm.textContent = "Usuário cadastrado com sucesso.";
        mensagemForm.hidden = false;

        } catch (erro) {
        console.error("Não foi possível cadastrar o usuário:", erro);

        mensagemForm.textContent = `Falha ao cadastrar: ${erro.message}`;
        mensagemForm.hidden = false;
    }
});

botoes.forEach((botao) => {
    botao.addEventListener("click", () => {
        titulo.textContent = botao.textContent;
        descricao.textContent = descricoesPorSecao[botao.textContent.trim()];
        botoes.forEach((item) => item.classList.remove("ativo"));
        botao.classList.add("ativo");
        secaoUsuarios.hidden = botao.textContent.trim() !== "Usuários";
        if (!secaoUsuarios.hidden) {
            carregarUsuarios();
        }
    });
});

async function carregarUsuarios() {
    listaUsuarios.replaceChildren();

    try {
        const resposta = await fetch("/usuarios");

        if (!resposta.ok) {
            throw new Error(`Erro HTTP: ${resposta.status}`);
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
            item.textContent = `${usuario.id} — ${usuario.nome} (${usuario.email})`;

            const botaoDesativar = document.createElement("button");
            botaoDesativar.type = "button";
            botaoDesativar.textContent = "Desativar";


            botaoDesativar.addEventListener("click", async () => {
                const confirmar = window.confirm(`Desativar o usuário ${usuario.nome}?`);

                if (!confirmar) {
                    return;
                }
                try {
                    const resposta = await fetch(`/usuarios/${usuario.id}`, {
                        method: "DELETE"
                    });

                    if (!resposta.ok) {
                        throw new Error(`Erro HTTP: ${resposta.status}`);
                    }

                    await carregarUsuarios();
                    mensagemForm.textContent = "Usuário desativado.";
                    mensagemForm.hidden = false;
                } catch (erro) {
                    mensagemForm.textContent = `Falha ao desativar: ${erro.message}`;
                    mensagemForm.hidden = false;
                }
            });

            item.append(" ", botaoDesativar);

            listaUsuarios.append(item);


        });
    } catch (erro) {
        console.error("Não foi possível carregar os usuários:", erro);

        const item = document.createElement("li");
        item.textContent = "Não foi possível carregar os usuários. Verifique se a aplicação está ativa e tente novamente.";
        listaUsuarios.append(item);
    }
}