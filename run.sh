#!/bin/sh
mkdir -p out
javac -d out $(find src -name '*.java') || exit 1
cp -r src/resources out/
java -cp out Main
