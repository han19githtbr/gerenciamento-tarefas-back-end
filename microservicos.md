# Guia Completo: Migração para Micro-serviços
### Aplicação: Gerenciamento de Tarefas (Spring Boot + Angular)

---

## Índice

1. [Arquitetura Atual (Monolito)](#1-arquitetura-atual-monolito)
2. [O que são Micro-serviços?](#2-o-que-são-micro-serviços)
3. [Comparativo: Monolito vs Micro-serviços](#3-comparativo-monolito-vs-micro-serviços)
4. [Mapa da Arquitetura de Micro-serviços](#4-mapa-da-arquitetura-de-micro-serviços)
5. [Os Micro-serviços Propostos](#5-os-micro-serviços-propostos)
6. [Passo a Passo da Migração](#6-passo-a-passo-da-migração)
7. [Tecnologias Recomendadas](#7-tecnologias-recomendadas)
8. [Desafios e Como Superá-los](#8-desafios-e-como-superá-los)
9. [Conclusão](#9-conclusão)

---

## 1. Arquitetura Atual (Monolito)

A aplicação atual é um **monolito modular** composto por:

```
Frontend (Angular)
        │
        ▼  HTTP REST
Backend (Spring Boot) ──────────────────────────────────────────────┐
  ├── controllers/                                                    │
  │     ├── AdminController.java        ← gestão administrativa      │
  │     ├── TarefaController.java       ← CRUD de tarefas            │
  │     ├── PessoaController.java       ← gestão de usuários         │
  │     ├── DepartamentoController.java ← gestão de departamentos    │
  │     ├── NotificacaoController.java  ← notificações               │
  │     └── UserController.java         ← ações do usuário comum     │
  │                                                                    │
  ├── service/                                                        │
  │     ├── TarefaService.java                                        │
  │     ├── PessoaService.java                                        │
  │     ├── DepartamentoService.java                                  │
  │     └── NotificacaoService.java                                   │
  │                                                                    │
  ├── model/ (Tarefa, Pessoa, Departamento, Notificacao, Mensagem)    │
  ├── scheduler/ (LembreteTarefaScheduler.java)                       │
  └── config/ (SecurityConfig, CorsConfig, GlobalExceptionHandler)   │
        │                                                              │
        ▼                                                              │
  PostgreSQL (banco único)  ◄─────────────────────────────────────────┘
```

**Características do monolito atual:**
- Um único processo JVM rodando tudo
- Um único banco de dados PostgreSQL compartilhado
- Autenticação via Google OAuth2 / JWT centralizada
- Envio de e-mail via SMTP integrado ao mesmo processo
- Deploy único no Render

---

## 2. O que são Micro-serviços?

Micro-serviços é um **estilo arquitetural** onde a aplicação é dividida em **serviços pequenos, independentes e autônomos**, cada um responsável por um único domínio de negócio.

Cada micro-serviço:
- Tem seu **próprio banco de dados** (isolamento de dados)
- É **implantado de forma independente** (deploy separado)
- Comunica-se com outros serviços via **API REST ou mensageria** (ex: RabbitMQ, Kafka)
- Pode ser **escalado individualmente** conforme a demanda

---

## 3. Comparativo: Monolito vs Micro-serviços

### Arquitetura Atual (Monolito)

| Aspecto | Situação Atual |
|---|---|
| **Deploy** | Um único JAR, deploy completo a cada mudança |
| **Escalabilidade** | Toda a aplicação escala junta (mesmo que só Tarefas esteja sobrecarregado) |
| **Banco de dados** | Um único PostgreSQL para todas as entidades |
| **Falha** | Se o serviço de e-mail falha, pode derrubar tudo |
| **Times** | Um único time precisa entender todo o código |
| **Tecnologia** | Preso a Java/Spring Boot para tudo |
| **Manutenção** | Simples no início; difícil conforme cresce |
| **Complexidade operacional** | Baixa — uma aplicação para monitorar |

### Arquitetura de Micro-serviços (Futura)

| Aspecto | Com Micro-serviços |
|---|---|
| **Deploy** | Cada serviço faz deploy independente, sem parar os outros |
| **Escalabilidade** | Escala apenas o serviço sobrecarregado (ex: só o de Tarefas) |
| **Banco de dados** | Cada serviço tem seu próprio banco isolado |
| **Falha** | Se Notificações falha, Tarefas continua funcionando |
| **Times** | Times independentes por domínio (squad de Tarefas, squad de Usuários) |
| **Tecnologia** | Liberdade: Notificações pode ser Node.js, Tarefas pode ser Java |
| **Manutenção** | Mais complexa operacionalmente, mas cada serviço é menor e focado |
| **Complexidade operacional** | Alta — múltiplos serviços, bancos, redes, monitoramento |

### Ganhos Concretos para esta Aplicação

**✅ Escalabilidade granular**
> Se no futuro houver muitas tarefas sendo criadas simultaneamente, você escala apenas o `ms-tarefas` sem desperdiçar recursos em outros domínios.

**✅ Resiliência e isolamento de falhas**
> Se o serviço de notificação por e-mail (`ms-notificacao`) cair, as tarefas, usuários e departamentos continuam funcionando normalmente.

**✅ Deploy sem downtime**
> Corrigir um bug no `ms-departamento` não exige derrubar toda a aplicação. Apenas aquele serviço é redeploy.

**✅ Times paralelos**
> Equipes diferentes podem trabalhar no `ms-tarefas` e no `ms-usuario` ao mesmo tempo, sem conflitos de merge no mesmo repositório.

**✅ Flexibilidade tecnológica**
> O serviço de notificação pode ser reescrito em Node.js (ótimo para I/O assíncrono e e-mails) enquanto o core de Tarefas continua em Java.

**✅ Evolução independente**
> Cada serviço tem seu próprio ciclo de vida de versão. O `ms-tarefas` pode ir para v2 enquanto o `ms-usuario` ainda está em v1.

---

## 4. Mapa da Arquitetura de Micro-serviços

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              CLIENTE                                                 │
│                    Angular (Frontend SPA)                                            │
│              Hospedado em CDN / Netlify / Vercel                                     │
└───────────────────────────────┬────────────────────────────────────────────────────┘
                                │  HTTPS
                                ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                           API GATEWAY                                                │
│                    (Spring Cloud Gateway / Kong)                                      │
│                                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │ Autenticação │  │  Rate Limit  │  │   Roteamento │  │     CORS     │            │
│  │  JWT / OAuth │  │  & Throttle  │  │  de Rotas    │  │  Centraliz.  │            │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘            │
└──────────┬──────────────┬──────────────┬──────────────┬─────────────────────────────┘
           │              │              │              │
     /tarefas/*     /usuarios/*    /deptos/*    /notificacoes/*
           │              │              │              │
           ▼              ▼              ▼              ▼
┌──────────────┐  ┌──────────────┐ ┌──────────────┐ ┌──────────────────┐
│ ms-tarefas   │  │ ms-usuario   │ │ ms-departa-  │ │ ms-notificacao   │
│              │  │              │ │ mento        │ │                  │
│ Spring Boot  │  │ Spring Boot  │ │ Spring Boot  │ │ Spring Boot /    │
│ Port: 8081   │  │ Port: 8082   │ │ Port: 8083   │ │ Node.js          │
│              │  │              │ │              │ │ Port: 8084       │
│ - CRUD       │  │ - Cadastro   │ │ - CRUD       │ │ - E-mail SMTP    │
│   tarefas    │  │   usuários   │ │   deptos     │ │ - Scheduler      │
│ - Alocações  │  │ - Perfil     │ │ - Vínculos   │ │ - Push/WebSocket │
│ - Mensagens  │  │ - Roles      │ │   pessoas    │ │ - Histórico      │
│ - Status     │  │ - Auth info  │ │              │ │                  │
└──────┬───────┘  └──────┬───────┘ └──────┬───────┘ └──────┬───────────┘
       │                 │                │                 │
       ▼                 ▼                ▼                 ▼
┌──────────────┐  ┌──────────────┐ ┌──────────────┐ ┌──────────────────┐
│  DB Tarefas  │  │  DB Usuários │ │  DB Deptos   │ │  DB Notificações │
│  PostgreSQL  │  │  PostgreSQL  │ │  PostgreSQL  │ │  PostgreSQL /    │
│              │  │              │ │              │ │  MongoDB         │
│  tarefa      │  │  pessoa      │ │  departamento│ │  notificacao     │
│  tarefa_     │  │  (roles,     │ │  (vínculos)  │ │  mensagem        │
│  alocacao    │  │  email,nome) │ │              │ │                  │
└──────────────┘  └──────────────┘ └──────────────┘ └──────────────────┘

                        │              │              │
                        └──────────────┴──────────────┘
                                       │
                    ┌──────────────────▼──────────────────┐
                    │         MESSAGE BROKER               │
                    │    RabbitMQ / Apache Kafka           │
                    │                                      │
                    │  Eventos assíncronos entre serviços: │
                    │  • TarefaCriada → ms-notificacao     │
                    │  • TarefaVencida → ms-notificacao    │
                    │  • UsuarioAlocado → ms-notificacao   │
                    └─────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│                       INFRAESTRUTURA DE SUPORTE                                      │
│                                                                                      │
│  ┌──────────────────┐  ┌─────────────────┐  ┌─────────────────────────────────┐    │
│  │ Service Discovery│  │  Config Server  │  │    Monitoramento & Logs         │    │
│  │  (Eureka /       │  │  (Spring Cloud  │  │  (Prometheus + Grafana +        │    │
│  │   Consul)        │  │   Config)       │  │   ELK Stack / Loki)             │    │
│  └──────────────────┘  └─────────────────┘  └─────────────────────────────────┘    │
│                                                                                      │
│  ┌──────────────────┐  ┌─────────────────┐                                         │
│  │  Circuit Breaker │  │  Distributed    │                                         │
│  │  (Resilience4j)  │  │  Tracing        │                                         │
│  │                  │  │  (Zipkin /      │                                         │
│  └──────────────────┘  │   Jaeger)       │                                         │
│                         └─────────────────┘                                         │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Os Micro-serviços Propostos

### 🟦 ms-tarefas (porta 8081)
**Responsabilidade:** Domínio completo de tarefas.

**Origem no código atual:** `TarefaController`, `TarefaService`, `TarefaRepository`, `TarefaAlocacaoRepository`, `MensagemRepository`, `LembreteTarefaScheduler`

**Entidades:** `Tarefa`, `TarefaAlocacao`, `Mensagem`

**Endpoints que herda:**
- `GET /tarefas` — listar todas
- `POST /tarefas` — criar tarefa
- `PUT /tarefas/{id}` — editar
- `DELETE /tarefas/{id}` — excluir
- `PUT /usuario/iniciar-tarefa/{id}` — iniciar
- `POST /usuario/tarefa/{id}/mensagem` — enviar mensagem
- `GET /admin/tarefas/vencidas` — listar vencidas

**Banco próprio:** tabelas `tarefa`, `tarefa_alocacao`, `mensagem`

---

### 🟩 ms-usuario (porta 8082)
**Responsabilidade:** Gestão de pessoas e autenticação.

**Origem no código atual:** `PessoaController`, `UserController` (parte de perfil), `PessoaService`, `PessoaRepository`

**Entidades:** `Pessoa` (nome, email, roles, departamento_id como referência)

**Endpoints que herda:**
- `GET /pessoas` — listar
- `POST /pessoas` — cadastrar
- `PUT /pessoas/{id}` — editar
- `DELETE /pessoas/{id}` — excluir
- `GET /usuario/minhas-tarefas` (delega para ms-tarefas via API interna)

**Banco próprio:** tabela `pessoa`

---

### 🟨 ms-departamento (porta 8083)
**Responsabilidade:** Gestão de departamentos e seus vínculos.

**Origem no código atual:** `DepartamentoController`, `DepartamentoService`, `DepartamentoRepository`

**Entidades:** `Departamento`

**Endpoints que herda:**
- `GET /departamentos` — listar
- `POST /departamentos` — criar
- `PUT /departamentos/{id}` — editar
- `DELETE /departamentos/{id}` — excluir

**Banco próprio:** tabela `departamento`

---

### 🟥 ms-notificacao (porta 8084)
**Responsabilidade:** Envio de e-mails, notificações in-app e agendamentos.

**Origem no código atual:** `NotificacaoController`, `NotificacaoService`, `NotificacaoRepository`, `LembreteTarefaScheduler`

**Entidades:** `Notificacao`

**Endpoints que herda:**
- `GET /notificacoes/pendentes`
- `PUT /notificacoes/ler/{id}`
- `PUT /notificacoes/aprovar-conclusao/{id}`
- `POST /usuario/notificacao/{id}/responder-vencimento`

**Comunicação assíncrona:** Consome eventos do `ms-tarefas` via RabbitMQ/Kafka:
- `TarefaVencidaEvent` → dispara e-mail de lembrete
- `TarefaCriadaEvent` → notifica usuário alocado
- `ConclusaoSolicitadaEvent` → notifica admin

**Banco próprio:** tabela `notificacao`

---

### 🔐 ms-auth (porta 8085) — opcional/embutido no gateway
**Responsabilidade:** Validação de tokens JWT / Google OAuth2.

**Origem no código atual:** `SecurityConfig`, configurações de OAuth2

Pode ser implementado como um filtro no próprio API Gateway ou como um serviço dedicado.

---

## 6. Passo a Passo da Migração

A migração deve seguir o padrão **"Strangler Fig"**: substituir partes do monolito gradualmente, sem reescrever tudo de uma vez.

---

### 🔵 FASE 0 — Preparação (Antes de Quebrar Qualquer Coisa)

**Duração estimada: 1–2 semanas**

#### Passo 0.1 — Adicionar testes de contrato no monolito atual
Antes de dividir, certifique-se de que os endpoints existentes têm testes funcionando. Eles vão garantir que a migração não quebra comportamentos.

```bash
# No monolito atual, rode os testes existentes
mvn test
```

#### Passo 0.2 — Mapear todas as dependências internas
Identifique onde os serviços se chamam diretamente. No código atual, o `TarefaService` acessa diretamente `PessoaRepository` e `DepartamentoRepository`. Isso precisa virar uma chamada HTTP ou evento.

#### Passo 0.3 — Criar o repositório mono-repo ou multi-repo
Decida a estrutura de repositórios:

```
# Opção A: Mono-repo (recomendado para equipes pequenas)
gerenciamento-tarefas/
├── ms-tarefas/
├── ms-usuario/
├── ms-departamento/
├── ms-notificacao/
├── ms-gateway/
└── infra/  (Docker Compose, K8s configs)

# Opção B: Multi-repo (um repositório por serviço)
```

---

### 🟡 FASE 1 — API Gateway (Sem mudar nada no backend ainda)

**Duração estimada: 1 semana**

#### Passo 1.1 — Criar o API Gateway com Spring Cloud Gateway

```xml
<!-- pom.xml do gateway -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

```yaml
# application.yml do gateway
spring:
  cloud:
    gateway:
      routes:
        - id: monolito
          uri: http://localhost:8090  # monolito atual
          predicates:
            - Path=/**
```

Neste ponto, o Angular passa a falar com o Gateway, que repassa tudo para o monolito. **Nenhuma mudança de comportamento ainda.**

#### Passo 1.2 — Mover autenticação JWT para o Gateway
O Gateway valida o token antes de repassar qualquer requisição. O monolito para de precisar validar JWT — apenas confia no header `X-User-Email` injetado pelo gateway.

---

### 🟠 FASE 2 — Extrair ms-notificacao (o mais isolado)

**Duração estimada: 2–3 semanas**

O serviço de notificação é o melhor para começar porque ele tem **menor acoplamento** com os outros domínios — ele basicamente recebe eventos e envia e-mails.

#### Passo 2.1 — Criar o projeto ms-notificacao

```bash
# Gerar via Spring Initializr ou copiar do monolito
spring init --dependencies=web,data-jpa,mail,postgresql ms-notificacao
```

#### Passo 2.2 — Criar banco de dados separado para notificações

```sql
-- Novo banco dedicado
CREATE DATABASE db_notificacao;

-- Migrar apenas a tabela notificacao
-- (use Flyway ou Liquibase para versionamento de schema)
```

#### Passo 2.3 — Adicionar RabbitMQ ao projeto

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

#### Passo 2.4 — Publicar eventos no ms-tarefas (monolito)

No `TarefaService` atual, ao criar/atualizar uma tarefa, publicar um evento:

```java
// Adicionar no TarefaService do monolito
@Autowired
private RabbitTemplate rabbitTemplate;

public TarefaDTO criarTarefa(Tarefa tarefa) {
    // lógica atual...
    Tarefa saved = tarefaRepository.save(tarefa);
    
    // NOVO: publicar evento para ms-notificacao
    rabbitTemplate.convertAndSend("tarefas.exchange", 
        "tarefa.criada", 
        new TarefaCriadaEvent(saved.getId(), saved.getTitulo(), emailUsuario));
    
    return saved.toDTO();
}
```

#### Passo 2.5 — ms-notificacao consome os eventos

```java
@RabbitListener(queues = "notificacao.tarefa-criada")
public void onTarefaCriada(TarefaCriadaEvent event) {
    // Envia e-mail de notificação
    emailService.enviarNotificacaoCriacao(event);
    // Salva notificação no próprio banco
    notificacaoRepository.save(new Notificacao(...));
}
```

#### Passo 2.6 — Atualizar rotas no Gateway

```yaml
routes:
  - id: ms-notificacao
    uri: http://ms-notificacao:8084
    predicates:
      - Path=/notificacoes/**
  - id: monolito
    uri: http://monolito:8090
    predicates:
      - Path=/**
```

**✅ Resultado:** ms-notificacao rodando de forma independente. O monolito ainda existe mas não cuida mais de notificações.

---

### 🔴 FASE 3 — Extrair ms-departamento

**Duração estimada: 1–2 semanas**

Departamentos têm baixo volume de operações e poucas dependências. É o segundo candidato.

#### Passo 3.1 — Criar ms-departamento com banco próprio

```sql
CREATE DATABASE db_departamento;
-- Migrar tabela departamento
```

#### Passo 3.2 — Substituir no monolito por chamadas HTTP

No `TarefaService`, onde hoje acessa `DepartamentoRepository` diretamente, substituir por:

```java
// ANTES (acesso direto ao banco)
Departamento depto = departamentoRepository.findById(deptoId).orElseThrow();

// DEPOIS (chamada HTTP para ms-departamento)
DepartamentoDTO depto = departamentoClient.findById(deptoId);
```

Usando `Feign Client`:
```java
@FeignClient(name = "ms-departamento", url = "${ms.departamento.url}")
public interface DepartamentoClient {
    @GetMapping("/departamentos/{id}")
    DepartamentoDTO findById(@PathVariable Long id);
}
```

#### Passo 3.3 — Atualizar o Gateway

```yaml
  - id: ms-departamento
    uri: http://ms-departamento:8083
    predicates:
      - Path=/departamentos/**
```

---

### 🟣 FASE 4 — Extrair ms-usuario

**Duração estimada: 2–3 semanas**

#### Passo 4.1 — Criar ms-usuario com banco próprio

```sql
CREATE DATABASE db_usuario;
-- Migrar tabela pessoa
```

#### Passo 4.2 — Resolver referências cruzadas

No ms-tarefas (quando for extraído), `Tarefa` referencia `Pessoa`. Em micro-serviços, **não há JOIN entre bancos**. A solução é:

**Opção A — Guardar dados denormalizados:**
```java
// Na tabela tarefa, guardar apenas o email e nome da pessoa
// (não a FK para o banco de usuários)
private String pessoaEmail;
private String pessoaNome;
```

**Opção B — Buscar via API quando necessário:**
```java
// Ao montar o TarefaDTO, consulta ms-usuario
PessoaDTO pessoa = usuarioClient.findByEmail(tarefa.getPessoaEmail());
```

---

### 🟤 FASE 5 — Extrair ms-tarefas (o core)

**Duração estimada: 3–4 semanas**

Este é o serviço mais complexo pois tem mais dependências.

#### Passo 5.1 — Criar ms-tarefas com banco próprio

```sql
CREATE DATABASE db_tarefas;
-- Migrar tabelas: tarefa, tarefa_alocacao, mensagem
```

#### Passo 5.2 — Substituir referências cruzadas

Todas as chamadas para `PessoaRepository` e `DepartamentoRepository` dentro do `TarefaService` viram chamadas HTTP para `ms-usuario` e `ms-departamento`.

#### Passo 5.3 — Desligar o monolito

Com todos os domínios extraídos, o monolito Spring Boot original é **aposentado**. O gateway agora roteia tudo para os micro-serviços.

---

### ⚙️ FASE 6 — Infraestrutura de Produção

**Duração estimada: 2–4 semanas**

#### Passo 6.1 — Docker e Docker Compose (desenvolvimento local)

```yaml
# docker-compose.yml
services:
  ms-gateway:
    build: ./ms-gateway
    ports: ["8080:8080"]
    
  ms-tarefas:
    build: ./ms-tarefas
    environment:
      - DB_URL=jdbc:postgresql://db-tarefas:5432/db_tarefas
    depends_on: [db-tarefas, rabbitmq]
    
  ms-usuario:
    build: ./ms-usuario
    environment:
      - DB_URL=jdbc:postgresql://db-usuario:5432/db_usuario
      
  ms-departamento:
    build: ./ms-departamento
    
  ms-notificacao:
    build: ./ms-notificacao
    
  db-tarefas:
    image: postgres:15
    environment:
      POSTGRES_DB: db_tarefas
      
  db-usuario:
    image: postgres:15
    
  db-departamento:
    image: postgres:15
    
  db-notificacao:
    image: postgres:15
    
  rabbitmq:
    image: rabbitmq:3-management
    ports: ["5672:5672", "15672:15672"]
```

#### Passo 6.2 — Kubernetes (produção)

Para produção com escala real, cada micro-serviço vira um `Deployment` no Kubernetes:

```yaml
# tarefa-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ms-tarefas
spec:
  replicas: 3  # 3 instâncias do ms-tarefas
  selector:
    matchLabels:
      app: ms-tarefas
  template:
    spec:
      containers:
      - name: ms-tarefas
        image: meu-registry/ms-tarefas:v1.2.0
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
```

#### Passo 6.3 — CI/CD Independente por serviço

Cada micro-serviço tem seu próprio pipeline:

```yaml
# .github/workflows/ms-tarefas.yml
on:
  push:
    paths:
      - 'ms-tarefas/**'  # só dispara quando ms-tarefas muda

jobs:
  build-and-deploy:
    steps:
      - run: mvn test
      - run: docker build -t ms-tarefas .
      - run: kubectl rollout restart deployment/ms-tarefas
```

---

## 7. Tecnologias Recomendadas

| Componente | Tecnologia Recomendada | Motivo |
|---|---|---|
| **API Gateway** | Spring Cloud Gateway | Já familiar com Spring; suporte nativo a JWT e Circuit Breaker |
| **Comunicação síncrona** | OpenFeign (REST) | Simples, bem integrado ao Spring Boot |
| **Comunicação assíncrona** | RabbitMQ | Mais simples que Kafka para este volume; suficiente para notificações |
| **Service Discovery** | Spring Eureka | Nativo do Spring Cloud; sem necessidade de Kubernetes inicialmente |
| **Config centralizado** | Spring Cloud Config | Centraliza `application.properties` de todos os serviços |
| **Circuit Breaker** | Resilience4j | Padrão moderno em Spring Boot; substitui o Hystrix |
| **Banco de dados** | PostgreSQL (um por serviço) | Já utilizado; isolar schemas por serviço |
| **Rastreamento distribuído** | Micrometer Tracing + Zipkin | Correlaciona logs entre todos os micro-serviços |
| **Monitoramento** | Prometheus + Grafana | Métricas de todos os serviços em dashboards centralizados |
| **Containerização** | Docker + Docker Compose | Desenvolvimento local; depois Kubernetes em produção |
| **CI/CD** | GitHub Actions | Pipelines independentes por micro-serviço |

---

## 8. Desafios e Como Superá-los

### 🔴 Desafio 1: Transações Distribuídas
**Problema:** No monolito, criar uma tarefa e enviar a notificação é uma transação única. Em micro-serviços, se a tarefa é criada mas o ms-notificacao está fora do ar, o e-mail não vai.

**Solução:** Padrão **Saga** com eventos e compensação. Se a notificação falha, um retry automático no RabbitMQ tenta novamente. A tarefa não é desfeita — apenas o e-mail é reenviado.

---

### 🔴 Desafio 2: Consultas que cruzam domínios
**Problema:** O dashboard atual retorna `totalPessoas + totalTarefas + totalDepartamentos` em uma única query. Em micro-serviços, são 3 chamadas HTTP separadas.

**Solução:** O API Gateway (ou um serviço de BFF — Backend for Frontend) agrega as respostas em paralelo:

```java
// No BFF ou Gateway
CompletableFuture<Long> tarefas = tarefasClient.count();
CompletableFuture<Long> pessoas = usuarioClient.count();
CompletableFuture<Long> deptos = deptoClient.count();
CompletableFuture.allOf(tarefas, pessoas, deptos).join();
```

---

### 🔴 Desafio 3: Consistência de dados entre bancos
**Problema:** `Tarefa` referencia `Pessoa`, mas agora estão em bancos diferentes.

**Solução:** **Eventual consistency**. Guardar o `pessoaEmail` na tabela `tarefa` como dado denormalizado. Quando a pessoa atualiza o nome, um evento `PessoaAtualizadaEvent` é publicado e o ms-tarefas atualiza seu registro local.

---

### 🟡 Desafio 4: Debugging e observabilidade
**Problema:** Uma requisição do Angular passa pelo gateway → ms-tarefas → ms-notificacao. Onde bugou?

**Solução:** **Distributed Tracing** com Micrometer + Zipkin. Cada requisição recebe um `traceId` que é propagado por todos os serviços. O Zipkin mostra o caminho completo e o tempo em cada hop.

---

### 🟡 Desafio 5: Latência adicional
**Problema:** O que antes era uma chamada interna Java vira uma chamada HTTP. Isso adiciona latência.

**Solução:** Cache local com `@Cacheable` (Spring Cache + Redis) para dados que raramente mudam (ex: lista de departamentos). Chamadas críticas ficam síncronas; menos críticas (notificações) ficam assíncronas.

---

## 9. Conclusão

### Quando vale a pena migrar?

A migração para micro-serviços **não é obrigatória** e tem custo. Faz sentido quando:

- ✅ A aplicação crescer e um único time não conseguir mais mantê-la
- ✅ Partes específicas precisarem de escala independente (ex: muito volume de tarefas)
- ✅ Você tiver equipes suficientes para manter serviços independentes
- ✅ O monolito começar a ter problemas de deploy (mudança em notificação derruba tarefas)

Se a aplicação continuar pequena, o monolito atual é a escolha certa. Micro-serviços trazem **complexidade operacional real** — mais bancos, mais processos, mais rede, mais pontos de falha.

### Resumo da Migração

| Fase | O que fazer | Resultado |
|---|---|---|
| 0 | Preparação, testes, estrutura de repos | Base sólida |
| 1 | API Gateway na frente do monolito | Roteamento centralizado |
| 2 | Extrair ms-notificacao + RabbitMQ | Primeiro serviço independente |
| 3 | Extrair ms-departamento | Segundo serviço independente |
| 4 | Extrair ms-usuario | Terceiro serviço independente |
| 5 | Extrair ms-tarefas (core) | Monolito aposentado |
| 6 | Docker, K8s, CI/CD independente | Produção escalável |

A abordagem gradual com o padrão **Strangler Fig** garante que a aplicação **nunca para de funcionar** durante a migração. Em cada fase, a versão atual continua servindo usuários enquanto o novo serviço é preparado e testado.

---

*Documento gerado com base na análise do código-fonte do backend (Spring Boot) e frontend (Angular) do sistema de Gerenciamento de Tarefas.*
