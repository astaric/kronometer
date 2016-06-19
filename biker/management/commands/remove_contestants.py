from django.core.management import BaseCommand
from biker.models import Biker


class Command(BaseCommand):
    def handle(self, *args, **options):
        Biker.objects.all().delete()
