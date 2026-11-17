# YouTrack REST API tests

Интеграционные тесты REST API YouTrack на Java 17, Maven, RestAssured и JUnit 5. Базовый адрес — `http://localhost:8080`, тесты работают с проектом `DEMO`.

## Требования

- JDK 17 или новее (компиляция настроена с `--release 17`)
- Maven 3.9+
- Запущенный YouTrack с доступным REST API
- Permanent token YouTrack с доступом к задачам. Для чтения административного списка проектов может понадобиться scope YouTrack Administration.

## Настройка токена

Создайте permanent token в YouTrack: **Profile → Account Security → New token**. Не вставляйте токен в Git, исходники или вывод команд. Запишите его локально в `.env`:

```dotenv
YOUTRACK_BASE_URL=http://localhost:8080
YOUTRACK_TOKEN=вставьте_токен_сюда
```

Файл `.env` игнорируется Git. Отслеживаемый шаблон находится в `.env.example`.

Для каждого параметра действует такой порядок приоритета:

1. Java system property (`youtrack.token`, `youtrack.baseUrl`)
2. Переменная окружения (`YOUTRACK_TOKEN`, `YOUTRACK_BASE_URL`)
3. Значение из `.env`
4. Для адреса — `http://localhost:8080`, если он нигде не задан. Токен обязателен.

Приоритетный способ запуска — через переменные окружения:

**Windows PowerShell**

```powershell
$env:YOUTRACK_TOKEN = "ваш-токен"
$env:YOUTRACK_BASE_URL = "http://localhost:8080"
mvn test
```

**macOS/Linux**

```bash
export YOUTRACK_TOKEN='ваш-токен'
export YOUTRACK_BASE_URL='http://localhost:8080'
mvn test
```

Java properties также поддерживаются: `-Dyoutrack.token=...` и `-Dyoutrack.baseUrl=...`. Учитывайте, что секрет в аргументах команды может сохраниться в истории shell или быть виден в списке процессов; для токена предпочтительны `.env` или переменная окружения.

Заголовок авторизации формируется как `Authorization: Bearer <token>`. Запросы и ответы не выводятся в консоль, чтобы не раскрыть заголовок.

## Запуск

```shell
mvn test
```

Тесты разрешаются в два потока. Набор содержит шесть REST API сценариев: получение списка задач, создание задачи с последующим чтением по `idReadable`, параметризованные успешное создание/отсутствующий summary/неверный `project.id`, а также запрос с неверным токеном. Тесты получают внутренний `project.id` из `/api/admin/projects`, сопоставляя проект по `shortName=DEMO`. Каждая создаваемая задача получает уникальный UUID в summary и остаётся в YouTrack.

На локальном экземпляре YouTrack 2026.2 проверены HTTP `400` при ошибках создания и `401` при неверном токене. Если другая версия или настройки YouTrack вернут иные статусы, фактический ответ будет виден в отчёте Surefire.

## Отчёты

После запуска Maven Surefire сохраняет результаты в `target/surefire-reports/`:

- `TEST-*.xml` — число тестов, успешные результаты, failures/errors и сведения об ошибках;
- `*.txt` — текстовый сводный отчёт.

Ожидаемые идентификаторы и summaries созданных задач печатаются в вывод Maven и сохраняются в `system-out` XML-отчётов. Для краткой сводки Surefire в конце команды покажет число тестов, failures и errors.
