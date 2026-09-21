#!/bin/bash
# Run without Maven (uses only the JDK). Works with Java 17+.
set -e
cd "$(dirname "$0")"
# Auto-pick a compatible JDK (17+): prefer JAVA_HOME, else Homebrew openjdk@17, else default java.
pick_java() {
  if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then return 0; fi
  for c in "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home" "/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"; do
    if [ -x "$c/bin/javac" ]; then export JAVA_HOME="$c"; export PATH="$JAVA_HOME/bin:$PATH"; return 0; fi
  done
}
pick_java
command -v javac >/dev/null || { echo "ERROR: No Java JDK found. Install JDK 17+: brew install openjdk@17"; exit 1; }
PORT="${PORT:-8080}"
echo "→ Compiling (javac)..."
rm -rf out
mkdir -p out
find src/main/java -name "*.java" > /tmp/skyjet_sources.txt
javac -encoding UTF-8 -d out @/tmp/skyjet_sources.txt
echo "→ Copying static UI..."
mkdir -p out/static
cp -r src/main/resources/static/* out/static/
echo "→ Starting on http://localhost:$PORT ..."
java -cp out com.airline.App --port "$PORT"
