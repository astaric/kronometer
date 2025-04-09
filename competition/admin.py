from django import http
from django.contrib import admin
from django.db import models
from django.forms import widgets

from competition.models import (
    Biker,
    Category,
    Competition,
    CompetitionBiker,
    ResultSection,
    ResultTemplate,
)


@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin[Category]):
    pass


class BikerResultsInline(admin.TabularInline[CompetitionBiker, Biker]):
    model = CompetitionBiker
    fields: list[str] = ["competition", "category", "duration"]
    readonly_fields = fields
    ordering = ("competition",)

    def has_add_permission(
        self,
        request: http.HttpRequest,
        obj: Biker | None = None,
    ) -> bool:
        return False

    def has_delete_permission(
        self,
        request: http.HttpRequest,
        obj: Biker | None = None,  # type: ignore[override]
    ) -> bool:
        return False


@admin.register(Biker)
class BikerAdmin(admin.ModelAdmin[Biker]):
    inlines = [BikerResultsInline]


@admin.register(CompetitionBiker)
class CompetitionBikerAdmin(admin.ModelAdmin[CompetitionBiker]):
    pass


class BikerInline(admin.TabularInline[CompetitionBiker, Competition]):
    model = CompetitionBiker

    fields = ["number", "biker", "category", "domestic"]
    extra = 0
    show_change_link = True

    formfield_overrides = {
        models.BooleanField: {"widget": widgets.CheckboxInput(attrs={"tabindex": 0})},
    }


@admin.register(Competition)
class CompetitionAdmin(admin.ModelAdmin[Competition]):
    prepopulated_fields = {"slug": ("name",)}

    inlines = [
        BikerInline,
    ]


class ResultSectionInline(admin.TabularInline[ResultSection, ResultTemplate]):
    model = ResultSection


@admin.register(ResultTemplate)
class ResultTemplateAdmin(admin.ModelAdmin[ResultTemplate]):
    inlines = [
        ResultSectionInline,
    ]
