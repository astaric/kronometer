from datetime import datetime
import json

from django.core import serializers
from django.http import HttpResponse

from kronometer.biker.models import Biker


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
    pass
