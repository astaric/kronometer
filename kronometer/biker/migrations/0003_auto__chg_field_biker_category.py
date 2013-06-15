# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):

        # Changing field 'Biker.category'
        db.alter_column(u'biker_biker', 'category_id', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['biker.Category'], null=True))

    def backwards(self, orm):

        # User chose to not deal with backwards NULL issues for 'Biker.category'
        raise RuntimeError("Cannot reverse this migration. 'Biker.category' and its values cannot be restored.")

    models = {
        u'biker.biker': {
            'Meta': {'object_name': 'Biker'},
            'category': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['biker.Category']", 'null': 'True'}),
            'domestic': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
            'end_time': ('django.db.models.fields.DateTimeField', [], {'null': 'True'}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.TextField', [], {}),
            'number': ('django.db.models.fields.IntegerField', [], {'unique': 'True'}),
            'start_time': ('django.db.models.fields.DateTimeField', [], {'null': 'True'}),
            'surname': ('django.db.models.fields.TextField', [], {})
        },
        u'biker.category': {
            'Meta': {'object_name': 'Category'},
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.TextField', [], {})
        }
    }

    complete_apps = ['biker']