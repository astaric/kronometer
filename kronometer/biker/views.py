from datetime import datetime

from django.core import serializers
from django.http import HttpResponse
from django.shortcuts import render

from kronometer.biker.models import Biker, Category


def results(request):
    bikers = list(Biker.objects.select_related('category'))
    bikers.sort(key=lambda b: (b.category_name, b.duration))
    return render(request, 'biker/results.html', {"bikers": bikers})


def biker_list(request):
    return HttpResponse(serializers.serialize("json", Biker.objects.all()),
                        mimetype="application/json")


def biker_create(request):
    number = request.POST.get('number') or request.GET.get('number')
    name = request.POST.get('name') or request.GET.get('name')
    surname = request.POST.get('surname') or request.GET.get('surname')
    biker = Biker.objects.create(number=number, name=name, surname=surname)

    return HttpResponse(serializers.serialize("json", [biker]), mimetype="application/json")


def set_start_time(request):
    number = request.POST.get('number') or request.GET.get('number')
    start_time = request.POST.get('start_time') or request.GET.get('start_time')
    start_time = float(start_time) / 1000
    start_time = datetime.fromtimestamp(start_time)

    biker = Biker.objects.get(number=number)
    biker.start_time = start_time
    biker.save()

    return HttpResponse(serializers.serialize("json", [biker]), mimetype="application/json")


def set_end_time(request):
    number = request.POST.get('number') or request.GET.get('number')
    end_time = request.POST.get('end_time') or request.GET.get('start_time')
    end_time = float(end_time) / 1000
    end_time = datetime.fromtimestamp(end_time)

    biker = Biker.objects.get(number=number)
    biker.end_time = end_time
    biker.save()

    return HttpResponse(serializers.serialize("json", [biker]), mimetype="application/json")


def category_list(request):
    return HttpResponse(serializers.serialize(
        "json", Category.objects.order_by('id')), mimetype="application/json")


def category_create(request):
    name = request.POST.get("name") or request.GET.get("name")
    category = Category.objects.create(name=name)
    return HttpResponse(serializers.serialize("json", [category]), mimetype="application/json")
