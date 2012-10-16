from django.conf.urls import patterns, include, url
# Uncomment the next two lines to enable the admin:
# from django.contrib import admin
# admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'django_site.views.home', name='home'),
    # url(r'^django_site/', include('django_site.foo.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    # url(r'^admin/', include(admin.site.urls)),
    url(r'^$', 'energylens.views.landing'),
    url(r'^download$', 'energylens.views.download'),
    url(r'^qrcgen$', 'energylens.views.qrcgen'),
    url(r'^grapher$', 'energylens.views.newgrapher'),
    #url(r'^graphite$', 'energylens.views.grapher2'),
    url(r'^grapher/(.+)/$', 'energylens.views.graph'),
)
