---
id: easypicker-20261103-agent-knowledge-policy
area: Сопровождение
kind: internal
at: 2026-11-03T10:27:18+04:00
title: Постоянные правила EasyPicker сохранены у владельца
why: Общие запросы должны сохранять ограничения и происхождение каждого владельца без ручных копий в root
covers:
  - agent-system/README.md
  - agent-system/knowledge/policy/workspace.git-delivery.json
  - agent-system/knowledge/policy/workspace.independent-change-card.json
  - agent-system/knowledge/policy/workspace.module-boundaries.json
  - agent-system/knowledge/policy/workspace.prerequisites.json
  - agent-system/knowledge/policy/workspace.report-acceptance.json
  - agent-system/knowledge/policy/workspace.report-hook-admission.json
  - agent-system/knowledge/policy/workspace.report-hook-enforcement.json
  - agent-system/knowledge/policy/workspace.report-owner-and-boundary.json
  - agent-system/knowledge/policy/workspace.report-persistence.json
  - agent-system/knowledge/policy/workspace.standalone-docs.json
  - agent-system/policy-migration.json
  - agent-system/source-dispositions.json
---

Полный прежний AGENTS разложен на явно выбранные логические правила и контракты.
Редактируемые записи и полная карта исходных символов находятся в этом владельце;
общий компилятор LISA проверяет ownership, pinned Git objects, тело и все условия.
Исходный AGENTS, product code, версии, device/storage и серверная политика не меняются.

Это весь согласованный шестирепозиторный перенос, а не завершённый новый клиент.
Новые записи включены в отдельный независимый owner corpus root PR282.
Проверки исходников/выдачи, commit/PR, merge, runtime и публикация учитываются отдельно.
Library Merge Commit предшествует будущему App gitlink; незамерженный pin не принят.
Publisher идёт после App/его зависимостей. Merge/APK/deployment не разрешены.
Related/Full/native/GUI/AVD/NGW/DB/cloud NOT_RUN; исходный продукт не изменён.
Source replay19sources/421units/758values PASS; все25 новых значений и19 полных тел сохранены.
Три прежних и новый27IDs/6fields корпуса PASS, все83 страницы сверены целиком.
Product source и Gradle inputs не менялись; Gradle/native NOT_RUN для этого переноса данных.
Own Draft: https://github.com/GeonicalSys/easypicker/pull/7
Root task: https://github.com/GeonicalSys/lisa/pull/282
