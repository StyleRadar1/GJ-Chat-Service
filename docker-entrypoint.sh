#!/bin/sh
set -eu

if [ "${SPRING_PROFILES_ACTIVE:-}" = "qa" ]; then
    : "${RABBITMQ_TRUSTSTORE_BASE64:?RABBITMQ_TRUSTSTORE_BASE64 es requerida para QA}"
    printf '%s' "$RABBITMQ_TRUSTSTORE_BASE64" | base64 -d \
        > "${RABBITMQ_TRUSTSTORE_PATH:-/tmp/rabbitmq-truststore.jks}"
fi

exec java -jar /app/app.jar
