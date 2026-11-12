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

## Interface web

Ao iniciar a aplicação, a interface estática fica disponível em `http://localhost:8080/`.
Ela apresenta seções para usuários, livros e exemplares, empréstimos e multas.

A seção de usuários permite cadastrar, listar, editar, desativar e reativar usuários.
A seção de livros e exemplares ainda não está totalmente integrada à interface; para
essas operações, use os endpoints da API descritos abaixo. As seções de empréstimos
e multas também são apenas informativas na interface.

Para executar os testes:

```bash
./mvnw test
```

## Como a lógica funciona

- **Usuários** são cadastrados com nome, e-mail e CPF.
- **Livros** são cadastrados pelo nome e podem ter vários exemplares. Um livro está disponível quando possui ao menos um exemplar ativo que não está emprestado.
- **Empréstimos** relacionam um exemplar a um usuário e guardam as datas do empréstimo, da devolução prevista e da devolução efetiva.
- Ao emprestar, o sistema verifica se o exemplar, o livro e o usuário estão ativos e impede um novo empréstimo do mesmo exemplar enquanto ele já estiver emprestado.
- O prazo padrão é de **7 dias**. Depois do prazo, a multa calculada é de **R$ 2,00 por dia de atraso**.
- A devolução é feita pelo ID do empréstimo. Empréstimos têm situação (`ATIVO`, `DEVOLVIDO` ou `RENOVADO`); apenas os ativos podem ser devolvidos ou renovados.
- A renovação deve ser feita no dia previsto para a devolução. Ela encerra o empréstimo atual e cria um novo empréstimo com prazo de 7 dias.
- Pagamentos de multa são registrados por empréstimo. O saldo corresponde à multa calculada menos os pagamentos já registrados; pagamentos parciais são aceitos.
- Usuários, livros e exemplares podem ser desativados e reativados. Um livro com qualquer empréstimo no histórico não pode ser excluído. Desativar um usuário não apaga seus empréstimos; um empréstimo ativo ainda pode ser devolvido.

## Endpoints

Todos os endpoints usam JSON, exceto os parâmetros indicados na URL.

### Usuários

| Método | Caminho | Ação |
|---|---|---|
| `GET` | `/usuarios` | Lista usuários |
| `GET` | `/usuarios/inativos` | Lista usuários desativados |
| `GET` | `/usuarios/{id}` | Busca um usuário |
| `POST` | `/usuarios` | Cadastra um usuário |
| `PUT` | `/usuarios/{id}` | Atualiza nome, e-mail e CPF |
| `DELETE` | `/usuarios/{id}` | Desativa um usuário |
| `PATCH` | `/usuarios/{id}/ativacao` | Reativa um usuário |
| `GET` | `/usuarios/{usuarioId}/multas/saldo-pendente` | Consulta o saldo de multas pendentes do usuário |
| `GET` | `/usuarios/{usuarioId}/pagamentos` | Lista pagamentos de multa do usuário |

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
| `GET` | `/livros` | Lista livros ativos |
| `GET` | `/livros/disponiveis` | Lista livros com exemplar disponível |
| `GET` | `/livros/inativos` | Lista livros desativados |
| `POST` | `/livros` | Cadastra um livro |
| `PATCH` | `/livros/{livroId}/desativacao` | Desativa um livro |
| `PATCH` | `/livros/{livroId}/ativacao` | Reativa um livro |
| `DELETE` | `/livros/{livroId}` | Exclui um livro sem histórico de empréstimos |

Exemplo de cadastro:

```json
{
  "nome": "O Hobbit"
}
```

### Exemplares

| Método | Caminho | Ação |
|---|---|---|
| `POST` | `/livros/{livroId}/exemplares?codigoPatrimonio={codigo}` | Cadastra um exemplar para o livro |
| `GET` | `/livros/{livroId}/exemplares/ativos` | Lista exemplares ativos |
| `GET` | `/livros/{livroId}/exemplares/inativos` | Lista exemplares desativados |
| `GET` | `/livros/{livroId}/exemplares/disponiveis` | Lista exemplares disponíveis para empréstimo |
| `PATCH` | `/livros/{livroId}/exemplares/{exemplarId}/desativacao` | Desativa um exemplar que não esteja emprestado |
| `PATCH` | `/livros/{livroId}/exemplares/{exemplarId}/ativacao` | Reativa um exemplar |

### Empréstimos

| Método | Caminho | Ação |
|---|---|---|
| `GET` | `/emprestimos` | Lista todos os empréstimos |
| `GET` | `/emprestimos/ativos` | Lista empréstimos ativos |
| `GET` | `/emprestimos/atrasados` | Lista empréstimos ativos em atraso |
| `GET` | `/emprestimos/usuario/{usuarioId}` | Lista empréstimos de um usuário |
| `GET` | `/emprestimos/usuario/{usuarioId}/atrasados` | Lista empréstimos em atraso de um usuário |
| `GET` | `/emprestimos/usuario/{usuarioId}/multa-total` | Consulta a multa total calculada para os empréstimos do usuário |
| `POST` | `/emprestimos?exemplarId={exemplarId}&usuarioId={usuarioId}` | Empresta um exemplar |
| `POST` | `/emprestimos/{emprestimoId}/devolucao` | Registra a devolução |
| `POST` | `/emprestimos/{emprestimoId}/renovacao` | Renova um empréstimo na data prevista para devolução |

A criação do empréstimo recebe o ID do exemplar e o ID do usuário como parâmetros de consulta, por exemplo:

```text
POST http://localhost:8080/emprestimos?exemplarId=1&usuarioId=1
```

### Pagamentos de multa

Os pagamentos são associados a um empréstimo. A data deve ser enviada no formato ISO `AAAA-MM-DD`, e `valorPago` deve ser um valor positivo que não ultrapasse o saldo atual da multa.

| Método | Caminho | Ação |
|---|---|---|
| `GET` | `/emprestimos/{emprestimoId}/pagamentos` | Lista pagamentos do empréstimo, ordenados por data |
| `GET` | `/emprestimos/{emprestimoId}/pagamentos/saldo` | Consulta o saldo da multa do empréstimo |
| `POST` | `/emprestimos/{emprestimoId}/pagamentos?valorPago={valor}&dataDePagamento={data}` | Registra um pagamento de multa |

Exemplo de pagamento de R$ 5,00:

```text
POST http://localhost:8080/emprestimos/1/pagamentos?valorPago=5.00&dataDePagamento=2026-10-29
```

O cadastro retorna `201 Created`. O pagamento não pode ter valor nulo ou não positivo, exceder o saldo da multa ou ter data futura. Empréstimo inexistente retorna `404`; dados inválidos retornam `400`.

## Estrutura do projeto

- `controller`: recebe as requisições HTTP e define os caminhos da API.
- `service`: concentra as regras de empréstimo, renovação, devolução, cálculo de multa e pagamentos.
- `repository`: consulta e grava dados usando Spring Data JPA.
- `entities`: define as entidades persistidas, incluindo usuários, livros, exemplares, empréstimos e pagamentos de multa.

## Banco de dados

O projeto usa H2 em memória. Os dados são perdidos quando a aplicação é encerrada. Durante a execução, o console do H2 está disponível em `http://localhost:8080/h2-console`, com JDBC URL `jdbc:h2:mem:cadastro`, usuário `sa` e senha vazia.

## Erros da API

O tratamento centralizado fica em `controller/ApiExceptHandler.java`. Em geral, registros não encontrados retornam `404`, dados ou operações inválidas retornam `400`, e conflitos de estado retornam `409`.
