from datetime import datetime, timezone
from django.db import models


class Category(models.Model):
    name = models.TextField()
    gender = models.TextField(null=True, blank=True)

    def __str__(self):
        return self.name


class Biker(models.Model):
    number = models.IntegerField(unique=True)

    name = models.TextField()
    surname = models.TextField()

    category = models.ForeignKey(Category, on_delete=models.CASCADE, null=True)
    birth_year = models.IntegerField(null=True, blank=True)
    domestic = models.BooleanField(default=False, blank=True)

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

    @property
    def nice_start_time(self):
        if self.start_time:
            return self.start_time.strftime("%H:%M:%S.%f")
        else:
            return None

    def set_start_time(self, start_time):
        if isinstance(start_time, float):
            start_time = datetime.fromtimestamp(start_time, tz=timezone.utc)

        BikerChangeLog.objects.create(biker=self, start_time=start_time)

        self.start_time = start_time
        self.save()

    def set_end_time(self, end_time):
        if isinstance(end_time, float):
            end_time = datetime.fromtimestamp(end_time, tz=timezone.utc)

        BikerChangeLog.objects.create(biker=self, end_time=end_time)

        self.end_time = end_time
        self.save()

    def __str__(self):
        return '%s %s %s' % (self.number, self.name, self.surname)


class BikerChangeLog(models.Model):
    biker = models.ForeignKey(Biker, on_delete=models.CASCADE)

    change_time = models.DateTimeField(auto_now_add=True)

    start_time = models.DateTimeField(null=True, blank=True)
    end_time = models.DateTimeField(null=True, blank=True)

    @property
    def detail_start_time(self):
        return str(self.start_time)

    @property
    def detail_end_time(self):
        return str(self.end_time)

    def __str__(self):
        return str(self.change_time)


class Competition(models.Model):
    id = models.AutoField(primary_key=True)
    title = models.CharField(max_length=100)
    description = models.TextField(null=True, blank=True)
    default = models.BooleanField(default=False)

    result_section_1 = models.TextField(null=True, blank=True)
    result_section_2 = models.TextField(null=True, blank=True)

    section_2_domestic_only = models.BooleanField(default=False)

    def __str__(self):
        return str(self.title)
