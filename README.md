# Task Manager

REST API для управления задачами — финальный проект курса JavaRush, Модуль 5 (Spring).

## Стек

- Java 17, Spring Boot 3.5 (Web, Data JPA, Security, Validation, Actuator)
- PostgreSQL 16, Liquibase
- JWT (jjwt)
- Springdoc OpenAPI (Swagger UI)
- Micrometer + Prometheus + Grafana
- SLF4J / Logback
- Docker, Docker Compose
- JUnit 5, Mockito, Testcontainers

## Архитектура (монолит)

```
com.javarush.lipin.taskmanager
├── configuration    # SecurityConfig, OpenApiConfig, фильтры
├── controller       # REST-контроллеры
├── dto              # Request/Response объекты + маппер
├── service          # бизнес-логика
├── model
│   ├── entity       # JPA-сущности (User, Task)
│   └── repository   # Spring Data репозитории
├── security         # JWT: сервис, фильтр, UserDetailsService
└── exception        # кастомные исключения + GlobalExceptionHandler
```

## База данных

```
users (id, username UK, email UK, password, role, created_at)
tasks (id, title, description, deadline, status, created_at, updated_at, user_id FK -&gt; users.id)
```

Схемой управляет Liquibase (`src/main/resources/db/changelog`), Hibernate работает в режиме `validate`.

## API

| Метод | Эндпоинт | Описание | Авторизация |
|---|---|---|---|
| POST | `/api/auth/register` | Регистрация | нет |
| POST | `/api/auth/login` | Логин, возвращает JWT | нет |
| GET | `/api/tasks` | Мои задачи (фильтр `?status=`) | JWT |
| GET | `/api/tasks/{id}` | Задача по id | JWT |
| POST | `/api/tasks` | Создать задачу | JWT |
| PUT | `/api/tasks/{id}` | Обновить задачу | JWT |
| DELETE | `/api/tasks/{id}` | Удалить задачу | JWT |

Пользователь видит и изменяет только свои задачи.

### Пример флоу

```bash
# Регистрация
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user","email":"user@test.com","password":"secret123"}'

# Логин -&gt; получаем токен
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"secret123"}'

# Запрос с токеном
curl http://localhost:8080/api/tasks \
  -H "Authorization: Bearer &lt;TOKEN&gt;"
```

JWT живёт 24 часа, передаётся в заголовке `Authorization: Bearer &lt;token&gt;`.

## Запуск

### Вариант 1: всё в Docker (рекомендуется)

```bash
docker compose up --build
```

Поднимутся: PostgreSQL, приложение, Prometheus, Grafana.

### Вариант 2: локально из IDE

```bash
docker compose up -d postgres   # только БД
./mvnw spring-boot:run
```

## Сервисные URL

| Сервис | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Actuator health | http://localhost:8080/actuator/health |
| Метрики Prometheus | http://localhost:8080/actuator/prometheus |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin/admin) |

## Тестирование

- Postman-коллекция: `postman/TaskManager.postman_collection.json` (позитивные и негативные сценарии)
- Unit- и интеграционные тесты: `./mvnw test`