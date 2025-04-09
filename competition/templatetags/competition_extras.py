from datetime import timedelta

from django import template

register = template.Library()


@register.filter()
def nice_duration(value: timedelta | None) -> str:
    if value is not None:
        minutes = value.seconds // 60
        seconds = value.seconds % 60
        millis = value.microseconds // 1000

        return f"{minutes:02}:{seconds:02}.{millis:03}"
    else:
        return ""
