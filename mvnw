#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper (versão enxuta)
# Baixa a distribuição do Maven definida em .mvn/wrapper/maven-wrapper.properties
# para ~/.m2/wrapper/dists na primeira execução e repassa os argumentos ao mvn.
# Usa curl, wget ou, na falta dos dois, o próprio Java.
# ----------------------------------------------------------------------------
set -eu

BASEDIR=$(cd "$(dirname "$0")" && pwd)
PROPS="$BASEDIR/.mvn/wrapper/maven-wrapper.properties"

distributionUrl=$(sed -n 's/^distributionUrl=//p' "$PROPS" | tr -d '\r')
[ -n "$distributionUrl" ] || { echo "distributionUrl ausente em $PROPS" >&2; exit 1; }

distName=$(basename "$distributionUrl")
distDir=${distName%-bin.*}
MAVEN_USER_HOME=${MAVEN_USER_HOME:-"$HOME/.m2"}
MAVEN_HOME="$MAVEN_USER_HOME/wrapper/dists/$distDir"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  echo "Baixando $distributionUrl" >&2
  tmp=$(mktemp -d)
  trap 'rm -rf "$tmp"' EXIT
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$tmp/$distName" "$distributionUrl"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$tmp/$distName" "$distributionUrl"
  else
    JAVA_CMD=${JAVA_HOME:+"$JAVA_HOME/bin/java"}
    "${JAVA_CMD:-java}" "$BASEDIR/.mvn/wrapper/MavenWrapperDownloader.java" "$distributionUrl" "$tmp/$distName"
  fi
  case "$distName" in
    *.zip) (cd "$tmp" && unzip -q "$distName") ;;
    *)     tar -xzf "$tmp/$distName" -C "$tmp" ;;
  esac
  mkdir -p "$(dirname "$MAVEN_HOME")"
  if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
    mv "$tmp/$distDir" "$MAVEN_HOME"
  fi
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
