"""
This file demonstrates writing tests using the unittest module. These will pass
when you run "manage.py test".

Replace this with more appropriate tests for your application.
"""
import datetime

from kronometer.biker.models import Biker
from django.test import TestCase


class BikerTest(TestCase):
    def test_create_biker_duration(self):
        b = Biker(name="John", surname="Doe")
        self.assertIsNone(b.duration)

        b.start_time = datetime.datetime.now()
        self.assertIsNone(b.duration)

        b.end_time = b.start_time + datetime.timedelta(minutes=1)
        self.assertEqual(b.duration, datetime.timedelta(minutes=1))
