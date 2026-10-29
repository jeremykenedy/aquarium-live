# Continuous Integration

Four GitHub Actions workflows run on every push to `main` and every pull request.

| Workflow | File | What it does |
|----------|------|--------------|
| Tests | `.github/workflows/tests.yml` | Runs `./test.sh` on Java 17 and 21. Builds the APK with a throwaway key and fails if it requests any Android permission. |
| Code style | `.github/workflows/style.yml` | Runs `scripts/check-style.sh`: the plain-Java classes and tests compile with every warning as an error; no tabs, trailing spaces, Windows line endings, missing final newlines, em dashes or curly quotes in any file; no logging left in the app; `shellcheck` on every script. |
| Documentation | `.github/workflows/docs.yml` | Runs `scripts/check-docs.py`: every local link and anchor resolves, the README contents list every section, the banners are 800x200 and say there is no tracking, the README ends with the license line, and every action is pinned to a commit. |
| Security | `.github/workflows/security.yml` | Runs `scripts/check-security.sh`: the manifest requests no permissions, turns backup off, refuses cleartext traffic through the network security policy, exports only the launcher entry and lets only the system start the screensaver; the app reads no intent input and has no network code; `gitleaks` (checksum-verified) finds no secrets in the files or the git history. |

Every action is pinned to a full commit SHA, with the version in a comment.

GitGuardian, SonarCloud, Codacy, CodeFactor and Aikido integrations are deferred.
Their workflows, provider configuration and badges have been removed.
Independent builds, tests, style, documentation and security checks remain.

## Running the same checks locally

```bash
./test.sh                        # tests
./coverage.sh                    # tests with coverage, writes build/jacoco.xml
./build.sh                       # signed APK
bash scripts/check-style.sh      # code style
python3 scripts/check-docs.py    # documentation
bash scripts/check-security.sh   # manifest and secrets
```
