# Ficha de Play Store y respuestas a los formularios

Todo lo que Google Play Console te va a pedir, con la respuesta que corresponde a esta app.
Los textos de la ficha están listos para copiar en `play-store/listing/es-419/`.

## Datos básicos
| Campo | Valor |
|---|---|
| Nombre de la app (máx. 30) | Ansiedad Control |
| Descripción breve (máx. 80) | `short_description.txt` |
| Descripción completa (máx. 4000) | `full_description.txt` |
| Idioma predeterminado | Español (Latinoamérica) – es-419 |
| Tipo | App (no juego) |
| Gratis o de pago | Gratis |
| Categoría | Salud y bienestar |
| Correo de contacto | **tu correo** (se muestra públicamente) |
| Política de privacidad (URL) | Ver `docs/PRIVACIDAD.md` (publícala con GitHub Pages, instrucciones en `docs/PUBLICAR.md`) |

## Gráficos que debes subir
| Elemento | Tamaño | Archivo |
|---|---|---|
| Icono de la app | 512 × 512 PNG | `graphics/icono-512.png` |
| Gráfico de funciones | 1024 × 500 PNG/JPG | `graphics/grafico-funciones-1024x500.png` |
| Capturas de pantalla del teléfono | mínimo 2, recomendado 4–8 | **Hazlas tú** con la app instalada (no se pueden inventar: Google exige capturas reales) |

Capturas recomendadas: pantalla de inicio, un paso de respiración, un minijuego, el diario con gráfico, la lista de contactos.

## Contenido de la app (Policy → App content)
- **Anuncios:** No contiene anuncios.
- **Acceso a la app:** Todas las funciones están disponibles sin credenciales (el PIN es opcional y lo crea el propio usuario).
- **Público objetivo:** 18 años o más (recomendado por ser una app de bienestar emocional).
- **Clasificación de contenido:** responde el cuestionario con «No» a violencia, sexo, drogas, apuestas y contenido generado por usuarios. Resultado esperado: apta para todo público.
- **Apps de salud (Health apps declaration):** Declara que es una app de **bienestar/salud mental de autoayuda**, que **no es un dispositivo médico**, que no diagnostica ni trata y que incluye aviso y recursos de ayuda urgente.
- **Noticias, COVID, finanzas, gobierno:** No.

## Seguridad de los datos (Data safety)
- ¿Recopila o comparte datos del usuario? **No** (todo se guarda localmente en el dispositivo y no sale de él).
- ¿Cifrado en tránsito? No aplica (no transmite datos).
- ¿Puede el usuario pedir que se borren sus datos? **Sí**: en la app, Ajustes → «Borrar todos mis datos».

> La app abre enlaces, el marcador, SMS o WhatsApp solo cuando la persona lo pide, usando las apps del propio teléfono. La app no recibe ni guarda nada de esas apps.

## Permisos que declara la app
| Permiso | Para qué |
|---|---|
| `POST_NOTIFICATIONS` | Mostrar los recordatorios que el usuario crea |
| `RECEIVE_BOOT_COMPLETED` | Reprogramar los recordatorios después de reiniciar el teléfono |
| `VIBRATE` | Vibración suave en la respiración y los juegos |
| `USE_BIOMETRIC` | Desbloquear con huella o rostro (opcional) |

No usa Internet, ubicación ni contactos. Pide el micrófono solo para grabar notas de voz (se quedan en el teléfono; no se envían). Los videos se graban con la app de cámara del sistema (la app no declara el permiso de cámara). Datos de seguridad: «No se recopilan datos». Declaración del permiso RECORD_AUDIO: se usa solo para grabar notas de voz que el usuario guarda en su dispositivo. No necesita declaraciones especiales (no usa alarmas exactas ni permisos sensibles).

## Requisito técnico vigente
Desde el 31 de agosto de 2026, las apps nuevas y las actualizaciones deben apuntar a **Android 16 (API 36)**. Esta app ya apunta a API 36 (`targetSdk = 36`).

## Cuenta personal nueva: prueba cerrada obligatoria
Si tu cuenta de desarrollador es **personal y se creó después del 13 de noviembre de 2023**, antes de publicar en producción Google exige una **prueba cerrada con un mínimo de testers (12 según la ayuda de Google; confirma el número vigente en tu Play Console) que permanezcan inscritos 14 días seguidos**. Las cuentas de organización no tienen este requisito. Ver los pasos en `docs/PUBLICAR.md`.
