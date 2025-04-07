from datetime import datetime, timedelta, timezone

from django.db import models


class Competition(models.Model):
    id = models.AutoField(primary_key=True)
    title = models.CharField(max_length=100)
    description = models.TextField(null=True, blank=True)
    active = models.BooleanField(default=False)

    result_section_1 = models.TextField(null=True, blank=True)
    result_section_2 = models.TextField(null=True, blank=True)

    section_2_domestic_only = models.BooleanField(default=False)

    def __str__(self) -> str:
        return str(self.title)


class Category(models.Model):
    competition = models.ForeignKey(Competition, on_delete=models.CASCADE)
    id = models.AutoField(primary_key=True)
    name = models.TextField()
    gender = models.TextField(null=True, blank=True)

    def __str__(self) -> str:
        return self.name


class Biker(models.Model):
    competition = models.ForeignKey(Competition, on_delete=models.CASCADE)
    number = models.IntegerField()

    name = models.TextField()
    surname = models.TextField()

    category = models.ForeignKey(Category, on_delete=models.CASCADE)
    birth_year = models.IntegerField(null=True, blank=True)
    domestic = models.BooleanField(default=False, blank=True)

    start_time = models.DateTimeField(null=True, blank=True)
    end_time = models.DateTimeField(null=True, blank=True)

    class Meta:
        unique_together = ("competition", "number")

    @property
    def duration(self) -> timedelta | None:
        if self.start_time and self.end_time:
            return self.end_time - self.start_time
        else:
            return None

    @property
    def nice_duration(self) -> str:
        duration = self.duration
        if duration is not None:
            minutes = duration.seconds // 60
            seconds = duration.seconds % 60
            millis = duration.microseconds // 1000

            return f"{minutes:02}:{seconds:02}.{millis:03}"
        else:
            return ""

    @property
    def category_name(self) -> str:
        return self.category.name

    @property
    def nice_start_time(self) -> str | None:
        if self.start_time:
            return self.start_time.strftime("%H:%M:%S.%f")
        else:
            return None

    def set_start_time(self, start_time: datetime | float) -> None:
        if isinstance(start_time, float):
            start_time = datetime.fromtimestamp(start_time, tz=timezone.utc)

        BikerChangeLog.objects.create(
            competition=self.competition, biker=self, start_time=start_time
        )

        self.start_time = start_time
        self.save()

    def set_end_time(self, end_time: datetime | float) -> None:
        if isinstance(end_time, float):
            end_time = datetime.fromtimestamp(end_time, tz=timezone.utc)

        BikerChangeLog.objects.create(
            competition=self.competition, biker=self, end_time=end_time
        )

        self.end_time = end_time
        self.save()

    def __str__(self) -> str:
        return "%s %s %s" % (self.number, self.name, self.surname)


class BikerChangeLog(models.Model):
    competition = models.ForeignKey(Competition, on_delete=models.CASCADE)
    biker = models.ForeignKey(Biker, on_delete=models.CASCADE)

    change_time = models.DateTimeField(auto_now_add=True)

    start_time = models.DateTimeField(null=True, blank=True)
    end_time = models.DateTimeField(null=True, blank=True)

    @property
    def detail_start_time(self) -> str:
        return str(self.start_time)

    @property
    def detail_end_time(self) -> str:
        return str(self.end_time)

    def __str__(self) -> str:
        return str(self.change_time)
