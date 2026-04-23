#!/data/data/com.termux/files/usr/bin/env bash
# scripts/install-termux.sh
# One-shot installer: deps + git hooks, Termux-specific.
#
# Usage (from repo root):
#   bash scripts/install-termux.sh

set -euo pipefail

echo "==> FarmDirectoryPro hooks — Termux installer"

# 1. Deps
echo ""
echo "==> Installing packages (git, gh, openjdk-17, termux-api)..."
pkg update -y >/dev/null 2>&1 || true
pkg install -y git gh termux-api

# 2. Auth check
if ! gh auth status >/dev/null 2>&1; then
  echo ""
  echo "==> gh not authenticated. Run this after install finishes:"
  echo "    gh auth login"
fi

# 3. Repo root
REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || true)"
if [ -z "$REPO_ROOT" ]; then
  echo "Error: run this from inside the FarmDirectoryPro repo." >&2
  exit 1
fi
cd "$REPO_ROOT"

# 4. Install pre-push hook (symlink so updates to scripts/pre-push.sh auto-apply)
HOOK_SRC="$REPO_ROOT/scripts/pre-push.sh"
HOOK_DST="$REPO_ROOT/.git/hooks/pre-push"

if [ ! -f "$HOOK_SRC" ]; then
  echo "Error: $HOOK_SRC not found." >&2
  exit 1
fi

mkdir -p "$(dirname "$HOOK_DST")"
chmod +x "$HOOK_SRC" "$REPO_ROOT/scripts/gh-watch.sh"

# Symlink is cleaner than copy — edits to the repo hook propagate immediately
ln -sf "$HOOK_SRC" "$HOOK_DST"

echo ""
echo "==> Installed:"
echo "     pre-push hook  -> $HOOK_DST (symlink to scripts/pre-push.sh)"
echo "     ci watcher     -> scripts/gh-watch.sh"
echo ""
echo "Flow:"
echo "   git push                  # local checks run, blocks on obvious breakage"
echo "   ./scripts/gh-watch.sh     # watch CI, termux-notification on finish"
echo ""
echo "Bypass hook (emergency):   git push --no-verify"
