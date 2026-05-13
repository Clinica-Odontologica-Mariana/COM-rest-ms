# Pull Request

## Resumo

Descreva objetivamente o que este PR altera e por quê.

Exemplo:
> Implementa o módulo de agendamentos com validação de conflito de horário e integração com o schema PostgreSQL via Flyway.

## Contexto

Explique o problema, requisito, issue ou decisão técnica que motivou a mudança.

- Issue relacionada:
- Documentação relacionada:
- Protótipo/Figma relacionado:
- Dependências externas:

## Tipo de alteração

Marque todas as opções aplicáveis.

- [ ] Correção de bug
- [ ] Nova feature
- [ ] Refatoração
- [ ] Alteração de schema/migration
- [ ] Alteração de configuração/infra
- [ ] Testes
- [ ] Documentação
- [ ] Segurança/autenticação
- [ ] Integração externa
- [ ] Outro:

## Escopo técnico

Descreva os principais arquivos, pacotes ou módulos alterados.

Exemplo:
- `patient/controller`
- `patient/service`
- `appointment/entity`
- `src/main/resources/db/migration`
- `config/SecurityConfig.java`

## Checklist de arquitetura

- [ ] Mantém o padrão de pacotes por domínio do projeto.
- [ ] Controllers não expõem entidades JPA diretamente.
- [ ] Regras de negócio estão em services, não em controllers.
- [ ] Repositories usam Spring Data JPA.
- [ ] DTOs são usados para entrada/saída da API.
- [ ] Operações de escrita usam transação quando necessário.
- [ ] Código novo segue o padrão já existente no módulo `patient`.
- [ ] Não foi criada arquitetura paralela ou pacote genérico desnecessário.

## Banco de dados / Flyway

Preencha esta seção se o PR altera schema, migrations ou entidades persistidas.

- [ ] Criei ou atualizei migration em `src/main/resources/db/migration`.
- [ ] A migration é compatível com PostgreSQL.
- [ ] A migration foi testada em banco limpo.
- [ ] Não há SQL destrutivo indevido em migration de produção.
- [ ] Não há conflito com migrations anteriores.
- [ ] Entidades JPA estão alinhadas com nomes de tabelas e colunas.
- [ ] Índices, constraints e FKs necessários foram adicionados.
- [ ] Seeds de tabelas de domínio foram adicionados quando necessário.
- [ ] Não usei `timerange`; para intervalos de `TIME`, usei `tsrange` com data fixa quando aplicável.

Comandos executados:

```bash
./gradlew clean build
# outros comandos:
```

## Keycloak / Segurança

Preencha se o PR altera autenticação, autorização ou usuários.

- [ ] Não foi adicionada autenticação local por senha.
- [ ] Não foi adicionado campo `password_hash` em `app_user`.
- [ ] Integração considera Keycloak como provedor de SSO.
- [ ] JWT/resource server foi mantido ou configurado corretamente.
- [ ] Roles/authorities foram tratadas de forma compatível com Keycloak.
- [ ] Endpoints protegidos exigem autenticação quando necessário.
- [ ] Endpoints públicos foram explicitamente justificados.

Observações de segurança:

```text
Descreva riscos, decisões ou pontos pendentes.
```

## MinIO / Arquivos

Preencha se o PR altera upload, download, imagens ou anexos.

- [ ] Arquivos não são persistidos diretamente no PostgreSQL.
- [ ] O banco guarda apenas metadados e referência ao objeto no MinIO.
- [ ] O vínculo com arquivos usa `stored_file` ou entidade equivalente.
- [ ] URLs públicas permanentes não foram persistidas como fonte principal.
- [ ] Downloads usam URL presignada ou mecanismo controlado.
- [ ] Upload valida nome, tamanho e tipo do arquivo quando aplicável.

## API

Liste endpoints criados, alterados ou removidos.

| Método | Endpoint | Descrição | Autenticação |
|---|---|---|---|
| `GET` | `/api/v1/...` | ... | Sim/Não |
| `POST` | `/api/v1/...` | ... | Sim/Não |

Mudanças incompatíveis:

- [ ] Não há breaking changes.
- [ ] Há breaking changes, descritas abaixo.

Descrição dos breaking changes:

```text
Descreva alterações que exigem ajuste no frontend, scripts, integrações ou dados.
```

## Testes

Descreva os testes adicionados ou atualizados.

- [ ] Testes unitários adicionados/atualizados.
- [ ] Testes de controller adicionados/atualizados.
- [ ] Testes de service adicionados/atualizados.
- [ ] Testes de repository adicionados/atualizados.
- [ ] Testes de integração adicionados/atualizados.
- [ ] Mocks foram usados para dependências externas como Keycloak e MinIO.
- [ ] Cenários de erro foram cobertos.

Comandos executados:

```bash
./gradlew test
./gradlew build
```

Resultado:

```text
Cole o resumo do resultado dos testes/build.
```

## Evidências

Inclua prints, logs, payloads ou respostas da API quando útil.

Exemplo de request/response:

```json
{
  "example": true
}
```

## Checklist de qualidade

- [ ] O código compila localmente.
- [ ] O build passa.
- [ ] Os testes passam.
- [ ] Não há código comentado desnecessário.
- [ ] Não há logs sensíveis.
- [ ] Não há secrets, tokens, senhas ou chaves commitadas.
- [ ] Nomes de classes, métodos e variáveis estão claros.
- [ ] Erros são tratados de forma consistente.
- [ ] README, `.env.example` ou documentação foram atualizados quando necessário.

## Impacto no frontend

- [ ] Não há impacto no frontend.
- [ ] Há impacto no frontend.

Descreva o impacto:

```text
Exemplo: novo campo obrigatório em criação de paciente; endpoint de busca por CPF mudou para `/api/v1/patients/by-cpf/{cpf}`.
```
