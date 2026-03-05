#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Script for Unix/macOS
# Apache Maven Wrapper 3.3.2
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ] ; then
  if [ -f /usr/local/etc/mavenrc ] ; then . /usr/local/etc/mavenrc ; fi
  if [ -f /etc/mavenrc ] ; then . /etc/mavenrc ; fi
  if [ -f "$HOME/.mavenrc" ] ; then . "$HOME/.mavenrc" ; fi
fi

cygwin=false
darwin=false
mingw=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  MINGW*)  mingw=true  ;;
  Darwin*) darwin=true ;;
esac

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-}"

find_maven_basedir() {
  if [ -z "$1" ]; then echo "Path not specified to find_maven_basedir"; return 1; fi
  basedir="$1"
  wdir="$1"
  while [ "$wdir" != '/' ] ; do
    if [ -d "$wdir"/.mvn ] ; then basedir=$wdir; break; fi
    wdir=$(cd "$wdir/.."; pwd)
  done
  printf '%s' "$(cd "$basedir"; pwd)"
}

BASE_DIR=$(find_maven_basedir "$(dirname "$0")")
if [ -z "$BASE_DIR" ]; then exit 1; fi

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$BASE_DIR}"
MAVEN_OPTS="$(cat "$MAVEN_PROJECTBASEDIR/.mvn/jvm.config" 2>/dev/null | tr '\n' ' ') $MAVEN_OPTS"

WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

if [ ! -r "$WRAPPER_JAR" ]; then
  JAR_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
  mkdir -p "$MAVEN_PROJECTBASEDIR/.mvn/wrapper"
  if command -v curl > /dev/null; then
    curl -sL "$JAR_URL" -o "$WRAPPER_JAR"
  elif command -v wget > /dev/null; then
    wget -q "$JAR_URL" -O "$WRAPPER_JAR"
  fi
fi

JAVA_CMD="${JAVA_HOME:+$JAVA_HOME/bin/}java"

exec "$JAVA_CMD" $MAVEN_OPTS \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  $WRAPPER_LAUNCHER "$@"
