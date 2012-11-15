#wrapper functions for streamFS by Yongwoo Noh
import httplib
import json
import time

HOST = "ec2-184-169-204-224.us-west-1.compute.amazonaws.com:8080"
ROOT = ""

def getresponse(path=""):
  conn = httplib.HTTPConnection(HOST)
  conn.request("GET",ROOT+path)
  res = conn.getresponse()
  conn.close()
  return res

def putrequest(data,path=""):
  conn = httplib.HTTPConnection(HOST)
  conn.request("PUT",ROOT+path,data)
  res = conn.getresponse()
  conn.close()
  return res

def deleterequest(path):
  conn = httplib.HTTPConnection(HOST)
  conn.request("DELETE",path)
  res = conn.getresponse()
  conn.close()
  return res

def create_resource(name,rtype="default",path=""):
  data = json.dumps({"operation":"create_resource","resourceName":name,"resourceType":rtype})
  return putrequest(data,path)

def create_stream(name,path=""):
  data = json.dumps({"operation":"create_generic_publisher","resourceName":name})
  return putrequest(data,path)

def push_data(value,pubid,path):
  data = json.dumps({"value":value})
  return putrequest(data,path+"?type=generic&pubid="+str(pubid))

def ts_query(query,path):
  return getresponse(path+"?query=true=&ts_timestamp="+str(query))

def create_symlink(uri,name,path=""):
  data = json.dumps({"operation":"create_symlink","uri":ROOT+uri,"name":name})
  return putrequest(data,path)

def move_resource(src,dst,path=""):
  data = json.dumps({"operation":"move","src":src,"dst":dst})
  return putrequest(data,path)

############################################
# functions for testing ####################
############################################

import random
import time

def feed_random(path,period):
  rs = getresponse(path)
  if rs == "":
    print "path not found"
    return
  rs = json.loads(rs.read())
  pubid = rs["pubid"]
  while True:
    push_data(random.random(),pubid,path)  
    time.sleep(period)
