from django.conf.urls import include, url

from django.contrib import admin

import biker.views

admin.autodiscover()

urlpatterns = [
    url(r'^biker/list', biker.views.biker_list, name='biker_list'),
    url(r'^biker/update', biker.views.biker_update, name='biker_update'),
    url(r'^biker/create', biker.views.biker_create, name='biker_create'),
    url(r'^biker/set_start_time', biker.views.set_start_time, name='set_start_time'),
    url(r'^biker/set_end_time', biker.views.set_end_time, name='set_end_time'),
    url(r'^category/list', biker.views.category_list, name='category/list'),
    url(r'^category/create', biker.views.category_create, name='category/list'),
    url(r'^admin/', include(admin.site.urls)),
    url(r'^', biker.views.results, name='results'),
]
