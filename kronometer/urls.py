from django.conf.urls import patterns, url

from kronometer.biker.views import biker_list, biker_create, set_start_time, \
    set_end_time

urlpatterns = patterns('',
    url(r'^biker/list', biker_list, name='biker_list'),
    url(r'^biker/create', biker_create, name='biker_create'),
    url(r'^biker/set_start_time', set_start_time, name='set_start_time'),
    url(r'^biker/set_end_time', set_end_time, name='set_end_time'),
)
