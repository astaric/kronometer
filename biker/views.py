import datetime
import json
from dataclasses import dataclass
from itertools import groupby

from django.contrib.auth.decorators import login_required
from django.core import serializers
from django.http import HttpRequest, HttpResponse
from django.shortcuts import redirect, render

from biker.models import Biker, Category, Competition


@dataclass
class CategoryResults:
    category_name: str
    bikers: list[Biker]


@dataclass
class CategoryGroup:
    name: str
    categories: list[CategoryResults]


def results(request: HttpRequest, competition_id: int | None = None) -> HttpResponse:
    if competition_id is None:
        competition = Competition.objects.filter(active=True).first()
    else:
        competition = Competition.objects.get(id=competition_id)

    assert competition is not None

    all_bikers = Biker.objects.filter(competition=competition).select_related(
        "category"
    )

    results = list[CategoryGroup]()

    bikers = list(all_bikers)
    bikers.sort(key=lambda b: (b.category.gender, b.duration is None, b.duration))

    results.append(
        CategoryGroup(
            name=competition.result_section_1 or "Skupno",
            categories=[
                CategoryResults(category_name=category_name, bikers=list(bikers))
                for category_name, bikers in groupby(
                    bikers, key=lambda biker: biker.category.gender
                )
            ],
        )
    )

    if competition.section_2_domestic_only:
        bikers = list(all_bikers.filter(domestic=True))
    else:
        bikers = list(all_bikers)
    bikers.sort(key=lambda b: (b.category.name, b.duration is None, b.duration))

    results.append(
        CategoryGroup(
            name=competition.result_section_2 or "Občinsko",
            categories=[
                CategoryResults(category_name=category_name, bikers=list(bikers))
                for category_name, bikers in groupby(
                    bikers, key=lambda biker: biker.category.name
                )
            ],
        )
    )

    return render(
        request,
        "biker/results.html",
        {
            "competition": competition,
            "results": results,
        },
    )


def biker_list(request: HttpRequest, competition_id: int | None = None) -> HttpResponse:
    if competition_id is None:
        competition = Competition.objects.filter(active=True).first()
    else:
        competition = Competition.objects.get(id=competition_id)

    bikers = Biker.objects.filter(competition=competition)
    return HttpResponse(
        serializers.serialize("json", bikers), content_type="application/json"
    )


def biker_update(request: HttpRequest) -> HttpResponse:
    params = request.POST if request.method == "POST" else request.GET
    number = params["number"]

    biker = Biker.objects.get(number=number)

    if "start_time" in params:
        start_time = float(params["start_time"]) / 1000
        biker.set_start_time(start_time)

    if "end_time" in params:
        end_time = float(params["end_time"]) / 1000
        biker.set_end_time(end_time)

    return HttpResponse()


def biker_create(request: HttpRequest) -> HttpResponse:
    params = request.POST if request.method == "POST" else request.GET
    number = int(params["number"])
    name = params["name"]
    surname = params["surname"]
    category_id = params["category"]
    domestic = bool(params["domestic"])

    try:
        biker = Biker.objects.create(
            number=number,
            name=name,
            surname=surname,
            category_id=category_id,
            domestic=bool(domestic),
        )
        return HttpResponse(
            serializers.serialize("json", [biker]), content_type="application/json"
        )
    except Exception as e:
        return HttpResponse(json.dumps({"error": str(e)}), status=500)


def set_start_time(
    request: HttpRequest,
    competition_id: int | None = None,
) -> HttpResponse:
    try:
        params = request.POST if request.method == "POST" else request.GET
        number = params["number"]
        start_time = float(params["start_time"]) / 1000

        if competition_id is None:
            competition = Competition.objects.filter(active=True).first()
        else:
            competition = Competition.objects.get(id=competition_id)
        biker = Biker.objects.get(number=number, competition=competition)
        biker.set_start_time(start_time)

        return HttpResponse(
            serializers.serialize("json", [biker]), content_type="application/json"
        )
    except Exception as e:
        return HttpResponse(json.dumps({"error": str(e)}), status=500)


def set_end_time(
    request: HttpRequest,
    competition_id: int | None = None,
) -> HttpResponse:
    params = request.POST if request.method == "POST" else request.GET
    number = params["number"]
    end_time = float(params["end_time"]) / 1000

    if competition_id is None:
        competition = Competition.objects.filter(active=True).first()
    else:
        competition = Competition.objects.get(id=competition_id)

    biker = Biker.objects.get(number=number, competition=competition)
    biker.set_end_time(end_time)

    return HttpResponse(
        serializers.serialize("json", [biker]), content_type="application/json"
    )


def category_list(request: HttpRequest) -> HttpResponse:
    return HttpResponse(
        serializers.serialize("json", Category.objects.order_by("id")),
        content_type="application/json",
    )


def category_create(request: HttpRequest) -> HttpResponse:
    params = request.POST if request.method == "POST" else request.GET
    name = params["name"]
    category = Category.objects.create(name=name)
    return HttpResponse(
        serializers.serialize("json", [category]), content_type="application/json"
    )


@login_required
def biker_start(request: HttpRequest) -> HttpResponse:
    number = request.POST["number"]
    if number:
        biker = Biker.objects.get(number=number)
        biker.set_start_time(datetime.datetime.now())
        return redirect("biker/start")

    not_started = list(Biker.objects.filter(start_time=None).order_by("number").all())
    started = list(Biker.objects.exclude(start_time=None).order_by("number").all())
    return render(
        request,
        "biker/start.html",
        {
            "not_started": not_started,
            "started": started,
        },
    )


@login_required
def biker_finish(request: HttpRequest) -> HttpResponse:
    number = request.POST.get("number")
    if number:
        biker = Biker.objects.get(number=number)
        biker.set_end_time(datetime.datetime.now())
        return redirect("biker/finish")

    not_finished = list(Biker.objects.filter(end_time=None).order_by("number").all())
    finished = list(Biker.objects.exclude(end_time=None).order_by("number").all())
    return render(
        request,
        "biker/finish.html",
        {
            "not_finished": not_finished,
            "finished": finished,
        },
    )


def competitions(request: HttpRequest) -> HttpResponse:
    return HttpResponse(
        serializers.serialize("json", Competition.objects.all()),
        content_type="application/json; charset=utf-8",
    )
