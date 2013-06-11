from django.db import models


# Create your models here.
class Category(models.Model):
    pass


class Biker(models.Model):
    name = models.TextField()
    surname = models.TextField()

    category = models.ForeignKey(Category, null=True)

    start_time = models.DateTimeField(null=True)
    end_time = models.DateTimeField(null=True)

    @property
    def duration(self):
        if self.start_time and self.end_time:
            return self.end_time - self.start_time
