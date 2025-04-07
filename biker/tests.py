"""
This file demonstrates writing tests using the unittest module. These will pass
when you run "manage.py test".

Replace this with more appropriate tests for your application.
"""

import json
from datetime import datetime, timedelta, timezone

from django.test import TestCase
from django.urls import reverse
from django.utils.timezone import make_aware

from biker.models import Biker, Category


class BikerTest(TestCase):
    def test_create_biker_duration(self) -> None:
        b = Biker(name="John", surname="Doe")
        self.assertIsNone(b.duration)

        b.start_time = datetime.now(tz=timezone.utc)
        self.assertIsNone(b.duration)

        b.end_time = b.start_time + timedelta(minutes=1)
        self.assertEqual(b.duration, timedelta(minutes=1))


class ViewTests(TestCase):
    def test_set_start_time_from_java(self) -> None:
        Biker.objects.create(number=1, name="John", surname="Doe")

        response = self.client.post(
            reverse("set_start_time"), dict(number="1", start_time="1371076304067")
        )
        self.assertEqual(response.status_code, 200)
        biker = Biker.objects.get(number=1)
        self.assertEqual(
            biker.start_time,
            make_aware(datetime(2013, 6, 12, 22, 31, 44, 67000), timezone.utc),
        )

    def test_create_contestant(self) -> None:
        category = Category.objects.create(name="Dummy Category")
        self.client.post(
            reverse("biker_create"),
            dict(
                number="1",
                name="John",
                surname="Doe",
                category=str(category.id),
                domestic="Yes",
            ),
        )

        biker = Biker.objects.filter(number=1)[0]
        self.assertEqual(biker.name, "John")
        self.assertEqual(biker.surname, "Doe")
        self.assertEqual(biker.category, category)
        self.assertEqual(biker.domestic, True)

    def test_create_contestant_handles_errors(self) -> None:
        response = self.client.post(
            reverse("biker_create"),
            dict(
                name="",
                surname="",
            ),
        )

        self.assertEqual(response.status_code, 500)
        jsonResponse = json.loads(response.content)
        self.assertEqual(
            jsonResponse, {"error": "NOT NULL constraint failed: biker_biker.number"}
        )

    def test_partial_results(self) -> None:
        category = Category.objects.create(name="Cat")
        Biker.objects.create(
            number=1, category=category, start_time=datetime.now(tz=timezone.utc)
        )
        Biker.objects.create(
            number=2,
            category=category,
            start_time=datetime.now(tz=timezone.utc),
            end_time=datetime.now(tz=timezone.utc),
        )

        self.client.post(reverse("results"))
