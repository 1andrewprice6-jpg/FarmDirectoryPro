# Pre-push hooks — FarmDirectoryPro (Termux)

## Why this is different from the Linux/Windows version

Termux has no Android SDK by default. `./gradlew compileProdDebugKotlin` fails at plugin resolution.
So we can't do the "full compile before push" gate locally.

Instead this bundle does:

| Where | What |
|-------|------|
| **Pre-push (local, Termux)** | Fast static checks: merge markers, brace balance, BOM, CRLF, known bad imports. |
| **Post-push (`gh run watch`)** | Real compile validation on GitHub's compute. Termux notification on finish. |

Total loop time from `git push` to knowing it compiled: usually < 5 min, hands-free.

## Install

From the repo root:

```bash
bash scripts/install-termux.sh
```

That:
1. `pkg install git gh openjdk-17 termux-api`
2. Symlinks `.git/hooks/pre-push` → `scripts/pre-push.sh` (so future edits auto-apply)
3. `chmod +x` on `scripts/gh-watch.sh`

First time only: `gh auth login` (device-flow works fine in Termux).

## Normal flow

```bash
git add -A
git commit -m "..."
git push                    # pre-push runs static checks locally
./scripts/gh-watch.sh       # watches CI, notifies when done
```

If `termux-api` is installed + the Termux:API app is on your phone, the watcher fires a
notification on success/failure. Tap it to open the run in browser.

## What the pre-push hook catches

Lightweight checks, no SDK needed:

- Unresolved merge-conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`)
- Unbalanced `{ }` (rough count, catches the common case)
- UTF-8 BOM in `.kt`/`.kts` files (breaks kotlinc)
- CRLF line endings (warning only)
- Known bad imports from recent history (e.g. `import *.GsonDeserializer`)

Things it **can't** catch (those need GH Actions):
- Unresolved references to Android classes
- Missing imports
- Type errors
- ktlint style violations requiring full classpath

## Bypass

```bash
git push --no-verify
# or
SKIP_PREPUSH=1 git push
```

## Adding your own local checks

`scripts/pre-push.sh` is a plain bash script and is symlinked into `.git/hooks/`.
Add your own grep patterns into the per-file loop — next push picks them up.

## Uninstall

```bash
rm .git/hooks/pre-push
```
