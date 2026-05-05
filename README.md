<h2>Informações sobre o desafio</h2>

O desafio foi realizado na linguagem de programação JAVA (Java 11), usando o Spring Boot e o PostgreSQL como Banco de dados



<h2>Versão do JAVA</h2>

openjdk version "11.0.16.1" 2022-08-12 LTS



<h2>IDE utilizado</h2>

Spring Tool Suite 4



<h2>application.properties</h2>

Eu fiz algumas configurações para poder executar o projeto, como:

spring.datasource.url=jdbc:postgresql://localhost:5433/desafio
- Localmente a porta padrão pela qual o PostgreSQL está acessível é: 5432, mas estou usando a porta 5433 neste projeto.
- O nome do banco de dados que criei é: desafio


spring.datasource.username=postgres
- Este é o nome de usuário do banco de dados


spring.datasource.password=19handyrio
- Aqui está a senha que eu utilizei para acessar o banco de dados


server.port = 8090
- Aqui está a porta onde será executada o Spring Boot. O padrão é 8080, mas utilizei a porta 8090



<h2>Requisições</h2>

As requisições GET, POST, PUT e DELETE foram feitas utilizando o INSOMNIA 


Aqui, vou colocar as rotas para realizar todas as requisições:


# Gerenciamento de Tarefas — Back-end

API REST para gerenciamento de tarefas, pessoas e departamentos, com autenticação via Google OAuth2.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 11 |
| Framework | Spring Boot 2.5.4 |
| Segurança | Spring Security + OAuth2 Resource Server (Google JWT) |
| Banco de dados | PostgreSQL |
| ORM | Hibernate 5 / Spring Data JPA |
| Migração de banco | **Flyway** |
| Documentação | **Springdoc OpenAPI (Swagger UI)** |
| Containerização | Docker |
| Deploy | Render |

---

## Funcionalidades principais

- **Tarefas** — criar, listar, editar, reordenar, alocar pessoas, finalizar e remover
- **Pessoas** — CRUD completo com cálculo de horas totais trabalhadas
- **Departamentos** — CRUD com listagem de quantidades por departamento
- **Notificações** — sistema de notificações por e-mail (scheduler, lembretes de prazo)
- **Admin** — painel exclusivo com dashboard, mensagens e gestão de tarefas vencidas
- **Autenticação** — Google OAuth2 com roles USER e ADMIN

---

## Documentação da API (Swagger)

Com a aplicação rodando, acesse:

```
http://localhost:8090/swagger-ui.html
```

Em produção (Render):

```
https://<seu-servico>.onrender.com/swagger-ui.html
```

A documentação lista todos os endpoints com exemplos de request/response e permite testar diretamente via token JWT Bearer.

---

## Controle de versão do banco (Flyway)

O projeto usa **Flyway** para gerenciar o schema do banco de dados de forma versionada e segura.

Os scripts ficam em:

```
src/main/resources/db/migration/
  V1__schema_inicial.sql     ← estado atual do banco (todas as tabelas)
  V2__...                    ← próximas alterações (ex: nova coluna, índice)
```

**Regra:** nunca edite um script já aplicado. Para qualquer alteração no banco, crie um novo arquivo `V{n}__descricao.sql`.

---

## Como rodar localmente

### Pré-requisitos

- Java 11+
- Maven 3.8+
- PostgreSQL rodando localmente (ou Docker)

### Variáveis de ambiente necessárias

Crie um arquivo `.env` ou configure as variáveis no sistema:

```env
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/gerenciamento_tarefas
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=sua-senha-de-app
ADMIN_EMAIL=email-do-admin@gmail.com
CORS_ALLOWED_ORIGINS=http://localhost:4200
PORT=8090
```

> **Atenção:** Para `MAIL_PASSWORD`, use uma [senha de aplicativo do Gmail](https://support.google.com/accounts/answer/185833), não a senha normal da conta.

### Executar

```bash
# Clonar o repositório
git clone https://github.com/<seu-usuario>/gerenciamento-tarefas-back-end.git
cd gerenciamento-tarefas-back-end

# Compilar e rodar
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8090`.

---

## Como rodar com Docker

```bash
# Build da imagem
docker build -t gerenciamento-tarefas-api .

# Executar (ajuste as variáveis conforme seu ambiente)
docker run -p 8090:8090 \
  -e JDBC_DATABASE_URL="jdbc:postgresql://host.docker.internal:5432/gerenciamento_tarefas" \
  -e MAIL_USERNAME="seu-email@gmail.com" \
  -e MAIL_PASSWORD="sua-senha-de-app" \
  -e ADMIN_EMAIL="admin@gmail.com" \
  -e CORS_ALLOWED_ORIGINS="http://localhost:4200" \
  gerenciamento-tarefas-api
```

---

## Estrutura do projeto

```
src/
├── main/
│   ├── java/com/desafio/
│   │   ├── config/          # SecurityConfig, SwaggerConfig, CorsConfig, GlobalExceptionHandler
│   │   ├── controllers/     # TarefaController, PessoaController, DepartamentoController, AdminController...
│   │   ├── model/           # Entidades JPA: Tarefa, Pessoa, Departamento, Notificacao...
│   │   ├── repository/      # Interfaces Spring Data JPA
│   │   ├── scheduler/       # LembreteTarefaScheduler (notificações automáticas)
│   │   ├── service/         # Regras de negócio: TarefaService, PessoaService...
│   │   └── view/            # DTOs de resposta
│   └── resources/
│       ├── db/migration/    # Scripts Flyway (V1__, V2__...)
│       └── application.properties
└── test/
    └── java/com/desafio/
        └── controllers/     # Testes de integração
```

---

## Endpoints principais

| Método | Endpoint | Descrição | Role |
|---|---|---|---|
| `POST` | `/tarefas/salvarTarefa` | Criar tarefa | USER |
| `GET` | `/tarefas/getAllTarefa` | Listar tarefas | USER |
| `PUT` | `/tarefas/alocar/{tarefaId}/{pessoaId}` | Alocar pessoa | USER |
| `PUT` | `/tarefas/finalizar/{tarefaId}` | Finalizar tarefa | USER |
| `DELETE` | `/tarefas/removerTarefa/{id}` | Remover tarefa | USER |
| `GET` | `/tarefas/pendentes` | Tarefas sem alocação | USER |
| `GET` | `/tarefas/vencidas` | Tarefas vencidas | USER |
| `GET` | `/pessoas` | Listar pessoas | USER |
| `POST` | `/pessoas/salvarPessoa` | Criar pessoa | USER |
| `GET` | `/departamentos` | Listar departamentos | USER |
| `POST` | `/departamentos/salvarDepartamento` | Criar departamento | USER |
| `GET` | `/admin/dashboard` | Estatísticas gerais | ADMIN |
| `GET` | `/admin/mensagens/pendentes` | Mensagens sem resposta | ADMIN |
| `PUT` | `/admin/tarefa/{id}/prorrogar` | Prorrogar prazo | ADMIN |
| `GET` | `/notificacoes` | Notificações do usuário | USER |

> Consulte o Swagger para a documentação completa de todos os endpoints.

---

## Autenticação

A API usa **Google OAuth2** com tokens JWT Bearer.

1. O front-end realiza o login com Google e obtém um ID Token
2. O token é enviado no header `Authorization: Bearer <token>` em cada requisição
3. O back-end valida o token nas chaves públicas do Google (`jwk-set-uri`)
4. O role é determinado pelo e-mail: se for o `ADMIN_EMAIL` configurado → ADMIN, caso contrário → USER

---

## Deploy (Render)

O projeto está configurado para deploy automático no **Render** via Dockerfile.

Variáveis de ambiente necessárias no Render:
- `JDBC_DATABASE_URL` — injetada automaticamente pelo banco PostgreSQL vinculado
- `MAIL_USERNAME`, `MAIL_PASSWORD` — credenciais Gmail
- `ADMIN_EMAIL` — e-mail do administrador
- `CORS_ALLOWED_ORIGINS` — URL do front-end em produção

---

## Melhorias implementadas recentemente

- **Swagger / OpenAPI** — documentação interativa da API em `/swagger-ui.html`
- **Flyway** — controle de versão do banco, substituindo o `ddl-auto=update`
- **Multi-alocação de pessoas** — uma tarefa pode ter várias pessoas alocadas (`tarefa_alocacao`)
- **Scheduler de lembretes** — notificações automáticas de tarefas vencendo em 2 dias
- **Painel admin** — dashboard com estatísticas, gestão de tarefas vencidas e sistema de mensagens