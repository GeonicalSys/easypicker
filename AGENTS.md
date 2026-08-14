# easypicker-module — инструкции для ИИ-агентов

Перед изменением прочитай `docs/README.md` и `docs/manifest.yaml`. В parent
workspace проверь `../docs/registry/dependencies.yaml` и change-impact.

Это отдельный fork upstream EasyPicker, подключённый root как
`:easypicker-module` и экспортируемый `maplibui`. Изменение публичного widget API
проверять в `maplibui` и `app`. Не переносить сюда продуктовую GIS-логику.

При standalone checkout central docs могут отсутствовать; local pack остаётся
обязательным, а cross-repository изменение нужно дополнительно отразить в root.

## Git-доставка

Запрос изменить/исправить/добавить содержимое репозитория разрешает агенту
создать `codex/*` ветку, выполнить проверки, scoped commits, push и открыть
Draft PR без отдельных подтверждений каждого шага. Read-only запросы этого не
разрешают. Direct push в `master`, force push, tag/release и merge запрещены без
явного намерения пользователя завершить выпуск. PR библиотеки сливается через
Merge Commit; связанный root PR обновляет submodule pointer после merge.
