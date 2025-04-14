from django.urls import path

import competition.views

urlpatterns = [
    path(r"", competition.views.index, name="competition_index"),
    path(r"list", competition.views.competition_list, name="competition_list"),
    path(
        r"biker/<int:biker_id>/", competition.views.biker_results, name="biker_results"
    ),
    path(r"<slug>/", competition.views.results, name="competition_results"),
]
