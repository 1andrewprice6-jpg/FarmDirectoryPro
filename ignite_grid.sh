#!/bin/bash
# [0xAF1] UNIFIED GRID IGNITION SEQUENCE

echo "[0xAF1] Ignition started. Spinning up concurrent stacks..."

# Function to handle cleanup on exit
cleanup() {
  echo "[0xAF1] Terminating grid nodes..."
  kill $BACKEND_PID $DATABASE_WATCHER_PID
  exit
}

trap cleanup SIGINT SIGTERM

# 1. Start Backend Server
echo "[0xAF1] Node 01: Igniting Backend Server..."
cd FarmDirectoryPro_repo/server && npm run dev &
BACKEND_PID=$!

# 2. Database Watcher (Mock or simple log tail)
echo "[0xAF1] Node 02: Initializing Database Watcher..."
tail -f FarmDirectoryPro_repo/server/db/farm_directory.db &
DATABASE_WATCHER_PID=$!

# 3. Frontend Compiler (Android/Gradle status)
echo "[0xAF1] Node 03: Polling Frontend Compiler..."
cd FarmDirectoryPro_repo && ./gradlew tasks --quiet

echo "[0xAF1] UNIFIED GRID ACTIVE. MONITORING NODES..."
wait
