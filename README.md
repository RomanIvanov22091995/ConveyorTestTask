# Сервис загрузки наград сотрудников

REST API сервис для загрузки и обработки наград сотрудников компании из CSV файлов.

## Технологический стек

- **Java 21** - современная версия Java с поддержкой virtual threads и records
- **Spring Boot 3.2.0** - фреймворк для создания приложений
- **Spring WebFlux** - реактивный веб-фреймворк
- **Spring Data R2DBC** - реактивный доступ к данным
- **PostgreSQL** - реляционная база данных
- **Gradle 8.14** - система сборки
- **JUnit 5** - фреймворк для тестирования
- **OpenCSV** - библиотека для парсинга CSV файлов

## Обоснование выбора WebFlux

Выбран **Spring WebFlux** вместо традиционного Spring MVC по следующим причинам:

### 1. Реактивная модель программирования
- **Неблокирующая обработка**: WebFlux использует реактивные потоки (Reactive Streams), что позволяет обрабатывать запросы асинхронно без блокировки потоков
- **Масштабируемость**: Приложение может обрабатывать больше одновременных запросов с меньшим количеством потоков
- **Эффективность ресурсов**: Меньшее потребление памяти и CPU за счет неблокирующей модели

### 2. Реактивная база данных (R2DBC)
- **Совместимость**: WebFlux естественно интегрируется с R2DBC (реактивный доступ к БД)
- **End-to-end реактивность**: От HTTP запроса до БД - весь стек неблокирующий
- **Производительность**: При работе с большими объемами данных реактивный подход показывает лучшую производительность

### 3. Обработка файлов
- **Стриминг**: WebFlux позволяет обрабатывать файлы потоково, не загружая весь файл в память
- **Backpressure**: Автоматическое управление потоком данных при больших файлах
- **Асинхронность**: Парсинг и сохранение данных происходят асинхронно

### 4. Современные требования
- **Микросервисная архитектура**: WebFlux лучше подходит для микросервисов с высокой нагрузкой
- **Cloud-native**: Оптимизирован для работы в облачных средах
- **Future-proof**: Реактивное программирование - тренд в современной разработке

### Когда MVC был бы лучше:
- Простые CRUD приложения с низкой нагрузкой
- Команда не знакома с реактивным программированием
- Требуется синхронная обработка с блокировками

## Функциональность

### API Endpoints

#### POST `/api/rewards/upload`
Загружает CSV файл с наградами сотрудников.

**Параметры:**
- `file` (multipart/form-data) - CSV файл с наградами

**Формат CSV файла:**
```csv
employeeId,employeeFullName,rewardId,rewardName,receivedDate
1,Иванов Иван Иванович,100,Лучший сотрудник,2024-01-15T10:30:00
2,Петров Петр Петрович,101,За отличную работу,2024-02-20T14:45:00
```

**Поля:**
- `employeeId` - ID сотрудника (положительное число)
- `employeeFullName` - ФИО сотрудника (непустая строка)
- `rewardId` - ID награды (положительное число)
- `rewardName` - Название награды (непустая строка)
- `receivedDate` - Дата получения в формате ISO-8601 (например: `2024-01-15T10:30:00`)

**Ответ:**
```json
{
  "totalRecords": 2,
  "savedRecords": 2,
  "skippedRecords": 0,
  "message": "Обработано записей: 2, сохранено: 2, пропущено: 0"
}
```

**Ошибки:**
- `400 Bad Request` - неверный формат файла или записи
- `500 Internal Server Error` - внутренняя ошибка сервера

### Бизнес-логика

1. **Валидация файла**: Проверка формата файла (только CSV)
2. **Парсинг CSV**: Извлечение записей из файла с валидацией полей
3. **Проверка сотрудников**: Награды сохраняются только для сотрудников, существующих в БД
4. **Сохранение наград**: Валидные награды сохраняются в базу данных

## Структура проекта

```
src/
├── main/
│   ├── java/org/example/
│   │   ├── RewardApplication.java          # Главный класс приложения
│   │   └── reward/
│   │       ├── config/
│   │       │   └── DatabaseInitializer.java # Инициализация БД
│   │       ├── controller/
│   │       │   ├── RewardController.java    # REST контроллер
│   │       │   └── GlobalExceptionHandler.java # Обработка ошибок
│   │       ├── dto/
│   │       │   ├── RewardRecord.java        # DTO для записи из CSV
│   │       │   └── RewardUploadResponse.java # DTO для ответа
│   │       ├── entity/
│   │       │   ├── Employee.java            # Сущность сотрудника
│   │       │   └── Reward.java              # Сущность награды
│   │       ├── exception/
│   │       │   ├── InvalidFileFormatException.java
│   │       │   └── InvalidRecordException.java
│   │       ├── repository/
│   │       │   ├── EmployeeRepository.java   # Реактивный репозиторий сотрудников
│   │       │   └── RewardRepository.java     # Реактивный репозиторий наград
│   │       └── service/
│   │           ├── CsvParserService.java     # Сервис парсинга CSV
│   │           └── RewardService.java        # Сервис обработки наград
│   └── resources/
│       ├── application.yml                   # Конфигурация приложения
│       ├── schema.sql                        # Схема БД
│       └── data.sql                          # Начальные данные
└── test/
    ├── java/org/example/reward/
    │   ├── controller/
    │   │   └── RewardControllerTest.java
    │   ├── integration/
    │   │   └── RewardIntegrationTest.java
    │   └── service/
    │       ├── CsvParserServiceTest.java
    │       └── RewardServiceTest.java
    └── resources/
        └── application-test.yml              # Конфигурация для тестов
```

## Запуск приложения

### Требования
- Java 21 или выше
- Gradle 8.5 или выше
- PostgreSQL 14+ или Docker (для запуска через docker-compose)

### Настройка базы данных

** Использование Docker Compose **
```bash
# Запустите PostgreSQL в Docker
docker-compose up -d

# Проверьте, что база данных запущена
docker-compose ps
```

### Сборка проекта
```bash
./gradlew build
```

### Запуск приложения
```bash
./gradlew bootRun
```

Приложение запустится на порту `8080` и автоматически создаст схему БД при первом запуске.

### Запуск тестов
```bash
./gradlew test
```

**Важно**: Интеграционные тесты используют **Testcontainers** для автоматического запуска PostgreSQL в Docker контейнере. Убедитесь, что Docker запущен перед выполнением тестов. Testcontainers автоматически скачает образ PostgreSQL при первом запуске тестов.

## Примеры использования

### Загрузка файла через curl

**Важно**: Убедитесь, что файл находится в текущей директории или используйте правильный путь.

**Пример 1: Файл в корне проекта**
```bash
cp src/main/resources/test-data/rewards_valid.csv rewards.csv
curl -X POST http://localhost:8080/api/rewards/upload -F "file=@rewards.csv"
```

**Пример 2: Использование тестового файла из проекта**
```bash
curl -X POST http://localhost:8080/api/rewards/upload -F "file=@src/main/resources/test-data/rewards_valid.csv"
```

**Пример 3: С полным путем**
```bash
curl -X POST http://localhost:8080/api/rewards/upload -F "file=@$(pwd)/src/main/resources/test-data/rewards_valid.csv"
```

**Примечание**: Заголовок `Content-Type: multipart/form-data` не нужен при использовании `-F`, curl добавляет его автоматически.

### Пример CSV файла (rewards.csv)

В проекте есть готовые тестовые файлы в `src/main/resources/test-data/`:
- `rewards_valid.csv` - валидные записи для всех сотрудников
- `rewards_with_invalid_employees.csv` - смешанные записи (валидные и несуществующие сотрудники)
- `rewards_large.csv` - большой файл для тестирования производительности

Пример содержимого:
```csv
employeeId,employeeFullName,rewardId,rewardName,receivedDate
1,Иванов Иван Иванович,100,Лучший сотрудник,2024-01-15T10:30:00
2,Петров Петр Петрович,101,За отличную работу,2024-02-20T14:45:00
3,Сидоров Сидор Сидорович,102,За инновации,2024-03-10T09:15:00
```

### Использование тестовых файлов

Тестовые CSV файлы находятся в `src/main/resources/test-data/`. 

**Способ 1: Скопировать файл в корень проекта**
```bash
cp src/main/resources/test-data/rewards_valid.csv rewards.csv
curl -X POST http://localhost:8080/api/rewards/upload -F "file=@rewards.csv"
```

**Способ 2: Использовать полный путь**
```bash
curl -X POST http://localhost:8080/api/rewards/upload -F "file=@$(pwd)/src/main/resources/test-data/rewards_valid.csv"
```

**Способ 3: Использовать относительный путь (из корня проекта)**
```bash
curl -X POST http://localhost:8080/api/rewards/upload -F "file=@src/main/resources/test-data/rewards_valid.csv"
```

**Другие тестовые файлы:**
```bash
curl -X POST http://localhost:8080/api/rewards/upload -F "file=@src/main/resources/test-data/rewards_with_invalid_employees.csv"
curl -X POST http://localhost:8080/api/rewards/upload -F "file=@src/main/resources/test-data/rewards_large.csv"
```

## Тестирование

Проект содержит комплексные тесты:

1. **Unit тесты**:
   - `CsvParserServiceTest` - тестирование парсинга CSV
   - `RewardServiceTest` - тестирование бизнес-логики
   - `RewardControllerTest` - тестирование REST API

2. **Integration тесты**:
   - `RewardIntegrationTest` - полный цикл обработки файла с использованием PostgreSQL через Testcontainers

Все тесты используют JUnit 5 и Reactor Test для проверки реактивных потоков. Интеграционные тесты используют **Testcontainers** для автоматического запуска PostgreSQL в Docker контейнере, что обеспечивает тестирование на реальной базе данных.

## Использование Java 21

Проект использует современные возможности Java 21:

### Records для DTO
- `RewardRecord` и `RewardUploadResponse` реализованы как records
- Автоматическая генерация equals, hashCode, toString
- Иммутабельность по умолчанию
- Меньше boilerplate кода

### Virtual Threads (Project Loom)
- Java 21 поддерживает virtual threads для эффективной многопоточности
- Реактивный стек (WebFlux + R2DBC) уже неблокирующий, что обеспечивает высокую производительность
- Virtual threads могут быть использованы для блокирующих операций, если потребуется

### Другие улучшения
- Pattern matching для switch expressions
- Sealed classes для ограничения наследования
- String templates (preview в Java 21)

## Принятые решения и спорные моменты

### 1. Формат даты
**Решение**: Использован формат ISO-8601 (`2024-01-15T10:30:00`)
**Обоснование**: Стандартный формат, поддерживается большинством систем

### 2. Начальное наполнение сотрудников
**Решение**: Сотрудники добавляются через `data.sql` при старте приложения
**Обоснование**: Упрощает тестирование и демонстрацию функциональности

### 3. Обработка ошибок
**Решение**: Награды с несуществующими сотрудниками пропускаются, не вызывая ошибку
**Обоснование**: Позволяет обрабатывать частично валидные файлы

### 4. Валидация данных
**Решение**: Строгая валидация всех полей на этапе парсинга
**Обоснование**: Раннее обнаружение ошибок, понятные сообщения об ошибках

## Производительность

Благодаря реактивной модели:
- **Неблокирующая обработка**: до 10,000+ одновременных запросов
- **Эффективное использование памяти**: потоковая обработка больших файлов
- **Масштабируемость**: легко масштабируется горизонтально

