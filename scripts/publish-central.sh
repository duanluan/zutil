#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MVN="${MVN:-/opt/maven-mvnd/mvn/bin/mvn}"

CENTRAL_USER="${CENTRAL_PORTAL_USERNAME:-${CENTRAL_USERNAME:-}}"
CENTRAL_PASS="${CENTRAL_PORTAL_PASSWORD:-${CENTRAL_PASSWORD:-}}"

if [[ -z "${CENTRAL_USER}" || -z "${CENTRAL_PASS}" ]]; then
  cat >&2 <<'EOF'
Central Portal token required.

Set CENTRAL_PORTAL_USERNAME and CENTRAL_PORTAL_PASSWORD,
or CENTRAL_USERNAME and CENTRAL_PASSWORD.
EOF
  exit 1
fi

TMP_SETTINGS="$(mktemp "${TMPDIR:-/tmp}/zutil-maven-settings.XXXXXX.xml")"
cleanup() {
  rm -f "${TMP_SETTINGS}"
}
trap cleanup EXIT

xml_escape() {
  local value="$1"
  value="${value//&/&amp;}"
  value="${value//</&lt;}"
  value="${value//>/&gt;}"
  value="${value//\"/&quot;}"
  value="${value//\'/&apos;}"
  printf '%s' "${value}"
}

cat > "${TMP_SETTINGS}" <<EOF
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">
  <servers>
    <server>
      <id>central</id>
      <username>$(xml_escape "${CENTRAL_USER}")</username>
      <password>$(xml_escape "${CENTRAL_PASS}")</password>
    </server>
  </servers>
</settings>
EOF

exec "${MVN}" --settings "${TMP_SETTINGS}" -f "${ROOT_DIR}/pom.xml" deploy "$@"
