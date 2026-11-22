# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Kronometer is a timing system for amateur bike races (Slovenian hill climbs such as Knej Karlovica). It has four parts in one repo:

- **Django backend** (repo root) — admin, public results pages, JSON API, OAuth provider. Deployed at `kronometer.staric.net`.
- **`ios/`** — SwiftUI app used at the start and finish lines. This is the active client.
- **`ArduinoSensor/`, `DummyArduinoSensor/`** — finish-line sensors that talk to the iOS app over BLE (`BLESensor.ino` is the current one; the dummy sensor is for testing).
- **`android/`** — legacy app, superseded by the iOS app. `pcb/` holds the KiCad sensor board.

## Backend commands

Python 3.13, dependencies via Poetry. Default DB is `db.sqlite3` (gitignored); `DATABASE_URL` overrides it. Debug mode (debug toolbar, django-sass, `/static/`) only turns on with `DEBUG=True`.

```bash
poetry install
DEBUG=True poetry run python manage.py runserver
poetry run python manage.py migrate
poetry run python manage.py test                      # all tests
poetry run python manage.py test biker.tests.SomeTest # single test
poetry run python manage.py import_results data/<legacy-biker-dump>.json  # legacy biker.* fixtures -> competition.*
poetry run python manage.py loaddata data/competition-2024.json            # fixtures already in competition.* form
```

Lint and type checks run through pre-commit (ruff, ruff-format, mypy in `strict` mode, `makemigrations --check`, `swift format -i` on Swift files). Run `pre-commit run --all-files` to check everything; a missing migration fails the commit.

## iOS commands

```bash
cd ios
xcodebuild test -project Kronometer.xcodeproj -scheme Kronometer \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro'
# single test: add  -only-testing:KronometerTests/AuthServiceTests
```

`ios/Kronometer/Secrets.xcconfig` supplies `CLIENT_ID` (the OAuth client id) to `Info.plist`. There is no client secret.

## Architecture

### Two overlapping Django apps: `biker` vs `competition`

`biker` is the original app (flat `Competition`/`Category`/`Biker` models, `result_section_1/2` text fields). `competition` is the newer, normalised replacement (`Competition` → `ResultTemplate` → `ResultSection`, `Biker` separate from `CompetitionBiker` which holds number, category and start/end times, plus `CompetitionChangeLog`). Both are in `INSTALLED_APPS`, and `kronometer/urls.py` still routes `biker/*` and `<int:competition_id>` to the old views. New work should go in `competition`. The two apps have separate models with the same names, so check the import.

### API and auth

- `competition/api_views.py` serves `/api/competition/…` for the iOS app. Views extend django-oauth-toolkit's `ProtectedResourceView` and POST handlers are `csrf_exempt`. Time values are POSTed as epoch milliseconds, and returned as `datetime.isoformat()` strings.
- `isoformat()` drops the fractional part when microseconds are zero, so clients must accept ISO 8601 dates both with and without fractional seconds (the iOS decoder does).
- OAuth endpoints are mounted at `/oauth/`. The iOS app is a **public** client using Authorization Code + PKCE (S256, custom scheme `x-kronometer-app://auth`).

### iOS app

`ApiManager` wraps `AuthService` (PKCE login, token refresh, Keychain storage) and `ApiService` (requests and decoding). `BikerStore` is the local source of truth (autosaved to disk); start and finish times are queued in the `UpdateManager` actor, which persists them to `updates.json` and retries sending, so timing keeps working offline. `SensorController` handles the BLE connection to the Arduino sensors. Views are grouped by feature under `Features/Start`, `Finish` and `Settings`.

### Data and deployment

- `data/*.json` are Django fixtures of past results, in two formats. Older files (e.g. `knej-karlovica-2013.json`) are dumps of the legacy `biker.*` models. `import_results` converts them into `competition.*` rows, mapping the many historical category spellings onto canonical `Category` names (`CATEGORY_MAP`). The canonical categories must already exist in the DB, otherwise the import fails. Newer files (`competition-2024.json`, the 2025 and 2026 dumps) already use `competition.*` models. They contain real participants' names and birth years.
- Deployment: a push to `main` triggers `.github/workflows/deploy.yml`, which SSHes into cPanel, pulls and runs `deploy.sh` (rsync, `migrate`, `collectstatic`, touch `tmp/restart.txt` for Passenger). `deploy.sh` excludes `android/`, `ios/`, `pcb/` and the Arduino folders. Production static files are served from `static.staric.net` (`STATIC_URL`).
- `README.md` is empty.
