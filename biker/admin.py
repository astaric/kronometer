from typing import Any, Type

from django import forms
from django.contrib import admin
from django.contrib.admin import TabularInline
from django.db import models
from django.http import HttpRequest

from biker.models import Biker, BikerChangeLog, Category, Competition


class ChangeLogInline(TabularInline[BikerChangeLog, Biker]):
    model = BikerChangeLog
    fields: list[str] = ["change_time", "detail_start_time", "detail_end_time"]
    readonly_fields = fields
    extra = 0

    def has_add_permission(
        self,
        request: HttpRequest,
        obj: models.Model | None = None,
    ) -> bool:
        return False

    def has_delete_permission(
        self,
        request: HttpRequest,
        obj: models.Model | None = None,
    ) -> bool:
        return False


class BikerAdmin(admin.ModelAdmin[Biker]):
    actions = ["clear_times"]
    list_display = ["number", "name", "surname", "domestic"]
    inlines = [
        ChangeLogInline,
    ]

    def get_form(
        self,
        request: HttpRequest,
        obj: Biker | None = None,
        change: bool = False,
        **kwargs: Any,
    ) -> Type[forms.ModelForm[Biker]]:
        form = super().get_form(request, obj, change, **kwargs)

        competition = Competition.objects.filter(active=True).first()
        biker = Biker.objects.filter(competition=competition).last()
        if competition is not None:
            form.base_fields["competition"].initial = competition.id
        if biker is not None:
            form.base_fields["number"].initial = biker.number + 1
        return form

    def get_queryset(self, request: HttpRequest) -> models.QuerySet[Biker]:
        qs = super().get_queryset(request)
        competition = Competition.objects.filter(active=True).first()
        if competition is not None:
            return qs.filter(competition=competition)
        else:
            return qs

    @admin.action(description="Remove start and end times from selected bikers")
    def clear_times(
        self, request: HttpRequest, queryset: models.QuerySet[models.Model]
    ) -> None:
        queryset.update(start_time=None, end_time=None)


class CategoryInline(TabularInline[Category, Competition]):
    model = Category
    formfield_overrides = {
        models.TextField: {"widget": forms.TextInput()},
    }


class CompetitionAdmin(admin.ModelAdmin[Competition]):
    model = Competition
    actions = ["copy_competition", "make_active"]
    inlines = [CategoryInline]
    list_display = ["title", "active"]

    @admin.action(description="Make active")
    def make_active(
        self, request: HttpRequest, queryset: models.QuerySet[models.Model]
    ) -> None:
        competition = queryset.first()
        assert isinstance(competition, Competition)
        Competition.objects.update(active=False)

        competition.active = True
        competition.save()

    @admin.action(description="Copy selected competition")
    def copy_competition(
        self,
        request: HttpRequest,
        queryset: models.QuerySet[Competition],
    ) -> None:
        template = queryset.first()
        if not template:
            return

        competition = Competition.objects.get(id=template.id)
        competition.pk = None
        competition.title = f"{competition.title} (copy)"
        competition.active = False
        competition.save()

        print("Competition")
        for category in Category.objects.filter(competition=template):
            print(f"Category {category}")
            category.pk = None
            category.competition = competition
            category.save()


admin.site.register(Competition, CompetitionAdmin)

admin.site.register(Biker, BikerAdmin)
