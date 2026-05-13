# Todo List API

API REST de gerenciamento de tarefas desenvolvida em Java com Spring Boot. O projeto foi construído como parte do meu aprendizado em Java e Spring, e cobre autenticação básica, persistência com JPA, validações de domínio e deploy via Docker.

## Aplicação em produção

A API está publicada no Render e pode ser acessada em:

**https://todo-list-spring-boot-09kb.onrender.com**

> Observação: como o deploy roda no plano gratuito do Render, a primeira requisição após um período de inatividade pode demorar alguns segundos enquanto o serviço sobe.

## Tecnologias utilizadas

- **Java 17**
- **Spring Boot 3.5** (Web, Data JPA)
- **Maven** para build e gerenciamento de dependências
- **Lombok** para reduzir boilerplate
- **BCrypt** (`at.favre.lib`) para hash de senhas
- **H2 / PostgreSQL** como banco de dados
- **Docker** para empacotamento e deploy no Render

## Funcionalidades

- Cadastro de usuários com senha criptografada (BCrypt)
- Autenticação via **HTTP Basic Auth** nas rotas protegidas
- Filtro de autenticação que injeta o `user_id` na requisição
- CRUD parcial de tarefas (criar, listar e atualizar)
- Validações de domínio:
  - Título com no máximo 50 caracteres
  - Datas de início e término devem ser futuras
  - A data de início deve ser anterior à data de término
- Cada usuário só pode atualizar as suas próprias tarefas
- Cálculo automático do `timeSpan` (duração entre `startAt` e `endAt`)
- Atualização parcial de tarefas via `BeanWrapper` (copia apenas propriedades não nulas)
- Tratamento global de erros via `@ControllerAdvice`

## Estrutura do projeto

```
todolist/todolist/
└── src/main/java/com/kevincontri/todolist
    ├── errors/      # Handlers globais de exceção
    ├── filter/      # Filtro de autenticação Basic Auth
    ├── task/        # Controller, model e repository de tarefas
    ├── user/        # Controller, model e repository de usuários
    └── utils/       # Utilitários (ex.: copyNonNullProperties)
```

## Endpoints

### Usuários

#### `POST /users`
Cria um novo usuário.

**Body:**
```json
{
  "username": "kevin",
  "email": "kevin@email.com",
  "password": "minhasenha"
}
```

**Erros:**
- `400 Bad Request` — usuário já existe

---

### Tarefas

Todas as rotas abaixo exigem o header:

```
Authorization: Basic base64(email:senha)
```

#### `POST /tasks`
Cria uma nova tarefa para o usuário autenticado.

**Body:**
```json
{
  "title": "Estudar Spring Boot",
  "description": "Revisar autenticação e JPA",
  "priority": "ALTA",
  "startAt": "2026-06-01T09:00:00",
  "endAt":   "2026-06-05T18:00:00"
}
```

**Erros:**
- `400 Bad Request` — datas no passado ou `startAt` posterior a `endAt`

#### `GET /tasks`
Lista todas as tarefas do usuário autenticado.

#### `PUT /tasks/{id}`
Atualiza parcialmente uma tarefa do usuário autenticado. Apenas os campos enviados no body são atualizados.

**Erros:**
- `400 Bad Request` — tarefa não encontrada
- `403 Forbidden` — a tarefa pertence a outro usuário

## Rodando localmente

### Pré-requisitos
- Java 17
- Maven 3.9+

### Passos

```bash
cd todolist/todolist
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

### Rodando com Docker

Na raiz do repositório:

```bash
docker build -t todolist .
docker run -p 8080:8080 todolist
```

A imagem usa multi-stage build (Maven + Eclipse Temurin) e respeita a variável de ambiente `PORT`, o que torna o container compatível com plataformas como o Render.

## Deploy no Render

1. Criar um novo **Web Service** apontando para este repositório.
2. Selecionar **Docker** como runtime.
3. O Render injeta automaticamente a variável `PORT`; o `Dockerfile` já está configurado para fazer o Spring Boot escutar nessa porta.

## Autor

**Kevin Contri** — projeto de estudo de Java + Spring Boot.