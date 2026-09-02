#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$ROOT_DIR"

echo "Checking required tools..."

for command in git java mvn; do
    if ! command -v "$command" >/dev/null 2>&1; then
        echo "Error: '$command' is not installed." >&2
        exit 1
    fi
done

echo
echo "Running Git checks..."

git diff --check
git diff --cached --check

if git grep -nE '^(<<<<<<< .+|=======|>>>>>>> .+)$'; then
    echo "Error: unresolved merge conflict markers found." >&2
    exit 1
fi

echo
echo "Running Maven verification..."

mvn verify

echo
echo "All checks passed."
