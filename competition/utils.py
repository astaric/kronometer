from typing import Any, Callable, TypeVar

from django import http
from django.core.exceptions import ObjectDoesNotExist

F = TypeVar("F", bound=Callable[..., http.HttpResponse])


def handle_errors() -> Callable[[F], F]:
    """
    Decorator to handle errors in views and return them as JsonResponse.
    """

    def decorator(func: F) -> F:
        def wrapper(*args: Any, **kwargs: Any) -> http.HttpResponse:
            try:
                return func(*args, **kwargs)
            except ObjectDoesNotExist:
                return http.JsonResponse(
                    {
                        "error": "Object not found",
                    },
                    status=http.HttpResponseNotFound.status_code,
                )
            except Exception as ex:
                return http.JsonResponse(
                    {
                        "error": str(ex),
                    },
                    status=500,
                )

        return wrapper  # type: ignore

    return decorator
