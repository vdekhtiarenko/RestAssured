# YouTrack REST API tests

Интеграционные тесты REST API YouTrack на Java 17, Maven, RestAssured и JUnit 5. Базовый адрес — `http://localhost:8080`, тесты работают с проектом `DEMO`.

## Требования

- JDK 17 или новее
- Maven 3.9+
- Доступный YouTrack с REST API и проектом `DEMO`
- Permanent token YouTrack с доступом к задачам. Для чтения административного списка проектов может понадобиться scope YouTrack Administration.

## Настройка

Для запуска - создать permanent token в YouTrack: **Profile → Account Security → New token**. Записать его локально в `.env`:
```dotenv
YOUTRACK_BASE_URL=http://localhost:8080
YOUTRACK_TOKEN=ваш_permanent_token
```

`.env` игнорируется Git. Базовый адрес по умолчанию — `http://localhost:8080`; его можно переопределить.

Для каждого параметра действует такой порядок приоритета:

1. Java system property (`youtrack.token`, `youtrack.baseUrl`)
2. Переменная окружения (`YOUTRACK_TOKEN`, `YOUTRACK_BASE_URL`)
3. Значение из `.env`
4. Для адреса — `http://localhost:8080`, если он нигде не задан. Токен обязателен.

Приоритетный способ запуска — через переменные окружения:

**Windows PowerShell**

```powershell
$env:YOUTRACK_TOKEN = "ваш_permanent_token"
$env:YOUTRACK_BASE_URL = "http://localhost:8080"
mvn test
```

**macOS/Linux**

```bash
export YOUTRACK_TOKEN='ваш_permanent_token'
export YOUTRACK_BASE_URL='http://localhost:8080'
mvn test
```

Тесты отправляют токен в заголовке `Authorization: Bearer <token>`.

## Запуск

```shell
mvn test
```

Результаты Maven Surefire находятся в `target/surefire-reports/` (`*.txt` и `TEST-*.xml`).

## Структура и назначение

- `src/main/java/dev/example/youtrack/config/TestConfiguration.java` — загружает базовый URL и токен из Java properties, переменных среды или `.env`.
- `src/test/java/dev/example/youtrack/config/ApiSpecifications.java` — общие RestAssured request/response specifications: Bearer авторизация, JSON и ожидаемый HTTP-статус.
- `src/test/java/dev/example/youtrack/endpoints/ApiEndpoints.java` — константы путей API, чтобы не дублировать строки URL в тестах.
- `src/test/java/dev/example/youtrack/BaseTest.java` — общие методы настройки запросов, преобразования ответа в DTO и получения ID проекта `DEMO`.
- `src/main/java/dev/example/youtrack/dto/` — DTO запроса и ответа.
- `src/test/java/dev/example/youtrack/AuthorizationApiTest.java` — проверка отказа для неверного токена.
- `src/test/java/dev/example/youtrack/IssueReadApiTest.java` — получение списка задач и чтение задачи по читаемому ID.
- `src/test/java/dev/example/youtrack/IssueCreateApiTest.java` — параметризованные сценарии создания задачи: валидный запрос, отсутствие summary и несуществующий ID проекта.

### Эндпоинты, используемые тестами

| Константа | HTTP | Путь | Назначение |
|---|---|---|---|
| `ApiEndpoints.PROJECTS` | GET | `/api/admin/projects` | Получить проекты; базовый тест находит проект с `shortName=DEMO` и берет его внутренний ID. |
| `ApiEndpoints.ISSUES` | GET | `/api/issues` | Получить список задач. |
| `ApiEndpoints.ISSUES` | POST | `/api/issues` | Создать задачу в проекте. |
| `ApiEndpoints.ISSUE_BY_READABLE_ID` | GET | `/api/issues/{idReadable}` | Прочитать задачу по читаемому ID, например `DEMO-1`. |

`ApiEndpoints` хранит только пути. Базовый URL задается конфигурацией; авторизация и заголовки добавляются общей request specification. Описание ресурсов см. в [официальной документации YouTrack REST API](https://www.jetbrains.com/help/youtrack/devportal/resource-api-admin-projects.html) для проектов и [задач](https://www.jetbrains.com/help/youtrack/devportal/resource-api-issues.html).

## DTO

DTO (Data Transfer Object) описывают JSON, передаваемый API и получаемый от него:

- `IssueCreateRequest` — тело POST-запроса создания задачи (`summary`, `description`, `project`). Поля со значением `null` не сериализуются.
- `ProjectRef` — вложенная ссылка на проект с его внутренним `id`; используется и в запросе создания, и в DTO ответа задачи.
- `IssueResponse` — поля задачи, которые проверяют тесты: `id`, `idReadable`, `summary`, `description`, `project`.
- `ProjectResponse` — `id`, `name`, `shortName` проекта, нужные для выбора `DEMO`.

Тесты указывают `fields` в query string: YouTrack возвращает только запрошенные поля. Для списка/чтения задач запрашиваются `id,idReadable,summary,description,project(id,shortName)`, для проектов — `id,name,shortName`.
