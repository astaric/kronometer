# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding model 'Category'
        db.create_table(u'biker_category', (
            (u'id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('name', self.gf('django.db.models.fields.TextField')()),
        ))
        db.send_create_signal(u'biker', ['Category'])

        # Adding model 'Biker'
        db.create_table(u'biker_biker', (
            (u'id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('number', self.gf('django.db.models.fields.IntegerField')(unique=True)),
            ('name', self.gf('django.db.models.fields.TextField')()),
            ('surname', self.gf('django.db.models.fields.TextField')()),
            ('category', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['biker.Category'], null=True)),
            ('start_time', self.gf('django.db.models.fields.DateTimeField')(null=True)),
            ('end_time', self.gf('django.db.models.fields.DateTimeField')(null=True)),
        ))
        db.send_create_signal(u'biker', ['Biker'])


    def backwards(self, orm):
        # Deleting model 'Category'
        db.delete_table(u'biker_category')

        # Deleting model 'Biker'
        db.delete_table(u'biker_biker')


    models = {
        u'biker.biker': {
            'Meta': {'object_name': 'Biker'},
            'category': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['biker.Category']", 'null': 'True'}),
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