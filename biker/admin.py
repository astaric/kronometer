from django.contrib import admin
from django.contrib.admin import TabularInline
from django.db import models
from django.forms import TextInput

from biker.models import Biker, Category, BikerChangeLog, Competition


class ChangeLogInline(TabularInline):
    model = BikerChangeLog
    fields = ['change_time', 'detail_start_time', 'detail_end_time']
    readonly_fields = fields
    extra = 0

    def has_add_permission(self, request, obj=None):
        return False

    def has_delete_permission(self, request, obj=None):
        return False


class BikerAdmin(admin.ModelAdmin):
    actions = ["clear_times"]
    list_display = ['number', 'name', 'surname', 'domestic']
    inlines = [
        ChangeLogInline,
    ]

    def get_form(self, request, obj=None, **kwargs):
        form = super().get_form(request, obj, **kwargs)

        competition = Competition.objects.filter(active=True).first()
        biker = Biker.objects.filter(competition=competition).last()
        if competition is not None:
            form.base_fields['competition'].initial = competition.id
        if biker is not None:
            form.base_fields['number'].initial = biker.number + 1
        return form

    def get_queryset(self, request):
        qs = super().get_queryset(request)
        competition = Competition.objects.filter(active=True).first()
        if competition is not None:
            return qs.filter(competition=competition)
        else:
            return qs

    @admin.action(description="Remove start and end times from selected bikers")
    def clear_times(self, request, queryset):
        queryset.update(start_time=None, end_time=None)


class CategoryInline(TabularInline):
    model = Category

    formfield_overrides = {
        models.TextField: {'widget': TextInput()},
    }


class CompetitionAdmin(admin.ModelAdmin):
    actions = ["copy_competition", "make_active"]
    inlines = [CategoryInline]
    list_display = ['title', 'active']

    @admin.action(description="Make active")
    def make_active(self, request, queryset):
        competition = queryset.first()
        Competition.objects.update(active=False)

        competition.active = True
        competition.save()

    @admin.action(description="Copy selected competition")
    def copy_competition(self, request, queryset):
        template = queryset.first()
        if not template:
            return

        competition = Competition.objects.get(id=template.id)
        competition.pk = None
        competition.title = f"{competition.title} (copy)"
        competition.active = False
        competition.save()

        print("Competition")
        for category in Category.objects.filter(competition=template):
            print(f"Category {category}")
            category.pk = None
            category.competition = competition
            category.save()


admin.site.register(Competition, CompetitionAdmin)


admin.site.register(Biker, BikerAdmin)
