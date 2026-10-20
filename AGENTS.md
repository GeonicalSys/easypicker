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

## Реестр обновлений для отчёта

Доработка, которую нужно объяснить в отчёте заказчику или во внутреннем
списке для оплаты, получает карточку `update-reports/entries/<id>.md` в том
же PR. Префикс id: `easypicker-`. Срезы живут в монорепозитории `lisa`
(`docs/update-reports/`). Граница — номера карточек, не дата мержа.


## Обязательный контроль реестра перед push

Новая самостоятельная доработка получает новый ID карточки и `covers` с
точными путями всех её изменённых исходников/тестов/скриптов. Старый ID
не дописывать новым поведением. Для служебного исключения нужен новый
`exemptions/*.yaml` с точными путями и конкретной причиной.
Перед commit и push выполнить `py -3.13 -B tools/check_update_reports.py`.
При новом клоне установить PyYAML из `tools/requirements-update-reports.txt`
и выполнить `py -3.13 tools/check_update_reports.py --install-hook`.
Обычный push блокируется hook при пропуске или ошибке. Запрещено обходить
его `--no-verify`, менять политику либо выдавать фиктивное исключение ради
зелёной проверки. Проверка путей не заменяет содержательный review карточек.
