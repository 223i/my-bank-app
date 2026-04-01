# Transfer Service

Микросервис для перевода денежных средств между счетами разных пользователей.

## 📋 Обзор

Сервис обеспечивает безопасные переводы денег между аккаунтами пользователей:

### Основные функции
- **Перевод средств** - перевод денег с счета отправителя на счет получателя
- **Валидация операций** - проверка достаточности средств и валидности данных
- **Атомарность транзакций** - гарантия успешности обеих операций или откат
- **Уведомления** - автоматическое уведомление об успешных переводах

### Бизнес-логика
- Проверка достаточности средств у отправителя
- Списание средств со счета отправителя
- Зачисление средств на счет получателя
- Отправка уведомлений обоим участникам
- Обработка ошибок и откат операций при необходимости

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
- **8085** - основной порт сервиса

### API Эндпоинты

#### Операции перевода
- `POST /api/transfer` - Выполнение перевода между счетами

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
- **Пользователи** могут выполнять переводы только со своего счета
- **Микросервисы** имеют доступ по Client Credentials Flow
- **Валидация** отправителя через JWT токен

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
SERVER_PORT=8085
SPRING_PROFILES_ACTIVE=docker
```

### Структура конфигурации
```yaml
# application.yaml
spring:
  application:
    name: transfer-service
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
docker build -t transfer-service .

# Запуск
docker run -p 8085:8085 --env-file .env transfer-service
```

### Запуск в Docker Compose
```bash
# В корневой директории проекта
docker-compose up transfer-service
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

### Покрытие тестами
- **Unit тесты** - сервисный слой, бизнес-логика
- **Integration тесты** - REST контроллеры
- **Mock тесты** - взаимодействие с внешними сервисами

## 📊 Модели данных

### TransferRequest
```java
public record TransferRequest(
    String toLogin,      // Логин получателя
    BigDecimal amount   // Сумма перевода
) {}
```

### NotificationRequest
```java
public record NotificationRequest(
    String recipientLogin,  // Получатель уведомления
    String message,         // Текст уведомления
    String type           // Тип уведомления (TRANSFER)
) {}
```

## 🔗 Интеграции

### Исходящие вызовы
- **Accounts Service** - операции с балансом счетов
  - `PATCH /{login}/decrease-balance?amount={amount}` - списание у отправителя
  - `PATCH /{login}/increase-balance?amount={amount}` - зачисление получателю

- **Notifications Service** - отправка уведомлений
  - `POST /api/notifications/send` - отправка уведомления

### Входящие вызовы
- **Gateway API** - запросы от фронтенда

## 📝 Примеры запросов

### Перевод средств
```bash
curl -X POST \
     -H "Authorization: Bearer <JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"toLogin":"alice_99","amount":500.00}' \
     http://localhost:8085/api/transfer
```

## 🔄 Процесс перевода

### Успешный перевод
1. Извлечение логина отправителя из JWT токена
2. Валидация входных данных (сумма, получатель)
3. Списание средств со счета отправителя через Accounts Service
4. Зачисление средств на счет получателя через Accounts Service
5. Отправка уведомления получателю через Notifications Service
6. Возврат успешного ответа

### Обработка ошибок
1. Если списание не удалось - операция отменяется
2. Если зачисление не удалось - средства возвращаются отправителю
3. Все ошибки логируются и оборачиваются в TransferException

## 🛡️ Безопасность операций

### Валидации
- Проверка, что отправитель не переводит деньги самому себе
- Проверка положительной суммы перевода
- Проверка достаточности средств у отправителя
- Валидация существования счета получателя

### Защита от мошенничества
- Аутентификация отправителя через JWT
- Авторизация только для перевода с собственного счета
- Логирование всех операций перевода

### Логирование
```bash
# Просмотр логов
tail -f logs/transfer-service.log

# Уровень логирования в application.yaml
logging:
  level:
    com.iron: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web.client: DEBUG
```
