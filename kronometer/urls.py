from django.conf.urls import patterns, url

from kronometer.biker import  views

urlpatterns = patterns('',
    url(r'^$', views.results, name='results'),
    url(r'^biker/list', views.biker_list, name='biker_list'),
    url(r'^biker/create', views.biker_create, name='biker_create'),
    url(r'^biker/set_start_time', views.set_start_time, name='set_start_time'),
    url(r'^biker/set_end_time', views.set_end_time, name='set_end_time'),
)
