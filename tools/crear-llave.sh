#!/usr/bin/env bash
# Crea la llave de subida para Google Play en tu propia computadora (necesita Java instalado).
# Uso:  bash tools/crear-llave.sh
set -euo pipefail

if ! command -v keytool >/dev/null; then
  echo "No encuentro 'keytool'. Instala Java (JDK 17 o superior) o usa el flujo 'Crear llave de firma' en GitHub Actions."
  exit 1
fi

OUT="${1:-llave-privada}"
mkdir -p "$OUT"
PASS="$(openssl rand -base64 24 | tr -dc 'A-Za-z0-9' | head -c 24)"

keytool -genkeypair -v \
  -keystore "$OUT/calmaceleste-upload.jks" \
  -alias calmaceleste \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass "$PASS" -keypass "$PASS" \
  -dname "CN=Ansiedad Control, OU=App, O=Ansiedad Control, C=VE" >/dev/null 2>&1

base64 -w 0 "$OUT/calmaceleste-upload.jks" 2>/dev/null > "$OUT/keystore-base64.txt" \
  || base64 -i "$OUT/calmaceleste-upload.jks" | tr -d '\n' > "$OUT/keystore-base64.txt"

cat > "$OUT/LEEME-SECRETOS.txt" <<EOF
Crea estos 4 secretos en GitHub (Settings → Secrets and variables → Actions):

KEYSTORE_BASE64    = contenido de keystore-base64.txt (una sola línea)
KEYSTORE_PASSWORD  = $PASS
KEY_ALIAS          = calmaceleste
KEY_PASSWORD       = $PASS

GUARDA una copia del .jks y de la contraseña en un lugar seguro. NUNCA los subas al repositorio.
EOF

echo "Listo. Revisa la carpeta: $OUT"
echo "IMPORTANTE: no subas esa carpeta a GitHub (el .gitignore ya ignora *.jks)."
