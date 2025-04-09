import argparse
import json
import os
from typing import Any

from django.core.management import BaseCommand
from django.template.defaultfilters import slugify

from competition.models import Biker, Category, Competition, CompetitionBiker

CATEGORY_MAP = {
    "Predšolski": ["Predšolski", "predšolski", "predšolski otroci"],
    "Učenci OŠ (1. triada)": [
        "Učenci OŠ - 1. triada",
        "učenci - 1. triada",
        "učenci OŠ (1. triada)",
    ],
    "Učenci OŠ (2. triada)": [
        "Učenci OŠ - 2. triada",
        "učenci - 2. triada",
        "učenci OŠ (2. triada)",
    ],
    "Učenci OŠ (3. triada)": [
        "Učenci OŠ - 3. triada",
        "učenci - 3. triada",
        "učenci OŠ (3. triada)",
    ],
    "Učenci OŠ": ["Učenci OŠ"],
    "Učenke OŠ (1. triada)": [
        "Učenke OŠ - 1. triada",
        "učenke - 1. triada",
        "učenke OŠ  (1. triada)",
    ],
    "Učenke OŠ (2. triada)": [
        "Učenke OŠ - 2. triada",
        "učenke - 2. triada",
        "učenke OŠ (2. triada)",
    ],
    "Učenke OŠ (3. triada)": [
        "Učenke OŠ - 3. triada",
        "učenke - 3. triada",
        "učenke OŠ (3. triada)",
    ],
    "Učenke OŠ": ["Učenke OŠ"],
    "Moški do 40": [
        "Moški do 40 let",
        "fantje do 40",
        "Fantje do 40 let",
        "Fantje do 40",
    ],
    "Moški 41 do 60": [
        "Moški od 40 do 60 let",
        "fantje 40 do 60",
        "fantje med 41 in 60",
        "Fantje od 40 do 60 let",
        "Moški od 41 do 60 let",
        "Fantje 41-60",
    ],
    "Moški nad 61": [
        "Moški 60 in več",
        "fantje nad 60",
        "fantje nad 61",
        "Fantje 60 in več",
        "Moški 61 in več",
        "Fantje nad 61",
    ],
    "Ženske do 35": [
        "Ženske do 35",
        "dekleta do 35 let",
        "dekleta do 35",
        "Dekleta do 35",
    ],
    "Ženske nad 36": [
        "Ženske nad 35",
        "dekleta nad 35 let",
        "dekleta nad 36",
        "Dekleta nad 35",
        "Dekleta nad 36",
    ],
    "Dvojice": [
        "Moške dvojice",
        "Dvojice",
        "dvojice",
        "Vožnja v paru",
    ],
    "Monocikel": ["monocikel"],
}


class Command(BaseCommand):
    def add_arguments(self, parser: argparse.ArgumentParser) -> None:
        parser.add_argument(
            "fns",
            nargs="+",
            help="File name to import results from",
        )

    def handle(self, *args: Any, **options: Any) -> None:
        name_to_category = {
            category.name: category for category in Category.objects.all()
        }
        category_name_map = {
            old_name: name_to_category[new_name]
            for new_name, old_names in CATEGORY_MAP.items()
            for old_name in old_names
        }

        for fn in options["fns"]:
            with open(fn, "r") as file:
                data = json.load(file)

            categories = {
                item["pk"]: category_name_map[item["fields"]["name"]]
                for item in data
                if item["model"] == "biker.category"
            }

            competitions = [
                item
                for item in data
                if item["model"] == "biker.competition"
                and item["fields"].get("active", True)
            ]

            basename = os.path.splitext(os.path.basename(fn))[0]
            slug = slugify(basename)
            if len(competitions) == 0:
                competition_name = basename
                competition_id = 1
            else:
                competition_name = competitions[0]["fields"]["title"]
                competition_id = competitions[0]["pk"]

            if Competition.objects.filter(name=competition_name).exists():
                continue

            competition = Competition.objects.create(
                name=competition_name, slug=slug, active=False, result_template_id=1
            )

            bikers = [
                item
                for item in data
                if item["model"] == "biker.biker"
                and item["fields"].get("competition", 1) == competition_id
            ]
            for biker in bikers:
                number = biker["fields"]["number"]
                name = biker["fields"]["name"]
                surname = biker["fields"]["surname"]
                category = categories[biker["fields"]["category"]]
                domestic = biker["fields"]["domestic"]
                start_time = biker["fields"]["start_time"]
                end_time = biker["fields"]["end_time"]

                biker = Biker.objects.filter(
                    name=name,
                    surname=surname,
                ).first()
                if biker is None:
                    biker = Biker.objects.create(
                        name=name, surname=surname, domestic=domestic
                    )

                CompetitionBiker.objects.create(
                    competition=competition,
                    number=number,
                    biker=biker,
                    category=category,
                    domestic=domestic,
                    start_time=start_time,
                    end_time=end_time,
                )
