# Kronometer

Timing system for amateur bike races, built for hill climbs such as Knej Karlovica. Start and finish times are recorded on phones at the start and finish lines and synced to a web server that publishes the results.

## Components

- **Django backend** (repo root) — admin, public results pages, JSON API and OAuth provider. The `competition` app is the current one; `biker` is the original app, kept for old routes and data.
- **`ios/`** — SwiftUI app for the start and finish lines. It works offline and syncs times when a connection is available. It logs in with OAuth (Authorization Code with PKCE).
- **`ArduinoSensor/`, `DummyArduinoSensor/`** — finish-line sensors that signal the iOS app over Bluetooth LE. The dummy sensor is for testing.
- **`pcb/`** — KiCad project for the sensor board.
- **`android/`** — legacy Android app, superseded by the iOS app.
- **`data/`** — results of past races as Django fixtures.

## Running the backend

Requires Python 3.13 and [Poetry](https://python-poetry.org/).

```bash
poetry install
export SECRET=dev          # Django SECRET_KEY, required
export DEBUG=True          # optional: debug toolbar, local static files
poetry run python manage.py migrate
poetry run python manage.py runserver
poetry run python manage.py test
```

The database is `db.sqlite3` by default; set `DATABASE_URL` to use another one.

Linting and type checking run through [pre-commit](https://pre-commit.com/) (ruff, mypy, `swift format`).

## Running the iOS app

Open `ios/Kronometer.xcodeproj` in Xcode and run the `Kronometer` scheme. `ios/Kronometer/Secrets.xcconfig` holds the OAuth `CLIENT_ID` of a public OAuth application registered on your backend (client type *Public*, grant type *Authorization code*, redirect URI `x-kronometer-app://auth`). The server address is hardcoded as `baseUrl` in `AuthService.swift` and `ApiService.swift` (under `ios/Kronometer/Services/API/`); change both to point at your own server.

## License

Kronometer is free software, released under the [GNU General Public License v3.0](LICENSE) or, at your option, any later version.
