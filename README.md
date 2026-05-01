# My Bank App

Микросервисное банковское приложение на Spring Boot, OAuth 2.0 и Apache Kafka.

## 🏗️ Архитектура

### Схема взаимодействия сервисов

```text
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Front UI      │    │  Gateway API    │    │   Keycloak      │
│   (Port 8083)   │◄──►│   (Port 8080)   │◄──►│   OAuth 2.0     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
        ┌──────────────────────────────────────────────────────┐
        │                  Микросервисы                        │
        │  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐  │
        │  │ Accounts    │  │ Cash        │  │ Transfer     │  │
        │  │ (Port 8081) │  │ (Port 8084) │  │ (Port 8085)  │  │
        │  └──────┬──────┘  └──────┬──────┘  └──────┬───────┘  │
        │         │                │                │          │
        │         └────────────────┴────────────────┘          │
        │                          │                           │
        │                          ▼                           │
        │                ┌──────────────────┐                  │
        │                │   Apache Kafka   │                  │
        │                │ notifications.*  │                  │
        │                └────────┬─────────┘                  │
        │                         ▼                            │
        │               ┌────────────────────┐                 │
        │               │ Notifications      │                 │
        │               │ (Port 8082)        │                 │
        │               └────────────────────┘                 │
        └──────────────────────────────────────────────────────┘
                                │
                                ▼
        ┌──────────────────────────────────────────────────────┐
        │               Система мониторинга                     │
        │  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐  │
        │  │   Zipkin    │  │ Prometheus  │  │   Grafana    │  │
        │  │ (Tracing)   │  │ (Metrics)   │  │ (Dashboard)  │  │
        │  └──────┬──────┘  └──────┬──────┘  └──────┬───────┘  │
        │         │                │                │          │
        │         └────────────────┴────────────────┘          │
        │                          │                           │
        │                          ▼                           │
        │                ┌──────────────────┐                  │
        │                │     ELK Stack    │                  │
        │                │ (Elasticsearch,  │                  │
        │                │  Logstash, Kibana)│                 │
        │                └──────────────────┘                 │
        └──────────────────────────────────────────────────────┘
```

### Принципы взаимодействия

- **Front UI** выполняет аутентификацию/авторизацию на сервере авторизации по **Authorization Code Flow**
- **Front UI** выполняет запросы в микросервисы через **Gateway API** с пробросом JWT-токена
- **Cash** и **Transfer** аутентифицируются на сервере авторизации по **Client Credentials Flow** для REST-вызовов в **Accounts**
- **Accounts**, **Cash** и **Transfer** публикуют события уведомлений в **Apache Kafka**
- **Notifications** читает события из Kafka и сохраняет/логирует уведомления
- **У пользователя** есть доступ только к информации о сумме на своём счёте

### Мониторинг и логирование

- **Zipkin** собирает распределенные трассировки всех HTTP-запросов между микросервисами
- **Prometheus** собирает метрики производительности, JVM-метрики и бизнес-метрики
- **Grafana** предоставляет дашборды для визуализации метрик и алертинга
- **ELK Stack** собирает, обрабатывает и визуализирует логи из всех микросервисов
- **Все микросервисы** отправляют структурированные логи с trace/span ID для корреляции с трассировкой

## 🚀 Используемые технологии

### Основной стек
- **Java 21** - язык программирования
- **Spring Boot 3.4.4** - фреймворк для создания микросервисов
- **Spring Security 6** - безопасность и аутентификация
- **Spring Cloud Gateway** - API Gateway
- **Spring for Apache Kafka** - публикация и обработка уведомлений
- **OAuth 2.0 & JWT** - протокол авторизации и токены
- **Maven** - система сборки

### Мониторинг и логирование
- **Zipkin** - распределенная трассировка запросов
- **Prometheus** - сбор и хранение метрик
- **Grafana** - визуализация метрик и дашборды
- **Elasticsearch** - хранение и поиск логов
- **Logstash** - обработка и фильтрация логов
- **Kibana** - визуализация и анализ логов
- **Micrometer** - инструментация метрик в Spring Boot
- **Logback** - структурированное логирование с отправкой в ELK

### Инфраструктура
- **Docker & Docker Compose** - контейнеризация (локальная разработка)
- **Kubernetes + Helm** - оркестрация контейнеров
- **Apache Kafka** - обмен уведомлениями между микросервисами
- **PostgreSQL** - база данных
- **Keycloak** - сервер авторизации (OAuth 2.0 / OIDC)
- **nginx-ingress** - внешний доступ к кластеру
- **MapStruct** - маппинг объектов
- **Lombok** - уменьшение шаблонного кода

## 📋 Сервисы

| Сервис | Порт | Описание | Технологии |
|--------|------|----------|------------|
| **Gateway API** | 8080 | API Gateway, маршрутизация запросов | Spring Cloud Gateway, OAuth 2.0 |
| **Accounts** | 8081 | Управление аккаунтами пользователей и отправка notification events | Spring Boot, JPA, Kafka Producer |
| **Cash** | 8084 | Операции пополнения/снятия средств | Spring Boot, RestClient, Kafka Producer |
| **Transfer** | 8085 | Переводы между аккаунтами | Spring Boot, RestClient, Kafka Producer |
| **Notifications** | 8082 | Чтение notification events из Kafka, сохранение и логирование уведомлений | Spring Boot, JPA, Kafka Consumer |
| **Front UI** | 8083 | Веб-интерфейс пользователя | Spring Boot, Thymeleaf |
| **Kafka** | 9092 | Брокер сообщений для Notifications pipeline | Apache Kafka, Helm subchart |

### Компоненты мониторинга

| Компонент | Порт | Описание | Технологии |
|-----------|------|----------|------------|
| **Zipkin** | 9411 | Распределенная трассировка запросов | Zipkin, Micrometer Tracing |
| **Prometheus** | 9090 | Сбор и хранение метрик | Prometheus, Micrometer |
| **Grafana** | 3000 | Визуализация метрик и дашборды | Grafana, Prometheus datasource |
| **Elasticsearch** | 9200 | Хранение и поиск логов | Elasticsearch, Helm subchart |
| **Logstash** | 5044/9600 | Обработка и фильтрация логов | Logstash, Logback encoder |
| **Kibana** | 5601 | Визуализация и анализ логов | Kibana, Elasticsearch |

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
Для локальной разработки нужны PostgreSQL, Keycloak и Kafka. Если вы запускаете сервисы вне Kubernetes, поднимите их любым удобным способом и убедитесь, что Kafka доступна по `localhost:9092`.

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
CASH_SERVICE_URL=http://localhost:8084
TRANSFER_SERVICE_URL=http://localhost:8085
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

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_NOTIFICATIONS_TOPIC=notifications.events
```

#### Cash Service (`.env`)
```env
# Services
ACCOUNTS_SERVICE_URL=http://localhost:8081

# Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/my-bank-app
JWT_SECRET=your-jwt-secret

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_NOTIFICATIONS_TOPIC=notifications.events
```

#### Transfer Service (`.env`)
```env
# Services
ACCOUNTS_SERVICE_URL=http://localhost:8081

# Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/my-bank-app
JWT_SECRET=your-jwt-secret

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_NOTIFICATIONS_TOPIC=notifications.events
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

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_NOTIFICATIONS_TOPIC=notifications.events
KAFKA_NOTIFICATIONS_GROUP=notifications-service
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

### 6. Локальная проверка Kafka
После запуска сервисов создайте топик, если он не создан автоматически:

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --create --if-not-exists --topic notifications.events
```

Проверка:

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --list
```

## ☸️ Kubernetes / Helm 

### Требования

- Docker
- `kubectl`
- `helm` ≥ 3.8

---
### Шаг 1 — Настройка /etc/hosts

Добавьте записи в `/etc/hosts`:

```bash
echo "127.0.0.1 bank.local keycloak.bank.local elasticsearch.bank.local kibana.bank.local grafana.bank.local" | sudo tee -a /etc/hosts
```

---

### Шаг 2 — Сборка Docker-образов

Собираем все сервисы из корня репозитория:

```bash
for svc in gateway-service accounts-service cash-service transfer-service notifications-service my-bank-front-app; do
  docker build -f ${svc}/Dockerfile -t ${svc}:latest .
done
```

Проверяем, что образы появились:

```bash
docker images | grep my-bank-app
```

---

### Шаг 3 — Зависимости Helm-чарта

 Перед установкой чарта нужно обновить зависимости:

```bash
helm dependency update charts/my-bank-app
```

### Шаг 4 — Установка Helm-чарта

```bash
helm install my-bank-app ./charts/my-bank-app \
  --namespace bank \
  --create-namespace \
  --wait \
  --timeout 15m
```

> Kafka поднимается как subchart внутри `my-bank-app`, а broker доступен сервисам по адресу `my-bank-app-kafka:9092`.
> Keycloak при первом запуске импортирует realm (~30–60 секунд), поэтому Spring-сервисы могут перезапускаться несколько раз. `--wait` дождётся готовности всех подов.

---

### Шаг 5 — Проверка

```bash
# Состояние подов (все должны быть Running)
kubectl get pods -n bank

# Логи конкретного пода
kubectl logs -n bank -l app=accounts-service --tail=50

# Убедиться, что Keycloak загрузил realm
kubectl logs -n bank -l app=keycloak | grep "bank-app-realm"

# Убедиться, что Kafka broker отвечает
kubectl exec -n bank my-bank-app-kafka-controller-0 -- \
  kafka-topics.sh --bootstrap-server localhost:9092 --list

# Создать топик уведомлений, если он еще не создан автоматически
kubectl exec -n bank my-bank-app-kafka-controller-0 -- \
  kafka-topics.sh --bootstrap-server localhost:9092 \
  --create --if-not-exists --topic notifications.events

# Проверить наличие топика
kubectl exec -n bank my-bank-app-kafka-controller-0 -- \
  kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### Шаг 6 — Helm-тесты

```bash
# Запустить Helm-тесты

helm test my-bank-app --namespace bank
```

---

### Шаг 7 — Доступ к приложению

| Компонент | URL |
|-----------|-----|
| Веб-интерфейс | http://bank.local/ |
| API через Gateway | http://bank.local/api/accounts, /api/cash, /api/transfer |
| Keycloak Admin Console | http://keycloak.bank.local/admin |
| Keycloak логин / пароль | `admin` / `password` |

### Доступ к компонентам мониторинга

| Компонент | Внешний URL | Внутри кластера | Описание |
|-----------|------------|----------------|----------|
| **Kibana** | http://kibana.bank.local | http://my-bank-app-kibana:5601 | Анализ логов |
| **Elasticsearch** | http://elasticsearch.bank.local | http://my-bank-app-elasticsearch:9200 | Поиск логов |
| **Grafana** | http://grafana.bank.local | http://my-bank-app-grafana:3000 | Дашборды (admin/admin) |
| **Zipkin** | - | http://my-bank-app-zipkin:9411 | Распределенная трассировка |
| **Prometheus** | - | http://my-bank-app-prometheus:9090 | Метрики и алерты |

> Kibana, Elasticsearch и Grafana доступны извне через ingress. Остальные компоненты доступны только внутри кластера или через `kubectl port-forward`

---

### Обновление чарта после изменений

```bash
# Пересобрать изменённый образ
docker build -f accounts-service/Dockerfile -t accounts-service:latest .

# Обновить релиз
helm dependency update charts/my-bank-app
helm upgrade my-bank-app ./charts/my-bank-app -n bank --wait --timeout 15m
```

---

### Удаление

```bash
helm uninstall my-bank-app -n bank

# Удалить данные Postgres и Kafka (PersistentVolumeClaim)
kubectl delete pvc -n bank --all

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

### Проверка Kafka-интеграции в Kubernetes
После выполнения операции обновления аккаунта, пополнения/снятия или перевода проверьте логи `notifications-service`:

```bash
kubectl logs -n bank deployment/notifications-service --tail=200
```

Ожидаемый результат:

```text
[NOTIFICATION] To: <login> | Type: <event-type> | Message: <message>
```

## 📊 Метрики и алерты

### Бизнес-метрики

Система собирает следующие бизнес-метрики для мониторинга операционной активности:

- **`failed_transfers_total`** - количество неудачных переводов (с группировкой по отправителю/получателю)
- **`failed_cash_operations_total`** - количество неудачных операций пополнения/снятия (с группировкой по логину)
- **`notification_failures_total`** - количество неудачных отправок уведомлений (с группировкой по логину)

### Алерты

Настроены алерты для следующих метрик:

#### HTTP-метрики
- **HighErrorRate** - более 5% ошибок 5xx
- **High4xxRate** - более 10% ошибок 4xx
- **HighResponseTime** - 95-й перцентиль времени ответа более 1 секунды

#### JVM-метрики
- **HighMemoryUsage** - использование heap-памяти более 80%
- **HighCPUUsage** - использование CPU более 80%

#### Бизнес-метрики
- **FailedTransfersSpike** - резкое увеличение неудачных переводов
- **FailedCashOperationsSpike** - резкое увеличение неудачных кассовых операций
- **NotificationFailuresSpike** - резкое увеличение неудачных уведомлений

#### Доступность сервисов
- **ServiceDown** - сервис недоступен более 1 минуты

### Дашборды Grafana

Предустановленные дашборды в Grafana:

- **My Bank App - HTTP Metrics** - графики RPS, ошибок, времени ответа
- **My Bank App - JVM Metrics** - графики памяти, CPU, GC
- **My Bank App - Business Metrics** - графики бизнес-метрик и ошибок операций

## 📝 Документация

- [Gateway API](./gateway-service/README.md)
- [Accounts Service](./accounts-service/README.md)
- [Cash Service](./cash-service/README.md)
- [Transfer Service](./transfer-service/README.md)
- [Notifications Service](./notifications-service/README.md)
- [Front UI](./my-bank-front-app/README.md)

