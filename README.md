# Cadastro e empréstimo de livros

API REST simples para cadastrar usuários e livros e controlar empréstimos, devoluções e multas.

## Requisitos

- Java 25
- Maven (ou o Maven Wrapper incluído no projeto)

## Como executar

Na pasta do projeto, execute:

```bash
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`. A API inicia, por padrão, em `http://localhost:8080`.

Para executar os testes:

```bash
./mvnw test
```

## Como a lógica funciona

- **Usuários** são cadastrados com nome, e-mail e CPF.
- **Livros** são cadastrados pelo nome. Um livro está disponível quando não tem empréstimo ativo.
- **Empréstimos** relacionam um livro a um usuário e guardam as datas do empréstimo, da devolução prevista e da devolução efetiva.
- Ao emprestar, o sistema verifica se o livro e o usuário existem e impede um novo empréstimo se o livro já estiver emprestado.
- O prazo padrão é de **7 dias**. Depois do prazo, a multa calculada é de **R$ 2,00 por dia de atraso**.
- A devolução é feita pelo ID do empréstimo. Um empréstimo continua ativo enquanto sua data de devolução estiver vazia.
- Um livro com qualquer empréstimo no histórico não pode ser excluído. Um usuário não pode ser excluído enquanto tiver empréstimo ativo; empréstimos antigos também podem impedir a exclusão por estarem relacionados no banco.

## Endpoints

Todos os endpoints usam JSON, exceto os parâmetros indicados na URL.

### Usuários

| Método | Caminho | Ação |
|---|---|---|
| `GET` | `/usuarios` | Lista usuários |
| `GET` | `/usuarios/{id}` | Busca um usuário |
| `POST` | `/usuarios` | Cadastra um usuário |
| `PUT` | `/usuarios/{id}` | Atualiza nome, e-mail e CPF |
| `DELETE` | `/usuarios/{id}` | Exclui um usuário |

Exemplo de cadastro:

```json
{
  "nome": "Ana Silva",
  "email": "ana@example.com",
  "cpf": "12345678900"
}
```

### Livros

| Método | Caminho | Ação |
|---|---|---|
| `GET` | `/livros` | Lista todos os livros |
| `GET` | `/livros/disponiveis` | Lista livros sem empréstimo ativo |
| `POST` | `/livros` | Cadastra um livro |
| `DELETE` | `/livros/{livroId}` | Exclui um livro sem histórico de empréstimos |

Exemplo de cadastro:

```json
{
  "nome": "O Hobbit"
}
```

### Empréstimos

| Método | Caminho | Ação |
|---|---|---|
| `GET` | `/emprestimos` | Lista todos os empréstimos |
| `GET` | `/emprestimos/usuario/{usuarioId}` | Lista empréstimos de um usuário |
| `POST` | `/emprestimos?livroId={livroId}&usuarioId={usuarioId}` | Empresta um livro |
| `POST` | `/emprestimos/{emprestimoId}/devolucao` | Registra a devolução |

A criação do empréstimo recebe os IDs como parâmetros de consulta, por exemplo:

```text
POST http://localhost:8080/emprestimos?livroId=1&usuarioId=1
```

## Estrutura do projeto

- `controller`: recebe as requisições HTTP e define os caminhos da API.
- `service`: aplica as regras de negócio.
- `repository`: consulta e grava dados usando Spring Data JPA.
- `entities`: define as entidades `Usuario` e `LivrosModel` persistidas no banco.
- `emprestimos`: define a entidade `Emprestimo` e as regras de prazo, devolução e multa.

## Banco de dados

O projeto usa H2 em memória. Os dados são perdidos quando a aplicação é encerrada. Durante a execução, o console do H2 está disponível em `http://localhost:8080/h2-console`, com JDBC URL `jdbc:h2:mem:cadastro`, usuário `sa` e senha vazia.

## Erros da API

O tratamento centralizado fica em `controller/ApiExceptHandler.java`. Em geral, registros não encontrados retornam `404`, dados ou operações inválidas retornam `400`, e conflitos de estado retornam `409`.
