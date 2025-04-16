from django import http

from oauth2_provider.views.generic import ProtectedResourceView

from competition.models import Competition, CompetitionBiker
from competition.utils import handle_errors


class CompetitionListView(ProtectedResourceView):  # type: ignore
    @handle_errors()
    def get(self, request: http.HttpRequest) -> http.HttpResponse:
        competitions = Competition.objects.all()

        return http.JsonResponse(
            {
                "competitions": [
                    {
                        "id": competition.id,
                        "name": competition.name,
                        "archived": competition.archived,
                    }
                    for competition in competitions
                ]
            }
        )


class CompetitionDetailView(ProtectedResourceView):  # type: ignore
    @handle_errors()
    def get(self, request: http.HttpRequest, competition_id: int) -> http.HttpResponse:
        competition = Competition.objects.get(id=competition_id)

        return http.JsonResponse(
            {
                "id": competition.id,
                "name": competition.name,
                "archived": competition.archived,
            }
        )


class CompetitionBikerListView(ProtectedResourceView):  # type: ignore
    @handle_errors()
    def get(self, request: http.HttpRequest, competition_id: int) -> http.HttpResponse:
        bikers = CompetitionBiker.objects.filter(
            competition_id=competition_id
        ).select_related("biker")

        return http.JsonResponse(
            {
                "bikers": [
                    {
                        "number": biker.number,
                        "name": biker.biker.name,
                        "surname": biker.biker.surname,
                        "start_time": biker.start_time.isoformat()
                        if biker.start_time
                        else None,
                        "end_time": biker.end_time.isoformat()
                        if biker.end_time
                        else None,
                    }
                    for biker in bikers.all()
                ]
            }
        )


class CompetitionBikerDetailView(ProtectedResourceView):  # type: ignore
    @handle_errors()
    def get(
        self, request: http.HttpRequest, competition_id: int, biker_number: int
    ) -> http.HttpResponse:
        biker = CompetitionBiker.objects.select_related("biker", "competition").get(
            competition_id=competition_id, number=biker_number
        )
        return http.JsonResponse(
            {
                "number": biker.number,
                "name": biker.biker.name,
                "surname": biker.biker.surname,
                "start_time": biker.start_time.isoformat()
                if biker.start_time
                else None,
                "end_time": biker.end_time.isoformat() if biker.end_time else None,
            }
        )

    @handle_errors()
    def post(
        self, request: http.HttpRequest, competition_id: int, biker_number: int
    ) -> http.HttpResponse:
        biker = CompetitionBiker.objects.select_related("competition").get(
            competition_id=competition_id, number=biker_number
        )
        if biker.competition.archived:
            return http.JsonResponse(
                {"error": "Cannot modify biker times for archived competition"},
                status=400,
            )

        params = request.POST
        if "start_time" in params:
            start_time = float(params["start_time"]) / 1000
            biker.set_start_time(start_time)

        if "end_time" in params:
            end_time = float(params["end_time"]) / 1000
            biker.set_end_time(end_time)

        return self.get(request, competition_id, biker_number)
