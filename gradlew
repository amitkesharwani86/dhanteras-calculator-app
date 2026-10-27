#!/bin/sh
# Newly.app provides a compatible Gradle installation in its cloud build environment.
# Use that installation directly so this project does not require gradle-wrapper.jar.
set -eu
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle is not installed on this system." >&2
exit 1
