# easypicker-module — инструкции для ИИ-агентов

Перед изменением прочитай `docs/README.md` и `docs/manifest.yaml`. В parent
workspace проверь `../docs/registry/dependencies.yaml` и change-impact.

Это отдельный fork upstream EasyPicker, подключённый root как
`:easypicker-module` и экспортируемый `maplibui`. Изменение публичного widget API
проверять в `maplibui` и `app`. Не переносить сюда продуктовую GIS-логику.

При standalone checkout central docs могут отсутствовать; local pack остаётся
обязательным, а cross-repository изменение нужно дополнительно отразить в root.
