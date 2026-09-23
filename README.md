# API de Cursos Online

API REST em Java + Spring Boot com PostgreSQL, deploy automático na AWS via GitHub Actions.

## Rotas

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/cursos` | Lista todos os cursos **não deletados** |
| `GET` | `/cursos?nome=Java` | Mesma coisa, filtrando por nome que **começa com** "Java" |
| `POST` | `/cursos` | Cria um curso (retorna `201 Created`) |
| `DELETE` | `/cursos/{id}` | Deleção **lógica** (retorna `204 No Content`, ou `404` se não existir) |

Exemplo de body do POST:

```json
{
  "nome": "Java Basico",
  "descricao": "Curso introdutorio de Java",
  "instrutor": "Eduardo",
  "cargaHoraria": 40,
  "preco": 500.00
}
```

---

# PASSO A PASSO COMPLETO

Este é o roteiro na ordem em que se deve fazer na prova.

## Passo 1 — Criar o projeto

Use o [Spring Initializr](https://start.spring.io) com:

- **Project:** Maven · **Language:** Java · **Spring Boot:** 4.1.0
- **Group:** `br.insper` · **Artifact:** `curso` · **Java:** 25
- **Dependencies:** Spring Web, Spring Data JPA, PostgreSQL Driver, Lombok

Depois adicione no `pom.xml` o que o Initializr **não** traz (ver arquivo neste repo):

- `spring-boot-starter-webmvc-test`, `testcontainers`, `testcontainers-postgresql`, `testcontainers-junit-jupiter` (escopo `test`)
- plugin **JaCoCo** com `outputDirectory` em `${project.basedir}/tests` — é de lá que o workflow lê o `jacoco.xml`

> ⚠️ No Spring Boot 4.x o starter chama-se `spring-boot-starter-webmvc` (não `-web`), o `ObjectMapper` vem de `tools.jackson.databind` (não `com.fasterxml`), e o `@AutoConfigureMockMvc` está em `org.springframework.boot.webmvc.test.autoconfigure`. Copie do projeto de pagamento da aula se estiver na dúvida.

## Passo 2 — Estrutura de pastas

```
src/main/java/br/insper/curso/
├── CursoApplication.java
├── controller/CursoController.java
├── dto/CursoDto.java
├── entity/Curso.java
├── exception/CursoNaoEncontradoException.java
├── repository/CursoRepository.java
└── service/CursoService.java

src/test/java/br/insper/curso/
├── controller/CursoControllerTests.java   (integração, Testcontainers)
└── service/CursoServiceTests.java         (unitário, Mockito, 100%)
```

## Passo 3 — A entidade e a deleção lógica

O truque do exercício inteiro está num campo booleano:

```java
@Column(nullable = false)
private Boolean deletado;
```

- No `fromDto()` ele nasce `false`.
- O `DELETE` **não** chama `deleteById`. Ele busca o curso, faz `setDeletado(true)` e salva.
- O `GET` **nunca** consulta `findAll()`. Sempre filtra por `deletado = false`.

## Passo 4 — O repository (é aqui que mora a mágica)

Spring Data monta a query a partir do **nome do método** — não precisa escrever SQL:

```java
List<Curso> findByDeletadoFalse();
List<Curso> findByNomeStartingWithIgnoreCaseAndDeletadoFalse(String nome);
```

Decorar o padrão do nome:
`findBy` + `Nome` + `StartingWith` + `IgnoreCase` + `And` + `Deletado` + `False`

| Se o professor pedir | Use |
|---|---|
| começa com (startsWith) | `StartingWith` |
| termina com | `EndingWith` |
| contém | `Containing` |
| ignorar maiúsculas | `IgnoreCase` |

## Passo 5 — Service

```java
public List<Curso> listar(String nome) {
    if (nome != null && !nome.isBlank()) {
        return cursoRepository.findByNomeStartingWithIgnoreCaseAndDeletadoFalse(nome);
    }
    return cursoRepository.findByDeletadoFalse();
}
```

O `if` gera **dois branches** — precisa de teste para cada um, senão não fecha 100% no JaCoCo.

O `deletar` lança exception quando não acha:

```java
Curso curso = cursoRepository.findById(id)
    .orElseThrow(() -> new CursoNaoEncontradoException("Curso com ID " + id + " nao encontrado"));
curso.setDeletado(true);
cursoRepository.save(curso);
```

A exception tem `@ResponseStatus(HttpStatus.NOT_FOUND)` — assim o Spring devolve 404 sozinho, sem precisar de `@ControllerAdvice`.

## Passo 6 — Controller

O filtro é **opcional**, então `required = false`:

```java
@GetMapping
public List<Curso> listar(@RequestParam(required = false) String nome) {
    return cursoService.listar(nome);
}
```

## Passo 7 — Banco de dados

### 7.1 — Local (Docker, para desenvolver)

Suba um Postgres com o `docker-compose.yml` deste repo:

```bash
docker compose up -d
```

Ou sem compose, em um comando só:

```bash
docker run --name curso-postgres -e POSTGRES_DB=cursodb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:15-alpine
```

Comandos úteis:

```bash
docker ps                                   # ver se está rodando
docker logs curso-postgres                  # ver logs
docker exec -it curso-postgres psql -U postgres -d cursodb   # entrar no psql
docker stop curso-postgres                  # parar
docker rm curso-postgres                    # remover
```

Dentro do `psql`, para conferir se a deleção é mesmo lógica:

```sql
\dt                          -- lista as tabelas
SELECT * FROM cursos;        -- o curso deletado CONTINUA aqui, com deletado = true
```

### 7.2 — Rodar a aplicação local

O `application.properties` **não tem senha escrita** — lê tudo de variável de ambiente:

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:cursodb}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

`ddl-auto=update` faz o Hibernate criar a tabela `cursos` sozinho — não precisa escrever DDL.

PowerShell:

```powershell
$env:DB_USER="postgres"; $env:DB_PASSWORD="postgres"; ./mvnw spring-boot:run
```

Git Bash:

```bash
DB_USER=postgres DB_PASSWORD=postgres ./mvnw spring-boot:run
```

Ou, no IntelliJ: *Run → Edit Configurations → Environment variables* → `DB_USER=postgres;DB_PASSWORD=postgres`.

### 7.3 — Postgres na máquina da AWS

Conecte na EC2 e suba o banco em container:

```bash
ssh -i sua-chave.pem ubuntu@<IP_DA_EC2>

# instalar docker (só na primeira vez)
sudo apt update && sudo apt install -y docker.io
sudo usermod -aG docker ubuntu && newgrp docker

# subir o postgres
docker run --name curso-postgres \
  -e POSTGRES_DB=cursodb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=UMA_SENHA_FORTE \
  -p 5432:5432 \
  --restart always \
  -d postgres:15-alpine
```

No **Security Group** da EC2 libere as portas de entrada:

| Porta | Para quê |
|---|---|
| 22 | SSH (o GitHub Actions usa para o deploy) |
| 8080 | a API |
| 5432 | o Postgres (pode deixar só interno se app e banco estão na mesma máquina) |

> Se a aplicação e o banco rodam na **mesma EC2**, o `DB_HOST` continua sendo o IP privado da máquina (ou use `--network host` no `docker run` da app).

## Passo 8 — Secrets e variáveis no GitHub

No repositório: **Settings → Secrets and variables → Actions**.

### Aba "Secrets" (dados sensíveis, ficam mascarados nos logs)

| Nome | Valor |
|---|---|
| `DOCKERHUB_TOKEN` | token gerado no Docker Hub (*Account Settings → Personal access tokens*) |
| `AWS_HOST` | IP público da EC2 |
| `AWS_SSH_KEY` | conteúdo **inteiro** do arquivo `.pem`, incluindo `-----BEGIN...-----` e `-----END...-----` |
| `DB_USER` | usuário do banco |
| `DB_PASSWORD` | senha do banco |
| `DB_HOST` | IP/host do banco |

### Aba "Variables" (não sensível, aparece no log)

| Nome | Valor |
|---|---|
| `DOCKERHUB_USERNAME` | seu usuário do Docker Hub |
| `AWS_USER` | `ubuntu` |
| `DB_NAME` | `cursodb` |
| `DB_PORT` | `5432` |

No workflow: secret → `${{ secrets.NOME }}` · variável → `${{ vars.NOME }}`.

> `GITHUB_TOKEN` já existe automaticamente, não precisa criar.

## Passo 9 — Os dois workflows

**`.github/workflows/tests.yml`** — roda em **pull request** para `main`: `mvn clean install` (executa os testes) e depois o `madrapps/jacoco-report` lê `tests/jacoco.xml` e falha o build se a cobertura cair abaixo do mínimo.

**`.github/workflows/deploy.yml`** — roda em **push** para `main`: build → imagem Docker → push pro Docker Hub → SSH na EC2 → `docker run` com as variáveis de ambiente do banco injetadas.

Repare no `deploy.yml`: nenhuma senha ou IP está escrito no arquivo, tudo vem de `secrets`/`vars`, e são passados para o container pelo bloco `envs:` + `-e`.

## Passo 10 — Testes

### Service (100% de cobertura, Mockito puro, sem subir Spring)

`@ExtendWith(MockitoExtension.class)` + `@InjectMocks` no service + `@Mock` no repository.

Checklist do que precisa de teste para fechar 100%:

- [x] `criar`
- [x] `listar(null)` → branch sem filtro
- [x] `listar("   ")` → branch do `isBlank()`
- [x] `listar("Java")` → branch com filtro
- [x] `deletar(id)` que existe → verifica `deletado = true`
- [x] `deletar(id)` que não existe → verifica a exception

O `ArgumentCaptor` é o que prova que a deleção foi lógica:

```java
ArgumentCaptor<Curso> captor = ArgumentCaptor.forClass(Curso.class);
Mockito.verify(cursoRepository).save(captor.capture());
Assertions.assertTrue(captor.getValue().getDeletado());
Mockito.verify(cursoRepository, Mockito.never()).deleteById(Mockito.any());
```

### Integração (Testcontainers sobe um Postgres real)

`@SpringBootTest` + `@AutoConfigureMockMvc` + `@Testcontainers`, e o `@DynamicPropertySource` aponta o datasource para o container — por isso o teste **não** usa o banco da AWS.

Precisa do Docker rodando na máquina.

### Rodar

```bash
./mvnw test                  # todos
./mvnw test -Dtest=CursoServiceTests    # só os unitários (não precisa de Docker)
```

Relatório de cobertura: abra `tests/index.html` no navegador.

## Passo 11 — Git: uma rota via Pull Request

O enunciado pede que **uma das rotas** seja criada via PR, para o pipeline de testes rodar. Faça assim:

```bash
# 1) primeiro commit na main, SEM uma das rotas (ex.: sem o DELETE)
git add .
git commit -m "feat: API de cursos com GET e POST"
git branch -M main
git push -u origin main

# 2) cria a branch da rota que faltou
git checkout -b feature/delete-curso

# ... implementa o DELETE (controller + service + teste) ...

git add .
git commit -m "feat: adiciona rota DELETE /cursos/{id} com delecao logica"
git push -u origin feature/delete-curso
```

Depois abra o PR no GitHub (`Compare & pull request`), espere o workflow **Testes (Pull Request)** ficar verde, e faça o merge. O merge na `main` dispara o **deploy** automaticamente.

## Passo 12 — Testar a API

```bash
# criar
curl -X POST http://localhost:8080/cursos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Java Basico","descricao":"Intro","instrutor":"Eduardo","cargaHoraria":40,"preco":500}'

# listar
curl http://localhost:8080/cursos

# filtrar
curl "http://localhost:8080/cursos?nome=Java"

# deletar
curl -X DELETE http://localhost:8080/cursos/1

# listar de novo -> o curso deletado sumiu, mas ainda está no banco
curl http://localhost:8080/cursos
```

---

## Checklist final da prova

- [ ] `GET /cursos` não retorna deletados
- [ ] `GET /cursos?nome=X` filtra com `StartingWith`
- [ ] `POST /cursos` cria e retorna 201
- [ ] `DELETE /cursos/{id}` só marca a flag (nada de `deleteById`)
- [ ] Postgres rodando na AWS e conectado
- [ ] Nenhuma senha/IP escrito no código ou no YAML
- [ ] Secrets e variables cadastrados no GitHub
- [ ] `deploy.yml` (push na main) e `tests.yml` (pull request) funcionando
- [ ] Service com 100% de cobertura
- [ ] Teste de integração para as 3 rotas
- [ ] Uma rota entregue via Pull Request
