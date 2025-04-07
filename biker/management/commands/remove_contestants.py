from typing import Any

from django.core.management import BaseCommand

from biker.models import Biker


class Command(BaseCommand):
    def handle(self, *args: Any, **options: Any) -> None:
        Biker.objects.all().delete()
