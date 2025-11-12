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
javac --module-path "$BASE_DIR/frontend/lib/javafx-sdk-25.0.1/lib" 
\
      --add-modules javafx.controls \
      -cp "$MONGO_JAR:$SHARED_OUT" \
      -d "$FRONTEND_OUT" \
      "$BASE_DIR"/frontend/src/com/todo/ui/*.java 

echo "Build completo."