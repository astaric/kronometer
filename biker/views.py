from itertools import groupby
import json

from django.core import serializers
from django.http import HttpResponse
from django.shortcuts import render
from django.utils.safestring import mark_safe

from biker.models import Biker, Category, Competition


def results(request):
    competition = Competition.objects.first()
    assert isinstance(competition, Competition)

    results = []
    results.append((mark_safe("<h2>{}</h2>".format(competition.result_section_1 or "Skupno")), []))
    bikers = list(Biker.objects.select_related('category'))
    bikers.sort(
        key=lambda b: (b.category.gender, b.duration is None, b.duration))

    for c, b in groupby(bikers, key=lambda b: b.category.gender):
        results.append((c, list(b)))

    results.append((mark_safe("<h2>{}</h2>".format(competition.result_section_2 or "Občinsko")), []))
    bikers = list(Biker.objects.filter(domestic=1).select_related('category'))
    bikers.sort(
        key=lambda b: (b.category.name, b.duration is None, b.duration))
    for c, b in groupby(bikers, key=lambda b: b.category.name):
        results.append((c, list(b)))



    return render(request, 'biker/results.html', {
        "competition": competition,
        "results": results,
    })


def biker_list(request):
    return HttpResponse(serializers.serialize("json", Biker.objects.all()),
                        content_type="application/json")


def biker_update(request):
    params = request.POST if request.method == 'POST' else request.GET
    number = params.get('number')

    biker = Biker.objects.get(number=number)

    if "start_time" in params:
        start_time = float(params.get("start_time")) / 1000
        biker.set_start_time(start_time)

    if "end_time" in params:
        end_time = float(params.get("end_time")) / 1000
        biker.set_end_time(end_time)

    return HttpResponse()


def biker_create(request):
    params = request.POST if request.method == 'POST' else request.GET
    number = params.get('number')
    name = params.get('name')
    surname = params.get('surname')
    category_id = params.get('category')
    domestic = params.get('domestic')
    try:
        biker = Biker.objects.create(
            number=number,
            name=name,
            surname=surname,
            category_id=category_id,
            domestic=bool(domestic)
        )
        return HttpResponse(serializers.serialize("json", [biker]),
                            content_type="application/json")
    except Exception as e:
        return HttpResponse(json.dumps({"error": str(e)}), status=500)


def set_start_time(request):
    params = request.POST if request.method == 'POST' else request.GET
    number = params.get('number')
    start_time = float(params.get('start_time')) / 1000

    biker = Biker.objects.get(number=number)
    biker.set_start_time(start_time)

    return HttpResponse(serializers.serialize("json", [biker]),
                        content_type="application/json")


def set_end_time(request):
    params = request.POST if request.method == 'POST' else request.GET
    number = params.get('number')
    end_time = float(params.get('end_time')) / 1000

    biker = Biker.objects.get(number=number)
    biker.set_end_time(end_time)
    biker.end_time = end_time
    biker.save()

    return HttpResponse(serializers.serialize("json", [biker]),
                        content_type="application/json")


def category_list(request):
    return HttpResponse(serializers.serialize(
        "json", Category.objects.order_by('id')), content_type="application/json")


def category_create(request):
    name = request.POST.get("name") or request.GET.get("name")
    category = Category.objects.create(name=name)
    return HttpResponse(serializers.serialize("json", [category]),
                        content_type="application/json")
