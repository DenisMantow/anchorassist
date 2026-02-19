#!/bin/sh

DIR="$(cd "$(dirname "$0")" && pwd)"

JAVA_CMD=java
WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"

exec "$JAVA_CMD" \
  -classpath "$WRAPPER_JAR" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
