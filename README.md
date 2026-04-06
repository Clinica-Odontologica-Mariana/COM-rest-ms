# COM-rest-ms

Aplicacao Spring Boot com PostgreSQL gerenciado por container Docker.

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

- Java 25
- Docker 24+
- Docker Compose (plugin `docker compose`)

## Banco de dados PostgreSQL

O banco e criado em container com dados persistidos em volume Docker (`postgres_data`).

Na primeira subida do banco, os scripts em `docker/postgres/init` sao executados automaticamente:

- `01-schema.sql`: cria extensao, tabelas e indices.

No momento, a inicializacao automatica esta configurada para subir somente a estrutura (sem carga de dados).

> Importante: scripts em `/docker-entrypoint-initdb.d` rodam apenas quando o volume do Postgres esta vazio.

## Configuracao local com `.env`

As variaveis locais ficam em `.env` (arquivo nao versionado). Copie o template e ajuste `POSTGRES_*` e
`SPRING_DATASOURCE_*`:

```bash
cp .env.example .env
```

## Rodando com Docker Compose

```bash
docker compose up --build
```

A API fica disponivel em `http://localhost:8080`.

Banco PostgreSQL de DESENVOLVIMENTO: `localhost:5432`

Para rodar em background:

```bash
docker compose up --build -d
```

Para parar:

```bash
docker compose down
```

Para remover containers e limpar o volume do banco (forcar nova carga de schema/dados):

```bash
docker compose down -v
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
