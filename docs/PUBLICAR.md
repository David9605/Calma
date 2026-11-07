# Cómo publicar Calma Celeste en Google Play (paso a paso)

> Los menús de GitHub y de Google cambian de vez en cuando. Si algún botón no está donde digo, busca el nombre entre comillas.

Tiempo estimado: 1 hora de preparación + 2 a 3 semanas si tu cuenta de Play es personal y nueva (por la prueba cerrada obligatoria).

---

## Paso 1 · Subir el proyecto a GitHub
1. Entra a github.com → **New repository** → nombre `CalmaCeleste` → **Private** (privado) → crear.
2. En tu computadora, abre una terminal dentro de la carpeta `CalmaCeleste` y ejecuta:

```bash
git init -b main
git add .
git update-index --chmod=+x gradlew
git commit -m "Primera versión de Calma Celeste"
git remote add origin https://github.com/TU_USUARIO/CalmaCeleste.git
git push -u origin main
```

Sin terminal: instala **GitHub Desktop**, elige *Add local repository* y pulsa *Publish repository*.

## Paso 2 · Comprobar que compila
1. En GitHub abre la pestaña **Actions**. Verás el flujo **Compilar y probar** corriendo.
2. Cuando termine con ✅ verde, abre la ejecución y descarga el **APK de prueba** (sección *Artifacts*) para instalarlo en tu teléfono.
3. Si sale ❌ rojo: abre el paso que falló, copia el error y pásamelo; se corrige en minutos. *(El código se escribió sin poder compilarlo en el entorno donde se creó, así que esta primera ejecución es la prueba real.)*

## Paso 3 · Crear la llave de firma
Google Play necesita que el AAB esté firmado con una llave tuya (la «llave de subida»).

**Opción A (sin instalar nada):**
1. Pestaña **Actions** → **Crear llave de firma (solo una vez)** → **Run workflow**.
2. Al terminar, descarga el archivo `llave-de-firma-PRIVADA` (sección *Artifacts*). Dentro hay 3 archivos.
3. **Guarda una copia** del `.jks` y de `LEEME-SECRETOS.txt` en un lugar seguro (gestor de contraseñas o USB). Se borran de GitHub solos en 1 día.
4. En el repositorio: **Settings → Secrets and variables → Actions → New repository secret** y crea estos 4:

| Nombre | Valor |
|---|---|
| `KEYSTORE_BASE64` | Todo el contenido de `keystore-base64.txt` |
| `KEYSTORE_PASSWORD` | La contraseña que aparece en `LEEME-SECRETOS.txt` |
| `KEY_ALIAS` | `calmaceleste` |
| `KEY_PASSWORD` | La misma contraseña |

**Opción B (en tu computadora, con Java):** `bash tools/crear-llave.sh` y haz lo mismo con los 4 secretos.

> 🔐 La llave nunca se sube al repositorio (el `.gitignore` bloquea `*.jks`). Si la pierdes no podrás actualizar la app con la misma firma; por eso la copia de seguridad.

## Paso 4 · Crear el AAB firmado
```bash
git tag v1.0.0
git push origin v1.0.0
```
El flujo **Publicar versión** compila y deja:
- `CalmaCeleste-1.0.0.aab` ← el archivo que sube a Play Store
- `CalmaCeleste-1.0.0.apk` ← para instalar y probar
- `mapping-1.0.0.txt` ← ayuda a leer errores de la versión publicada

Los encuentras en **Releases** (columna derecha del repositorio) y en *Actions → la ejecución → Artifacts*.

## Paso 5 · Crear tu cuenta y la app en Google Play Console
1. play.google.com/console → pago único de 25 USD y verificación de identidad.
2. **Crear app**: nombre «Calma Celeste: ansiedad», idioma español (Latinoamérica), tipo **App**, **Gratis**, acepta las declaraciones.
3. El identificador de la app es `com.calmaceleste.app`. **Si ya está tomado por otra app**, cámbialo antes de publicar por primera vez en `app/build.gradle.kts` (`applicationId`) y en `.github/workflows/release.yml` (`packageName`). Después de publicar no se puede cambiar.
4. Completa los formularios de **Contenido de la app** con las respuestas de `play-store/FICHA-Y-FORMULARIOS.md` (anuncios, público, clasificación, apps de salud, seguridad de datos).
5. Sube la ficha: textos de `play-store/listing/es-419/`, icono y gráfico de `play-store/graphics/` y **tus capturas de pantalla reales** (mínimo 2).

## Paso 6 · Política de privacidad (URL pública obligatoria)
Google exige un enlace público. Lo más fácil:
1. Crea un repositorio **público** aparte llamado `calmaceleste-privacidad`.
2. Sube `docs/PRIVACIDAD.md` como `index.md` y **antes** escribe tu correo donde dice `[TU CORREO]`.
3. **Settings → Pages → Deploy from a branch → main** → copia la URL que te da y pégala en Play Console.

(Con un repositorio privado, GitHub Pages no funciona en el plan gratuito; por eso el repositorio aparte. También sirve una página de Google Sites o Notion pública.)

## Paso 7 · Primer lanzamiento (prueba cerrada)
Si tu cuenta es **personal y se creó después del 13 de noviembre de 2023**, Google exige:
1. Crear una **prueba cerrada** y subir ahí el AAB.
2. Invitar a un mínimo de **12 testers** (el número lo confirma tu Play Console) con cuenta de Google. Deben aceptar la invitación y **mantenerse inscritos 14 días seguidos**.
3. Pasados los 14 días: **Solicitar acceso a producción** y responder el cuestionario con honestidad (cómo probaste, qué feedback recibiste).
4. Google revisa (normalmente unos días) y ya puedes publicar en producción.

Las cuentas de **organización** no tienen este requisito. Consejo: invita a 15 o más personas, porque algunas se salen.

La primera subida del AAB se hace **a mano** en Play Console (Pruebas → Prueba cerrada → Crear versión → subir `.aab`). Es lo normal: Google registra el identificador de la app en esa primera subida.

## Paso 8 · Automatizar las siguientes versiones (opcional, pero cómodo)
Con esto, cada `git tag v1.x.x` sube el AAB solo a Play.
1. En **Google Cloud Console** crea un proyecto → **IAM y administración → Cuentas de servicio → Crear**. Crea una **clave JSON** y descárgala.
2. En **Play Console → Usuarios y permisos → Invitar usuarios** invita el correo de esa cuenta de servicio con permiso para **administrar versiones**.
3. En GitHub crea el secreto `PLAY_SERVICE_ACCOUNT_JSON` con **todo el contenido** del JSON.
4. (Opcional) En **Settings → Secrets and variables → Actions → Variables** puedes crear:
   - `PLAY_TRACK`: `internal` (por defecto), `alpha` (prueba cerrada), `beta` o `production`.
   - `PLAY_RELEASE_STATUS`: `draft` (por defecto: queda como borrador y tú pulsas «Enviar») o `completed` (se publica solo).
   - `VERSION_CODE_OFFSET`: solo si ya subiste versiones a mano y necesitas que el número suba más.

Los textos de «Novedades» que verán los usuarios están en `play-store/whatsnew/whatsnew-es-419` (máx. 500 caracteres): edítalo antes de cada versión.

## Lista final antes de publicar
- [ ] Probé el APK en mi teléfono: botón «Me siento ansioso», respiración, canción, video, contacto, recordatorio, PIN.
- [ ] Revisé los números de ayuda urgente de mi país (`HelpData.kt`).
- [ ] Puse mi correo en la política de privacidad y en la ficha.
- [ ] Subí capturas reales.
- [ ] Copia de seguridad de la llave `.jks` y su contraseña.
