from dataclasses import dataclass
from typing import Any

from django import http
from django.shortcuts import redirect
from django.views.generic import DetailView, ListView

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
        return redirect("competition_list")
    return redirect("competition_detail", slug=competition.slug)


class CompetitionListView(ListView[Competition]):
    model = Competition


@dataclass
class CompetitionResultsSection:
    name: str
    bikers: list[CompetitionBiker]


@dataclass
class CompetitionResultsGroup:
    name: str
    sections: list[CompetitionResultsSection]


class CompetitionDetailView(DetailView[Competition]):
    model = Competition

    def get_context_data(self, **kwargs: dict[str, Any]) -> dict[str, object]:
        context = super().get_context_data(**kwargs)
        competition: Competition = context["object"]

        result_sections = ResultSection.objects.filter(
            result_template_id=competition.result_template_id
        ).prefetch_related("categories")
        competition_bikers = CompetitionBiker.objects.filter(
            competition=competition
        ).select_related("biker")

        competition_results = list[CompetitionResultsGroup]()
        current_group: CompetitionResultsGroup | None = None
        for section in result_sections:
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
                    for biker in competition_bikers
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

        context["results"] = competition_results

        return context


class BikerDetailView(DetailView[Biker]):
    model = Biker

    def get_context_data(self, **kwargs: dict[str, Any]) -> dict[str, Any]:
        context = super().get_context_data(**kwargs)
        biker: Biker = context["object"]
        results = CompetitionBiker.objects.filter(biker=biker).select_related(
            "competition", "category"
        )
        context["results"] = results
        return context
