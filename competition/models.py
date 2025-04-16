from datetime import datetime, timedelta, timezone

from django.db import models


class Category(models.Model):
    class Meta:
        verbose_name = "Category"
        verbose_name_plural = "Categories"
        ordering = ["name"]

    name = models.CharField(max_length=100)

    def __str__(self) -> str:
        return self.name


class Biker(models.Model):
    class Meta:
        verbose_name = "Biker"
        verbose_name_plural = "Bikers"
        ordering = ["surname", "name"]

    name = models.CharField(max_length=100)
    surname = models.CharField(max_length=100)
    comment = models.CharField(max_length=100, null=True, blank=True)
    domestic = models.BooleanField(default=False, blank=True)

    def __str__(self) -> str:
        if self.comment is not None:
            return f"{self.name} {self.surname} {self.comment}"
        else:
            return f"{self.name} {self.surname}"


class ResultTemplate(models.Model):
    class Meta:
        verbose_name = "Result Template"
        verbose_name_plural = "Result Templates"

    name = models.CharField(max_length=100)

    def __str__(self) -> str:
        return self.name


CAPTION = "caption"
RESULTS = "results"

SECTION_TYPE_CHOICES = [
    (CAPTION, "Caption"),
    (RESULTS, "Results"),
]


class ResultSection(models.Model):
    class Meta:
        verbose_name = "Result Section"
        verbose_name_plural = "Result Sections"
        ordering = ["section_number"]

    result_template = models.ForeignKey(
        ResultTemplate, on_delete=models.CASCADE, related_name="sections"
    )
    section_number = models.IntegerField()
    section_type = models.CharField(
        max_length=100, choices=SECTION_TYPE_CHOICES, default=RESULTS
    )
    name = models.CharField(max_length=100)
    categories = models.ManyToManyField(Category, blank=True, related_name="categories")
    domestic_only = models.BooleanField(default=False, blank=True)

    def __str__(self) -> str:
        return f"Section {self.section_number}"


class Competition(models.Model):
    class Meta:
        verbose_name = "Competition"
        verbose_name_plural = "Competitions"
        ordering = ["slug"]

    name = models.CharField(max_length=100)
    slug = models.SlugField(max_length=100, unique=True)
    description = models.TextField(null=True, blank=True)
    active = models.BooleanField(default=False)
    archived = models.BooleanField(default=False)
    result_template = models.ForeignKey(
        ResultTemplate,
        on_delete=models.CASCADE,
        related_name="competitions",
    )

    def __str__(self) -> str:
        return self.name


class CompetitionBiker(models.Model):
    class Meta:
        unique_together = ("competition", "number")
        verbose_name = "Competition Biker"
        verbose_name_plural = "Competition Bikers"
        ordering = ["competition", "number"]

    competition = models.ForeignKey(
        Competition, on_delete=models.CASCADE, related_name="competition_bikers"
    )
    number = models.IntegerField()
    biker = models.ForeignKey(
        Biker, on_delete=models.CASCADE, related_name="competitions"
    )
    category = models.ForeignKey(
        Category, on_delete=models.CASCADE, related_name="competition_bikers"
    )
    domestic = models.BooleanField(default=False, blank=True)

    start_time = models.DateTimeField(null=True, blank=True)
    end_time = models.DateTimeField(null=True, blank=True)

    def set_start_time(self, start_time: datetime | float) -> None:
        if isinstance(start_time, float):
            start_time = datetime.fromtimestamp(start_time, tz=timezone.utc)

        CompetitionChangeLog.objects.create(
            competition=self.competition, biker=self, start_time=start_time
        )

        self.start_time = start_time
        self.save()

    def set_end_time(self, end_time: datetime | float) -> None:
        if isinstance(end_time, float):
            end_time = datetime.fromtimestamp(end_time, tz=timezone.utc)

        CompetitionChangeLog.objects.create(
            competition=self.competition, biker=self, end_time=end_time
        )

        self.end_time = end_time
        self.save()

    @property
    def duration(self) -> timedelta | None:
        if self.start_time and self.end_time:
            return self.end_time - self.start_time
        else:
            return None

    def __str__(self) -> str:
        return f"{self.competition.name} - {self.number}: {self.biker.name} {self.biker.surname}"


class CompetitionChangeLog(models.Model):
    class Meta:
        verbose_name = "Competition Change Log"
        verbose_name_plural = "Competition Change Logs"
        ordering = ["-change_time"]

    competition = models.ForeignKey(
        Competition, on_delete=models.CASCADE, related_name="change_logs"
    )
    biker = models.ForeignKey(
        CompetitionBiker, on_delete=models.CASCADE, related_name="change_logs"
    )
    change_time = models.DateTimeField(auto_now_add=True)
    start_time = models.DateTimeField(null=True, blank=True)
    end_time = models.DateTimeField(null=True, blank=True)

    def __str__(self) -> str:
        return str(self.change_time)
