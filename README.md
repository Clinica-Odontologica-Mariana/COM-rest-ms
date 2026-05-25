# COM-rest-ms

Aplicacao Spring Boot com PostgreSQL e Keycloak (RBAC com JWT).

![CI-CD](https://github.com/Clinica-Odontologica-Mariana/COM-rest-ms/actions/workflows/cicd.yml/badge.svg)
![Coverage](https://codecov.io/gh/Clinica-Odontologica-Mariana/COM-rest-ms/graph/badge.svg)

## Stack e padroes implementados

Aplicacao Spring Boot com PostgreSQL (Supabase ou local) e Flyway para migrations de banco.

## Exemplo da Estrutura arquitetural (MVC em camadas)

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
- `professional`: profissionais vinculados a usuarios, clinicas e especialidades
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

### Professionals

- `POST /api/v1/professionals` (`ADMIN`, `RECEPTIONIST`)
- `GET /api/v1/professionals` (`ADMIN`, `RECEPTIONIST`, `DOCTOR`)
- `GET /api/v1/professionals/{id}` (`ADMIN`, `RECEPTIONIST`, `DOCTOR`)
- `PUT /api/v1/professionals/{id}` (`ADMIN`, `RECEPTIONIST`)
- `DELETE /api/v1/professionals/{id}` (`ADMIN`)

As respostas REST sao envelopadas em `{ "success": true, "data": ... }` para sucesso e
`{ "success": false, "error": ... }` para erro.

## CI/CD (base SDD)

O projeto possui uma base inicial de CI/CD para validar PRs e branches principais sem deploy real.

Workflow atual:

- `cicd.yml` (workflow unico `CI-CD`):
  - `pull_request` para `develop` e `main`
  - `push` para `develop` e `main`
  - `workflow_dispatch` para placeholder manual de deploy futuro
  - job de Gradle: `chmod +x ./gradlew`, `./gradlew test --no-daemon`,
    `./gradlew check --no-daemon`, `./gradlew build --no-daemon`
  - step de Docker: `docker build -t com-rest-ms:ci .`, sem push de imagem
  - step manual de notas para deploy futuro, sem deploy real

Boas praticas aplicadas no CI/CD:

- `permissions` minimas (`contents: read`)
- `concurrency` para cancelar execucoes antigas por branch/workflow
- `timeout-minutes` por job
- nenhum deploy em PR
- nenhuma exigencia de secret real para CI basico
- imagem Docker validada com `push: false`

Observacoes importantes para o pipeline atual:

- Os testes automatizados usam H2 em memoria com modo PostgreSQL.
- Flyway esta desabilitado nos testes (`spring.flyway.enabled=false` em perfil de teste/task Gradle); a migration
  principal roda no startup da aplicacao com PostgreSQL, como no ambiente Docker Compose.
- Testes de seguranca usam JWT mockado (`spring-security-test`), sem necessidade de subir Keycloak no CI.
- Nao ha dependencia ativa de MinIO nos testes atuais.
- O pipeline nao sobe PostgreSQL, Keycloak ou MinIO porque os testes atuais nao dependem desses services externos.

Comandos locais equivalentes ao CI:

```bash
./gradlew test --no-daemon
./gradlew check --no-daemon
./gradlew build --no-daemon
docker build -t com-rest-ms:local .
```

### Secrets e variaveis para deploy futuro (placeholder)

O deploy real ainda nao esta ativado. Quando a hospedagem for definida, os secrets abaixo devem ser configurados
conforme estrategia final:

- Registry: `REGISTRY_USERNAME`, `REGISTRY_PASSWORD`, `GHCR_TOKEN`
- Host: `HOST`, `HOST_USER`, `HOST_SSH_KEY`, `HOST_PORT`
- Banco: `PROD_ENV_FILE`, `PROD_DATABASE_URL`, `PROD_DATABASE_USERNAME`, `PROD_DATABASE_PASSWORD`
- Keycloak: `KEYCLOAK_ISSUER_URI`, `KEYCLOAK_JWK_SET_URI`
- MinIO: `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET`

Acoplamento futuro previsto:

1. publicar imagem no registry definido;
2. autenticar no host por SSH;
3. atualizar stack/container com variaveis de producao;
4. aplicar estrategia de healthcheck e rollback.

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
- MinIO API: `http://localhost:9000`
- MinIO Console: `http://localhost:9001`

O schema inicial e aplicado pelo Flyway a partir de `src/main/resources/db/migration`.
O container do PostgreSQL nao executa scripts em `docker-entrypoint-initdb.d`; ele sobe
apenas o banco vazio e a aplicacao aplica as migrations ao iniciar.

Se voce ja possui um volume local antigo criado a partir de SQL de bootstrap, recrie o
volume uma vez para alinhar com Flyway:

```bash
docker compose down -v
docker compose up --build
```

Evite `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` no desenvolvimento comum, pois isso pode
registrar uma baseline em um schema legado e pular a migration inicial.

Arquivos de apoio:

- `.env.example` para copiar e ajustar as variaveis locais
- `.env` para desenvolvimento local com Docker Compose

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

## Stored files e MinIO

O modulo `storedfile` guarda binarios no MinIO e persiste no PostgreSQL apenas metadados e vinculos.
O banco nao armazena o conteudo dos arquivos nem URLs publicas permanentes.

Categorias suportadas:

- `USER_PROFILE_PHOTO`: fotos de perfil de usuarios do app.
- `ODONTOGRAM`: arquivos de odontograma vinculados a paciente/prontuario/entrada de odontograma.

Variaveis principais:

- `MINIO_ENDPOINT`: endpoint S3/MinIO usado pela API.
- `MINIO_ACCESS_KEY`: usuario de acesso MinIO.
- `MINIO_SECRET_KEY`: senha/chave MinIO.
- `MINIO_BUCKET`: bucket usado pela aplicacao.
- `MINIO_REGION`: regiao S3 opcional.
- `MINIO_PRESIGNED_URL_EXPIRATION_SECONDS`: expiracao das URLs presignadas.
- `APP_FILES_PROFILE_PHOTO_MAX_SIZE_BYTES`: limite para foto de perfil.
- `APP_FILES_PROFILE_PHOTO_ALLOWED_MIME_TYPES`: MIME types aceitos para foto.
- `APP_FILES_ODONTOGRAM_MAX_SIZE_BYTES`: limite para odontograma.
- `APP_FILES_ODONTOGRAM_ALLOWED_MIME_TYPES`: MIME types aceitos para odontograma.

Defaults de desenvolvimento:

- foto de perfil: `image/jpeg`, `image/png`, `image/webp`, ate 5 MB.
- odontograma: `image/jpeg`, `image/png`, `image/webp`, `application/pdf`, ate 10 MB.

Endpoints principais:

| Metodo   | Endpoint                                             | Uso                                                   |
| -------- | ---------------------------------------------------- | ----------------------------------------------------- |
| `POST`   | `/api/v1/users/me/profile-photo`                     | Envia/substitui a propria foto de perfil.             |
| `GET`    | `/api/v1/users/me/profile-photo`                     | Consulta metadados da propria foto.                   |
| `GET`    | `/api/v1/users/me/profile-photo/download-url`        | Gera URL presignada temporaria.                       |
| `DELETE` | `/api/v1/users/me/profile-photo`                     | Remove a propria foto com hard delete.                |
| `POST`   | `/api/v1/users/{userId}/profile-photo`               | `ADMIN` envia/substitui foto de outro usuario.        |
| `GET`    | `/api/v1/users/{userId}/profile-photo`               | `ADMIN` consulta foto de outro usuario.               |
| `GET`    | `/api/v1/users/{userId}/profile-photo/download-url`  | `ADMIN` gera URL presignada.                          |
| `DELETE` | `/api/v1/users/{userId}/profile-photo`               | `ADMIN` remove foto de outro usuario com hard delete. |
| `POST`   | `/api/v1/stored-files/odontograms/{patientId}`       | `ADMIN`/`DOCTOR` envia odontograma.                   |
| `GET`    | `/api/v1/stored-files/odontograms/{id}`              | `ADMIN`/`DOCTOR` consulta metadados.                  |
| `GET`    | `/api/v1/stored-files/odontograms/{id}/download-url` | `ADMIN`/`DOCTOR` gera URL presignada.                 |
| `DELETE` | `/api/v1/stored-files/odontograms/{id}`              | `ADMIN`/`DOCTOR` remove odontograma com hard delete.  |
| `GET`    | `/api/v1/patients/{patientId}/odontogram-files`      | `ADMIN`/`DOCTOR` lista odontogramas do paciente.      |

Uploads usam `multipart/form-data` com o campo `file`. Odontogramas tambem recebem `patientId`
e podem receber `medicalRecordId`, `odontogramEntryId` e `description`.

As chaves dos objetos sao geradas pela API com UUID e nome sanitizado, por exemplo:

- `profile-photos/{userId}/{uuid}-{fileName}`
- `odontograms/{patientId}/{uuid}-{fileName}`

Remocoes de fotos e odontogramas usam hard delete: o vinculo, o metadado e o objeto no MinIO sao removidos.
Nao reutilize as credenciais de desenvolvimento do MinIO em homologacao ou producao.

O realm local versionado define `accessTokenLifespan=86400` e
`ssoSessionMaxLifespan=86400` para desenvolvimento, ou seja, sessoes e access tokens
de ate 24 horas. Revise essa duracao antes de usar em homologacao ou producao.

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
