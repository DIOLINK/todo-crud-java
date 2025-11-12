#!/usr/bin/env bash
set -e

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LIB_DIR="$BASE_DIR/lib"
MONGO_JAR="$LIB_DIR/mongo-java-driver-3.12.14.jar"

BACKEND_OUT="$BASE_DIR/backend/out"
FRONTEND_OUT="$BASE_DIR/frontend/out"
SHARED_OUT="$BASE_DIR/shared/out"

# 1. Compilar shared
echo "Compilando shared..."
mkdir -p "$SHARED_OUT"
javac -d "$SHARED_OUT" "$BASE_DIR"/shared/src/com/todo/model/*.java

# 2. Compilar backend
echo "Compilando backend..."
mkdir -p "$BACKEND_OUT"
javac -cp "$MONGO_JAR:$SHARED_OUT" -d "$BACKEND_OUT" \
      "$BASE_DIR"/backend/src/com/todo/server/*.java


# 3. Compilar frontend
echo "Compilando frontend..."
mkdir -p "$FRONTEND_OUT"
find "$BASE_DIR/frontend/src" -name "*.java" > sources.txt
javac --module-path "lib/javafx-sdk-17.0.17/lib" \
      --add-modules javafx.controls \
      -Dprism.order=sw -Dprism.verbose=true
      -cp "$MONGO_JAR:$SHARED_OUT" \
      -d "$FRONTEND_OUT" \
      @sources.txt
rm sources.txt

echo "Build completo."