# Accounts Service

Микросервис управления аккаунтами пользователей в банковской системе.

## 📋 Обзор

Сервис отвечает за хранение и управление информацией о пользователях и их счетах:

### Хранимые данные
- **Логин пользователя** - уникальный идентификатор аккаунта
- **Фамилия и имя** - персональные данные пользователя
- **Дата рождения** - для валидации возраста (18+)
- **Количество денег на счету** - баланс пользователя

### Основные функции
- Управление профилем пользователя
- Валидация данных (возраст от 18 лет)
- Предоставление списка пользователей для переводов
- Интеграция с сервисом уведомлений

## 🚀 Технологии

| Технология | Версия | Назначение |
|------------|--------|------------|
| **Java** | 21 | Язык программирования |
| **Spring Boot** | 3.4.4 | Основной фреймворк |
| **Spring Security** | 6 | Безопасность и OAuth 2.0 |
| **Spring Data JPA** | 3.4.4 | Работа с базой данных |
| **PostgreSQL** | 15+ | СУБД |
| **MapStruct** | 1.5+ | Маппинг DTO |
| **Lombok** | 1.18+ | Уменьшение шаблонного кода |
| **Maven** | 3.8+ | Сборка проекта |

## 🌐 Порты и эндпоинты

### Порт
- **8081** - основной порт сервиса

### API Эндпоинты

#### Управление аккаунтом
- `GET /api/accounts/me` - Получение данных текущего пользователя
- `PUT /api/accounts/me` - Обновление данных текущего пользователя
- `GET /api/accounts/others` - Получение списка других пользователей (для переводов)

#### Внутренние эндпоинты
- `PATCH /api/accounts/{login}/decrease-balance` - Уменьшение баланса
- `PATCH /api/accounts/{login}/increase-balance` - Увеличение баланса

## 🔐 Безопасность

### OAuth 2.0 Configuration
- **Client Credentials Flow** для межсервисного взаимодействия
- **JWT Bearer Token** для аутентификации пользователей
- **Required scopes**: `ROLE_NOTIFICATIONS_USER` для доступа к уведомлениям

### Права доступа
- **Пользователи** могут управлять только своим аккаунтом
- **Микросервисы** имеют доступ по Client Credentials Flow

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

# Внешние сервисы
NOTIFICATIONS_SERVICE_URL=http://localhost:8084

# Приложение
SERVER_PORT=8081
SPRING_PROFILES_ACTIVE=docker
```

### Структура конфигурации
```yaml
# application.yaml
spring:
  application:
    name: accounts-service
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
docker build -t accounts-service .

# Запуск
docker run -p 8081:8081 --env-file .env accounts-service
```

### Запуск в Docker Compose
```bash
# В корневой директории проекта
docker-compose up accounts-service
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
- **Unit тесты** - сервисный слой, мапперы
- **Integration тесты** - REST контроллеры, база данных
- **TestContainers** - тесты с реальной PostgreSQL

## 📊 Модели данных

### Account Entity
```java
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String login;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false)
    private LocalDate birthday;
    
    @Column(nullable = false)
    private BigDecimal balance;
}
```

## 🔗 Интеграции

### Исходящие вызовы
- **Notifications Service** - отправка уведомлений об операциях

### Входящие вызовы
- **Gateway API** - запросы от фронтенда
- **Cash Service** - операции с балансом
- **Transfer Service** - переводы между аккаунтами

## 📝 Примеры запросов

### Получение данных аккаунта
```bash
curl -H "Authorization: Bearer <JWT_TOKEN>" \
     http://localhost:8081/api/accounts/me
```

### Обновление данных аккаунта
```bash
curl -X PUT \
     -H "Authorization: Bearer <JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"name":"John Doe","birthday":"1990-01-01"}' \
     http://localhost:8081/api/accounts/me
```

### Логирование
```bash
# Просмотр логов
tail -f logs/accounts-service.log

# Уровень логирования в application.yaml
logging:
  level:
    com.iron: DEBUG
    org.springframework.security: DEBUG
```