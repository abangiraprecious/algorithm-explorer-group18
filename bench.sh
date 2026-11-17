#!/usr/bin/env sh
# Compile and run the Module D timing experiment; writes results/module_d.csv.
cd "$(dirname "$0")" || exit 1
rm -rf out
javac -encoding UTF-8 -d out $(find src -name "*.java") || exit 1
java -cp out explorer.Benchmark "${1:-3}"
