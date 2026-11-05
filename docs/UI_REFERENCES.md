# Références UI consultées

Source de vérité n°1 = code du dépôt d'origine (layouts, thèmes, styles) — voir `PORTING_AUDIT.md`.
Source de vérité n°2 = captures du dépôt `metadata/en-US/images/`.
Ce fichier liste les références externes consultées en cas de doute, avec la décision prise.

| Date | URL | Écran concerné | Décision |
|---|---|---|---|
| 2026-11-05 | https://github.com/Kotatsu-Redo/Kotatsu-Redo (code source) | tous | disposition, couleurs, dimensions issues du code (res/values/*, res/layout*) |
| 2026-11-05 | https://m3.material.io/components/navigation-rail/overview | shell principal (rail latéral) | comportement Material 3 standard, libellés = ceux de l'app d'origine (res/values/strings.xml) |
| 2026-11-05 | https://m3.material.io/styles/color/system/overview | thème clair/sombre/AMOLED | palette M3 reprise tel quel de `colors_kotatsu.xml` + `values-night` |
