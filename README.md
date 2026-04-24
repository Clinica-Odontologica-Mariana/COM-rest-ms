# COM-rest-ms

Aplicacao Spring Boot com PostgreSQL (Supabase ou local).

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

Endpoint inicial:

- `POST /api/v1/patients` -> cria paciente
- `GET /api/v1/patients` -> lista pacientes ativos
- `GET /api/v1/patients/{id}` -> busca paciente por id
- `PUT /api/v1/patients/{id}` -> atualiza paciente
- `DELETE /api/v1/patients/{id}` -> inativa paciente (soft delete)
- `GET /api/v1/patients/example` -> paciente de exemplo em JSON

## Requisitos

- Java 25
- Docker 24+
- Docker Compose (plugin `docker compose`)

## Banco de dados PostgreSQL (duas configuracoes)

A API pode ser executada com duas configuracoes de banco:

1. Supabase (remoto)
2. PostgreSQL local (container ou instancia local)

> Importante: mantenha credenciais reais somente no arquivo `.env` local (nao versionado).

## Configuracao local com `.env`

As variaveis locais ficam em `.env` (arquivo nao versionado). Copie o template:

```bash
cp .env.example .env
```

Depois, escolha uma das opcoes abaixo.

### Opcao A: Supabase (remoto) - [link](https://supabase.com/dashboard/project/jqllvpeqwwliztchfhwd)

```dotenv
SPRING_DATASOURCE_URL=jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres?user=postgres&password=<db-password>
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
```

### Opcao B: PostgreSQL local

Exemplo para aplicacao rodando localmente (sem container da API):

```dotenv
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/clinica-mariana
SPRING_DATASOURCE_USERNAME=rest_user
SPRING_DATASOURCE_PASSWORD=rest_password
```

## Rodando com Docker Compose

```bash
docker compose up --build
```

A API fica disponivel em `http://localhost:8080`.

Banco de dados: definido por `SPRING_DATASOURCE_URL` (Supabase ou PostgreSQL local).

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

## Testando endpoint de exemplo do patient

Com a aplicacao rodando na porta 8080:

```bash
curl -s http://localhost:8080/api/v1/patients/example
```

## Criando paciente

```bash
curl -s -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -d '{
	"fullName": "Maria Silva",
	"cpf": "12345678901",
	"phone": "11999999999",
	"email": "maria.silva@clinic.com",
	"birthDate": "1990-01-10"
  }'
```
