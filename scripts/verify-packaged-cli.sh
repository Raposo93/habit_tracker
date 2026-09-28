#!/usr/bin/env bash

set -Eeuo pipefail

jar_path="$1"
test_dir="$(mktemp -d)"
trap 'rm -rf -- "$test_dir"' EXIT
export DB_PATH="$test_dir/habits.db"

run_cli() {
    if ! timeout 20s java -jar "$jar_path" "$@" > "$test_dir/output" 2>&1; then
        cat "$test_dir/output" >&2
        echo "Packaged CLI failed: $*" >&2
        exit 1
    fi

    if grep -Eq 'Tomcat|Started HabitTrackerApplication' "$test_dir/output"; then
        cat "$test_dir/output" >&2
        echo "Packaged CLI started the web application: $*" >&2
        exit 1
    fi
}

run_cli --query-between-dates 2026-09-21 2026-09-27
grep -Fq -- '- Current range: 2026-09-21 to 2026-09-27' "$test_dir/output"
grep -Fq 'Summary:' "$test_dir/output"
test -f "$DB_PATH"

run_cli --query-last-week
grep -Fq -- '- Current range:' "$test_dir/output"
grep -Fq 'Summary:' "$test_dir/output"

run_cli --query-between-dates 2026-09-21
grep -Fq -- '--query-between-dates <start-date> <end-date>' "$test_dir/output"

run_cli --query-last-week extra
grep -Fq -- '--query-last-week' "$test_dir/output"

run_cli --query-between-dates invalid 2026-09-27
grep -Fq -- '--query-between-dates <start-date> <end-date>' "$test_dir/output"

if ! timeout 20s java -jar "$jar_path" --spring.main.web-application-type=none --server.port=9001 > "$test_dir/output" 2>&1; then
    cat "$test_dir/output" >&2
    echo "Packaged application failed to start with Spring arguments" >&2
    exit 1
fi
grep -Fq 'Started HabitTrackerApplication' "$test_dir/output"
