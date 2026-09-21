#!/bin/bash
# Double-click to launch SkyJet (macOS). Starts server + opens browser.
cd "$(dirname "$0")"
PORT="${PORT:-8080}"
# free the port if a previous run is still alive
lsof -ti tcp:$PORT | xargs kill -9 2>/dev/null
java -jar SkyJet.jar --port $PORT > skyjet.log 2>&1 &
sleep 2
open "http://localhost:$PORT"
echo "SkyJet is running at http://localhost:$PORT"
echo "Close this window to keep it running, or run: kill %1  (or lsof -ti tcp:$PORT | xargs kill)"
wait
