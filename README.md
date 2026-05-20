# COM-rest-ms

Aplicacao Spring Boot com PostgreSQL e Keycloak (RBAC com JWT).

## Stack e padroes implementados

Aplicacao Spring Boot com PostgreSQL (Supabase ou local) e Flyway para migrations de banco.

## Estrutura arquitetural (MVC em camadas)

```text
src/main/java/com/clinica/mariana/restms
├── config
│   └── .gitkeep
├── patient
│   ├── model
│   │   └── PatientModel.java
│   ├── view
│   │   └── PatientView.java
│   └── controller
│       └── PatientController.java
└── RestMsApplication.java

src/test/java/com/clinica/mariana/restms
└── patient
	└── test
		└── PatientControllerTest.java
```

- Spring Security como guard global (`SecurityFilterChain`)
- RBAC com `@RolesAllowed`
- Usuario autenticado via `@AuthenticationPrincipal Jwt`
- Interceptor HTTP para logging de request (`RequestLoggingInterceptor`)
- Envelope global de resposta:
  - sucesso: `{ "success": true, "data": ... }`
  - erro: `{ "success": false, "error": ... }`
- Keycloak como fonte unica de autenticacao/usuarios

## Estrutura simplificada

- `auth`: login e usuario autenticado (`/auth/login`, `/auth/me`)
- `users`: criacao de usuarios no Keycloak (`/users`)
- `patient`: dominio da clinica (CRUD de pacientes)
- `security`: configuracao de autenticacao/autorizacao
- `common`: padrao de resposta e tratamento global de erros

## Endpoints

### Auth

- `POST /api/v1/auth/login` (publico)
- `GET /api/v1/auth/me` (autenticado, retorna claims principais do token)
- `POST /api/v1/users` (somente `ADMIN`) -> cria usuario no Keycloak e atribui role existente

### Patients

- `POST /api/v1/patients` (`ADMIN`, `RECEPTIONIST`)
- `GET /api/v1/patients` (`ADMIN`, `RECEPTIONIST`, `DOCTOR`)
- `GET /api/v1/patients/{id}` (`ADMIN`, `RECEPTIONIST`, `DOCTOR`)
- `PUT /api/v1/patients/{id}` (`ADMIN`, `RECEPTIONIST`)
- `DELETE /api/v1/patients/{id}` (`ADMIN`)

As respostas REST sao envelopadas em `{ "success": true, "data": ... }` para sucesso e
`{ "success": false, "error": ... }` para erro.

## Variaveis de ambiente

Copie o template:

```bash
cp .env.example .env
```

Variaveis obrigatorias (validadas no startup com `@ConfigurationProperties` + `@Validated`):

- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`
- `KEYCLOAK_BASE_URL`
- `KEYCLOAK_REALM`
- `KEYCLOAK_CLIENT_ID`
- `KEYCLOAK_CLIENT_SECRET`
- `KEYCLOAK_ADMIN_USERNAME`
- `KEYCLOAK_ADMIN_PASSWORD`

Se alguma estiver ausente/invalida, a aplicacao falha na inicializacao com erro claro no terminal.

`SPRING_DATASOURCE_URL` aceita tanto o formato JDBC (`jdbc:postgresql://...`) quanto
o formato `postgresql://usuario:senha@host:porta/banco` comum em provedores como Supabase.

Para Docker Compose, prefira `SPRING_DATASOURCE_DOCKER_URL`,
`SPRING_DATASOURCE_DOCKER_USERNAME` e `SPRING_DATASOURCE_DOCKER_PASSWORD` quando precisar
sobrescrever a conexao interna entre containers. Isso evita conflito com `SPRING_DATASOURCE_URL`
local apontando para `localhost`.

## Rodando com Docker Compose

O Compose possui defaults de desenvolvimento equivalentes ao `.env.example`, entao pode
subir sem `.env`. Para customizar credenciais, copie o template:

```bash
cp .env.example .env
```

```bash
docker compose up --build
```

Servicos:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/api/v1/swagger-ui/index.html`
- Keycloak: `http://localhost:8081`
- PostgreSQL: `localhost:5432`

O schema inicial e aplicado pelo Flyway a partir de `src/main/resources/db/migration`.
Para bancos ja inicializados antes do Flyway, `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` permite registrar uma baseline sem recriar as tabelas existentes.

Para rodar em background:

- `docker/keycloak/rest-ms-realm.json`

Persistencia local do Keycloak:

- dados ficam no volume Docker `keycloak_data`
- `docker compose up/down` preserva usuarios e roles
- `docker compose down -v` remove volumes e apaga os dados (incluindo Keycloak e PostgreSQL)

Roles preconfiguradas no realm:

- `ADMIN`
- `RECEPTIONIST`
- `DOCTOR`

Usuario admin de API (local):

- username: `api-admin`
- password: `api-admin123`

Essas credenciais e o `KEYCLOAK_CLIENT_SECRET` do realm versionado sao fixtures locais
para desenvolvimento. Nao reutilize esses valores em homologacao ou producao.

## Mudancas de contrato

- Endpoints protegidos exigem `Authorization: Bearer <jwt>`.
- Respostas passam a usar envelope global `success/data/error`.
- `DELETE /api/v1/patients/{id}` retorna envelope de sucesso.

## Producao (importante)

- Nao use `start-dev` em producao.
- Use Keycloak com banco persistente dedicado (PostgreSQL/MySQL) e backup.
- Nao use `docker compose down -v` em ambiente produtivo.
- Gere secrets e usuarios administrativos proprios para cada ambiente.

## Exemplos cURL

### Login

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "api-admin",
    "password": "api-admin123"
  }'
```

### Criar usuario (ADMIN)

```bash
TOKEN="<access_token>"

curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joao",
    "email": "joao@clinic.local",
    "firstName": "Joao",
    "lastName": "Silva",
    "password": "SenhaForte123",
    "role": "DOCTOR"
  }'
```
