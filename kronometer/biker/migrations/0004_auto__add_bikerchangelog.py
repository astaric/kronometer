# -*- coding: utf-8 -*-
from south.utils import datetime_utils as datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding model 'BikerChangeLog'
        db.create_table('biker_bikerchangelog', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('biker', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['biker.Biker'])),
            ('change_time', self.gf('django.db.models.fields.DateTimeField')(blank=True, auto_now_add=True)),
            ('start_time', self.gf('django.db.models.fields.DateTimeField')(null=True, blank=True)),
            ('end_time', self.gf('django.db.models.fields.DateTimeField')(null=True, blank=True)),
        ))
        db.send_create_signal('biker', ['BikerChangeLog'])


    def backwards(self, orm):
        # Deleting model 'BikerChangeLog'
        db.delete_table('biker_bikerchangelog')


    models = {
        'biker.biker': {
            'Meta': {'object_name': 'Biker'},
            'category': ('django.db.models.fields.related.ForeignKey', [], {'null': 'True', 'to': "orm['biker.Category']"}),
            'domestic': ('django.db.models.fields.BooleanField', [], {}),
            'end_time': ('django.db.models.fields.DateTimeField', [], {'null': 'True', 'blank': 'True'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.TextField', [], {}),
            'number': ('django.db.models.fields.IntegerField', [], {'unique': 'True'}),
            'start_time': ('django.db.models.fields.DateTimeField', [], {'null': 'True', 'blank': 'True'}),
            'surname': ('django.db.models.fields.TextField', [], {})
        },
        'biker.bikerchangelog': {
            'Meta': {'object_name': 'BikerChangeLog'},
            'biker': ('django.db.models.fields.related.ForeignKey', [], {'to': "orm['biker.Biker']"}),
            'change_time': ('django.db.models.fields.DateTimeField', [], {'blank': 'True', 'auto_now_add': 'True'}),
            'end_time': ('django.db.models.fields.DateTimeField', [], {'null': 'True', 'blank': 'True'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'start_time': ('django.db.models.fields.DateTimeField', [], {'null': 'True', 'blank': 'True'})
        },
        'biker.category': {
            'Meta': {'object_name': 'Category'},
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.TextField', [], {})
        }
    }

    complete_apps = ['biker']