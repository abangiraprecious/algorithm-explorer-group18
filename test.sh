#!/usr/bin/env sh
# Compile and run the self-test of every algorithm and data structure.
cd "$(dirname "$0")" || exit 1
rm -rf out
javac -encoding UTF-8 -d out $(find src -name "*.java") || exit 1
java -cp out explorer.SelfTest
