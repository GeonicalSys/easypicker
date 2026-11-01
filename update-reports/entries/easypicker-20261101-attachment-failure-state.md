---
id: easypicker-20261101-attachment-failure-state
area: Мобильное приложение
kind: user
at: 2026-11-01T15:00:00+04:00
title: Неудачная загрузка вложения освобождает ожидание и сохраняет прежний кеш
why: После отсутствующего или оборванного вложения можно повторить загрузку без ложной готовности неполного файла.
covers:
  - easypicker/src/main/java/com/keenfin/easypicker/DownloadPhotoIntentService.java
  - easypicker/src/test/java/com/keenfin/easypicker/AttachmentDownloadFailureStateTest.java
  - easypicker/build.gradle
  - docs/README.md
  - docs/manifest.yaml
  - README.md
---

HTTP 404 завершает ожидание с исходным статусом. Передача использует собственный
временный файл и публикует результат после 200 и закрытия потока. Ошибка сохраняет
прежний кеш и соседние файлы; staging принадлежит только текущей попытке.

Семь адресных JVM-проверок покрывают 404, неполную передачу, прежний кеш,
успешную публикацию, повторную попытку, preview/отказ/прерывание и ошибки
закрытия/публикации. Они используют настоящий service/copy path с HTTP double.
Одинаковый окончательный fixture: прежний код — 7 assertion FAIL, исправление —
7 PASS, без errors/skips; GREEN XML 2.703s. Результаты фиксируются отдельно в task evidence; native/GUI/device,
реальные серверы и данные не проверялись. App получает исправление только после
слияния библиотеки и отдельного обновления gitlink; наличие этого PR не означает выпуск.
