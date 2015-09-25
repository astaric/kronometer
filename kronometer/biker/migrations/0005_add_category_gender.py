# -*- coding: utf-8 -*-
from south.utils import datetime_utils as datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding field 'Category.gender'
        db.add_column('biker_category', 'gender',
                      self.gf('django.db.models.fields.TextField')(blank=True, null=True),
                      keep_default=False)


    def backwards(self, orm):
        # Deleting field 'Category.gender'
        db.delete_column('biker_category', 'gender')


    models = {
        'biker.biker': {
            'Meta': {'object_name': 'Biker'},
            'category': ('django.db.models.fields.related.ForeignKey', [], {'to': "orm['biker.Category']", 'null': 'True'}),
            'domestic': ('django.db.models.fields.BooleanField', [], {}),
            'end_time': ('django.db.models.fields.DateTimeField', [], {'blank': 'True', 'null': 'True'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.TextField', [], {}),
            'number': ('django.db.models.fields.IntegerField', [], {'unique': 'True'}),
            'start_time': ('django.db.models.fields.DateTimeField', [], {'blank': 'True', 'null': 'True'}),
            'surname': ('django.db.models.fields.TextField', [], {})
        },
        'biker.bikerchangelog': {
            'Meta': {'object_name': 'BikerChangeLog'},
            'biker': ('django.db.models.fields.related.ForeignKey', [], {'to': "orm['biker.Biker']"}),
            'change_time': ('django.db.models.fields.DateTimeField', [], {'blank': 'True', 'auto_now_add': 'True'}),
            'end_time': ('django.db.models.fields.DateTimeField', [], {'blank': 'True', 'null': 'True'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'start_time': ('django.db.models.fields.DateTimeField', [], {'blank': 'True', 'null': 'True'})
        },
        'biker.category': {
            'Meta': {'object_name': 'Category'},
            'gender': ('django.db.models.fields.TextField', [], {'blank': 'True', 'null': 'True'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.TextField', [], {})
        }
    }

    complete_apps = ['biker']