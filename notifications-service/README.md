# Notifications Service

Микросервис для отправки уведомлений пользователям о банковских операциях.

## 📋 Обзор

Сервис отвечает за доставку уведомлений о различных банковских операциях:

### Основные функции
- **Отправка уведомлений** - логирование всех банковских операций

### Типы уведомлений
- **TRANSFER** - уведомления о переводах между счетами
- **CASH** - уведомления о пополнении/снятии средств
- **ACCOUNT** - уведомления об изменении данных аккаунта

## 🚀 Технологии

| Технология | Версия | Назначение |
|------------|--------|------------|
| **Java** | 21 | Язык программирования |
| **Spring Boot** | 3.4.4 | Основной фреймворк |
| **Spring Security** | 6 | Безопасность и OAuth 2.0 |
| **Spring Data JPA** | 3.4.4 | Работа с базой данных |
| **PostgreSQL** | 15+ | СУБД |
| **Lombok** | 1.18+ | Уменьшение шаблонного кода |
| **Maven** | 3.8+ | Сборка проекта |

## 🌐 Порты и эндпоинты

### Порт
- **8084** - основной порт сервиса

### API Эндпоинты

#### Управление уведомлениями
- `POST /api/notifications/send` - Отправка нового уведомления
- `GET /api/notifications/user/{login}` - Получение уведомлений пользователя

#### Внутренние эндпоинты
- Отсутствуют (сервис не имеет собственных эндпоинтов для других сервисов)

## 🔐 Безопасность

### OAuth 2.0 Configuration
- **Client Credentials Flow** для межсервисного взаимодействия
- **JWT Bearer Token** для аутентификации пользователей
- **Required scopes**: `ROLE_NOTIFICATIONS_USER` для доступа к уведомлениям

### Права доступа
- **Пользователи** могут просматривать только свои уведомления
- **Микросервисы** имеют доступ по Client Credentials Flow для отправки уведомлений

## 🛠️ Конфигурация

### Переменные окружения (.env)

```env
# База данных
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mybank
DB_USER=mybank
DB_PASSWORD=mybank

# OAuth 2.0 / Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/my-bank-app
JWT_SECRET=your-jwt-secret

# Приложение
SERVER_PORT=8084
SPRING_PROFILES_ACTIVE=docker
```

### Структура конфигурации
```yaml
# application.yaml
spring:
  application:
    name: notifications-service
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI}
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
docker build -t notifications-service .

# Запуск
docker run -p 8084:8084 --env-file .env notifications-service
```

### Запуск в Docker Compose
```bash
# В корневой директории проекта
docker-compose up notifications-service
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
- **Unit тесты** - сервисный слой, репозитории
- **Integration тесты** - REST контроллеры, база данных
- **TestContainers** - тесты с реальной PostgreSQL

## 📊 Модели данных

### Notification Entity
```java
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String recipientLogin;
    
    @Column(nullable = false)
    private String message;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
```

### NotificationRequest
```java
public record NotificationRequest(
    String recipientLogin,  // Получатель уведомления
    String message,         // Текст уведомления
    String type           // Тип уведомления
) {}
```

### NotificationDto
```java
public record NotificationDto(
    Long id,
    String recipientLogin,
    String message,
    String type,
    LocalDateTime createdAt
) {}
```

## 🔗 Интеграции

### Входящие вызовы
- **Gateway API** - запросы от фронтенда
- **Accounts Service** - уведомления об операциях с аккаунтом
- **Cash Service** - уведомления о пополнении/снятии
- **Transfer Service** - уведомления о переводах

### Исходящие вызовы
- Отсутствуют (сервис не вызывает другие сервисы)

## 📝 Примеры запросов

### Отправка уведомления
```bash
curl -X POST \
     -H "Authorization: Bearer <JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"recipientLogin":"alice_99","message":"Вам пришел перевод: 500.00","type":"TRANSFER"}' \
     http://localhost:8084/api/notifications/send
```

### Получение уведомлений пользователя
```bash
curl -H "Authorization: Bearer <JWT_TOKEN>" \
     http://localhost:8084/api/notifications/user/alice_99
```

## 🔄 Процесс обработки уведомлений

### Прием уведомления
1. Валидация входных данных
2. Создание объекта Notification
3. Сохранение в базу данных
4. Логирование уведомления
5. Возврат успешного ответа

### Получение истории
1. Извлечение логина пользователя из JWT или параметра
2. Поиск уведомлений в базе данных
3. Сортировка по дате создания (новые первыми)
4. Возврат списка уведомлений


### Логирование
```bash
# Просмотр логов
tail -f logs/notifications-service.log

# Уровень логирования в application.yaml
logging:
  level:
    com.iron: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
```
