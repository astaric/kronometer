from datetime import datetime
from django.db import models


class Category(models.Model):
    name = models.TextField()

    def __str__(self):
        return self.name


class Biker(models.Model):
    number = models.IntegerField(unique=True)

    name = models.TextField()
    surname = models.TextField()

    category = models.ForeignKey(Category, null=True)
    domestic = models.BooleanField()

    start_time = models.DateTimeField(null=True, blank=True)
    end_time = models.DateTimeField(null=True, blank=True)

    @property
    def duration(self):
        if self.start_time and self.end_time:
            return self.end_time - self.start_time
        else:
            return None

    @property
    def category_name(self):
        if self.category is not None:
            return self.category.name
        else:
            return "Nerazporejeni"

    def set_start_time(self, start_time):
        if isinstance(start_time, float):
            start_time = datetime.fromtimestamp(start_time)

        BikerChangeLog.objects.create(biker=self, start_time=start_time)

        self.start_time = start_time
        self.save()

    def set_end_time(self, end_time):
        if isinstance(end_time, float):
            end_time = datetime.fromtimestamp(end_time)

        BikerChangeLog.objects.create(biker=self, end_time=end_time)

        self.end_time = end_time
        self.save()

    def __str__(self):
        return '%s %s %s' % (self.number, self.name, self.surname)


class BikerChangeLog(models.Model):
    biker = models.ForeignKey(Biker)

    change_time = models.DateTimeField(auto_now_add=True)

    start_time = models.DateTimeField(null=True, blank=True)
    end_time = models.DateTimeField(null=True, blank=True)

    def __str__(self):
        return str(self.change_time)
