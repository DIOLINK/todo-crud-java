#!/usr/bin/env bash
set -e

# Colores para output
GREEN='\033[0;32m'
NC='\033[0m'

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_OUT="$BASE_DIR/backend/out"
FRONTEND_OUT="$BASE_DIR/frontend/out"
SHARED_OUT="$BASE_DIR/shared/out"
MONGO_JAR="$BASE_DIR/lib/mongo-java-driver-3.12.14.jar"
JAVAFX_LIB="$BASE_DIR/lib/javafx-sdk-17.0.17/lib"
JAVAFX_JARS=$(find "$JAVAFX_LIB" -name '*.jar' | tr '\n' ':')

# 1. Build si no existen los .class
if [ ! -d "$BACKEND_OUT" ] || [ ! -d "$FRONTEND_OUT" ] || [ ! -d "$SHARED_OUT" ]; then
    echo -e "${GREEN}==> Compilando por primera vez${NC}"
    chmod +x "$BASE_DIR/build.sh"
    "$BASE_DIR/build.sh"
fi

# 2. Limpiar posibles instancias anteriores (puerto 8080)
lsof -ti:8080 | xargs -r kill -9 2>/dev/null || true

# 3. Lanzar backend en background
echo "${GREEN}==> Iniciando backend HTTP en puerto 8080${NC}"
java -cp "$MONGO_JAR:$BACKEND_OUT:$SHARED_OUT" com.todo.server.HttpServer &
BACKEND_PID=$!

# 4. Esperar a que el servidor esté arriba
sleep 2

# 5. Lanzar frontend JavaFX (bloqueante, GUI)
echo "${GREEN}==> Iniciando cliente JavaFX${NC}"
java \
    -Dprism.order=sw \
    -Dprism.verbose=true \
    -Djava.library.path="$JAVAFX_LIB" \
    --module-path "$JAVAFX_JARS" \
    --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.swing,javafx.web \
    -cp "$MONGO_JAR:$FRONTEND_OUT:$SHARED_OUT" \
    com.todo.ui.Main

# 6. Cuando el usuario cierre la GUI, matamos el backend
echo -e "${GREEN}==> Cerrando backend${NC}"
kill -9 $BACKEND_PID 2>/dev/null || true