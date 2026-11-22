#!/bin/bash
# Copies the checked-out repo to the app directory, migrates, collects static, restarts Passenger.
set -euo pipefail
cd "$(dirname "$0")"

DEPLOYPATH=/home/naring85/kronometer.staric.net
PYTHON=/home/naring85/virtualenv/kronometer.staric.net/3.13/bin/python

/bin/rsync -rlt --no-perms \
  --exclude='.git' --exclude='.venv' --exclude='.idea' --exclude='*_cache' \
  --exclude='db.sqlite3' --exclude='android' --exclude='ios' --exclude='pcb' \
  --exclude='ArduinoSensor' --exclude='DummyArduinoSensor' \
  ./ "$DEPLOYPATH/"

cd "$DEPLOYPATH"
$PYTHON manage.py migrate --noinput
$PYTHON manage.py collectstatic --noinput
mkdir -p tmp && touch tmp/restart.txt
