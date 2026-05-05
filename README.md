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


# 📋 Gerenciamento de Tarefas — Documentação Completa

Sistema full-stack de gerenciamento de tarefas com controle de acesso por perfil (Admin/Usuário), notificações automáticas, alocação múltipla de pessoas e autenticação via Google OAuth2.

---

## 🗂️ Índice

1. [Visão Geral da Aplicação](#-visão-geral-da-aplicação)
2. [Funcionalidades Desenvolvidas](#-funcionalidades-desenvolvidas)
3. [Tecnologias e Bibliotecas — Back-end](#-tecnologias-e-bibliotecas--back-end)
4. [Tecnologias e Bibliotecas — Front-end](#-tecnologias-e-bibliotecas--front-end)
5. [Arquitetura e Relacionamento entre as Tecnologias](#-arquitetura-e-relacionamento-entre-as-tecnologias)
6. [Fluxo da Aplicação — Back-end](#-fluxo-da-aplicação--back-end)
7. [Fluxo da Aplicação — Front-end](#-fluxo-da-aplicação--front-end)
8. [Como o Angular se conecta com o Back-end](#-como-o-angular-se-conecta-com-o-back-end)
9. [Endpoints da API REST](#-endpoints-da-api-rest)
10. [Variáveis de Ambiente](#-variáveis-de-ambiente)
11. [Como executar o projeto](#-como-executar-o-projeto)

---

## 🌐 Visão Geral da Aplicação

A aplicação é dividida em dois projetos independentes que se comunicam via HTTP:

| Camada     | Tecnologia        | Porta padrão |
|------------|-------------------|--------------|
| Back-end   | Spring Boot (Java 11) | 8090     |
| Front-end  | Angular 13        | 4200         |
| Banco de dados | PostgreSQL    | 5432         |

O **back-end** expõe uma API REST protegida por JWT (Google OAuth2). O **front-end** Angular consome essa API, injetando automaticamente o token JWT em todas as requisições HTTP via um interceptor HTTP.

---

## ✅ Funcionalidades Desenvolvidas

### Módulo de Departamentos
- Criar, listar, editar e remover departamentos
- Reordenar departamentos por drag-and-drop (ordem de apresentação persistida no banco)
- Listar departamentos com a quantidade de pessoas e tarefas associadas
- Visualizar as pessoas de um departamento específico

### Módulo de Pessoas
- Criar, listar, editar e remover pessoas
- Cada pessoa pertence a um departamento
- Listagem com nome, departamento e total de horas gastas em tarefas
- Busca por nome e período com cálculo de média de horas por tarefa
- Reordenar pessoas por drag-and-drop
- Filtrar pessoas por departamento (usado na alocação de tarefas)
- Cadastro de e-mail por pessoa (necessário para notificações)

### Módulo de Tarefas
- Criar, listar, editar e remover tarefas, vinculadas a um departamento
- Alocar **múltiplas pessoas** em uma mesma tarefa (multi-alocação via tabela `tarefa_alocacao`)
- Desalocar pessoas individualmente de uma tarefa
- Finalizar tarefa (marca como concluída com data de conclusão)
- Listar as 3 tarefas mais antigas sem pessoa alocada (pendentes)
- Listar tarefas em andamento e contagem por status
- Listar e contar tarefas com prazo vencido
- Reordenar tarefas por drag-and-drop
- Verificação de regras de negócio: não alocar em tarefas vencidas, não realocar pessoa já alocada, exigir mesmo departamento

### Módulo de Usuário (Painel do Colaborador)
- Login com Google (perfil de usuário comum)
- Visualizar somente as tarefas em que o usuário está alocado
- Iniciar uma tarefa (muda status para "em andamento")
- Enviar mensagens/dúvidas ao administrador vinculadas a uma tarefa
- Excluir as próprias mensagens
- Solicitar conclusão antecipada de uma tarefa (notifica o admin para aprovação)
- Responder notificações de prazo vencido com duas opções: A) Precisar de mais prazo ou B) Não conseguir executar a tarefa

### Módulo de Admin (Painel do Administrador)
- Login com Google (perfil exclusivo para o e-mail do admin configurado)
- Dashboard com contagem total de pessoas, tarefas, departamentos, tarefas em andamento, pendentes e concluídas
- Listar e responder mensagens pendentes de usuários
- Excluir mensagens respondidas ou não respondidas
- Visualizar tarefas com prazo vencido
- Prorrogar prazo de uma tarefa vencida (notifica as pessoas alocadas)
- Editar dados de uma tarefa (título, descrição, prazo)
- Encerrar forçadamente uma tarefa vencida (desaloca todos os colaboradores e notifica cada um)
- Visualizar e aprovar solicitações de conclusão antecipada enviadas pelos usuários
- Receber notificações automáticas de mensagens e prazo vencido

### Sistema de Notificações
- Notificações em tempo real no painel do admin e do usuário
- Lembrete automático D-1: notificação para todos os alocados quando a tarefa vence amanhã (executado a cada 2 horas via scheduler)
- Notificação automática de prazo vencido: enviada uma única vez para o colaborador e para o admin quando o prazo expira (executado a cada hora)
- Notificação ao prorrogar prazo: avisa os alocados sobre o novo prazo
- Notificação ao encerrar tarefa: avisa os desalocados sobre o encerramento
- Marcar notificação como lida
- Notificações tipadas (`PRAZO_VENCIDO`, `CONCLUSAO_PENDENTE`) para tratamento diferenciado no front-end

### Autenticação e Segurança
- Autenticação via Google Sign-In (OAuth2 / OpenID Connect)
- O back-end valida o JWT emitido pelo Google via JWK Set URI
- Perfis de acesso: `ROLE_ADMIN` (e-mail configurado no servidor) e `ROLE_USER` (todos os demais)
- Todos os endpoints protegidos por Spring Security; apenas `OPTIONS` é público
- CORS configurado globalmente no back-end

---

## ⚙️ Tecnologias e Bibliotecas — Back-end

### Linguagem e Plataforma
| Tecnologia | Versão | Papel |
|---|---|---|
| **Java** | 11 (OpenJDK) | Linguagem de programação |
| **Maven** | 3.x | Gerenciador de dependências e build |

### Framework Principal
| Biblioteca | Versão | Papel |
|---|---|---|
| **Spring Boot** | 2.5.4 | Framework base; configura automaticamente o servidor embarcado (Tomcat), o contexto Spring e as dependências |
| **Spring Boot Starter Web** | 2.5.4 | Habilita o servidor HTTP, o mapeamento REST (`@RestController`, `@RequestMapping`) e o Jackson para serialização JSON |
| **Spring Boot Starter Data JPA** | 2.5.4 | Integração com JPA/Hibernate para acesso ao banco de dados via repositórios |
| **Spring Boot Starter Security** | 2.5.4 | Segurança HTTP: filtros de autenticação, autorização e CORS |
| **Spring Boot Starter OAuth2 Resource Server** | 2.5.4 | Valida tokens JWT emitidos pelo Google, decodifica claims e constrói o contexto de segurança |
| **Spring Boot DevTools** | 2.5.4 | Hot reload em desenvolvimento |

### Banco de Dados e ORM
| Biblioteca | Versão | Papel |
|---|---|---|
| **PostgreSQL Driver** | runtime | Driver JDBC para conexão com o banco PostgreSQL |
| **Hibernate Core** | 5.6.5.Final | Implementação JPA que mapeia entidades Java para tabelas do banco e gera/executa as queries SQL |
| **javax.servlet-api** | 4.0.1 | API de servlet (compatível com Spring Boot 2.5.x) |

### Produtividade
| Biblioteca | Papel |
|---|---|
| **Lombok** | Gera automaticamente getters, setters, construtores (`@Data`, `@NoArgsConstructor`, `@RequiredArgsConstructor`) e logging (`@Slf4j`) em tempo de compilação, eliminando boilerplate |

### Testes
| Biblioteca | Papel |
|---|---|
| **Spring Boot Starter Test** | JUnit 5 + Mockito + Spring Test para testes de unidade e integração |

---

## 🖥️ Tecnologias e Bibliotecas — Front-end

### Linguagem e Plataforma
| Tecnologia | Versão | Papel |
|---|---|---|
| **TypeScript** | ^4.5.5 | Superset do JavaScript com tipagem estática |
| **Node.js / npm** | — | Ambiente de execução e gerenciador de pacotes |

### Framework Principal
| Biblioteca | Versão | Papel |
|---|---|---|
| **Angular** | ~13.1.0 | Framework SPA (Single Page Application); gerencia componentes, rotas, injeção de dependências e comunicação HTTP |
| **@angular/core** | ~13.1.0 | Núcleo do Angular: decoradores, injeção de dependência, ciclo de vida dos componentes |
| **@angular/common** | ~13.1.0 | Diretivas comuns (`*ngIf`, `*ngFor`), pipes e `HttpClient` |
| **@angular/router** | ~13.1.0 | Roteamento SPA com lazy loading dos módulos Admin e Usuário |
| **@angular/forms** | ~13.1.0 | Formulários reativos e baseados em template |
| **@angular/animations** | ~13.1.0 | Animações de interface |
| **@angular/platform-browser** | ~13.1.0 | Integração com o DOM do navegador |

### UI e Componentes Visuais
| Biblioteca | Versão | Papel |
|---|---|---|
| **@angular/material** | ^13.3.9 | Biblioteca de componentes Material Design: botões, diálogos (`MatDialog`), tabelas, campos de formulário, badges de notificação e muito mais |
| **@angular/cdk** | ^13.3.9 | Component Dev Kit: base para drag-and-drop (`CdkDragDrop`) utilizado na reordenação de tarefas, pessoas e departamentos |

### Feedback ao Usuário
| Biblioteca | Versão | Papel |
|---|---|---|
| **ngx-toastr** | ^13.1.0 | Exibição de mensagens de toast (sucesso, erro, informação) após operações na API |

### Comunicação Assíncrona
| Biblioteca | Versão | Papel |
|---|---|---|
| **RxJS** | ~7.4.0 | Programação reativa com Observables; usado em chamadas HTTP, `BehaviorSubject` para estado compartilhado entre componentes e `tap` para efeitos colaterais |

### Ferramentas de Build
| Ferramenta | Versão | Papel |
|---|---|---|
| **@angular/cli** | ^16.0.0 | CLI do Angular para servir, compilar e gerar código |
| **@angular-devkit/build-angular** | ~13.1.3 | Webpack + esbuild para bundling e otimização |
| **esbuild** | ^0.14.0 | Bundler de alta performance usado no build de produção |

---

## 🏛️ Arquitetura e Relacionamento entre as Tecnologias

```
┌─────────────────────────────────────────────────────────────────┐
│                        NAVEGADOR (Browser)                       │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   Angular 13 (SPA)                       │    │
│  │                                                          │    │
│  │  Google Sign-In JS SDK ──► AuthService                   │    │
│  │         │                      │                         │    │
│  │         │ JWT Token             │ armazena token         │    │
│  │         │                      ▼                         │    │
│  │         │               localStorage                     │    │
│  │         │                      │                         │    │
│  │         │               AuthInterceptor                  │    │
│  │         │               (injeta Bearer Token)            │    │
│  │         │                      │                         │    │
│  │  Components ◄──► Services ──► HttpClient ──────────────► │    │
│  │  (Admin/Usuário)  (Angular)    (RxJS/HTTP)              │    │
│  │                                                          │    │
│  └──────────────────────────────────┬───────────────────────┘    │
│                                     │ HTTP/REST (JSON)            │
└─────────────────────────────────────┼───────────────────────────┘
                                      │
                       ┌──────────────▼────────────────┐
                       │     Spring Boot (Java 11)       │
                       │                                 │
                       │  SecurityConfig (Spring Security)│
                       │  ├─ CORS Filter                 │
                       │  ├─ JWT Validation (Google JWK) │
                       │  └─ Role extraction (ADMIN/USER)│
                       │                                 │
                       │  Controllers (REST)             │
                       │  ├─ TarefaController            │
                       │  ├─ PessoaController            │
                       │  ├─ DepartamentoController      │
                       │  ├─ NotificacaoController       │
                       │  ├─ AdminController             │
                       │  └─ UserController              │
                       │                                 │
                       │  Services (Regras de Negócio)  │
                       │  ├─ TarefaService               │
                       │  ├─ PessoaService               │
                       │  ├─ DepartamentoService         │
                       │  └─ NotificacaoService          │
                       │                                 │
                       │  Repositories (JPA/Hibernate)   │
                       │                                 │
                       │  Scheduler (Spring @Scheduled)  │
                       │  └─ LembreteTarefaScheduler     │
                       │                                 │
                       └──────────────┬──────────────────┘
                                      │ JPA / JDBC
                       ┌──────────────▼──────────────────┐
                       │         PostgreSQL               │
                       │  Tabelas:                        │
                       │  ├─ departamento                 │
                       │  ├─ pessoa                       │
                       │  ├─ tarefa                       │
                       │  ├─ tarefa_alocacao              │
                       │  ├─ mensagem                     │
                       │  └─ notificacao                  │
                       └─────────────────────────────────┘
```

### Como as tecnologias se relacionam

**Google OAuth2 → Spring Security → Angular:**
O Google emite um JWT (token ID) quando o usuário faz login via o botão do Google Sign-In. O Angular armazena esse token no `localStorage`. O `AuthInterceptor` injeta o token no cabeçalho `Authorization: Bearer <token>` de todas as requisições HTTP. O Spring Boot recebe a requisição, a `SecurityConfig` extrai e valida o JWT consultando os certificados públicos do Google (`jwk-set-uri`). Em seguida, lê o campo `email` do token e atribui `ROLE_ADMIN` (se for o e-mail configurado em `admin.email`) ou `ROLE_USER` para qualquer outro e-mail válido.

**Angular → HttpClient → API REST:**
Os serviços Angular (`TarefaService`, `PessoaService`, etc.) usam o `HttpClient` para realizar chamadas HTTP ao back-end Spring Boot. As respostas chegam como JSON e são mapeadas para os modelos TypeScript (`Tarefa`, `Pessoa`, `Departamento`). O RxJS gerencia o fluxo assíncrono via `Observable`.

**Spring Boot → Hibernate → PostgreSQL:**
Os Controllers delegam a lógica para os Services. Os Services usam os Repositories (interfaces JPA) para operações CRUD. O Hibernate traduz as chamadas JPA em SQL e se comunica com o PostgreSQL via JDBC.

**Angular CDK → Spring Boot (reordenação):**
Quando o usuário arrasta e solta um item da lista, o CDK Drag-and-Drop recalcula as posições. O Angular então chama endpoints como `PUT /tarefas/salvarTarefaOrder` enviando a lista com os novos valores de `ordem_apresentacao`. O back-end persiste a nova ordem no banco.

---

## 🔄 Fluxo da Aplicação — Back-end

### 1. Inicialização
O Spring Boot sobe o Tomcat embarcado na porta `8090` (ou `$PORT` no ambiente de produção). O `DatabaseSchemaMaintenance` executa `@PostConstruct` para ajustar colunas legadas no PostgreSQL. O `LembreteTarefaScheduler` é registrado para execução periódica.

### 2. Recebimento de uma requisição HTTP
```
Requisição HTTP ──► Filtro CORS (SecurityConfig) ──► Filtro JWT (Spring Security)
     ──► Verificação de Role (hasRole) ──► Controller ──► Service ──► Repository ──► PostgreSQL
                                                                              │
                                                                    Resposta JSON ◄── DTO
```

### 3. Autenticação e autorização
- Toda requisição (exceto `OPTIONS`) passa pelo filtro de autenticação JWT.
- O `JwtAuthenticationConverter` lê o claim `email` do token e define o principal e os papéis.
- O `GlobalExceptionHandler` captura exceções (`EntityNotFoundException` → 404, `AccessDeniedException` → 403, `IllegalStateException` → 409) e retorna JSON de erro padronizado.

### 4. Lógica de negócio no Service
Os Services implementam as regras:
- Validação de campos obrigatórios
- Verificação de unicidade (título da tarefa, nome da pessoa, título do departamento)
- Verificação de departamento ao alocar (pessoa e tarefa devem ter o mesmo departamento)
- Bloqueio de alocação em tarefas vencidas ou finalizadas
- Criação de notificações após eventos (alocação, mensagem, prazo vencido, prorrogação, encerramento)

### 5. Agendamento automático (`LembreteTarefaScheduler`)
- A cada **2 horas**: busca tarefas com prazo amanhã e envia notificações para todos os alocados (lembrete D-1).
- A cada **1 hora**: busca tarefas vencidas e não finalizadas. Para cada uma que ainda não foi notificada, envia mensagem ao colaborador (com as opções A/B) e notificação de alerta ao admin. Marca o campo `notificacaoVencimentoEnviada = true` para evitar reenvio.

---

## 🔄 Fluxo da Aplicação — Front-end

### 1. Inicialização e roteamento
O Angular carrega o `AppModule` e o `AppRoutingModule`. A rota raiz exibe o `HomeSelectorComponent` (seleção de perfil). Os módulos `AdminModule` e `UsuarioModule` são carregados de forma lazy (somente quando a rota é acessada).

### 2. Autenticação (Google Sign-In)
```
Usuário clica em "Entrar com Google"
    ──► AuthService.initGoogleSignIn() / initUserSignIn()
    ──► Google SDK exibe popup de login
    ──► Google retorna JWT (credential)
    ──► AuthService decodifica o payload (parseJwt)
    ──► Token salvo em localStorage ('admin_token' ou 'user_token')
    ──► Router navega para o dashboard correspondente
```

### 3. Guards de rota
- `AdminGuard`: verifica `AuthService.isAdminLoggedIn()` antes de ativar a rota `/admin`.
- `UsuarioGuard`: verifica `AuthService.isUserLoggedIn()` antes de ativar a rota `/usuario`.
- Se não autenticado, redireciona para a tela de login.

### 4. Comunicação com o back-end
```
Componente chama Service Angular
    ──► Service usa HttpClient
    ──► AuthInterceptor injeta { Authorization: Bearer <token> }
    ──► Requisição HTTP chega ao back-end Spring Boot
    ──► Resposta JSON retorna como Observable
    ──► Componente assina (.subscribe()) e atualiza o estado da tela
    ──► ngx-toastr exibe feedback visual ao usuário
```

### 5. Estado e notificações frontend
- `NotificationService` (frontend): gerencia um contador de ações locais via `BehaviorSubject` (ex: "adicionou tarefa").
- `DashboardStateService`: compartilha estado entre componentes do dashboard via `BehaviorSubject`.
- `LoadingInterceptor` + `LoadingService`: exibe/oculta indicador de carregamento durante as requisições HTTP.

### 6. Drag-and-drop (CDK)
Quando o usuário solta um card em nova posição:
1. `CdkDragDrop` recalcula o array local.
2. O componente chama o `ReordenacaoService`.
3. O service envia `PUT /tarefas/salvarTarefaOrder` (ou equivalente para pessoas/departamentos) com a lista reordenada.

---

## 🔌 Como o Angular se conecta com o Back-end

### 1. Configuração da URL base
Em `src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8090',
  googleClientId: 'SEU_CLIENT_ID',
  adminEmail: 'SEU_EMAIL@gmail.com'
};
```
Em produção, `environment.prod.ts` aponta para a URL do servidor hospedado (ex: Render.com).

### 2. HttpClient nos Services
Cada service Angular monta a URL completa a partir de `environment.apiUrl`:
```typescript
// TarefaService
public CONTROLLER = environment.apiUrl + '/tarefas';

getAllTarefa(): Observable<any> {
  return this.http.get(this.CONTROLLER + '/getAllTarefa', { observe: 'response' });
}
```

### 3. AuthInterceptor — Injeção automática do JWT
```typescript
// auth.interceptor.ts
intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
  const token = this.auth.getToken() || this.auth.getUserToken();
  if (token) {
    const authReq = req.clone({
      setHeaders: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json; charset=utf-8'
      }
    });
    return next.handle(authReq);
  }
  return next.handle(req);
}
```
Este interceptor é registrado em `AppModule` como `HTTP_INTERCEPTORS` com `multi: true`, garantindo que **toda** requisição HTTP carregue o token automaticamente.

### 4. CORS no back-end
O Spring Boot permite requisições do Angular com a configuração em `SecurityConfig`:
```java
config.setAllowedOriginPatterns(List.of("*"));
config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
config.setAllowedHeaders(List.of("*"));
```
Em produção, a variável de ambiente `CORS_ALLOWED_ORIGINS` restringe as origens permitidas.

### 5. Módulos lazy-loaded e guard de autenticação
```
/ (HomeSelectorComponent)
├── /admin  (lazy: AdminModule)
│   ├── AdminGuard → verifica isAdminLoggedIn()
│   ├── /admin/login  (AdminLoginComponent)
│   └── /admin/dashboard  (AdminDashboardComponent)
└── /usuario  (lazy: UsuarioModule)
    ├── UsuarioGuard → verifica isUserLoggedIn()
    ├── /usuario/login  (UsuarioLoginComponent)
    └── /usuario/dashboard  (UsuarioDashboardComponent)
```

---

## 📡 Endpoints da API REST

### Departamentos (`/departamentos`)
| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/departamentos/salvarDepartamento` | Criar departamento |
| `GET` | `/departamentos` | Listar com qtd. de pessoas e tarefas |
| `GET` | `/departamentos/getAllDepartamento` | Listar todos |
| `PUT` | `/departamentos/alterarDepartamento/{titulo}` | Editar departamento |
| `PUT` | `/departamentos/salvarDepartamentoOrder` | Salvar nova ordem |
| `DELETE` | `/departamentos/removerDepartamento/{id}` | Remover departamento |

### Pessoas (`/pessoas`)
| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/pessoas/salvarPessoa` | Criar pessoa |
| `GET` | `/pessoas/getAllPessoa` | Listar todas as pessoas |
| `GET` | `/pessoas/gastos` | Busca por nome/período com média de horas |
| `GET` | `/pessoas/getPessoasDepartamentos/{departamentoId}` | Pessoas de um departamento |
| `PUT` | `/pessoas/alterarPessoa/{nome}` | Editar pessoa |
| `PUT` | `/pessoas/salvarPessoaOrder` | Salvar nova ordem |
| `DELETE` | `/pessoas/removerPessoa/{id}` | Remover pessoa |

### Tarefas (`/tarefas`)
| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/tarefas/salvarTarefa` | Criar tarefa |
| `GET` | `/tarefas/getAllTarefa` | Listar todas as tarefas |
| `GET` | `/tarefas/pendentes` | 3 tarefas sem alocação mais antigas |
| `GET` | `/tarefas/emAndamento` | Tarefas em andamento |
| `GET` | `/tarefas/contagemEmAndamento` | Contagem de tarefas em andamento |
| `GET` | `/tarefas/vencidas` | Tarefas com prazo vencido |
| `GET` | `/tarefas/vencidas/count` | Contagem de tarefas vencidas |
| `PUT` | `/tarefas/alocar/{tarefaId}/{pessoaId}` | Alocar pessoa em tarefa |
| `PUT` | `/tarefas/finalizar/{tarefaId}` | Finalizar tarefa |
| `PUT` | `/tarefas/alterarTarefa/{titulo}` | Editar tarefa |
| `PUT` | `/tarefas/salvarTarefaOrder` | Salvar nova ordem |
| `DELETE` | `/tarefas/removerTarefa/{id}` | Remover tarefa |
| `DELETE` | `/tarefas/desalocar/{tarefaId}/{pessoaId}` | Desalocar pessoa |

### Admin (`/admin`) — `ROLE_ADMIN`
| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/admin/dashboard` | Totais do dashboard |
| `GET` | `/admin/mensagens/pendentes` | Mensagens não respondidas |
| `GET` | `/admin/tarefas/vencidas` | Tarefas vencidas (visão admin) |
| `PUT` | `/admin/mensagem/{mensagemId}/responder` | Responder mensagem de usuário |
| `PUT` | `/admin/tarefa/{tarefaId}/prorrogar` | Prorrogar prazo da tarefa |
| `PUT` | `/admin/tarefa/{tarefaId}/editar` | Editar tarefa como admin |
| `PUT` | `/admin/tarefa/{tarefaId}/encerrar` | Encerrar tarefa vencida |
| `DELETE` | `/admin/mensagem/{mensagemId}` | Excluir mensagem |

### Usuário (`/usuario`) — `ROLE_USER` ou `ROLE_ADMIN`
| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/usuario/minhas-tarefas` | Tarefas do usuário logado |
| `POST` | `/usuario/tarefa/{id}/mensagem` | Enviar mensagem ao admin |
| `POST` | `/usuario/tarefa/{tarefaId}/notificar-conclusao` | Solicitar aprovação de conclusão |
| `POST` | `/usuario/notificacao/{notifId}/responder-vencimento` | Responder notificação de prazo vencido (opção A ou B) |
| `PUT` | `/usuario/iniciar-tarefa/{tarefaId}` | Iniciar tarefa |
| `DELETE` | `/usuario/mensagem/{msgId}` | Excluir própria mensagem |

### Notificações (`/notificacoes`) — `ROLE_USER` ou `ROLE_ADMIN`
| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/notificacoes/pendentes` | Notificações não lidas do usuário logado |
| `GET` | `/notificacoes/conclusao-pendentes` | Notificações de conclusão pendente (admin) |
| `PUT` | `/notificacoes/ler/{id}` | Marcar notificação como lida |
| `PUT` | `/notificacoes/aprovar-conclusao/{id}` | Aprovar conclusão antecipada |

---

## 🔧 Variáveis de Ambiente

Configure as seguintes variáveis de ambiente no servidor (ex: Render.com):

| Variável | Descrição | Exemplo |
|---|---|---|
| `JDBC_DATABASE_URL` | URL completa do banco PostgreSQL | `jdbc:postgresql://host:5432/dbname?user=u&password=p` |
| `PORT` | Porta do servidor HTTP | `8090` |
| `ADMIN_EMAIL` | E-mail do administrador do sistema | `admin@empresa.com` |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas para CORS | `https://seuapp.vercel.app` |
| `MAIL_USERNAME` | E-mail para envio de e-mails (SMTP) | `noreply@gmail.com` |
| `MAIL_PASSWORD` | Senha de app do Gmail | `xxxx xxxx xxxx xxxx` |

---

## 🚀 Como executar o projeto

### Back-end

**Pré-requisitos:** Java 11, Maven 3.x, PostgreSQL rodando

```bash
# Clone o repositório
git clone <url-do-repo>
cd gerenciamento-tarefas-back-end

# Configure o banco no application.properties (desenvolvimento local)
# spring.datasource.url=jdbc:postgresql://localhost:5432/desafio
# spring.datasource.username=postgres
# spring.datasource.password=SUA_SENHA

# Execute
./mvnw spring-boot:run
# ou no Windows:
mvnw.cmd spring-boot:run
```

O servidor sobe em `http://localhost:8090`.

### Front-end

**Pré-requisitos:** Node.js 16+, Angular CLI

```bash
cd gerenciamento_tarefas_angular

# Instale as dependências
npm install

# Execute em modo de desenvolvimento
npm start
# ou
ng serve
```

O front-end sobe em `http://localhost:4200` e se comunica com o back-end em `http://localhost:8090`.

### Build de produção (Front-end)
```bash
npm run build
# Os arquivos gerados estarão em /dist/projeto
```

### Docker (Back-end)
```bash
# Build da imagem
docker build -t gerenciamento-tarefas-api .

# Execute com variáveis de ambiente
docker run -p 8090:8090 \
  -e JDBC_DATABASE_URL="jdbc:postgresql://host:5432/db?user=u&password=p" \
  -e ADMIN_EMAIL="admin@email.com" \
  -e CORS_ALLOWED_ORIGINS="http://localhost:4200" \
  gerenciamento-tarefas-api
```

---

## 🏗️ Estrutura do Projeto — Back-end

```
src/main/java/com/desafio/
├── config/
│   ├── SecurityConfig.java          # CORS, JWT validation, roles
│   ├── GlobalExceptionHandler.java  # Tratamento centralizado de erros
│   └── DatabaseSchemaMaintenance.java # Ajustes de schema pós-boot
├── controllers/
│   ├── TarefaController.java
│   ├── PessoaController.java
│   ├── DepartamentoController.java
│   ├── NotificacaoController.java
│   ├── AdminController.java
│   └── UserController.java
├── service/
│   ├── TarefaService.java
│   ├── PessoaService.java
│   ├── DepartamentoService.java
│   └── NotificacaoService.java
├── model/
│   ├── Tarefa.java
│   ├── Pessoa.java
│   ├── Departamento.java
│   ├── TarefaAlocacao.java
│   ├── Mensagem.java
│   └── Notificacao.java
├── repository/
│   ├── TarefaRepository.java
│   ├── PessoaRepository.java
│   ├── DepartamentoRepository.java
│   ├── TarefaAlocacaoRepository.java
│   ├── MensagemRepository.java
│   └── NotificacaoRepository.java
├── view/
│   ├── TarefaDTO.java
│   ├── PessoaDTO.java
│   └── DepartamentoDTO.java
├── scheduler/
│   └── LembreteTarefaScheduler.java
└── DesafioApplication.java
```

---

## 🏗️ Estrutura do Projeto — Front-end

```
src/app/
├── admin/
│   ├── admin-login/           # Tela de login do admin
│   ├── admin-dashboard/       # Painel principal do admin
│   ├── admin-routing.module.ts
│   └── admin.module.ts
├── usuario/
│   ├── usuario-login/         # Tela de login do usuário
│   ├── usuario-dashboard/     # Painel principal do usuário
│   ├── usuario-routing.module.ts
│   └── usuario.module.ts
├── components/
│   ├── dashboard/             # Dashboard com listas de tarefas/pessoas
│   ├── adicionar-tarefa/      # Formulário de tarefa
│   ├── adicionar-pessoa/      # Formulário de pessoa
│   ├── adicionar-departamento/# Formulário de departamento
│   ├── alocar-pessoa-tarefa/  # Modal de alocação
│   ├── departamento-pessoas/  # Pessoas por departamento
│   ├── dialog-finalizar-tarefa/
│   ├── dialog-pessoa-tarefa/
│   ├── confirmacao-dialog/    # Diálogos de confirmação
│   ├── alert-modal/
│   ├── home-selector/         # Seleção de perfil
│   ├── theme-toggle/          # Toggle dark/light mode
│   └── footer-component/
├── services/
│   ├── auth.service.ts        # Google Sign-In e gestão de tokens
│   ├── tarefa.service.ts
│   ├── pessoa.service.ts
│   ├── departamento.service.ts
│   ├── admin.service.ts
│   ├── usuario.service.ts
│   ├── alocacao.service.ts
│   ├── notification.service.ts
│   ├── loading.service.ts
│   ├── theme.service.ts
│   ├── alert-modal.service.ts
│   ├── dashboard-state.service.ts
│   ├── dashboard-dialog.service.ts
│   └── reordenacao.service.ts
├── interceptors/
│   ├── auth.interceptor.ts    # Injeta JWT em todas as requests
│   └── loading.interceptor.ts
├── guards/
│   ├── admin.guard.ts
│   └── usuario.guard.ts
├── model/
│   ├── Tarefa.model.ts
│   ├── Pessoa.model.ts
│   └── Departamento.model.ts
└── environments/
    ├── environment.ts         # Dev: apiUrl, googleClientId
    └── environment.prod.ts    # Prod: URLs de produção
```

---

**Versão do Java:** OpenJDK 11 | **IDE:** Spring Tool Suite 4 / IntelliJ IDEA


###### Executar o projeto ####################################################

"C:\Program Files\apache-maven-3.9.12\bin\mvn" clean spring-boot:run