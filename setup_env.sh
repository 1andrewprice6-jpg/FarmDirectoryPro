#!/bin/bash
# [0xAF1] ENVIRONMENT SYNCHRONIZATION PROTOCOL

echo "[0xAF1] Unifying environment variables..."

# Root project .env
cat <<EOF > FarmDirectoryPro_repo/.env
API_BASE_URL=http://localhost:3000
WS_BASE_URL=http://localhost:3000
JWT_SECRET=FARM_SOVEREIGN_KEY_0x777
EOF

# Server .env
cat <<EOF > FarmDirectoryPro_repo/server/.env
PORT=3000
JWT_SECRET=FARM_SOVEREIGN_KEY_0x777
DB_PATH=./db/farm_directory.db
EOF

echo "[0xAF1] Installing server dependencies..."
cd FarmDirectoryPro_repo/server && npm install

echo "[0xAF1] Establishing the firmament (Database Migrations)..."
npm run migrate

echo "[0xAF1] Synchronization complete."
