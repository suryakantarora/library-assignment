#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
STATIC_DIR="$ROOT/backend/src/main/resources/static"

echo "Building frontend..."
cd "$ROOT/frontend"
npm ci
npm run build -- --configuration production

rm -rf "$STATIC_DIR"
mkdir -p "$STATIC_DIR"
cp -R dist/frontend/browser/. "$STATIC_DIR"/

echo "Building backend..."
cd "$ROOT/backend"
./mvnw -q -DskipTests clean package

echo "Starting Library Management on http://localhost:${PORT:-9999}"
exec java -jar target/library.jar --server.port="${PORT:-9999}"
