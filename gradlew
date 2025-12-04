#!/usr/bin/env bash
set -x

##############################################################################
##
##  Gradle wrapper script
##
##############################################################################

# Determine the Java command to use to launch the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java" 
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME\n\nPlease set the JAVA_HOME environment variable in your shell to the correct location of your Java Development Kit (JDK)."
    fi
else
    JAVACMD="java"
    which java >/dev/null || die "ERROR: JAVA_HOME is not set and no 'java' command can be found in your PATH.\n\nPlease set the JAVA_HOME environment variable in your shell to the correct location of your Java Development Kit (JDK) or install Java."
fi

# Determine the script directory.
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Use the maximum available file descriptors.
ulimit -n 4096

# For Darwin, add options to allow the Java VM to be a UI element.
case "`uname`" in
  Darwin)
    DEFAULT_JVM_OPTS="-Xdock:name=$APP_NAME -Xdock:icon=$SCRIPT_DIR/media/gradle.icns $DEFAULT_JVM_OPTS"
    ;;
esac

# For Cygwin or MSYS, convert path to Windows format.
case "`uname`" in
  CYGWIN*|MSYS*) 
    SCRIPT_DIR=`cygpath --path --windows "$SCRIPT_DIR"`
    ;;
esac

# Execute Gradle.
exec "$JAVACMD" \
  $DEFAULT_JVM_OPTS \
  $JAVA_OPTS \
  $GRADLE_OPTS \
  -Dorg.gradle.appname="$APP_BASE_NAME" \
  -classpath "$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
