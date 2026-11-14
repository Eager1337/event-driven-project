#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
command -v java >/dev/null || { echo 'Install JDK 17 or newer first.'; exit 1; }
if [[ ! -f target/marketflow.jar && -f dist/marketflow.jar ]]; then
  exec java -jar dist/marketflow.jar
fi
if [[ ! -f target/marketflow.jar ]]; then
  command -v mvn >/dev/null || { echo 'Install Maven 3.9+ or download the built bundle from GitHub Actions.'; exit 1; }
  mvn -B clean package
fi
exec java -jar target/marketflow.jar