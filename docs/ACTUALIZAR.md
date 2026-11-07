# Cómo actualizar la app en el futuro

## Sacar una versión nueva (3 pasos)
1. Cambia el número de versión en `version.properties`:
   ```
   versionName=1.1.0
   ```
2. Escribe qué cambió en `play-store/whatsnew/whatsnew-es-419`.
3. Sube los cambios y la etiqueta:
   ```bash
   git add .
   git commit -m "Versión 1.1.0"
   git push
   git tag v1.1.0
   git push origin v1.1.0
   ```
GitHub compila, firma y deja el AAB en *Releases* (y lo sube a Play si activaste el Paso 8 de `PUBLICAR.md`).
El `versionCode` (número interno que Google exige que siempre suba) lo pone GitHub solo.

## Dónde cambiar cada cosa
| Quiero… | Archivo |
|---|---|
| Añadir **chistes** | `app/src/main/assets/chistes.txt` → una línea nueva: `Categoria\|Texto##Remate` |
| Añadir/quitar **pasos** de los 10 pasos | `app/src/main/java/com/calmaceleste/app/data/Catalog.kt` (cada paso es una línea) |
| Cambiar **números de ayuda** | `data/HelpData.kt` (revísalos en cada versión) |
| Cambiar los **colores** celestes | `ui/theme/Theme.kt` y `ui/Components.kt` (`CalmBackground`) |
| Cambiar **textos de bienvenida** | `ui/Settings.kt` (`onboardPages`) |
| Añadir un **patrón de respiración** | `data/Catalog.kt` (`BREATH_PATTERNS`) |
| Añadir un **sonido** | `system/Audio.kt` (`SoundKind` y su generador) |
| Añadir un **minijuego** | `ui/Games.kt` + ruta en `ui/Nav.kt` + entrada en `GamesScreen` |
| Cambiar el **icono** | `app/src/main/res/drawable/ic_launcher_foreground.xml` y `values/colors.xml` |
| Cambiar **versiones de librerías** | `gradle/libs.versions.toml` (un solo lugar) |

Las pruebas automáticas (`app/src/test`) avisan si, al editar, el banco de pasos queda con menos de 60, hay ids repetidos o los chistes bajan de 300.

## Mantenerse al día con Google Play (cada año)
- Cada **agosto**, Google sube el nivel de API exigido. Para subirlo cambia `compileSdk` y `targetSdk` en `app/build.gradle.kts` (hoy 36) y la versión de `agp` en `gradle/libs.versions.toml`. Lee las «Behavior changes» de Android para esa versión.
- **Dependabot** (ya configurado) te abre una solicitud mensual con librerías nuevas. Si el flujo **Compilar y probar** sale ✅, aprueba y combina.
- Mientras la app no se actualice a la API nueva, sigue disponible para quien ya la tiene, pero Google deja de mostrarla a usuarios nuevos en teléfonos más modernos.

## Ideas para próximas versiones
- Cifrado adicional de los datos del diario en el teléfono.
- Copia de seguridad manual (exportar/importar un archivo) para cambiar de teléfono; hoy los datos no se respaldan en la nube a propósito.
- Más juegos (rompecabezas con tus fotos, mandalas para colorear).
- Grabaciones reales de sonidos (lluvia, mar) en `res/raw`.
- Widget en la pantalla de inicio con el botón «Me siento ansioso».
- Otros idiomas (cada idioma extra: textos y chistes nuevos).
- Versión de pago o suscripción: paquetes de sonidos y juegos, respaldo cifrado, informe PDF para el terapeuta.
