from django.shortcuts import render_to_response
from django.http import HttpResponse
from httplib import HTTPConnection
from uuid import uuid4
import streamfs
import json
import os

def landing(request):
    return render_to_response('landing.html')

"""
def qrcgen(request):
    tinyurls = []
    conn = HTTPConnection("tinyurl.com")
    for i in range(int(request.GET['num'])):
        url = "http://streamfs.cs.berkeley.edu/mobile/mobile.php?qrc=" + str(uuid4())
        conn.request("GET", "/api-create.php?url=" + url)
        tinyurls.append(conn.getresponse().read())
    return render_to_response('qrcgen.html', {'tinyurls': tinyurls})
"""

def download(request):
    return render_to_response('download.html')

def newlanding(request):
    return return_to_response('newlanding.html')
# deprecated because the http req and rs will make it slow.
# newgrapher is used instead.
def grapher(req):
    try:
        rs = streamfs.getresponse('/sub')
    except Exception as e:
        return render_to_response('error.html',{'error':e})
    graphs = []
    if rs.status == 200:
        data = json.loads(rs.read())
        subs = data['children']
        for sub in subs:
            if sub == 'all':
                continue
            rs = streamfs.getresponse('/sub/'+sub)
            subdata = json.loads(rs.read())
            if subdata['destination'] == 'http://ec2-204-236-167-113.us-west-1.compute.amazonaws.com:1338':
                graphs.append(subdata['sourcePath'])
    graphs.sort()
    return render_to_response('grapher.html',{'graphs':graphs})

def newgrapher(req):
    development = []
    production = []
    for dirname, dirnames, filenames in os.walk('/opt/graphite/storage/whisper/development'):
        for filename in filenames:
            graph_name = os.path.join(dirname, filename).split('/opt/graphite/storage/whisper/development')[1][:-4]
            development.append(graph_name)
    for dirname, dirnames, filenames in os.walk('/opt/graphite/storage/whisper/production'):
        for filename in filenames:
            graph_name = os.path.join(dirname, filename).split('/opt/graphite/storage/whisper/production')[1][:-4]
            production.append(graph_name)
    development.sort()
    production.sort()
    return render_to_response('grapher.html',{'dev':development,'pro':production})
def graph(req, path):
    start = "-3h"
    unit = ''
    if 'energy' in path:
        unit = 'Energy(mWh)'
    elif 'power' in path:
        unit = 'Power(mW)'
    if 'start' in req.GET:
        start = req.GET['start']
    graph_path = path
    if graph_path.find("development") == 0:
        graph_path = graph_path[12:]
    elif graph_path.find("production") == 0:
        graph_path = graph_path[11:]
    return render_to_response('graph.html',{'graph_path':'/'+graph_path,'graph_name':path.replace('/','.'),'alias':'/'+graph_path,'start':start,'unit':unit})

def qrcgen(request):
    if request.method == 'GET' and 'num' in request.GET:
        n=request.GET['num']
        if n:
            tinyurls = []
            conn = HTTPConnection("tinyurl.com")
            for i in range(int(request.GET['num'])):
              url = "http://streamfs.cs.berkeley.edu/mobile/mobile.php?qrc=" + str(uuid4())
              conn.request("GET", "/api-create.php?url=" + url)
              tinyurls.append(conn.getresponse().read())
            return render_to_response('qrcgen.html', {'tinyurls': tinyurls})
        else:
             return render_to_response('qrcgen.html')
    else:
        return render_to_response('qrcgen.html')


