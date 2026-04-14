# My Bank App

Микросервисное банковское приложение с архитектурой на основе Spring Boot и OAuth 2.0.

## 🏗️ Архитектура

### Схема взаимодействия сервисов

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Front UI      │    │  Gateway API    │    │  Keycloak       │
│   (Port 8083)   │◄──►│   (Port 8080)   │◄──►│   (Port 8080)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
        ┌─────────────────────────────────────────────────┐
        │              Микросервисы                       │
        │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ │
        │  │ Accounts    │ │ Cash        │ │ Transfer    │ │
        │  │ (Port 8081) │ │ (Port 8082) │ │ (Port 8085) │ │
        │  └─────────────┘ └─────────────┘ └─────────────┘ │
        │  ┌─────────────┐                                 │
        │  │ Notifications│                                 │
        │  │ (Port 8084) │                                 │
        │  └─────────────┘                                 │
        └─────────────────────────────────────────────────┘
```

### Принципы взаимодействия

- **Front UI** выполняет аутентификацию/авторизацию на сервере авторизации по **Authorization Code Flow**
- **Front UI** выполняет запросы в микросервисы через **Gateway API** с пробросом JWT-токена
- **Микросервисы** аутентифицируются на сервере авторизации по **Client Credentials Flow** для межсервисного взаимодействия
- **У пользователя** есть доступ только к информации о сумме на своём счёте

## 🚀 Используемые технологии

### Основной стек
- **Java 21** - язык программирования
- **Spring Boot 3.4.4** - фреймворк для создания микросервисов
- **Spring Security 6** - безопасность и аутентификация
- **Spring Cloud Gateway** - API Gateway
- **OAuth 2.0 & JWT** - протокол авторизации и токены
- **Maven** - система сборки

### Инфраструктура
- **Docker & Docker Compose** - контейнеризация (локальная разработка)
- **Kubernetes + Helm** - оркестрация контейнеров
- **PostgreSQL** - база данных
- **Keycloak** - сервер авторизации (OAuth 2.0 / OIDC)
- **nginx-ingress** - внешний доступ к кластеру
- **MapStruct** - маппинг объектов
- **Lombok** - уменьшение шаблонного кода

## 📋 Сервисы

| Сервис | Порт | Описание | Технологии |
|--------|------|----------|------------|
| **Gateway API** | 8080 | API Gateway, маршрутизация запросов | Spring Cloud Gateway, OAuth 2.0 |
| **Accounts** | 8081 | Управление аккаунтами пользователей | Spring Boot, JPA, PostgreSQL |
| **Cash** | 8082 | Операции пополнения/снятия средств | Spring Boot, RestClient |
| **Transfer** | 8085 | Переводы между аккаунтами | Spring Boot, RestClient |
| **Notifications** | 8084 | Система уведомлений | Spring Boot, JPA |
| **Front UI** | 8083 | Веб-интерфейс пользователя | Spring Boot, Thymeleaf |

## 🛠️ Быстрый старт

### Требования
- Java 21+
- Maven 3.8+
- Docker & Docker Compose

### 1. Клонирование репозитория
```bash
git clone <repository-url>
cd my-bank-app
```

### 2. Запуск инфраструктуры
```bash
docker-compose up -d
```

### 3. Создание переменных окружения

Создайте `.env` файлы в корневой директории и в каждом сервисе:

#### Корневой `.env`
```env
# Database
POSTGRES_DB=mybank
POSTGRES_USER=mybank
POSTGRES_PASSWORD=mybank

# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin
```

#### Gateway API (`.env`)
```env
# Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/my-bank-app
JWT_SECRET=your-jwt-secret

# Services
ACCOUNTS_SERVICE_URL=http://localhost:8081
CASH_SERVICE_URL=http://localhost:8082
TRANSFER_SERVICE_URL=http://localhost:8083
NOTIFICATIONS_SERVICE_URL=http://localhost:8084
```

#### Accounts Service (`.env`)
```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mybank
DB_USER=mybank
DB_PASSWORD=mybank

# Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/my-bank-app
JWT_SECRET=your-jwt-secret

# Services
NOTIFICATIONS_SERVICE_URL=http://localhost:8084
```

#### Cash Service (`.env`)
```env
# Services
ACCOUNTS_SERVICE_URL=http://localhost:8081
NOTIFICATIONS_SERVICE_URL=http://localhost:8084

# Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/my-bank-app
JWT_SECRET=your-jwt-secret
```

#### Transfer Service (`.env`)
```env
# Services
ACCOUNTS_SERVICE_URL=http://localhost:8081
NOTIFICATIONS_SERVICE_URL=http://localhost:8084

# Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/my-bank-app
JWT_SECRET=your-jwt-secret
```

#### Notifications Service (`.env`)
```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mybank
DB_USER=mybank
DB_PASSWORD=mybank

# Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/my-bank-app
JWT_SECRET=your-jwt-secret
```

#### Front UI (`.env`)
```env
# Gateway
GATEWAY_URL=http://localhost:8080

# Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/my-bank-app
OAUTH2_CLIENT_ID=my-bank-front
OAUTH2_CLIENT_SECRET=your-client-secret
```

### 4. Запуск сервисов
```bash
# Запуск всех сервисов
./mvnw spring-boot:run

# Или запуск каждого сервиса отдельно
cd gateway-service && ./mvnw spring-boot:run
cd accounts-service && ./mvnw spring-boot:run
cd cash-service && ./mvnw spring-boot:run
cd transfer-service && ./mvnw spring-boot:run
cd notifications-service && ./mvnw spring-boot:run
cd my-bank-front-app && ./mvnw spring-boot:run
```

### 5. Доступ к приложению
- **Front UI**: http://localhost:8083
- **Gateway API**: http://localhost:8080
- **Keycloak Admin**: http://localhost:8080/admin
  - Логин: `admin`
  - Пароль: `admin`

## ☸️ Kubernetes / Helm (Minikube)

### Требования

- Docker
- [Minikube](https://minikube.sigs.k8s.io/docs/start/) ≥ 1.32
- `kubectl`
- `helm` ≥ 3.8

---

### Шаг 1 — Запуск Minikube

```bash
# Запускаем кластер с достаточным количеством ресурсов
minikube start --cpus=4 --memory=6g --driver=docker

# Включаем nginx-ingress контроллер
minikube addons enable ingress

# Проверяем, что ingress-контроллер запустился
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s
```

---

### Шаг 2 — Настройка /etc/hosts

В отдельном терминале запускаем туннель Minikube (держим открытым всё время работы):

```bash
minikube tunnel
```

Добавляем записи в `/etc/hosts` (один раз):

```bash
echo "127.0.0.1 bank.local keycloak.bank.local" | sudo tee -a /etc/hosts
```

---

### Шаг 3 — Сборка Docker-образов

Направляем Docker в реестр Minikube, чтобы образы были доступны кластеру:

```bash
eval $(minikube docker-env)
```

Собираем все сервисы из корня репозитория:

```bash
for svc in gateway-service accounts-service cash-service transfer-service notifications-service my-bank-front-app; do
  docker build -f ${svc}/Dockerfile -t my-bank-app-${svc}:latest .
done
```

Проверяем, что образы появились:

```bash
docker images | grep my-bank-app
```

---

### Шаг 4 — Установка Helm-чарта

```bash
helm install bank ./charts/my-bank-app \
  --namespace bank \
  --create-namespace \
  --wait \
  --timeout 8m
```

> Keycloak при первом запуске импортирует realm (~30–60 секунд), поэтому Spring-сервисы
> могут перезапускаться несколько раз — это нормально. `--wait` дождётся готовности всех подов.

---

### Шаг 5 — Проверка

```bash
# Состояние подов (все должны быть Running)
kubectl get pods -n bank

# Логи конкретного пода
kubectl logs -n bank -l app=accounts-service --tail=50

# Убедиться, что Keycloak загрузил realm
kubectl logs -n bank -l app=keycloak | grep "bank-app-realm"

# Запустить Helm-тесты (проверка /actuator/health всех сервисов + OIDC Keycloak)
helm test bank -n bank --logs
```

---

### Шаг 6 — Доступ к приложению

| Компонент | URL |
|-----------|-----|
| Веб-интерфейс | http://bank.local |
| API через Gateway | http://bank.local/api/accounts, /api/cash, /api/transfer, /api/notifications |
| Keycloak Admin Console | http://keycloak.bank.local/admin |
| Keycloak логин / пароль | `admin` / `password` |

---

### Обновление чарта после изменений

```bash
# Пересобрать изменённый образ (docker-env должен быть активен)
docker build -f accounts-service/Dockerfile -t my-bank-app-accounts-service:latest .

# Обновить релиз
helm upgrade bank ./charts/my-bank-app -n bank --wait --timeout 5m
```

---

### Удаление

```bash
helm uninstall bank -n bank

# Удалить данные Postgres (PersistentVolumeClaim)
kubectl delete pvc -n bank --all

# Остановить Minikube (опционально)
minikube stop
```

---

## 🧪 Тестирование

### Запуск тестов
```bash
# Все тесты
./mvnw test

# Тесты конкретного сервиса
cd accounts-service && ./mvnw test
cd cash-service && ./mvnw test
cd transfer-service && ./mvnw test
cd notifications-service && ./mvnw test
```

## 📝 Документация

- [Gateway API](./gateway-service/README.md)
- [Accounts Service](./accounts-service/README.md)
- [Cash Service](./cash-service/README.md)
- [Transfer Service](./transfer-service/README.md)
- [Notifications Service](./notifications-service/README.md)
- [Front UI](./my-bank-front-app/README.md)

## 📄 Лицензия

MIT License