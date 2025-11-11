#!/bin/bash
set -e

JAVAFX=frontend/lib/javafx-sdk-17/lib

echo "Compilando shared..."
mkdir -p shared/out
javac -d shared/out shared/src/com/todo/model/*.java

echo "Compilando backend..."
mkdir -p backend/out
javac -cp backend/lib/mongo-java-driver-3.12.14.jar:shared/out -d 
backend/out backend/src/com/todo/server/*.java

echo "Compilando frontend..."
mkdir -p frontend/out
javac --module-path $JAVAFX \
      --add-modules javafx.controls,javafx.fxml \
      -cp frontend/lib/mongo-java-driver-3.12.14.jar:shared/out \
      -d frontend/out \
      frontend/src/com/todo/ui/*.java

echo "Build completo."