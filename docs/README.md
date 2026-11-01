---
title: easypicker-module — picker UI library
module_id: easypicker-module
last_verified: 2026-11-01
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

### Загрузка вложений

`DownloadPhotoIntentService` завершает обычную загрузку сообщением `END`
с фактическим HTTP 404, если вложение не найдено. Это снимает отметку ожидания;
404 не становится успешной загрузкой. Preview по-прежнему не отправляет `START`,
но отправляет свой terminal status.

Каждая попытка пишет в собственный `.attachment-*.part` в каталоге назначения.
Конечное имя появляется только после завершённого HTTP 200 и закрытия output,
через atomic move без copy fallback. При ошибке передачи, закрытия или публикации
прежний конечный файл и соседние записи не удаляются. Удаляется только staging
этой попытки; неудача его удаления остаётся в локальном журнале. Входной HTTP
поток и connection закрываются. Публичный `getStream` сохраняет прежнее закрытие
переданного output при успешном 200.

Если файловая система не поддерживает atomic move, попытка считается неудачной;
перенос копированием в конечный файл не выполняется, прежний кеш сохраняется.

Это не проверка полноты по checksum/Content-Length, не очистка старого повреждённого
кеша и не новое правило одновременных загрузок/общего preview-state. Наличие
metadata/URI и положительный размер старого файла сами по себе не доказывают
получение актуальных bytes.

Адресная JVM-регрессия находится в библиотеке, а не в App со старым gitlink:

```powershell
.\gradlew.bat '--offline' '--no-daemon' '--max-workers=2' '-Pkotlin.incremental=false' '-PciReliabilityChecks=true' ':easypicker-module:testDebugUnitTest' '--tests' 'com.keenfin.easypicker.AttachmentDownloadFailureStateTest' '--console=plain'
```

Команда запускается из корня Android workspace с установленным JBR 21 и
существующими Q Gradle/Maven cache. Для автономного стенда Robolectric используйте
`JAVA_TOOL_OPTIONS=-Djava.io.tmpdir=Q:/Codex/Temp -Drobolectric.dependency.repo.url=file:///Q:/Codex/Cache/maven`,
`GRADLE_USER_HOME=Q:\Codex\Cache\gradle`, `TEMP/TMP/TMPDIR=Q:\Codex\Temp`.
Тесты вызывают настоящий service worker и copy-код с техническим HTTP double;
они не открывают сеть, реальные фото, устройство или БД. Это не native/GUI acceptance.


## Учёт доработок

Карточки и автоматический контроль исходных путей: [реестр обновлений](../update-reports/README.md).
Перед push выполняется локальный hook; в новом клоне требуется его установка.
