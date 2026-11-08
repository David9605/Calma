# 💙 Ansiedad Control

App Android (Kotlin + Jetpack Compose) para calmar la ansiedad. Funciona **100 % sin Internet**, sin cuentas y sin anuncios. Todo se guarda solo en el teléfono.

## Qué hace
| # | Función | Dónde está |
|---|---|---|
| 1 | Botón «Me siento ansioso» | `ui/Home.kt` |
| 2 | Respiración guiada (5 patrones) | `ui/Breathing.kt` |
| 3 | Mi canción favorita (audio local) | `ui/Media.kt` |
| 4 | Mis videos divertidos (locales; enlaces necesitan Internet) | `ui/Media.kt` |
| 5 | Anclaje 5-4-3-2-1 | `ui/Breathing.kt` |
| 6 | Contactos de apoyo (llamar, SMS, WhatsApp) | `ui/Contacts.kt` |
| 7 | Recordatorios (notificaciones sin Internet) | `ui/Reminders.kt`, `system/Scheduler.kt` |
| 8 | Diario de ánimo con gráfico semanal | `ui/Diary.kt` |
| 9 | Detector de patrones y desencadenantes | `data/Insights.kt` |
| 10 | Caja de cosas buenas (fotos y frases) | `ui/GoodBox.kt` |
| 11 | Sonidos relajantes + temporizador (generados en el teléfono) | `ui/Sounds.kt`, `system/Audio.kt` |
| 12 | Registro de pensamientos | `ui/Thoughts.kt` |
| 13 | Plan de crisis personal | `ui/Safety.kt` |
| 14 | Ayuda urgente por país | `ui/Safety.kt`, `data/HelpData.kt` |
| 15 | Privacidad: PIN, huella, borrar todo | `ui/Settings.kt` |
| 16 | Minijuegos: burbujas, jardín zen, luciérnagas | `ui/Games.kt` |
| 17 | Mates para calmar la mente (adaptativas) | `ui/Fun.kt` |
| 18 | Tararea para calmarte (ejercicio guiado) | `ui/Fun.kt` |
| 21 | Promesas de Dios, Biblia hablada, Mensajes de amor, copia de seguridad | `ui/Faith.kt`, `ui/Love.kt`, `data/Backup.kt` |
| 19 | 300+ chistes sin Internet, con favoritos | `assets/chistes.txt`, `ui/Fun.kt` |
| 20 | **Sistema de 10 pasos** que aprende lo que te ayuda | `ui/Session.kt`, `data/Catalog.kt` |

## Cómo está organizado
```
app/src/main/java/com/calmaceleste/app/
  data/     modelos, almacenamiento, banco de 60+ pasos, patrones, ayuda
  system/   recordatorios, audio, llamadas/mensajes
  ui/       pantallas, tema celeste y animaciones
app/src/main/assets/chistes.txt   los chistes (se editan como texto)
.github/workflows/                compilar, firmar y publicar
docs/                             guías (publicar, actualizar, privacidad)
play-store/                       ficha, gráficos, formularios, novedades
```

## Empezar
1. Sube el proyecto a GitHub → `docs/PUBLICAR.md`.
2. Para una versión nueva → `docs/ACTUALIZAR.md`.

## Notas honestas
- Los datos se guardan en el almacenamiento privado de la app (Android los aísla de otras apps), pero **no llevan un cifrado propio adicional**. El PIN protege el acceso a la app.
- «Tararea» es un ejercicio guiado: **no** reconoce canciones ni usa el micrófono.
- La app es un apoyo de bienestar, **no un dispositivo médico**.
- Versión mínima de Android: 8.0 (API 26). Apunta a Android 16 (API 36).
