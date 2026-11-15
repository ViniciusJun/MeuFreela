# 🚀 MeuFreela

<p align="center">
  <strong>Conectando pessoas, oportunidades e serviços.</strong>
</p>

<p align="center">
  Marketplace mobile desenvolvido para conectar clientes a profissionais freelancers, facilitando a contratação de serviços por meio de tecnologia, comunicação em tempo real e reputação digital.
</p>

<p align="center">
  <a href="https://github.com/ViniciusJun/MeuFreela/actions/workflows/backend-tests.yml">
    <img src="https://github.com/ViniciusJun/MeuFreela/actions/workflows/backend-tests.yml/badge.svg" alt="Backend Tests">
  </a>
  <a href="https://github.com/ViniciusJun/MeuFreela/actions/workflows/android-build.yml">
    <img src="https://github.com/ViniciusJun/MeuFreela/actions/workflows/android-build.yml/badge.svg" alt="Android Build">
  </a>
  <img src="https://img.shields.io/badge/Java-21%2F17-orange?logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Android-Native-3DDC84?logo=android" alt="Android">
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker" alt="Docker">
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License">
</p>

<p align="center">
  <a href="#-sobre-o-projeto">Sobre</a> •
  <a href="#-arquitetura">Arquitetura</a> •
  <a href="#-tecnologias">Tecnologias</a> •
  <a href="#-funcionalidades">Funcionalidades</a> •
  <a href="#-instalação-e-execução">Instalação</a> •
  <a href="#-testes">Testes</a> •
  <a href="#-segurança">Segurança</a> •
  <a href="#-roadmap">Roadmap</a>
</p>

---

## 📌 Sobre o projeto

O **MeuFreela** é uma plataforma mobile desenvolvida com o objetivo de aproximar clientes e profissionais autônomos, simplificando o processo de descoberta, comunicação e contratação de serviços.

A proposta é criar um ambiente digital no qual freelancers possam divulgar seus serviços, construir sua reputação profissional e conquistar novos clientes, enquanto os contratantes encontram profissionais de maneira prática e organizada.

A plataforma se apoia em três pilares:

- **Conectividade:** aproximação entre clientes e profissionais por localização e categorias de serviços.
- **Confiabilidade:** sistema de avaliações bilaterais para construção de reputação.
- **Comunicação:** chat em tempo real e notificações para facilitar a interação entre as partes.

O projeto é composto por uma **API RESTful** em Java 21 + Spring Boot 4 e um **aplicativo Android nativo** em Java 17.

### 🎯 Objetivos

- Democratizar a divulgação de serviços profissionais.
- Facilitar a descoberta e contratação de freelancers.
- Proporcionar comunicação direta entre clientes e prestadores.
- Estabelecer um sistema de reputação baseado em avaliações.
- Criar uma infraestrutura preparada para futuras funcionalidades financeiras e expansão da plataforma.

<sub>📍 Status: **MVP funcional** — 82 testes automatizados, CI ativo, deploy via Docker Compose.</sub>

---

## 🏗️ Arquitetura

O projeto utiliza uma arquitetura de **monólito modular**, organizada em camadas com responsabilidades bem definidas. Essa abordagem foi escolhida considerando o estágio atual do produto: reduz complexidade operacional, facilita a manutenção e mantém a possibilidade de evolução arquitetural conforme o crescimento da aplicação.

    App Android (Java 17)
    Retrofit + OkHttp + WebSocket
            │
            │ HTTPS / WSS
            ▼
    Backend Spring Boot 4 (Java 21)
    Controller → Service → Repository → Entity
            │
        ┌───┴───┐
        ▼       ▼
    PostgreSQL  Firebase (FCM)
    (via Flyway)

### Decisões arquiteturais

| Decisão | Justificativa |
|---|---|
| Monólito modular | Reduz a complexidade inicial e facilita a manutenção. |
| API RESTful | Padroniza a comunicação entre aplicativo e backend. |
| JWT stateless (access + refresh) | Autenticação sem sessão, com rotação de tokens. |
| Spring Data JPA + Hibernate | Abstração da persistência com tipagem forte. |
| PostgreSQL (produção) + H2 (dev/testes) | Persistência real em produção; H2 para agilidade em dev e testes. |
| Flyway (migrations versionadas) | Schema controlado por arquivos SQL versionados no Git. |
| WebSocket | Comunicação bidirecional em tempo real para o chat. |
| Firebase FCM | Entrega de notificações push. |
| Docker Compose | Reprodutibilidade total do ambiente (backend + banco). |
| GitHub Actions | CI automático em cada push/PR. |
| Spring Boot Actuator | Health checks e métricas prontas para monitoramento. |

---

## 🛠️ Tecnologias

### Backend

| Tecnologia | Aplicação |
|---|---|
| Java 21 LTS | Linguagem principal |
| Spring Boot 4.1.1 | Framework base |
| Spring Web MVC | API REST |
| Spring Security + JWT (JJWT) | Autenticação e autorização |
| Spring Data JPA + Hibernate | Persistência |
| Flyway | Migrations versionadas |
| PostgreSQL 16 | Banco de dados principal |
| H2 Database | Banco em memória para testes |
| Spring WebSocket | Chat em tempo real |
| Firebase Admin SDK | Notificações push |
| Spring Boot Actuator + Micrometer | Health checks e métricas |
| Maven | Build e dependências |
| JUnit 5 + Mockito + AssertJ | Testes |

### Aplicativo Android

| Tecnologia | Aplicação |
|---|---|
| Java 17 | Desenvolvimento mobile |
| Android SDK 35 (minSdk 24) | Plataforma |
| Retrofit + Gson | Consumo da API REST |
| OkHttp | HTTP e WebSocket |
| Firebase Cloud Messaging | Notificações push |
| EncryptedSharedPreferences | Armazenamento seguro de tokens |
| ViewBinding | Binding de views |
| Gradle | Build |

### Infraestrutura

| Tecnologia | Aplicação |
|---|---|
| Docker + Docker Compose | Ambiente reproduzível |
| GitHub Actions | CI/CD |
| Adminer | UI web para inspeção do banco |

---

## ✨ Funcionalidades

### ✅ Implementadas

- Cadastro de usuários (clientes e freelancers).
- Autenticação JWT com **access token** (1h) e **refresh token** (7d).
- Endpoint `/api/auth/refresh` para renovação silenciosa.
- Armazenamento seguro de tokens no Android (EncryptedSharedPreferences).
- Publicação de serviços pelos freelancers.
- Criação e listagem de solicitações (cliente e freelancer).
- Atualização de status de solicitações com validação de transição.
- Avaliações bilaterais entre clientes e freelancers.
- Chat em tempo real via WebSocket (autenticado e com validação de participação).
- Histórico de mensagens via REST.
- Notificações push com Firebase Cloud Messaging.
- Tratamento global de exceções com respostas padronizadas.
- Health check e métricas via Actuator.
- Migrations versionadas com Flyway.

### 🚧 Em desenvolvimento

- Configuração avançada de perfil profissional.
- Portfólio de serviços com múltiplas imagens.
- Busca por proximidade geográfica.
- Filtros por categoria e faixa de preço no app.

### 🔮 Funcionalidades planejadas

- Integração com pagamentos (Stripe/MercadoPago).
- Sistema de escrow e split de pagamentos.
- Verificação de identidade (KYC).
- Rastreamento de serviços em tempo real.
- Assinatura premium.
- Painel administrativo.

---

## 📂 Estrutura do repositório

    MeuFreela/
    │
    ├── .github/workflows/          # CI (backend-tests.yml, android-build.yml)
    │
    ├── backend/
    │   ├── Dockerfile              # Build multi-stage
    │   ├── mvnw / mvnw.cmd
    │   ├── pom.xml
    │   ├── src/main/java/com/meufreela/api/
    │   │   ├── config/             # Security, WebSocket, Actuator, CORS
    │   │   ├── controller/         # Endpoints REST
    │   │   ├── dto/                # Request/Response
    │   │   ├── entity/             # Entidades JPA
    │   │   ├── exception/          # Tratamento global (@RestControllerAdvice)
    │   │   ├── repository/         # Spring Data JPA
    │   │   ├── security/           # JWT, Filter, WebSocket Handler
    │   │   └── service/            # Regras de negócio
    │   ├── src/main/resources/
    │   │   ├── application.properties
    │   │   ├── application-dev.properties
    │   │   ├── application-postgres.properties
    │   │   ├── application-test.properties
    │   │   └── db/migration/V1__init.sql
    │   └── src/test/               # 82 testes automatizados
    │
    ├── app/                        # Aplicativo Android
    │   ├── build.gradle
    │   └── src/main/java/com/meufreela/app/
    │       ├── activities/
    │       ├── adapters/
    │       ├── models/
    │       ├── network/            # Retrofit + OkHttp
    │       ├── storage/            # TokenManager
    │       └── utils/
    │
    ├── .env.example
    ├── .dockerignore
    ├── .gitignore
    ├── docker-compose.yml          # Postgres + Backend + Adminer
    ├── LICENSE
    └── README.md

---

## ⚙️ Instalação e execução

### Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (recomendado)
- [Java JDK 21](https://adoptium.net/) + [Maven](https://maven.apache.org/)
- [Java JDK 17](https://adoptium.net/) + [Android Studio](https://developer.android.com/studio)
- Conta no [Firebase](https://firebase.google.com/)

### 🐳 Método 1 — Docker Compose (recomendado)

Sobe **backend + PostgreSQL + Adminer** com um único comando:

    git clone https://github.com/ViniciusJun/MeuFreela.git
    cd MeuFreela

    cp .env.example .env
    # Editar .env e gerar um JWT_SECRET: openssl rand -base64 64

    docker compose up -d

    curl http://localhost:8080/actuator/health

**Serviços em execução**:

| URL | Serviço |
|-----|---------|
| `http://localhost:8080` | Backend (API) |
| `http://localhost:8080/actuator/health` | Health check |
| `http://localhost:8081` | Adminer (UI do banco) |

Para inspecionar o banco no Adminer:
- **Sistema**: PostgreSQL
- **Servidor**: `postgres`
- **Usuário**: `meufreela`
- **Senha**: `meufreela123`
- **Base**: `meufreela`

**Comandos úteis**:

    docker compose logs -f backend       # logs em tempo real
    docker compose down                  # parar (mantém dados)
    docker compose down -v               # parar e apagar dados do banco
    docker compose up -d --build backend # rebuild após mudança de código

### 🔧 Método 2 — Local com Maven (desenvolvimento)

Requer PostgreSQL rodando em `localhost:5432` com database `meufreela`.

    cd backend

    # Perfil postgres (padrão)
    export SPRING_PROFILES_ACTIVE=postgres   # PowerShell: $env:SPRING_PROFILES_ACTIVE="postgres"
    export JWT_SECRET="<secret-base64-de-64-bytes>"

    ./mvnw spring-boot:run

Ou com **H2 em memória** (rápido, sem precisar de Postgres):

    cd backend
    export SPRING_PROFILES_ACTIVE=dev
    export JWT_SECRET="<secret>"
    ./mvnw spring-boot:run

### 📱 App Android

1. Abra a pasta `app/` no **Android Studio**.
2. Coloque o arquivo `app/google-services.json` (obtido do Firebase Console → Configurações → Seus Apps → Android).
3. Ajuste a URL base da API em `app/src/main/java/com/meufreela/app/network/ApiConfig.java`:
    - **Emulador**: `http://10.0.2.2:8080/`
    - **Dispositivo físico**: `http://<IP-da-máquina>:8080/`
4. Execute em emulador ou dispositivo físico (`Run ▶`).

> ⚠️ O `google-services.json` **não é versionado**. Cada dev precisa obter o seu no Firebase Console.

---

## 🔐 Configuração e variáveis de ambiente

### Variáveis obrigatórias

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `JWT_SECRET` | Chave Base64 para assinar JWT (≥ 256 bits) | `openssl rand -base64 64` |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo | `postgres`, `dev`, `test` |
| `SPRING_DATASOURCE_URL` | URL JDBC (sobrescreve `postgres` padrão) | `jdbc:postgresql://postgres:5432/meufreela` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `meufreela` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `meufreela123` |

### Arquivos de configuração por perfil

| Arquivo | Uso |
|---------|-----|
| `application.properties` | Base (server port, JWT, Actuator) |
| `application-dev.properties` | H2 em memória (dev rápido) |
| `application-postgres.properties` | PostgreSQL (produção/Docker) |
| `application-test.properties` | Testes (H2 + configs de teste) |

### Configuração JWT

    jwt.secret=${JWT_SECRET}          # Obrigatório via env
    jwt.expiration=3600000             # 1 hora (access token)
    jwt.refresh-expiration=604800000   # 7 dias (refresh token)

### Firebase

O backend usa o **Firebase Admin SDK** para push. O arquivo `firebase-service-account.json` deve ser mantido **fora do Git** e disponibilizado via variável de ambiente em produção (`GOOGLE_APPLICATION_CREDENTIALS` ou mount em Docker secrets).

### ⚠️ Segurança de credenciais

Nunca versione:
- `application-local.properties`, `application-secrets.properties`
- `.env`, `firebase-service-account.json`, `google-services.json`
- `*.jks`, `*.keystore`, `*.pem`, `*.key`

Use o `.env.example` como template e **adicione `.env` ao `.gitignore`** (já configurado).

---

## 🔌 Endpoints principais

Todos os endpoints (exceto `/auth/**` e `/actuator/health`) exigem `Authorization: Bearer <access_token>`.

### Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/cadastro` | Cadastro de usuário |
| POST | `/api/auth/login` | Login + emissão de tokens |
| POST | `/api/auth/refresh` | Troca de refresh token por novo par |

### Usuários

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/usuarios/me` | Perfil do usuário logado |
| PATCH | `/api/usuarios/me` | Atualização de perfil |
| POST | `/api/usuarios/tornar-cliente` | Ativa perfil de cliente |

### Serviços

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/servicos` | Lista serviços (público) |
| GET | `/api/servicos/{id}` | Detalhe de serviço (público) |
| POST | `/api/servicos` | Publica novo serviço |
| PUT | `/api/servicos/{id}` | Atualiza próprio serviço |
| PATCH | `/api/servicos/{id}/ativo` | Pausa/reativa serviço |
| DELETE | `/api/servicos/{id}` | Exclui serviço |

### Freelancers

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/freelancers` | Lista freelancers (público) |
| GET | `/api/freelancers/{id}` | Detalhe (público) |
| GET | `/api/freelancers/{id}/avaliacoes` | Avaliações (público) |

### Solicitações

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/solicitacoes` | Cria solicitação |
| GET | `/api/solicitacoes/como-cliente` | Lista como cliente |
| GET | `/api/solicitacoes/como-freelancer` | Lista como freelancer |
| PATCH | `/api/solicitacoes/{id}/status` | Atualiza status |

### Chat

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/solicitacoes/{id}/mensagens` | Histórico |
| POST | `/api/solicitacoes/{id}/mensagens` | Envia mensagem |
| WS | `/ws/chat?token=<jwt>&solicitacao=<id>` | Chat em tempo real |

### Avaliações

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/avaliacoes` | Registra avaliação |

### Actuator

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/actuator/health` | Health check (público) |
| GET | `/actuator/metrics` | Métricas (autenticado) |
| GET | `/actuator/prometheus` | Formato Prometheus (autenticado) |

> A documentação interativa com **OpenAPI/Swagger** está planejada para uma próxima etapa.

---

## 🛡️ Segurança

Segurança é pilar fundamental do projeto, tratada em **todas as camadas**.

### Práticas adotadas

- **Senhas**: BCrypt com fator 12.
- **JWT stateless** com HMAC-SHA256, incluindo `iss`, `aud`, `jti`, `type` (`access` vs `refresh`) e expiração.
- **Access token curto** (1h) + **refresh token longo** (7d) com endpoint dedicado `/api/auth/refresh`.
- **Secret JWT** carregado via variável de ambiente (`JWT_SECRET`), nunca versionado.
- **WebSocket autenticado**: handshake valida assinatura + expiração + tipo; `validarParticipacao` bloqueia IDOR (só cliente/freelancer da solicitação entra na sala).
- **H2 Console** restrito a `@Profile("dev")` (`H2SecurityConfig`).
- **AuthenticationEntryPoint** customizado → 401 (não 403) para requisições não autenticadas.
- **Autorização por dono** em operações sobre serviços e solicitações (409 para violações).
- **Armazenamento seguro de tokens no Android** com EncryptedSharedPreferences.
- **Validação de entrada** com `@Valid` e constraints do Jakarta Validation.
- **Tratamento global de exceções** com mensagens padronizadas (sem stack trace exposto).
- **Firebase service account** fora do repositório.
- **Secret JWT removido do histórico Git** (via `git filter-repo`) + rotacionado.

### Melhorias de segurança em andamento

- [ ] Configurar **CORS** explicitamente por origem.
- [ ] Implementar **rate limiting** no endpoint de login (Bucket4j + Redis).
- [ ] Adicionar **blacklist de refresh tokens** (Redis) para revogação imediata.
- [ ] Habilitar **GitHub Secret Scanning** + `push protection`.
- [ ] **Dependabot** para CVEs em dependências.
- [ ] **Trivy** para scan de vulnerabilidades na imagem Docker.

---

## 🧪 Testes

**82 testes automatizados** cobrindo controllers, services, segurança e integração.

### Backend

    cd backend
    ./mvnw test

**Cobertura por camada**:

| Camada | Testes | Descrição |
|--------|--------|-----------|
| `JwtServiceTest` | 12 | Geração, validação, expiração, `type`, `iss`/`aud`, assinatura |
| `AuthServiceTest` | 8 | Cadastro, login, refresh (rotação, expirado, malformado) |
| `ServicoServiceTest` | 10 | CRUD, autorização por dono, categoria |
| `SolicitacaoServiceTest` | 7 | Criação, preço (serviço/freelancer), autorização, status |
| `AvaliacaoServiceTest` | 9 | Criação, validações, atualização de média |
| `ChatServiceTest` | 8 | Envio, histórico, participação |
| `ChatWebSocketHandlerTest` | 9 | Handshake (IDOR, refresh token), mensagens, broadcast |
| `AuthControllerTest`, `ServicoControllerTest`, `SolicitacaoControllerTest`, `ChatControllerTest` | 30 | Integração REST (MockMvc) |
| `MeufreelaApiApplicationTests` | 1 | Contexto Spring |

### Android

    cd app
    ./gradlew test               # unitários
    ./gradlew connectedCheck     # instrumentados (requer emulador/dispositivo)

### CI (GitHub Actions)

Cada push/PR em `main`/`develop` dispara:

1. **`backend-tests.yml`** → `./mvnw clean test` (82 testes) + empacotamento JAR
2. **`android-build.yml`** → `./gradlew assembleDebug` + testes unitários Android

Cache de Maven e Gradle ativo — builds subsequentes em ~40% do tempo inicial.

---

## 🗺️ Roadmap

### ✅ Concluído

- [x] Autenticação JWT com `iss`, `aud`, `jti`, `type`
- [x] Refresh token + endpoint `/api/auth/refresh`
- [x] WebSocket autenticado + autorização por participação (IDOR fechado)
- [x] Tratamento global de exceções (`@RestControllerAdvice`)
- [x] Alinhamento de cálculo de preço app ↔ backend
- [x] Migração para PostgreSQL (produção)
- [x] Flyway (migrations versionadas)
- [x] Dockerização (`Dockerfile` + `docker-compose.yml`)
- [x] Pipeline CI/CD (GitHub Actions)
- [x] Spring Boot Actuator + Micrometer
- [x] Remoção do secret JWT do histórico Git + rotação
- [x] H2 Console restrito a `@Profile("dev")`
- [x] `@PrePersist` / `@PreUpdate` em timestamps

### 🚧 Curto prazo — Hardening

- [ ] CORS explícito por origem
- [ ] Rate limiting no login (Bucket4j + Redis)
- [ ] OpenAPI/Swagger (SpringDoc)
- [ ] GitHub Secret Scanning + push protection
- [ ] Dependabot + Trivy no CI

### 📈 Médio prazo — Observabilidade e resiliência

- [ ] Prometheus + Grafana no Compose
- [ ] Logs estruturados (Logback JSON)
- [ ] Blacklist de refresh tokens em Redis
- [ ] Cache com Redis em consultas frequentes
- [ ] OpenTelemetry (tracing distribuído)
- [ ] Testcontainers para testes de integração

### 🔮 Longo prazo — Evolução do produto

- [ ] Mensageria assíncrona (RabbitMQ)
- [ ] Avaliação de Keycloak para identidade
- [ ] Pagamentos com escrow e split (Stripe/MercadoPago)
- [ ] KYC de freelancers
- [ ] Rastreamento em tempo real
- [ ] Assinatura premium
- [ ] Painel administrativo
- [ ] Deploy em produção (Railway/Render/VPS)

---

## 🤝 Contribuição

Contribuições são bem-vindas. Para manter a qualidade:

1. Abra uma **Issue** descrevendo o problema ou a proposta.
2. Crie uma **branch** a partir da `main` com nome descritivo.
3. Faça commits seguindo **Conventional Commits**.
4. Envie um **Pull Request** explicando as alterações.
5. Aguarde **code review** antes do merge. CI precisa estar verde.

### Padrão de branches

    feature/nova-funcionalidade
    fix/correcao-de-bug
    docs/atualizacao-readme
    refactor/melhoria-arquitetural
    test/novos-testes

### Conventional Commits

    feat: adiciona endpoint de busca por categoria
    fix: corrige expiração do refresh token
    docs: atualiza instruções de deploy
    refactor: extrai validação de participação no chat
    test: cobre cenário de autorização por dono
    chore: atualiza dependências Spring Boot

---

## 📄 Licença

Este projeto está licenciado sob a **licença MIT**. Consulte o arquivo [LICENSE](LICENSE) para mais informações.

---

## 👨‍💻 Autor

**Vinicius Jun**

- GitHub: [@ViniciusJun](https://github.com/ViniciusJun)
- Repositório: [MeuFreela](https://github.com/ViniciusJun/MeuFreela)

---

<p align="center">
  Desenvolvido com ☕ e tecnologia.
  <br>
  <strong>MeuFreela — aproximando talentos e oportunidades.</strong>
</p>

<p align="center">
  <sub>Projeto em desenvolvimento contínuo. Decisões arquiteturais são documentadas à medida que amadurecem.</sub>
</p>