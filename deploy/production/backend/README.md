# Deploy de producao do backend

Esta pasta contem os artefatos minimos para subir o backend na VPS sem clonar o codigo-fonte do projeto.

## Arquivos para copiar para a VPS

Copie para `/opt/marianadias/backend`:

- `docker-compose.prod.yml`
- `.env.production.example` (renomeie para `.env` e ajuste os valores)
- `keycloak/rest-ms-realm.json`
- `postgres/init/01-create-keycloak-schema.sql`

## Imagem Docker publicada pelo GitHub Actions

O workflow publica a API no GHCR em:

- `ghcr.io/clinica-odontologica-mariana/com-rest-ms:develop`
- `ghcr.io/clinica-odontologica-mariana/com-rest-ms:main`
- `ghcr.io/clinica-odontologica-mariana/com-rest-ms:latest` (somente branch `main`)
- `ghcr.io/clinica-odontologica-mariana/com-rest-ms:sha-...`

Para producao, o arquivo `.env` deve apontar para uma tag estavel, por exemplo:

```env
REST_MS_IMAGE=ghcr.io/clinica-odontologica-mariana/com-rest-ms:main
```

## Banco de dados

A stack usa um unico container PostgreSQL e um unico database compartilhado entre:

- aplicacao `rest-ms`, no schema `public`
- Keycloak, no schema `keycloak`

Isso simplifica a operacao na VPS e evita conflito do Flyway com as tabelas do Keycloak dentro do mesmo database.

Se voce ja subiu a stack antes sem esse schema separado, derrube os volumes antes de subir novamente:

```bash
docker compose -f docker-compose.prod.yml down -v
```

## Variaveis obrigatorias na pratica

No arquivo `.env`, para esse compose funcionar como esta hoje, voce precisa preencher de fato:

- `REST_MS_IMAGE`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `KEYCLOAK_PUBLIC_URL`
- `KEYCLOAK_BOOTSTRAP_ADMIN_USERNAME`
- `KEYCLOAK_BOOTSTRAP_ADMIN_PASSWORD`
- `KEYCLOAK_REALM`
- `KEYCLOAK_CLIENT_ID`
- `KEYCLOAK_CLIENT_SECRET`
- `KEYCLOAK_ADMIN_USERNAME`
- `KEYCLOAK_ADMIN_PASSWORD`
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`
- `MINIO_BUCKET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REFRESH_TOKEN`

`GOOGLE_CALENDAR_ID` e opcional neste compose e usa `primary` por default.

## Primeira subida na VPS

```bash
cd /opt/marianadias/backend
cp .env.production.example .env
# edite o .env

docker login ghcr.io

docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

## Proxy hosts no Nginx Proxy Manager

- `api.marianadias.odo.br` -> `marianadias-rest-ms:8080`
- `auth.marianadias.odo.br` -> `marianadias-keycloak:8080`

Ambos os containers precisam estar na rede Docker externa `webproxy`.

## Validacoes

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f rest-ms
docker compose -f docker-compose.prod.yml logs -f keycloak
```

- API health: `https://api.marianadias.odo.br/api/v1/actuator/health`
- Swagger: `https://api.marianadias.odo.br/api/v1/swagger-ui/index.html`
- Keycloak: `https://auth.marianadias.odo.br`
