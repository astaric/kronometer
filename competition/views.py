from dataclasses import dataclass

from django import http
from django.db.models import Prefetch
from django.shortcuts import redirect, render

from competition.models import (
    Biker,
    CAPTION,
    Competition,
    CompetitionBiker,
    RESULTS,
    ResultSection,
)


def index(request: http.HttpRequest) -> http.HttpResponse:
    competition = Competition.objects.filter(active=True).first()
    if competition is None:
        competition = Competition.objects.last()
        if competition is None:
            return render(request, "competition/results.html")

    return redirect("competition_results", slug=competition.slug)


def competition_list(request: http.HttpRequest) -> http.HttpResponse:
    competitions = Competition.objects.all()
    return render(
        request,
        "competition/list.html",
        {
            "competitions": competitions,
        },
    )


@dataclass
class CompetitionResultsSection:
    name: str
    bikers: list[CompetitionBiker]


@dataclass
class CompetitionResultsGroup:
    name: str
    sections: list[CompetitionResultsSection]


def results(request: http.HttpRequest, slug: str) -> http.HttpResponse:
    competition = (
        Competition.objects.filter(slug=slug)
        .prefetch_related(
            Prefetch(
                "result_template__sections",
                queryset=ResultSection.objects.prefetch_related("categories"),
            ),
            Prefetch(
                "competition_bikers",
            ),
        )
        .first()
    )
    if competition is None:
        return render(request, "competition/results.html")

    competition_results = list[CompetitionResultsGroup]()
    current_group: CompetitionResultsGroup | None = None
    for section in competition.result_template.sections.all():
        if section.section_type == CAPTION:
            if current_group is not None:
                competition_results.append(current_group)
            current_group = CompetitionResultsGroup(name=section.name, sections=[])
        elif section.section_type == RESULTS:
            assert current_group is not None
            include_categories = set(
                category.id for category in section.categories.all()
            )
            bikers = [
                biker
                for biker in competition.competition_bikers.all()
                if biker.category_id in include_categories
                and (biker.domestic or not section.domestic_only)
            ]
            if len(bikers) > 0:
                bikers.sort(key=lambda b: (b.duration is None, b.duration))
                current_group.sections.append(
                    CompetitionResultsSection(name=section.name, bikers=bikers)
                )
        else:
            raise Exception("Unsupported section type")

    if current_group is not None:
        competition_results.append(current_group)

    return render(
        request,
        "competition/results.html",
        {
            "competition": competition,
            "results": competition_results,
        },
    )


def biker_results(request: http.HttpRequest, biker_id: int) -> http.HttpResponse:
    biker = Biker.objects.get(id=biker_id)

    results = CompetitionBiker.objects.filter(biker=biker).select_related("competition")

    return render(
        request,
        "competition/biker_results.html",
        {
            "biker": biker,
            "results": results,
        },
    )
