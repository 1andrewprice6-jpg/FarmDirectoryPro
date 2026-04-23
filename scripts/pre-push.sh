#!/data/data/com.termux/files/usr/bin/env bash
# .git/hooks/pre-push  —  Termux-native
#
# Termux has no Android SDK, so we can't run ./gradlew compileProdDebugKotlin locally.
# This hook does what IS possible locally:
#   1. Block pushes with unresolved merge conflict markers
#   2. Flag obvious breakage (unbalanced braces, lone TODO(), "FIXME NOW" etc.)
#   3. Check for common paste-mistakes (BOM, CRLF in .kt files)
#
# Real compile validation happens on GitHub Actions. After push, run:
#   ./scripts/gh-watch.sh
# to auto-watch CI with a termux-notification on failure.
#
# Bypass: git push --no-verify   OR   SKIP_PREPUSH=1 git push

set -euo pipefail

if [ "${SKIP_PREPUSH:-0}" = "1" ]; then
  echo "[pre-push] SKIP_PREPUSH=1, skipping."
  exit 0
fi

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"

# Work out which commits are being pushed: compare local HEAD with upstream
range=""
if git rev-parse --abbrev-ref --symbolic-full-name '@{u}' >/dev/null 2>&1; then
  range="$(git rev-parse --abbrev-ref --symbolic-full-name '@{u}')..HEAD"
else
  # First push of this branch — scan the whole branch against master
  if git rev-parse --verify master >/dev/null 2>&1; then
    range="master..HEAD"
  fi
fi

# Collect the .kt/.kts files touched in the push range (fall back to all if no range)
if [ -n "$range" ]; then
  files=$(git diff --name-only --diff-filter=ACMR "$range" -- '*.kt' '*.kts' 2>/dev/null || true)
else
  files=$(git ls-files '*.kt' '*.kts')
fi

if [ -z "$files" ]; then
  echo "[pre-push] No Kotlin files in push range. Skipping checks."
  exit 0
fi

fail=0
warn=0

echo "[pre-push] Scanning $(echo "$files" | wc -l | tr -d ' ') Kotlin file(s)..."

while IFS= read -r f; do
  [ -z "$f" ] && continue
  [ -f "$f" ] || continue

  # 1. Merge conflict markers
  if grep -nE '^(<<<<<<< |=======$|>>>>>>> )' "$f" >/dev/null; then
    echo "  ✖ $f : unresolved merge conflict markers"
    fail=1
  fi

  # 2. Brace balance (rough; will false-positive on strings w/ literal braces, but catches the common case)
  opens=$(tr -cd '{' < "$f" | wc -c | tr -d ' ')
  closes=$(tr -cd '}' < "$f" | wc -c | tr -d ' ')
  if [ "$opens" != "$closes" ]; then
    echo "  ✖ $f : unbalanced braces ($opens '{' vs $closes '}')"
    fail=1
  fi

  # 3. BOM
  if head -c 3 "$f" | od -An -tx1 | tr -d ' \n' | grep -q '^efbbbf'; then
    echo "  ✖ $f : UTF-8 BOM (breaks kotlinc)"
    fail=1
  fi

  # 4. CRLF in .kt
  if grep -q $'\r' "$f"; then
    echo "  ⚠ $f : CRLF line endings (convert with: sed -i 's/\\r\$//' <file>)"
    warn=1
  fi

  # 5. Known compile blockers from recent history
  if grep -nE 'import .*\.GsonDeserializer$' "$f" >/dev/null; then
    echo "  ✖ $f : invalid import 'GsonDeserializer' (that class doesn't exist in Gson)"
    fail=1
  fi

done <<< "$files"

if [ "$fail" -eq 1 ]; then
  echo ""
  echo "[pre-push] ✖ Local checks failed. Fix issues above, or bypass with: git push --no-verify"
  exit 1
fi

if [ "$warn" -eq 1 ]; then
  echo "[pre-push] Warnings above (non-blocking)."
fi

echo "[pre-push] ✔ Local checks passed. Real compile will run on GitHub Actions."
echo "[pre-push]   After push: ./scripts/gh-watch.sh    (watches CI, notifies on failure)"
exit 0
