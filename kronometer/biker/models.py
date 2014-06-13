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

    def __str__(self):
        return '%s %s %s' % (self.number, self.name, self.surname)
