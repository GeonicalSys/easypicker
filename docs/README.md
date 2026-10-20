---
title: easypicker-module — picker UI library
module_id: easypicker-module
last_verified: 2026-10-20
---

# easypicker-module — picker UI library

## Назначение

Вспомогательная Android UI-библиотека выбора значений. Root подключает каталог
`easypicker/easypicker` как Gradle-модуль `:easypicker-module`; `maplibui`
экспортирует его API, `app` также имеет прямую implementation dependency.

## Ограничения

- Не добавлять GIS, NGW или продуктовую brand-логику.
- Сохранять совместимость публичных widgets с `maplibui` forms.
- Upstream sync выполнять как отдельный repository до `maplib/maplibui/root`.

## Проверки

После изменения собрать библиотеку и `maplibui`, а для публичного API — app
release variants.


## Учёт доработок

Карточки и автоматический контроль исходных путей: [реестр обновлений](../update-reports/README.md).
Перед push выполняется локальный hook; в новом клоне требуется его установка.
