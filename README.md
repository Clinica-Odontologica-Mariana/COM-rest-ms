# COM-rest-ms

Aplicacao Spring Boot pronta para execucao em container Docker.

## Estrutura arquitetural (MVC em camadas)

```text
src/main/java/com/clinica/mariana/restms
├── controller
│   └── HelloController.java
└── RestMsApplication.java
```

Endpoint inicial:

- `GET /api/v1/hello` -> `Hello World`

## Requisitos

- Docker 24+
- Docker Compose (plugin `docker compose`)

## Rodando com Docker Compose

```bash
docker compose up --build
```

A API fica disponivel em `http://localhost:8080`.

Para rodar em background:

```bash
docker compose up --build -d
```

Para parar:

```bash
docker compose down
```

## Rodando com Docker (sem Compose)

```bash
docker build -t rest-ms:local .
docker run --rm -p 8080:8080 --name rest-ms rest-ms:local
```

## Testando o Hello World

Com a aplicacao rodando na porta 8080:

```bash
curl -s http://localhost:8080/api/v1/hello
```
