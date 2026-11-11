# 🚀 MeuFreela

<p align="center">
  **Conectando pessoas, oportunidades e serviços.**
</p>

<p align="center">
  Marketplace mobile desenvolvido para conectar clientes a profissionais freelancers, facilitando a contratação de serviços por meio de tecnologia, comunicação em tempo real e reputação digital.
</p>

<p align="center">
  ![Java](https://img.shields.io/badge/Java-21%2F17-orange?logo=openjdk)
  ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot)
  ![Android](https://img.shields.io/badge/Android-Native-3DDC84?logo=android)
  ![Firebase](https://img.shields.io/badge/Firebase-Integrated-FFCA28?logo=firebase)
  ![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven)
  ![License](https://img.shields.io/badge/License-MIT-blue.svg)
</p>

<p align="center">
  <a href="#-sobre-o-projeto">Sobre</a> •
  <a href="#-arquitetura">Arquitetura</a> •
  <a href="#-tecnologias">Tecnologias</a> •
  <a href="#-funcionalidades">Funcionalidades</a> •
  <a href="#-instalação-e-execução">Instalação</a> •
  <a href="#-roadmap">Roadmap</a>
</p>

---

## 📌 Sobre o projeto

O **MeuFreela** é uma plataforma mobile desenvolvida com o objetivo de aproximar clientes e profissionais autônomos, simplificando o processo de descoberta, comunicação e contratação de serviços.

A proposta é criar um ambiente digital no qual freelancers possam divulgar seus serviços, construir sua reputação profissional e conquistar novos clientes, enquanto os contratantes encontram profissionais de maneira prática e organizada.

A plataforma busca oferecer uma experiência baseada em três pilares:

- **Conectividade:** aproximação entre clientes e profissionais por localização e categorias de serviços.
- **Confiabilidade:** sistema de avaliações bilaterais para construção de reputação.
- **Comunicação:** chat em tempo real e notificações para facilitar a interação entre as partes.

O projeto é composto por uma API RESTful desenvolvida em Java com Spring Boot e um aplicativo Android nativo.

### 🎯 Objetivos

- Democratizar a divulgação de serviços profissionais.
- Facilitar a descoberta e contratação de freelancers.
- Proporcionar comunicação direta entre clientes e prestadores.
- Estabelecer um sistema de reputação baseado em avaliações.
- Criar uma infraestrutura preparada para futuras funcionalidades financeiras e expansão da plataforma.

<sub>📍 Status: desenvolvimento ativo — MVP.</sub>

---

## 🏗️ Arquitetura

O projeto utiliza uma arquitetura de **monólito modular**, organizada em camadas e com responsabilidades bem definidas.

Essa abordagem foi escolhida considerando o estágio atual do produto, permitindo reduzir a complexidade operacional, facilitar a manutenção e manter a possibilidade de evolução arquitetural conforme o crescimento da aplicação.

---

## Visão Geral

O **MeuFreela** é composto por dois componentes principais:

1. **Backend** — API RESTful em **Java 21 + Spring Boot**, responsável por autenticação, gestão de usuários, solicitações de serviço, avaliações, chat via WebSocket e envio de notificações push.
2. **Aplicativo Android** — cliente nativo em **Java 17**, consumindo a API via Retrofit e mantendo o chat aberto por WebSocket.

O objetivo do projeto é oferecer uma plataforma confiável para contratação de serviços freelance, com foco em reputação, comunicação direta e (futuramente) garantia de pagamento.

---

### Decisões arquiteturais

| Decisão | Justificativa |
|---|---|
| Monólito modular | Reduz a complexidade inicial e facilita a manutenção. |
| API RESTful | Padroniza a comunicação entre aplicativo e backend. |
| JWT Stateless | Permite autenticação sem necessidade de sessão no servidor. |
| Spring Data JPA | Simplifica o acesso e a persistência dos dados. |
| H2 Database | Facilita a configuração do ambiente de desenvolvimento. |
| WebSocket | Permite comunicação bidirecional em tempo real. |
| Firebase FCM | Responsável pela entrega de notificações push. |

A arquitetura poderá evoluir para serviços independentes caso o crescimento da aplicação justifique essa necessidade.

---

## 🛠️ Tecnologias

### Backend

| Tecnologia | Aplicação |
|---|---|
| Java 21 LTS | Linguagem principal |
| Spring Boot 4.1.1 | Framework de desenvolvimento |
| Spring Web MVC | Construção da API REST |
| Spring Security | Autenticação e autorização |
| JWT (JJWT) | Gerenciamento de tokens |
| BCrypt | Hash de senhas |
| Spring Data JPA | Persistência de dados |
| Hibernate | ORM |
| H2 Database | Banco de desenvolvimento |
| Spring WebSocket | Comunicação em tempo real |
| Firebase Admin SDK | Envio de notificações |
| Maven | Gerenciamento de dependências e build |
| JUnit 5 | Testes automatizados |

### Aplicativo Android

| Tecnologia | Aplicação |
|---|---|
| Java 17 | Desenvolvimento mobile |
| Android SDK | Plataforma Android |
| Retrofit | Consumo da API REST |
| Gson | Serialização e desserialização JSON |
| OkHttp | Comunicação HTTP e WebSocket |
| Firebase Cloud Messaging | Notificações push |
| EncryptedSharedPreferences | Armazenamento seguro de tokens |
| Gradle | Gerenciamento de build |

---

## ✨ Funcionalidades

### ✅ Implementadas

- [x] Cadastro de usuários (clientes e freelancers).
- [x] Autenticação utilizando JWT.
- [x] Armazenamento seguro de tokens no Android.
- [x] Criação de solicitações de serviços.
- [x] Consulta e listagem de solicitações.
- [x] Avaliações bilaterais entre clientes e freelancers.
- [x] Chat em tempo real utilizando WebSocket.
- [x] Notificações push com Firebase Cloud Messaging.
- [x] Tratamento de exceções com respostas padronizadas.

### 🚧 Em desenvolvimento

- [ ] Configuração de perfil profissional.
- [ ] Portfólio de serviços dos freelancers.
- [ ] Busca por proximidade geográfica.
- [ ] Filtros por categoria de serviço.
- [ ] Filtros por faixa de preço.

### 🔮 Funcionalidades planejadas

- [ ] Migração para PostgreSQL.
- [ ] Integração com pagamentos.
- [ ] Sistema de escrow e split de pagamentos.
- [ ] Verificação de identidade (KYC).
- [ ] Rastreamento de serviços em tempo real.
- [ ] Assinatura premium.
- [ ] Painel administrativo.

---

## 📂 Estrutura do repositório

```text
MeuFreela/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/meufreela/
│   │   │   │   ├── config/       # Configurações gerais
│   │   │   │   ├── controller/  # Endpoints REST
│   │   │   │   ├── dto/         # Objetos de transferência
│   │   │   │   ├── exception/   # Tratamento de exceções
│   │   │   │   ├── model/       # Entidades JPA
│   │   │   │   ├── repository/  # Camada de persistência
│   │   │   │   ├── security/    # Autenticação e JWT
│   │   │   │   └── service/     # Regras de negócio
│   │   │   │
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   │
│   │   └── test/                # Testes automatizados
│   │
│   └── pom.xml
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/meufreela/app/
│   │   │   │   ├── api/         # Retrofit e WebSocket
│   │   │   │   ├── model/       # Modelos de dados
│   │   │   │   ├── ui/          # Activities e Fragments
│   │   │   │   └── util/        # Classes utilitárias
│   │   │   │
│   │   │   └── res/             # Layouts e recursos Android
│   │   │
│   │   ├── test/                # Testes unitários
│   │   └── androidTest/         # Testes instrumentados
│   │
│   └── build.gradle
│
├── docs/                        # Documentação adicional
│
├── README.md
└── LICENSE
```

---

## ⚙️ Instalação e execução

### Pré-requisitos

Antes de executar o projeto, certifique-se de possuir:

- [Java JDK 21](https://adoptium.net/) — backend.
- [Java JDK 17](https://adoptium.net/) — ambiente Android.
- [Apache Maven](https://maven.apache.org/) 3.9 ou superior.
- [Android Studio](https://developer.android.com/studio).
- Android SDK e Gradle compatíveis com o projeto.
- Conta no [Firebase](https://firebase.google.com/).

### 1. Clonar o repositório

```bash
git clone https://github.com/ViniciusJun/MeuFreela.git

cd MeuFreela
```

### 2. Executar o backend

Acesse a pasta do backend:

```bash
cd backend
```

Execute a aplicação utilizando o Maven Wrapper:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Após a inicialização, a API estará disponível em:

```text
http://localhost:8080
```

### 3. Executar o aplicativo Android

1. Abra a pasta `app/` no Android Studio.
2. Configure o arquivo `google-services.json` fornecido pelo Firebase.
3. Configure a URL base da API no cliente Retrofit.
4. Para execução em emulador Android, utilize:

```text
http://10.0.2.2:8080/
```

5. Execute o aplicativo em um emulador ou dispositivo físico.

> **Importante:** para dispositivos físicos, utilize o endereço IP da máquina que executa o backend. A configuração de comunicação HTTP local deve ser restrita ao ambiente de desenvolvimento.

---

## 🔐 Configuração e variáveis de ambiente

As configurações do backend estão localizadas em:

```text
backend/src/main/resources/application.properties
```

### Configuração do servidor

```properties
server.port=8080
```

### Banco de dados H2

```properties
spring.datasource.url=jdbc:h2:mem:meufreela
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

### Autenticação JWT

```properties
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
```

A variável `JWT_SECRET` deve ser configurada no ambiente de execução.

**Segurança:** nunca versione segredos, senhas, tokens ou credenciais de serviços externos. Utilize variáveis de ambiente ou um gerenciador de segredos.

### Firebase

O backend utiliza o Firebase Admin SDK para envio de notificações.

O arquivo `serviceAccountKey.json` deve permanecer fora do controle de versão e ser disponibilizado por meio de configuração segura no ambiente de execução.

---

## 🔌 Endpoints principais

A API disponibiliza os seguintes recursos:

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/auth/register` | Cadastro de usuário |
| POST | `/auth/login` | Autenticação e emissão de JWT |
| GET | `/usuarios/{id}` | Consulta de usuário |
| POST | `/solicitacoes` | Criação de solicitação |
| GET | `/solicitacoes` | Listagem de solicitações |
| POST | `/avaliacoes` | Registro de avaliação |
| WS | `/ws/chat` | Comunicação em tempo real |

A documentação interativa da API utilizando OpenAPI/Swagger está planejada para uma próxima etapa.

---

## 🛡️ Segurança

A segurança é considerada um dos pilares fundamentais do MeuFreela, especialmente por se tratar de uma plataforma que manipula informações de usuários e interações entre clientes e profissionais.

### Práticas adotadas

- Hash de senhas utilizando BCrypt com fator de custo 12.
- Autenticação stateless com JWT.
- Chave JWT injetada por propriedade de ambiente.
- Armazenamento de tokens no Android com EncryptedSharedPreferences.
- Validação de dados de entrada utilizando `@Valid`.
- Configuração de segurança baseada em Spring Security.
- Comunicação por WebSocket para chat em tempo real.

### Melhorias de segurança planejadas

- Restringir o console H2 ao perfil de desenvolvimento.
- Configurar políticas CORS por origem.
- Implementar rate limiting no endpoint de autenticação.
- Implementar rotação de segredos JWT.
- Separar configurações e credenciais por ambiente.
- Fortalecer os controles de autorização sobre os recursos dos usuários.

---

## 🧪 Testes

A estratégia de testes busca garantir a confiabilidade das regras de negócio, da comunicação entre as camadas e do comportamento da aplicação mobile.

### Backend

Execute os testes com:

```bash
cd backend

./mvnw test
```

No Windows:

```powershell
.\mvnw.cmd test
```

### Estratégia de testes

| Tipo | Ferramentas |
|---|---|
| Testes unitários | JUnit 5 e Mockito |
| Testes de integração | Spring Boot Test |
| Testes de persistência | Testcontainers (planejado) |
| Validação de endpoints | Spring Boot Test |

### Android

Execute os testes unitários:

```bash
cd app

./gradlew test
```

Para testes instrumentados:

```bash
./gradlew connectedCheck
```

Os testes instrumentados exigem um emulador ou dispositivo Android conectado.

---

## 🗺️ Roadmap

A evolução do MeuFreela está organizada em três etapas principais.

### Curto prazo — Qualidade e segurança

- [ ] Restringir o console H2 com `@Profile("dev")`.
- [ ] Configurar CORS explicitamente.
- [ ] Implementar rate limiting no login.
- [ ] Adicionar tratamento global com `@RestControllerAdvice`.
- [ ] Implementar atualização automática de campos `atualizadoEm`.
- [ ] Separar os dados profissionais em `PerfilFreelancer`.
- [ ] Documentar endpoints com OpenAPI/Swagger.

### Médio prazo — Infraestrutura e automação

- [ ] Migrar para PostgreSQL.
- [ ] Implementar Flyway para versionamento do banco.
- [ ] Dockerizar o backend.
- [ ] Criar `docker-compose.yml` com backend e PostgreSQL.
- [ ] Implementar pipeline CI/CD com GitHub Actions.
- [ ] Integrar SonarQube e Quality Gate.
- [ ] Implementar testes de integração com Testcontainers.
- [ ] Adicionar Spring Boot Actuator e Micrometer.

### Longo prazo — Escalabilidade e evolução do negócio

- [ ] Monitoramento com Prometheus e Grafana.
- [ ] Logs estruturados.
- [ ] Observabilidade com OpenTelemetry.
- [ ] Cache com Redis.
- [ ] Mensageria com RabbitMQ.
- [ ] Avaliar integração com Keycloak.
- [ ] Implementar pagamentos com escrow e split.
- [ ] Implementar KYC para freelancers.
- [ ] Adicionar rastreamento em tempo real.
- [ ] Implementar assinatura premium.
- [ ] Desenvolver painel administrativo.

---

## 🤝 Contribuição

Contribuições são bem-vindas!

Para manter a organização e a qualidade do código, siga o fluxo de contribuição:

1. Abra uma Issue descrevendo o problema ou a funcionalidade proposta.
2. Crie uma branch a partir da `main`.
3. Utilize nomes descritivos para as branches.
4. Faça commits seguindo o padrão Conventional Commits.
5. Envie um Pull Request explicando as alterações.
6. Aguarde a revisão do código antes do merge.

### Padrão de branches

```text
feature/nova-funcionalidade
fix/correcao-de-bug
docs/atualizacao-readme
refactor/melhoria-arquitetural
test/novos-testes
```

### Conventional Commits

```text
feat: adiciona funcionalidade de busca
fix: corrige autenticação de usuário
docs: atualiza documentação
refactor: reorganiza camada de serviços
test: adiciona testes de integração
chore: atualiza dependências
```

---

## 📄 Licença

Este projeto está licenciado sob a licença MIT.

Consulte o arquivo [LICENSE](LICENSE) para obter mais informações.

---

## 👨‍💻 Autor

**Vinicius Jun**

- GitHub: [@ViniciusJun](https://github.com/ViniciusJun)
- Repositório: [MeuFreela](https://github.com/ViniciusJun/MeuFreela)

---

<p align="center">
  Desenvolvido com ☕ e tecnologia.
  <br>
  **MeuFreela — aproximando talentos e oportunidades.**
</p>

<p align="center">
  <sub>Projeto em desenvolvimento contínuo. Novas funcionalidades, melhorias arquiteturais e atualizações serão documentadas conforme a evolução da plataforma.</sub>
</p>