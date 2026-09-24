<div align="center">

# 🛠️ Meu Freela

### O iFood dos Freelancers

[![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)]()
[![Versão](https://img.shields.io/badge/versão-0.1.0-blue)]()
[![Licença](https://img.shields.io/badge/licença-MIT-green)]()
[![Plataformas](https://img.shields.io/badge/plataformas-iOS%20%7C%20Android-lightgrey)]()

<img src="./docs/assets/banner.png" alt="Banner do App" width="800"/>

**Conectamos clientes a freelancers qualificados em minutos — com pagamento seguro, chat em tempo real e avaliações confiáveis.**

[Documentação](./docs) · [Reportar Bug](../../issues) · [Sugerir Feature](../../issues) · [Roadmap](./docs/02-produto/roadmap.md)

</div>

---

## 📖 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Demonstração](#-demonstração)
- [Arquitetura](#-arquitetura)
- [Stack Tecnológica](#-stack-tecnológica)
- [Começando](#-começando)
  - [Pré-requisitos](#pré-requisitos)
  - [Instalação](#instalação)
  - [Variáveis de Ambiente](#variáveis-de-ambiente)
  - [Rodando o Projeto](#rodando-o-projeto)
- [Testes](#-testes)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Metodologia](#-metodologia)
- [Roadmap](#-roadmap)
- [Contribuindo](#-contribuindo)
- [Licença](#-licença)
- [Contato](#-contato)

---

## 🎯 Sobre o Projeto

O **Meu Freela** é um marketplace mobile que conecta **clientes** que precisam de serviços pontuais a **freelancers** qualificados das mais diversas áreas — reformas, tecnologia, beleza, eventos e serviços domésticos.

Inspirado na experiência do iFood, o app oferece:

- 🔍 **Descoberta por proximidade** — veja profissionais perto de você
- 💬 **Chat em tempo real** — negocie direto com o freelancer
- 🔒 **Pagamento com escrow** — seu dinheiro só é liberado após a conclusão
- ⭐ **Reputação verificada** — avaliações bilaterais em cada serviço
- 📍 **Rastreamento ao vivo** — acompanhe o deslocamento do profissional

> **Missão:** democratizar o acesso a serviços de qualidade e dar autonomia financeira a profissionais autônomos.

---

## ✨ Funcionalidades

### 👤 Para Clientes
- [x] Cadastro e login (e-mail, Google, Apple)
- [x] Busca por categoria, localização e avaliação
- [x] Solicitação de orçamento ou contratação direta
- [x] Chat em tempo real com o freelancer
- [x] Pagamento via PIX, cartão e carteira interna
- [x] Acompanhamento do serviço em tempo real
- [x] Avaliação e histórico de serviços

### 🧑‍🔧 Para Freelancers
- [x] Cadastro com verificação de identidade (KYC)
- [x] Configuração de áreas de atuação e preços
- [x] Recebimento de notificações de novas demandas
- [x] Gestão de agenda e disponibilidade
- [x] Recebimento automático via split de pagamento
- [x] Dashboard de ganhos e métricas

### 🚧 Em desenvolvimento (V1)
- [ ] Assinatura premium para freelancers (destaque nas buscas)
- [ ] Agendamento recorrente
- [ ] Programa de indicação
- [ ] Suporte multi-idioma

---

## 🎬 Demonstração

| Tela Inicial | Busca | Chat | Pagamento |
|:---:|:---:|:---:|:---:|
| ![Home](./docs/assets/screenshots/home.png) | ![Busca](./docs/assets/screenshots/search.png) | ![Chat](./docs/assets/screenshots/chat.png) | ![Pagamento](./docs/assets/screenshots/payment.png) |

> 💡 **Vídeo demonstrativo:** [assista aqui](https://link-do-video.com)

---

## 🏗️ Arquitetura

```mermaid
flowchart LR
    A[📱 App Mobile<br/>React Native] -->|HTTPS| B[🌐 API Gateway]
    B --> C[⚙️ Backend<br/>NestJS]
    C --> D[(🐘 PostgreSQL)]
    C --> E[(⚡ Redis)]
    C --> F[☁️ S3/R2]
    C --> G[👷 Workers<br/>BullMQ]
    G --> H[💳 Stripe/MercadoPago]
    G --> I[🔔 FCM/APNs]
    G --> J[📧 Resend]
    C -.WebSocket.-> A
