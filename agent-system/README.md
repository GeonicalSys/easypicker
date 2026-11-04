# EasyPicker knowledge sources

This owner participates in the whole LISA knowledge restructuring. Ten logical
rules/contracts from the complete unchanged [AGENTS](../AGENTS.md) are editable
typed Git records in [knowledge/policy](knowledge/policy). The
[source map](policy-migration.json) pins the original Git object and maps every
normalized character, including layout, to its reviewed destination. The
[disposition catalog](source-dispositions.json) retains that map as evidence.

The record owner is `android_projects/android_gisapp/easypicker`; stable IDs use
`lisa:easypicker`. Module/API consumers, standalone documentation, read-only versus
change authorization, library Merge Commit before the consuming app pointer,
report identity and hook admission retain all original conditions. They are
knowledge assertions, never current execution or publication authority.

Edit the native logical record and its explicit reviewed mapping together; check
the entire original body and every condition. Until measured client cutover,
existing AGENTS, validators and hooks remain effective. Standalone opening may
read these ordinary JSON sources; it must report the shared compiler unavailable
if the LISA root is absent, without creating a private scanner or claiming a
successful source replay. With the complete LISA workspace, use its shared
`py -3.13 -B -m tools.agent_system check-policy-migration` command. Its default
foreign-owner source basis is committed HEAD; uncommitted owner changes require
an explicitly declared live frame in the shared database build/check protocol.

The module pack has two authoring groups. The
[manifest records](knowledge/easypicker-module-manifest) preserve 14 ordered
fields, including the three empty reference lists. The
[documentation records](knowledge/documentation) preserve both README addresses
and the attachment download lifecycle. Their
[manifest](provenance/easypicker-module-manifest.json) and
[Markdown](provenance/documentation.json) provenance bind the original Git
objects and each ordered member. App and maplibui consumers are pinned in the
manifest provenance.

Edit the owner records, then run these commands from the consuming App
checkout that includes the shared module tools.

```powershell
python -B docs/scripts/prose_knowledge.py --write --module easypicker-module
python -B docs/scripts/structured_view_proof.py --write --module easypicker-module
python -B docs/scripts/validate_docs.py --workspace-root . --module easypicker-module --require-pack
```

Both README views and the YAML manifest are available at their existing
standalone addresses. Commit owner records, refreshed views and the
[manifest proof](provenance/structured-views.json) together. The common
catalogue can verify the proof using the exact App and EasyPicker source
frames. The remaining owner knowledge and actual client loading are tracked
in the whole migration.
