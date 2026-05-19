# COM-rest-ms

Aplicacao Spring Boot com PostgreSQL e Keycloak (RBAC com JWT).

## Stack e padroes implementados

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
- `GET /api/v1/patients/cpf/{cpf}` (`ADMIN`, `RECEPTIONIST`, `DOCTOR`)
- `PUT /api/v1/patients/{id}` (`ADMIN`, `RECEPTIONIST`)
- `DELETE /api/v1/patients/{id}` (`ADMIN`)

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

## Rodando com Docker Compose

```bash
docker compose up --build
```

Servicos:

- API: `http://localhost:8080`
- Keycloak: `http://localhost:8081`
- PostgreSQL: `localhost:5432`

O Keycloak importa automaticamente o realm `rest-ms` via:

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

## Producao (importante)

- Nao use `start-dev` em producao.
- Use Keycloak com banco persistente dedicado (PostgreSQL/MySQL) e backup.
- Nao use `docker compose down -v` em ambiente produtivo.

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
