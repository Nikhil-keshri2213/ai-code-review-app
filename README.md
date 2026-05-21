<div align="center">

# 🤖 AI Code Review Assistant

### An AI-powered microservices platform that automatically reviews GitHub Pull Requests —
### detecting bugs, security vulnerabilities, and code quality issues the moment a PR is opened.

<br/>

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)](https://kubernetes.io/)
[![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)](https://grafana.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

<br/>

> **Built by [Nikhil Keshri](https://github.com/Nikhil-keshri2213)** — Associate Software Engineer · B.Tech CSE 2025 · Oracle Cloud Infrastructure Certified

</div>

---

## 📌 Table of Contents

- [The Problem](#-the-problem)
- [The Solution](#-the-solution)
- [Architecture](#-architecture)
- [Microservices](#-microservices)
- [How It Works — Step by Step](#-how-it-works--step-by-step)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [API Reference](#-api-reference)
- [Monitoring & Observability](#-monitoring--observability)
- [Testing](#-testing)
- [Project Structure](#-project-structure)
- [Development Journey](#-development-journey)

---

## 🎯 The Problem

Every software team knows this moment: a developer opens a Pull Request and it just **sits there**.

Hours pass. Sometimes days. A human reviewer has to manually read through changes, spot bugs, hunt for security holes, and leave comments — all while juggling their own work. Reviews get rushed. They get inconsistent. Occasionally, they get skipped entirely.

Critical bugs slip through. Not because teams are careless, but because **human attention is finite and expensive.**

---

## ✅ The Solution

**This platform eliminates the waiting time entirely.**

The moment a PR is opened on GitHub, an AI review fires **automatically**. No human needs to be free. By the time a human reviewer looks at the code, the mechanical issues are already flagged — so they can focus on architecture, design, and intent: the things only a human can judge.

| What it catches | Example |
|---|---|
| 🐛 **Bugs** | Logic errors, null pointer risks, unhandled exceptions |
| 🔒 **Security** | SQL injection, hardcoded credentials, insecure configs |
| ⚡ **Performance** | N+1 queries, inefficient loops, blocking I/O |
| 🎨 **Code Quality** | Style violations, naming issues, dead code |
| 📊 **Severity** | Every finding ranked HIGH / MEDIUM / LOW |

Every review is posted **directly on the GitHub PR as inline comments** — right on the line of code where the issue was found.

---

## 🏗 Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                            GitHub                                │
│                  Pull Request Opened / Updated                   │
└────────────────────────────┬─────────────────────────────────────┘
                             │  Webhook  (HMAC-SHA256)
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│                    API Gateway  :8080                            │
│         JWT Auth · Redis Rate Limiting · Routing                 │
└────────────────────────────┬─────────────────────────────────────┘
                             │
              ┌──────────────▼──────────────┐
              │      Webhook Service :8081   │──── publishes ──►┐
              │      HMAC-SHA256 Validated   │                  │
              └─────────────────────────────┘                  │
                                                               │
              ┌──────────────────────────────────┐             │
              │   Code Fetch Service :8082        │◄── consumes ┘
              │   GitHub API · WebClient          │
              └──────────────┬───────────────────┘
                             │ publishes code-analysis-tasks
              ┌──────────────▼───────────────────┐
              │  Code Analysis Service :8083      │──── publishes ──►┐
              │  Diff Parser · Chunker            │                  │
              └──────────────────────────────────┘                  │
                                                                    │
                                          ┌─────────────────────────┘
              ┌───────────────────────────▼──────────────────────┐
              │         AI Review Service :8084                  │◄── Groq LLM
              │         Groq · RAG · Weaviate                    │◄── Weaviate (RAG)
              └───────────────────────────┬──────────────────────┘
                             │ publishes review-results
              ┌──────────────┴──────────────────────────────┐
              │                                             │
  ┌───────────▼────────────┐              ┌────────────────▼─────────────┐
  │  Review Storage :8085  │              │  Notification Service :8086  │
  │  PostgreSQL · JPA      │              │  GitHub PR Comments · Slack  │
  └────────────────────────┘              └──────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                    Supporting Infrastructure                     │
│    Eureka :8761  ·  Kafka  ·  Weaviate :8090                    │
│    Prometheus :9090  ·  Grafana :3000  ·  Redis :6379           │
└──────────────────────────────────────────────────────────────────┘
```

> 📊 **Visual architecture diagram:** [View on Napkin.ai](https://app.napkin.ai/page/CgoiCHByb2Qtb25lEiwKBFBhZ2UaJDFhYjJkNTlkLTJkZDEtNDJlNC1hYzc1LWI4NGM2NDg3YjMwYw?s=1)

---

## 🔧 Microservices

| Service | Port | What it does |
|---|---|---|
| `api-gateway` | **8080** | Entry point — JWT auth, Redis rate limiting, routing |
| `webhook-service` | **8081** | Receives GitHub webhooks, validates HMAC-SHA256, publishes to Kafka |
| `code-fetch-service` | **8082** | Consumes PR events, calls GitHub API, fetches file diffs |
| `code-analysis-service` | **8083** | Parses unified diffs, chunks large files, prepares LLM prompts |
| `ai-review-service` | **8084** | Calls Groq LLM, parses response, publishes structured ReviewResults |
| `review-storage-service` | **8085** | Persists all reviews to PostgreSQL, exposes search REST API |
| `notification-service` | **8086** | Posts inline comments on GitHub PR, sends Slack summary |
| `eureka-server` | **8761** | Service registry — all services register and discover via `lb://` URIs |

---

## ⚙️ How It Works — Step by Step

```
Step 1  ──  Developer opens a Pull Request on GitHub
Step 2  ──  GitHub fires webhook → POST /webhook/github  (HMAC-SHA256 validated)
Step 3  ──  API Gateway routes to Webhook Service
Step 4  ──  Webhook Service publishes PullRequestEvent → Kafka topic: pr-events
Step 5  ──  Code Fetch Service consumes pr-events → calls GitHub API → fetches diffs
Step 6  ──  Code Fetch Service publishes raw diff → Kafka topic: code-analysis-tasks
Step 7  ──  Code Analysis Service parses hunks, extracts changed lines, chunks large files
Step 8  ──  Code Analysis Service publishes ReviewRequest list → Kafka topic: review-tasks
Step 9  ──  AI Review Service queries Weaviate for codebase context (RAG)
Step 10 ──  AI Review Service builds prompt → calls Groq LLM → parses structured JSON
Step 11 ──  AI applies severity scoring → publishes ReviewResult → review-results
Step 12 ──  Review Storage Service saves to PostgreSQL (Flyway-managed schema)
Step 13 ──  Notification Service posts inline comments on GitHub PR + Slack summary
```

> 🔗 **Correlation IDs** propagate through all Kafka messages for full end-to-end traceability.

---

## 🌟 Key Features

### 🔒 Security First

- **HMAC-SHA256 Webhook Validation** — every GitHub event is authenticated before processing begins
- **JWT Authentication** — all `/api/**` routes protected via Spring Cloud Gateway GlobalFilter
- **Redis Rate Limiting** — 10 req/s per IP with a 20-request burst; returns `429` on exceeded
- **OWASP Dependency Check** — automated vulnerability scanning in CI

### 🤖 AI & RAG Pipeline

- **Multi-LLM Support** — switch between Groq, OpenAI, and Ollama via a single config flag; no code changes needed
- **RAG Context** — first PR indexes your codebase into Weaviate; subsequent reviews retrieve top-5 similar snippets as context, giving the AI knowledge of your project's patterns
- **Structured LLM Output** — the model returns `[{file, line, severity, comment, category}]` JSON; malformed responses are handled gracefully with fallback parsing
- **Language-Aware Prompts** — Java (Checkstyle rules), Python (PEP8), JavaScript (ESLint) rules are injected per file type
- **Multi-Language** — Java, Python, JavaScript, TypeScript, SQL

### 📊 Observability

- **Micrometer Metrics** — JVM, HTTP, Kafka, and Redis metrics exported from every service
- **Prometheus Scraping** — all 7 services scraped at `/actuator/prometheus`
- **Grafana Dashboards** — three pre-built dashboards: System Overview, Kafka Consumer Lag, LLM Latency & Review Throughput

### 🔄 Resilience

- **Event-Driven** — Kafka decouples every service; failures in one don't cascade
- **Retry Logic** — notification delivery retried with exponential backoff
- **Kubernetes HPA** — `ai-review-service` auto-scales under load
- **Full Audit Trail** — every review persisted with searchable history

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.x, Spring Cloud |
| **AI / LLM** | Groq API (OpenAI-compatible) · switchable to OpenAI / Ollama |
| **Messaging** | Apache Kafka 7.5 — 4 topics |
| **API Gateway** | Spring Cloud Gateway |
| **Service Discovery** | Netflix Eureka |
| **Database** | PostgreSQL 16 + Flyway migrations |
| **Vector DB** | Weaviate — RAG context retrieval |
| **Cache / Rate Limiting** | Redis 7 |
| **Security** | JWT (JJWT 0.12.3) · HMAC-SHA256 webhook validation |
| **Monitoring** | Micrometer + Prometheus + Grafana |
| **Containerization** | Docker + Docker Compose |
| **Orchestration** | Kubernetes (Deployments, Services, HPA, ConfigMaps) |
| **CI/CD** | GitHub Actions → GHCR |
| **Testing** | JUnit 5 · Mockito · WireMock · Testcontainers · k6 |
| **Build** | Maven (multi-module) |

---

## 🚀 Getting Started

### Prerequisites

- Docker Desktop (4 GB+ RAM allocated)
- Java 21
- Maven 3.9+

### 1. Configure Environment

Create a `.env` file in the project root:

```env
# GitHub
GITHUB_TOKEN=ghp_your_personal_access_token
WEBHOOK_SECRET=your-webhook-secret

# Groq (free at console.groq.com)
GROQ_API_KEY=gsk_your_groq_api_key

# JWT
JWT_SECRET=your-256-bit-secret-key

# Slack (optional)
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/xxx/yyy/zzz
```

### 2a. Development Mode (infra only)

Run infrastructure in Docker; run services locally in your IDE:

```bash
# Start Kafka, PostgreSQL, Redis, Eureka, Weaviate
docker-compose up -d

# Then run any service individually
mvn spring-boot:run -pl webhook-service
```

### 2b. Production Mode (full stack)

```bash
# Build all JARs
mvn clean package -DskipTests

# Start all 16 containers
docker-compose -f docker-compose.prod.yml up -d --build
```

### 3. Verify Everything is Running

```bash
# Platform health check
curl http://localhost:8080/actuator/health

# Service registry — see all registered services
open http://localhost:8761

# Grafana dashboards (admin / admin)
open http://localhost:3000

# Prometheus targets
open http://localhost:9090/targets
```

### 4. Fire a Test Webhook (PowerShell)

```powershell
$body = '{"action":"opened","number":1,"pull_request":{"id":1,"number":1,"title":"Test Review","state":"open","head":{"sha":"abc123","ref":"feature/test","repo":{"clone_url":"https://github.com/your-org/your-repo.git"}},"base":{"ref":"main"},"user":{"login":"your-username"}},"repository":{"id":123,"name":"your-repo","full_name":"your-org/your-repo","clone_url":"https://github.com/your-org/your-repo.git"}}'

[System.IO.File]::WriteAllText("payload.json", $body, [System.Text.Encoding]::UTF8)
$bytes  = [System.IO.File]::ReadAllBytes("payload.json")
$hmac   = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key = [System.Text.Encoding]::UTF8.GetBytes("your-webhook-secret")
$hash   = $hmac.ComputeHash($bytes)
$signature = "sha256=" + [BitConverter]::ToString($hash).Replace("-","").ToLower()

curl.exe -X POST http://localhost:8080/webhook/github `
  -H "Content-Type: application/json" `
  -H "X-GitHub-Event: pull_request" `
  -H "X-Hub-Signature-256: $signature" `
  --data-binary "@payload.json"
```

---

## 📡 API Reference

All `/api/**` routes require: `Authorization: Bearer <token>`

### Webhook

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/webhook/github` | Receive GitHub webhook events |

### Reviews

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/reviews/{id}` | Get a single review by ID |
| `GET` | `/api/reviews/pr/{prNumber}` | Get all reviews for a PR |
| `GET` | `/api/reviews` | List reviews (paginated) |

### Developer Dashboard

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard/stats` | Review stats per developer |
| `GET` | `/api/dashboard/trends` | Severity trend over time |
| `GET` | `/api/dashboard/top-issues` | Top recurring issues by repository |

---

## 📈 Monitoring & Observability

Three pre-built Grafana dashboards at `http://localhost:3000`:

| Dashboard | What it shows |
|---|---|
| **System Overview** | JVM heap, CPU usage, HTTP request rates, thread counts per service |
| **Kafka Consumer Lag** | Consumer group lag per topic; alerts if lag exceeds threshold |
| **LLM Latency & Throughput** | AI review processing time, reviews/min, Groq API response times |

---

## 🧪 Testing

| Type | Tool | What's covered |
|---|---|---|
| **Unit Tests** | JUnit 5 + Mockito | DiffParser, CodeChunker, SeverityScorer, PromptTemplate |
| **End-to-End Tests** | WireMock | Stubs GitHub API; asserts PR comment posted + DB record created |
| **Load Tests** | k6 | 50 concurrent PRs; Kafka lag < 5 s verified |

---

## 📁 Project Structure

```
ai-code-review-app/
├── common/                          # Shared DTOs, enums, Jackson config
├── api-gateway/                     # Spring Cloud Gateway + JWT + Redis rate limiting
├── webhook-service/                 # GitHub webhook receiver + HMAC validation
├── code-fetch-service/              # GitHub API client + diff fetcher
├── code-analysis-service/           # Unified diff parser + code chunker
├── ai-review-service/               # Groq LLM client + RAG + prompt engine
├── review-storage-service/          # PostgreSQL persistence + REST query API
├── notification-service/            # GitHub PR inline comments + Slack
├── eureka-server/                   # Netflix Eureka service registry
├── monitoring/
│   ├── prometheus.yml               # Scrape config for all 7 services
│   └── grafana/dashboards/          # JSON dashboard definitions
├── k8s/
│   ├── deployments/
│   ├── services/
│   ├── configmaps/
│   └── hpa/                         # HorizontalPodAutoscaler for ai-review-service
├── docker-compose.yml               # Dev: infrastructure only
├── docker-compose.prod.yml          # Prod: full stack (16 containers)
└── pom.xml                          # Root Maven POM (multi-module)
```

---

## 🗓 Development Journey

Built over a structured **42-day plan** across 8 phases:

| Phase | Days | Focus |
|---|---|---|
| **1 — Foundation** | 1–5 | Multi-module Maven setup, Docker, Flyway, HMAC webhook |
| **2 — Core Services** | 6–12 | Kafka pipeline, GitHub API client, diff parser, code chunker |
| **3 — AI Integration** | 13–18 | Groq LLM, prompt engineering, multi-LLM support, severity scoring |
| **4 — Storage & Notify** | 19–23 | PostgreSQL persistence, GitHub PR comments, Slack, retry logic |
| **5 — Gateway & Security** | 24–27 | Spring Cloud Gateway, JWT filter, Redis rate limiting, Eureka |
| **6 — Advanced Features** | 28–33 | Weaviate RAG, multi-language prompts, React developer dashboard |
| **7 — Testing & QA** | 34–38 | Unit tests, WireMock E2E, k6 load testing, OWASP security audit |
| **8 — DevOps** | 39–42 | Production Docker Compose, Kubernetes manifests, GitHub Actions CI/CD, Grafana |

---

## 👨‍💻 Author

<div align="center">

**Nikhil Keshri**

Associate Software Engineer · B.Tech CSE 2025 · Oracle Cloud Infrastructure Certified

[![GitHub](https://img.shields.io/badge/GitHub-Nikhil--keshri2213-181717?style=for-the-badge&logo=github)](https://github.com/Nikhil-keshri2213)

</div>

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).