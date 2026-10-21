#!/bin/bash
# Double-click to launch Airoplane Ticket Service (macOS) — native Java desktop window.
cd "$(dirname "$0")"
PORT="${PORT:-8080}"
if [ -x "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home/bin/java" ]; then
  export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
  export PATH="$JAVA_HOME/bin:$PATH"
fi
# free the port if a previous run is still alive
lsof -ti tcp:$PORT | xargs kill -9 2>/dev/null
java -jar AiroplaneTicketService.jar --port $PORT --ui > skyjet.log 2>&1 &
sleep 2
echo "Airoplane Ticket Service is running at http://localhost:$PORT"
echo "Close this window to keep it running, or run: lsof -ti tcp:$PORT | xargs kill"
wait
