# Gateway API Service

API Gateway для маршрутизации запросов между фронтендом и микросервисами банковской системы.

## 📋 Обзор

Сервис выступает в роли единой точки входа для всех клиентских запросов:

### Основные функции
- **Маршрутизация запросов** - перенаправление запросов соответствующим микросервисам
- **Проброс JWT токена** - передача токена аутентификации в микросервисы
- **Агрегация ответов** - объединение данных от нескольких сервисов
- **Безопасность** - централизованная аутентификация и авторизация
- **Балансировка нагрузки** - распределение запросов между экземплярами сервисов

### Маршруты
- `/api/accounts/**` → Accounts Service (8081)
- `/api/cash/**` → Cash Service (8082)
- `/api/transfer/**` → Transfer Service (8083)
- `/api/notifications/**` → Notifications Service (8084)

## 🚀 Технологии

| Технология | Версия | Назначение |
|------------|--------|------------|
| **Java** | 21 | Язык программирования |
| **Spring Boot** | 3.4.4 | Основной фреймворк |
| **Spring Cloud Gateway** | 4.1 | API Gateway |
| **Spring Security** | 6 | Безопасность и OAuth 2.0 |
| **OAuth 2.0** | 2.0 | Протокол авторизации |
| **JWT** | - | Токены аутентификации |
| **Maven** | 3.8+ | Сборка проекта |

## 🌐 Порты и эндпоинты

### Порт
- **8080** - основной порт сервиса

### API Эндпоинты

#### Проксированные эндпоинты
- `GET /api/accounts/me` - Получение данных аккаунта
- `PUT /api/accounts/me` - Обновление данных аккаунта
- `GET /api/accounts/others` - Получение списка других пользователей
- `POST /api/cash/process` - Операции с деньгами
- `POST /api/transfer` - Переводы между счетами
- `POST /api/notifications/send` - Отправка уведомлений

#### OAuth 2.0 эндпоинты
- `GET /oauth2/authorization/{registrationId}` - Начало авторизации
- `GET /login/oauth2/code/{registrationId}` - Callback авторизации

## 🔐 Безопасность

### OAuth 2.0 Configuration
- **Authorization Code Flow** для аутентификации пользователей
- **Client Credentials Flow** для межсервисного взаимодействия
- **JWT Bearer Token** для передачи данных аутентификации
- **Token Relay** - проброс токена в downstream сервисы

### Права доступа
- **Аутентифицированные пользователи** - доступ к персональным данным
- **Анонимные пользователи** - доступ только к публичным эндпоинтам
- **Микросервисы** - доступ по Client Credentials Flow

## 🛠️ Конфигурация

### Переменные окружения (.env)

```env
# OAuth 2.0 / Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/my-bank-app
JWT_SECRET=your-jwt-secret

# Сервисы
ACCOUNTS_SERVICE_URL=http://localhost:8081
CASH_SERVICE_URL=http://localhost:8082
TRANSFER_SERVICE_URL=http://localhost:8083
NOTIFICATIONS_SERVICE_URL=http://localhost:8084

# Приложение
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=docker
```

### Структура конфигурации
```yaml
# application.yaml
spring:
  application:
    name: gateway-service
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: my-bank-front
            client-secret: ${OAUTH2_CLIENT_SECRET}
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8080/login/oauth2/code/keycloak
        provider:
          keycloak:
            issuer-uri: ${KEYCLOAK_ISSUER_URI}
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI}

  cloud:
    gateway:
      routes:
        - id: accounts-service
          uri: ${ACCOUNTS_SERVICE_URL}
          predicates:
            - Path=/api/accounts/**
          filters:
            - TokenRelay=

        - id: cash-service
          uri: ${CASH_SERVICE_URL}
          predicates:
            - Path=/api/cash/**
          filters:
            - TokenRelay=

        - id: transfer-service
          uri: ${TRANSFER_SERVICE_URL}
          predicates:
            - Path=/api/transfer/**
          filters:
            - TokenRelay=

        - id: notifications-service
          uri: ${NOTIFICATIONS_SERVICE_URL}
          predicates:
            - Path=/api/notifications/**
          filters:
            - TokenRelay=
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
docker build -t gateway-service .

# Запуск
docker run -p 8080:8080 --env-file .env gateway-service
```

### Запуск в Docker Compose
```bash
# В корневой директории проекта
docker-compose up gateway-service
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
- **Unit тесты** - конфигурация маршрутизации, фильтры
- **Integration тесты** - эндпойнты, безопасность
- **Load тесты** - производительность маршрутизации

## 🔄 Поток запросов

### Аутентифицированный запрос
1. Клиент отправляет запрос с JWT токеном
2. Gateway валидирует токен
3. Gateway применяет правила маршрутизации
4. Gateway пробрасывает токен в downstream сервис
5. Downstream сервис обрабатывает запрос
6. Ответ возвращается через Gateway клиенту

### Неаутентифицированный запрос
1. Клиент отправляет запрос без токена
2. Gateway перенаправляет на страницу авторизации
3. После успешной авторизации клиент получает JWT токен
4. Повторный запрос обрабатывается с токеном

## 📊 Мониторинг и логирование

### Метрики
- Количество запросов по маршрутам
- Время обработки запросов
- Количество ошибок аутентификации
- Статус downstream сервисов

### Логирование
```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web.reactive: DEBUG
```

## 🔗 Интеграции

### Downstream сервисы
- **Accounts Service** - управление аккаунтами
- **Cash Service** - операции с деньгами
- **Transfer Service** - переводы между счетами
- **Notifications Service** - уведомления

### External сервисы
- **Keycloak** - сервер авторизации
- **Front UI** - веб-интерфейс

## 📝 Примеры запросов

### Получение данных аккаунта (требует аутентификации)
```bash
curl -H "Authorization: Bearer <JWT_TOKEN>" \
     http://localhost:8080/api/accounts/me
```

### Операция перевода (требует аутентификации)
```bash
curl -X POST \
     -H "Authorization: Bearer <JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"toLogin":"alice_99","amount":500.00}' \
     http://localhost:8080/api/transfer
```

### Инициация авторизации
```bash
curl http://localhost:8080/oauth2/authorization/keycloak
```

### Валидация токенов
- Проверка подписи JWT
- Проверка срока действия токена
- Проверка issuer (эмитента)
- Извлечение claims для авторизации

### Логирование
```bash
# Просмотр логов
tail -f logs/gateway-service.log

# Уровень логирования в application.yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.springframework.security: DEBUG
    reactor.netty: DEBUG
```
