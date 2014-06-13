from datetime import datetime
import json

from django.core import serializers
from django.http import HttpResponse, HttpResponseNotFound
from django.shortcuts import render

from kronometer.biker.models import Biker, Category


def results(request):
    bikers = list(Biker.objects.select_related('category'))
    bikers.sort(
        key=lambda b: (b.category_name, b.duration is None, b.duration))
    domestic_bikers = list(Biker.objects.filter(domestic=True)
                                        .select_related('category'))
    domestic_bikers.sort(
        key=lambda b: (b.category_name, b.duration is None, b.duration))
    return render(request, 'biker/results.html', {"bikers": bikers,
                                                  "domestic_bikers":domestic_bikers})


def biker_list(request):
    return HttpResponse(serializers.serialize("json", Biker.objects.all()),
                        mimetype="application/json")


def biker_update(request):
    params = request.POST if request.method == 'POST' else request.GET
    number = params.get('number')

    biker = Biker.objects.get(number=number)

    if "start_time" in params:
        start_time = float(params.get("start_time")) / 1000
        biker.start_time = datetime.fromtimestamp(start_time)

    if "end_time" in params:
        end_time = float(params.get("end_time")) / 1000
        biker.end_time = datetime.fromtimestamp(end_time)

    biker.save()
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
                            mimetype="application/json")
    except Exception as e:
        return HttpResponse(json.dumps({"error": str(e)}), status=500)


def set_start_time(request):
    number = request.POST.get('number') or request.GET.get('number')
    start_time = request.POST.get('start_time') or request.GET.get('start_time')
    start_time = float(start_time) / 1000
    start_time = datetime.fromtimestamp(start_time)

    biker = Biker.objects.get(number=number)
    biker.start_time = start_time
    biker.save()

    return HttpResponse(serializers.serialize("json", [biker]),
                        mimetype="application/json")


def set_end_time(request):
    number = request.POST.get('number') or request.GET.get('number')
    end_time = request.POST.get('end_time') or request.GET.get('start_time')
    end_time = float(end_time) / 1000
    end_time = datetime.fromtimestamp(end_time)

    biker = Biker.objects.get(number=number)
    biker.end_time = end_time
    biker.save()

    return HttpResponse(serializers.serialize("json", [biker]),
                        mimetype="application/json")


def category_list(request):
    return HttpResponse(serializers.serialize(
        "json", Category.objects.order_by('id')), mimetype="application/json")


def category_create(request):
    name = request.POST.get("name") or request.GET.get("name")
    category = Category.objects.create(name=name)
    return HttpResponse(serializers.serialize("json", [category]),
                        mimetype="application/json")
