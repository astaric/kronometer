from django.db import models


class Category(models.Model):
    name = models.TextField()


class Biker(models.Model):
    number = models.IntegerField(unique=True)

    name = models.TextField()
    surname = models.TextField()

    category = models.ForeignKey(Category, null=True)
    domestic = models.BooleanField()

    start_time = models.DateTimeField(null=True)
    end_time = models.DateTimeField(null=True)

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
