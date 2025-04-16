from django.urls import path
from django.views.decorators.csrf import csrf_exempt

import competition.api_views
import competition.views

api_urlpatterns = [
    path(
        "api/competition/<int:competition_id>/",
        competition.api_views.CompetitionDetailView.as_view(),
    ),
    path(
        "api/competition/",
        competition.api_views.CompetitionListView.as_view(),
    ),
    path(
        "api/competition/<int:competition_id>/biker/",
        csrf_exempt(competition.api_views.CompetitionBikerListView.as_view()),
    ),
    path(
        "api/competition/<int:competition_id>/biker/<int:biker_number>/",
        csrf_exempt(competition.api_views.CompetitionBikerDetailView.as_view()),
    ),
]

urlpatterns = api_urlpatterns + [
    path(r"", competition.views.index, name="index"),
    path(
        r"list/",
        competition.views.CompetitionListView.as_view(),
        name="competition_list",
    ),
    path(
        r"biker/<int:pk>/",
        competition.views.BikerDetailView.as_view(),
        name="biker_detail",
    ),
    path(
        r"<slug:slug>/",
        competition.views.CompetitionDetailView.as_view(),
        name="competition_detail",
    ),
]
