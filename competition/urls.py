from django.urls import path

import competition.views

urlpatterns = [
    path(r"", competition.views.index, name="index"),
    path(
        r"list",
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
