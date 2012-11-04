#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"
TEST_DIR=$SCRIPT_DIR/src/test

$ANDROID_HOME/platform-tools/aapt \
                        package \
                        -v \
                        -f \
                        -m \
                        -S $TEST_DIR/resources/res \
                        -J $TEST_DIR/java \
                        -M $TEST_DIR/resources/AndroidManifest.xml \
                        -I $ANDROID_HOME/platforms/android-10/android.jar
