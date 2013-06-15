# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding field 'Biker.domestic'
        db.add_column(u'biker_biker', 'domestic',
                      self.gf('django.db.models.fields.BooleanField')(default=False),
                      keep_default=False)


        # Changing field 'Biker.category'
        db.alter_column(u'biker_biker', 'category_id', self.gf('django.db.models.fields.related.ForeignKey')(default=False, to=orm['biker.Category']))

    def backwards(self, orm):
        # Deleting field 'Biker.domestic'
        db.delete_column(u'biker_biker', 'domestic')


        # Changing field 'Biker.category'
        db.alter_column(u'biker_biker', 'category_id', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['biker.Category'], null=True))

    models = {
        u'biker.biker': {
            'Meta': {'object_name': 'Biker'},
            'category': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['biker.Category']"}),
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