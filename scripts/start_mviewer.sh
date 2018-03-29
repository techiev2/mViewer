#!/bin/bash

FILE="$BASH_SOURCE"
if [[ "$FILE" == "" ]]; then
    FILE="$0"
fi

SCRIPT=$(readlink -f $FILE)
SCRIPT_DIR=$(dirname $SCRIPT)

PORT=$(echo "${1:-$WINSTONE_PORT}" | grep -E ^\-?[0-9]+$)
PROPERTIES="$SCRIPT_DIR/mViewer.properties"
WINSTONE_JAR="$SCRIPT_DIR/winstone-0.9.10.jar"
WAR_FILE="$SCRIPT_DIR/mViewer.war"

if [[ "$PORT" == "" ]]; then
    echo "Non-numeric port provided. Falling back to 8080"
    PORT=8080
fi


if [[ -f "$PROPERTIES" ]]; then
    echo "Sourcing properties"
    . "$PROPERTIES"
fi

echo "Using Http Port : $PORT"
java -jar $WINSTONE_JAR --httpPort=$PORT --ajp13Port=-1 --warfile=$WAR_FILE
