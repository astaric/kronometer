from django.urls import path

from django.contrib import admin

import biker.views

admin.autodiscover()

urlpatterns = [
    path(r'biker/list', biker.views.biker_list, name='biker_list'),
    path(r'biker/update', biker.views.biker_update, name='biker_update'),
    path(r'biker/create', biker.views.biker_create, name='biker_create'),
    path(r'biker/set_start_time', biker.views.set_start_time, name='set_start_time'),
    path(r'biker/set_end_time', biker.views.set_end_time, name='set_end_time'),
    path(r'biker/start', biker.views.biker_start, name='biker/start'),
    path(r'biker/finish', biker.views.biker_finish, name='biker/finish'),
    path(r'category/list', biker.views.category_list, name='category/list'),
    path(r'category/create', biker.views.category_create, name='category/list'),
    path(r'competition/list', biker.views.competitions, name='competitions'),
    path(r'<int:competition_id>', biker.views.results, name='results'),
    path(r'<int:competition_id>/biker/list', biker.views.biker_list, name='biker_list'),
    path(r'<int:competition_id>/biker/set_start_time', biker.views.set_start_time, name='set_start_time'),
    path(r'<int:competition_id>/biker/set_end_time', biker.views.set_end_time, name='set_end_time'),
    path(r'admin/', admin.site.urls),
    path(r'', biker.views.results, name='results'),
]
