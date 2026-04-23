#!/data/data/com.termux/files/usr/bin/env bash
# scripts/gh-watch.sh
#
# Watch the most recent GitHub Actions run for HEAD, and fire a termux-notification
# when it finishes. Usage (after `git push`):
#
#   ./scripts/gh-watch.sh
#
# Optional flags:
#   -w <workflow>   only watch a specific workflow (default: simple-build.yml)

set -euo pipefail

WORKFLOW="simple-build.yml"
while getopts "w:" opt; do
  case "$opt" in
    w) WORKFLOW="$OPTARG" ;;
    *) echo "Usage: $0 [-w workflow.yml]" >&2; exit 2 ;;
  esac
done

command -v gh >/dev/null 2>&1 || { echo "gh CLI not installed. Run scripts/install-termux.sh first."; exit 1; }

SHA="$(git rev-parse HEAD)"
echo "Watching workflow '$WORKFLOW' for $SHA ..."

# Give GH a moment to register the run after the push
for i in 1 2 3 4 5 6; do
  RUN_ID="$(gh run list --workflow "$WORKFLOW" --limit 1 --json databaseId,headSha \
            --jq ".[] | select(.headSha==\"$SHA\") | .databaseId" 2>/dev/null || true)"
  [ -n "${RUN_ID:-}" ] && break
  sleep 5
done

if [ -z "${RUN_ID:-}" ]; then
  echo "No run found for $SHA on $WORKFLOW after 30s. Check: gh run list --workflow $WORKFLOW"
  exit 1
fi

echo "Run: https://github.com/$(gh repo view --json nameWithOwner -q .nameWithOwner)/actions/runs/$RUN_ID"

# --exit-status makes `gh run watch` return non-zero on failure
if gh run watch "$RUN_ID" --exit-status; then
  STATUS="success"
  ICON="✔"
else
  STATUS="failed"
  ICON="✖"
fi

MSG="FarmDirectoryPro CI $ICON $STATUS  (run #$RUN_ID)"
echo "$MSG"

# Termux notification if termux-api is installed
if command -v termux-notification >/dev/null 2>&1; then
  termux-notification \
    --title "FarmDirectoryPro CI $ICON" \
    --content "$STATUS — run #$RUN_ID" \
    --action "gh run view $RUN_ID --web"
fi

[ "$STATUS" = "success" ]
