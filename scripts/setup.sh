#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
command -v docker >/dev/null || { echo 'Install Docker Desktop / Docker Engine with Compose first.'; exit 1; }
docker compose version >/dev/null
if [[ ! -f .env ]]; then
  command -v openssl >/dev/null || { echo 'Install openssl for secure local password generation.'; exit 1; }
  umask 077
  printf 'DB_PASSWORD=%s\nMYSQL_ROOT_PASSWORD=%s\n' "$(openssl rand -hex 24)" "$(openssl rand -hex 24)" > .env
fi
if [[ ! -f config.properties ]]; then
  umask 077
  password="$(sed -n 's/^DB_PASSWORD=//p' .env)"
  [[ -n "$password" ]] || { echo '.env must contain DB_PASSWORD.'; exit 1; }
  printf 'db.url=jdbc:mysql://127.0.0.1:3307/marketflow?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectionTimeZone=LOCAL&connectTimeout=5000&socketTimeout=15000\ndb.user=marketflow\ndb.password=%s\n' "$password" > config.properties
fi
docker compose up -d --wait --wait-timeout 240
echo 'Database ready. Run bash scripts/run.sh to build and open MarketFlow.'