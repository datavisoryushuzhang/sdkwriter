#!/bin/sh

echo "The application is starting... "
exec java ${JAVA_OPTS} -noverify -XX:+AlwaysPreTouch -Djava.security.egd=file:/dev/./urandom -jar app.jar  "$@"