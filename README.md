# Технологии
- Java 21
- Spring Boot
- Spring Data Jpa
- Spring Web
- Lombok
- Flyway
### БД
- PostgreSQL (для хранения лимитов и транзакций)
- Cassandra (для хранения курсов обмена валют)
### Сборка
- Maven
### Тестирование
- Junit
- Mockito
- WireMock
### Контейнеризация
- Docker, Docker compose

# Запуск
Для запуска в контейнере со всеми необходимыми зависимостями достаточно запустить Docker compose скрипт в [папке docker-compose](./docker-compose):
```
docker-compose up -d
```
Для запуска вне конейнера необходимо в [.properties](./src/main/resources/application.properties) прописать необходимые переменные (пароль для postgresql и т.д).

\
API будет доступен на порту 8080.

После запуска swagger будет доступен по пути `/swagger-ui.html`.
