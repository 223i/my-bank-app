# Cash Service

Микросервис для операций пополнения и снятия денежных средств со счетов пользователей.

## 📋 Обзор

Сервис обеспечивает финансовые операции с балансом пользователей:

### Основные функции
- **Пополнение счета** - внесение денежных средств на баланс пользователя
- **Снятие средств** - списание денежных средств с баланса пользователя
- **Валидация операций** - проверка достаточности средств для снятия
- **Уведомления** - автоматическое уведомление об успешных операциях

### Бизнес-логика
- При снятии средств проверяется достаточность баланса
- Все операции логируются и сопровождаются уведомлениями
- Сервис работает без собственного хранилища данных

## 🚀 Технологии

| Технология | Версия | Назначение |
|------------|--------|------------|
| **Java** | 21 | Язык программирования |
| **Spring Boot** | 3.4.4 | Основной фреймворк |
| **Spring Security** | 6 | Безопасность и OAuth 2.0 |
| **Spring Web** | 6.1 | REST API |
| **RestClient** | 6.1 | HTTP клиент для межсервисных вызовов |
| **Lombok** | 1.18+ | Уменьшение шаблонного кода |
| **Maven** | 3.8+ | Сборка проекта |

## 🌐 Порты и эндпоинты

### Порт
- **8082** - основной порт сервиса

### API Эндпоинты

#### Операции с деньгами
- `POST /api/cash/process` - Обработка операции пополнения/снятия

#### Внутренние эндпоинты
- Отсутствуют (сервис не имеет собственных эндпоинтов для других сервисов)

## 🔐 Безопасность

### OAuth 2.0 Configuration
- **Client Credentials Flow** для межсервисного взаимодействия
- **JWT Bearer Token** для аутентификации пользователей
- **Required scopes**: 
  - `ROLE_ACCOUNTS_USER` для доступа к Accounts Service
  - `ROLE_NOTIFICATIONS_USER` для доступа к Notifications Service

### Права доступа
- **Пользователи** могут выполнять операции только со своим счетом
- **Микросервисы** имеют доступ по Client Credentials Flow

## 🛠️ Конфигурация

### Переменные окружения (.env)

```env
# Внешние сервисы
ACCOUNTS_SERVICE_URL=http://localhost:8081
NOTIFICATIONS_SERVICE_URL=http://localhost:8084

# OAuth 2.0 / Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/my-bank-app
JWT_SECRET=your-jwt-secret

# Приложение
SERVER_PORT=8082
SPRING_PROFILES_ACTIVE=docker
```

### Структура конфигурации
```yaml
# application.yaml
spring:
  application:
    name: cash-service
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI}
  cloud:
    consul:
      enabled: false
  config:
    import: ""

services:
  accounts-url: ${ACCOUNTS_SERVICE_URL}
  notifications-url: ${NOTIFICATIONS_SERVICE_URL}
```

## 🚀 Запуск

### Локальный запуск
```bash
# Создание .env файла
cp .env.example .env
# Редактирование .env с вашими значениями

# Запуск сервиса
./mvnw spring-boot:run

# Или с профилем
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Запуск через Docker
```bash
# Сборка образа
docker build -t cash-service .

# Запуск
docker run -p 8082:8082 --env-file .env cash-service
```

### Запуск в Docker Compose
```bash
# В корневой директории проекта
docker-compose up cash-service
```

## 🧪 Тестирование

### Запуск тестов
```bash
# Все тесты
./mvnw test

# Только юнит-тесты
./mvnw test -Dtest="*Unit*"

# Только интеграционные тесты
./mvnw test -Dtest="*Integration*"
```

## 📊 Модели данных

### CashOperationRequest
```java
public record CashOperationRequest(
    BigDecimal amount,
    String action  // "PUT" для пополнения, "GET" для снятия
) {}
```

### CashOperationResponse
```java
public record CashOperationResponse(
    boolean success,
    String message,
    BigDecimal newBalance
) {}
```

## 🔗 Интеграции

### Исходящие вызовы
- **Accounts Service** - операции с балансом счета
  - `PUT /{login}/increase-balance?amount={amount}` - пополнение
  - `GET /{login}/decrease-balance?amount={amount}` - снятие

- **Notifications Service** - отправка уведомлений
  - `POST /api/notifications/send` - отправка уведомления

### Входящие вызовы
- **Gateway API** - запросы от фронтенда

## 📝 Примеры запросов

### Пополнение счета
```bash
curl -X POST \
     -H "Authorization: Bearer <JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"amount":1000,"action":"PUT"}' \
     http://localhost:8082/api/cash/process
```

### Снятие средств
```bash
curl -X POST \
     -H "Authorization: Bearer <JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"amount":500,"action":"GET"}' \
     http://localhost:8082/api/cash/process
```

## 🔄 Процесс операции

### Пополнение средств (PUT)
1. Валидация входных данных
2. Вызов Accounts Service для увеличения баланса
3. Отправка уведомления в Notifications Service
4. Возврат успешного ответа

### Снятие средств (GET)
1. Валидация входных данных
2. Вызов Accounts Service для уменьшения баланса
3. Проверка успешности операции
4. Отправка уведомления в Notifications Service
5. Возврат успешного ответа

### Логирование
```bash
# Просмотр логов
tail -f logs/cash-service.log

# Уровень логирования в application.yaml
logging:
  level:
    com.iron: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web.client: DEBUG
```