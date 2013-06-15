"""
This file demonstrates writing tests using the unittest module. These will pass
when you run "manage.py test".

Replace this with more appropriate tests for your application.
"""
from django.core.urlresolvers import reverse
from django.test import TestCase
from django.utils.timezone import datetime, make_aware, utc

from kronometer.biker.models import Biker, Category


class BikerTest(TestCase):
    def test_create_biker_duration(self):
        b = Biker(name="John", surname="Doe")
        self.assertIsNone(b.duration)

        b.start_time = datetime.datetime.now()
        self.assertIsNone(b.duration)

        b.end_time = b.start_time + datetime.timedelta(minutes=1)
        self.assertEqual(b.duration, datetime.timedelta(minutes=1))


class ViewTests(TestCase):
    def test_set_start_time_from_java(self):
        Biker.objects.create(number=1, name="John", surname="Doe")

        response = self.client.post(
            reverse('set_start_time'),
            dict(number="1", start_time="1371076304067"))
        self.assertEqual(response.status_code, 200)
        biker = Biker.objects.get(number=1)
        self.assertEqual(biker.start_time,
                         make_aware(datetime(2013, 6, 12, 22, 31, 44, 67000), utc))

    def test_create_contestant(self):
        category = Category.objects.create(name="Dummy Category")
        self.client.post(reverse('biker_create'), dict(
            number="1",
            name="John",
            surname="Doe",
            category=str(category.id),
            domestic="Yes")
        )

        biker = Biker.objects.filter(number=1)[0]
        self.assertEqual(biker.name, "John")
        self.assertEqual(biker.surname, "Doe")
        self.assertEqual(biker.category, category)
        self.assertEqual(biker.domestic, True)
