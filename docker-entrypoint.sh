#!/bin/sh
set -eu

if [ "${SPRING_PROFILES_ACTIVE:-}" = "qa" ]; then
    : "${RABBITMQ_CA_CERT_BASE64:?RABBITMQ_CA_CERT_BASE64 es requerida para QA}"
    printf '%s' "$RABBITMQ_CA_CERT_BASE64" | base64 -d > /tmp/rabbitmq-ca.pem
    keytool -importcert -trustcacerts -noprompt -storetype JKS \
        -keystore /tmp/rabbitmq-truststore.jks \
        -storepass "${RABBITMQ_TRUSTSTORE_PASSWORD:-changeit}" \
        -alias styleradar-rabbitmq-ca -file /tmp/rabbitmq-ca.pem
fi

exec java -jar /app/app.jar
