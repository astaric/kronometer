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
    pass


def set_end_time(request):
    pass
