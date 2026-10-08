# Continuous Integration

Six GitHub Actions workflows run on every push to `main` and every pull request.

| Workflow | File | What it does |
|----------|------|--------------|
| Tests | `.github/workflows/tests.yml` | Runs `./test.sh` on Java 17 and 21. Builds the APK with a throwaway key and fails if it requests any Android permission. |
| Code style | `.github/workflows/style.yml` | Runs `scripts/check-style.sh`: the plain-Java classes and tests compile with every warning as an error; no tabs, trailing spaces, Windows line endings, missing final newlines, em dashes or curly quotes in any file; no logging left in the app; `shellcheck` on every script. |
| Documentation | `.github/workflows/docs.yml` | Runs `scripts/check-docs.py`: every local link and anchor resolves, the README contents list every section, the banners are 800x200 and say there is no tracking, the README ends with the license line, and every action is pinned to a commit. |
| Security | `.github/workflows/security.yml` | Runs `scripts/check-security.sh`: the manifest requests no permissions, turns backup off, refuses cleartext traffic through the network security policy, exports only the launcher entry and lets only the system start the screensaver; the app reads no intent input and has no network code; `gitleaks` (checksum-verified) finds no secrets in the files or the git history. |
| GitGuardian scan | `.github/workflows/gitguardian.yml` | Scans the pushed commits for secrets with `ggshield`. Also runs on pushes to other branches. |
| SonarQube Cloud Scan | `.github/workflows/sonarcloud.yml` | Builds, runs the tests under JaCoCo with `./coverage.sh`, and sends the code and coverage to SonarCloud. |

Every action is pinned to a full commit SHA, with the version in a comment.

[Scrutinizer](https://scrutinizer-ci.com/g/jeremykenedy/aquarium-live/) builds from `.scrutinizer.yml` once the repository is added there: Java analysis, and the tests in a Temurin 17 container.

## Secrets

| Secret | Used by | Without it |
|--------|---------|------------|
| `GITGUARDIAN_API_KEY` | GitGuardian scan | The scan is skipped with a notice in the run summary. |
| `SONAR_TOKEN` | SonarQube Cloud Scan | The tests still run, the SonarCloud upload is skipped with a notice, and the SonarCloud badges in the README show "Project not found". |

Add them under the repository's **Settings > Secrets and variables > Actions**, or from a terminal (each command prompts for the value):

```bash
gh secret set GITGUARDIAN_API_KEY --repo jeremykenedy/aquarium-live
gh secret set SONAR_TOKEN --repo jeremykenedy/aquarium-live
```

## SonarCloud project

`sonar-project.properties` sets the project key `jeremykenedy_aquarium-live` in the organization `jeremykenedy-12345`. The project has to exist in SonarCloud under that key before the scan can upload to it. The classes that need Android are excluded from the coverage measure there; see [Testing](TESTING.md#what-the-tests-do-not-cover).

## Running the same checks locally

```bash
./test.sh                        # tests
./coverage.sh                    # tests with coverage, writes build/jacoco.xml
./build.sh                       # signed APK
bash scripts/check-style.sh      # code style
python3 scripts/check-docs.py    # documentation
bash scripts/check-security.sh   # manifest and secrets
```
