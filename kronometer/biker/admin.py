from django.contrib import admin
from django.contrib.admin import TabularInline
from django.contrib.contenttypes.generic import GenericTabularInline
from kronometer.biker.models import Biker, Category, BikerChangeLog


class ChangeLogInline(TabularInline):
    model = BikerChangeLog
    fields = ['change_time', 'detail_start_time', 'detail_end_time']
    readonly_fields = fields
    extra = 0

    def has_add_permission(self, request):
        return False

    def has_delete_permission(self, request, obj=None):
        return False


class BikerAdmin(admin.ModelAdmin):
    inlines = [
        ChangeLogInline,
    ]


admin.site.register(Biker, BikerAdmin)
admin.site.register(Category)
