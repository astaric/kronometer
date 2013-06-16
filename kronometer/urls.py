from django.conf.urls import patterns, url, include

from kronometer.biker import views
from kronometer import settings

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    url(r'^biker/list', views.biker_list, name='biker_list'),
    url(r'^biker/create', views.biker_create, name='biker_create'),
    url(r'^biker/set_start_time', views.set_start_time, name='set_start_time'),
    url(r'^biker/set_end_time', views.set_end_time, name='set_end_time'),
    url(r'^category/list', views.category_list, name='category/list'),
    url(r'^category/create', views.category_create, name='category/list'),
    url(r'^admin/', include(admin.site.urls)),
    url(r'^static/(?P<path>.*)$', 'django.views.static.serve', {
        'document_root': settings.STATIC_ROOT,
    }),
    url(r'^', views.results, name='results'),
)
