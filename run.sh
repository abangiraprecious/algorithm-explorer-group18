#!/usr/bin/env sh
# Compile and start Algorithm Explorer (needs JDK 11 or newer).
cd "$(dirname "$0")" || exit 1
rm -rf out
javac -encoding UTF-8 -d out $(find src -name "*.java") || exit 1
java -cp out explorer.server.Main "${1:-8080}"
