# Проект Explore With Me (англ. «исследуй со мной»)
Он позволяет пользователям делиться информацией об интересных событиях и находить компанию для участия в них.

Проект разработан по микросервисной архитектуре и состоит из следующих модулей и сервисов:

## Модуль `core`
Содержит основные сервисы, отвечающие за бизнес-логику приложения:

1. **event-service** — управление событиями, категориями и подборками событий
2. **user-service** — управление пользователями
3. **request-service** — управление заявками на участие в событиях
4. **comment-service** — управление комментариями к событиям
5. **interaction-api** — общие DTO, типы ошибок, API клиентов для межсервисного взаимодействия

## Модуль `infra`
Содержит инфраструктурные сервисы, обеспечивающие работу приложения:

1. **config-server** — централизованное хранение конфигураций для всех сервисов. Конфигурации находятся в пакете `resources` модуля `config-server`
2. **gateway** — API Gateway, который обеспечивает единую точку входа для всех запросов к сервисам
3. **discovery-server** — сервис Discovery (Eureka), который регистрирует все микросервисы и обеспечивает их обнаружение

## Модуль `stats`
Собирает информацию о статистике просмотров событий:

1. **stats-client** — обеспечивает взаимодействие с сервисами модуля `core`
2. **stats-dto** — модуль для DTO
3. **stats-server** — сервис для сбора статистики

## Спецификации внешнего API
Спецификации внешнего API можно найти по следующим ссылкам:
1. [Спецификация основного сервиса](https://github.com/masicool/java-plus-graduation/blob/main/ewm-main-service-spec.json)
2. [Спецификация сервиса статистики](https://github.com/masicool/java-plus-graduation/blob/main/ewm-stats-service-spec.json)

## Спецификация внутреннего API
Префиксы внутренних эндпоинтов (роутов): {имя сервиса}/internal

Взаимосвязь сервисов:

| Сервис          | Используемые сервисы                                          |
|-----------------|---------------------------------------------------------------|
| event-service   | user-service, stats-service, request-service, comment-service |
| comment-service | user-service, event-service, request-service                  |
| request-service | user-service, event-service                                   |
| user-service    | ---                                                           |
| stats-service   | ---                                                           |

Описание внутренних API:

| Event API                            | Описание                |
|--------------------------------------|-------------------------|
| GET event-service/internal/{eventId} | получение события по ID |

| Request API                                              | Описание                                 |
|----------------------------------------------------------|------------------------------------------|
| GET request-service/internal/find-by-events              | Получение событий по списку ID и статусу |
| GET request-service/internal/count                       | Количество запросов по событию и статусу |
| GET request-service/internal/find-by-requester-and-event | Получение события по ID и подписчику     |

| User API                           | Описание                                |
|------------------------------------|-----------------------------------------|
| GET user-service/internal/{userId} | Получение пользователя по ID            |
| GET user-service/internal          | Получение списка пользователей по их ID |

| Comment API                                          | Описание                                        |
|------------------------------------------------------|-------------------------------------------------|
| GET comment-service/internal/{commentId}             | Получение комментария по ID                     |
| GET comment-service/internal                         | Получение комментариев по списку ID событий     |
| GET comment-service/internal/find-by-event/{eventId} | Получение количества комментариев по ID событию |

## Тестирование
Для проверки работы сервисов разработаны Postman-тесты. Они находятся по следующим ссылкам:
1. [Проверка работоспособности основого сервиса включая event-service, user-service, request-service](https://github.com/masicool/java-plus-graduation/blob/main/postman/ewm-main-service.json)
2. [Проверка работоспособности сервиса статистики](https://github.com/masicool/java-plus-graduation/blob/main/postman/ewm-stat-service.json)
3. [Проверка работоспособности сервиса комментариев](https://github.com/masicool/java-plus-graduation/blob/main/postman/feature.json)

