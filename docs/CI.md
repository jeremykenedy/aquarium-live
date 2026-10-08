# Continuous Integration

Three GitHub Actions workflows run on every push to `main` and every pull request.

| Workflow | File | What it does |
|----------|------|--------------|
| Tests | `.github/workflows/tests.yml` | Runs `./test.sh` on Java 17 and 21. Builds the APK with a throwaway key and fails if it requests any Android permission. |
| GitGuardian scan | `.github/workflows/gitguardian.yml` | Scans the pushed commits for secrets with `ggshield`. Runs on every push to any branch and on pull requests. |
| SonarQube Cloud Scan | `.github/workflows/sonarcloud.yml` | Builds, runs the tests under JaCoCo with `./coverage.sh`, and sends the code and coverage to SonarCloud. |

Third-party actions that handle secrets (`ggshield-action`, `sonarqube-scan-action`) are pinned to a full commit SHA, with the version in a comment.

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

`sonar-project.properties` sets the project key `jeremykenedy_aquarium-live` in the organization `jeremykenedy-12345`. The project has to exist in SonarCloud under that key before the scan can upload to it.

## Running the same checks locally

```bash
./test.sh       # tests
./coverage.sh   # tests with coverage, writes build/jacoco.xml
./build.sh      # signed APK
```
