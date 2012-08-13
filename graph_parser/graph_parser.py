import pydot
import streamfs
import json
import argparse

parser = argparse.ArgumentParser(description="parse streamfs files into dot file graph representation.")
parser.add_argument('-s','--server',default='is4server.com:8083')
parser.add_argument('-o','--output',default='output.dot')
parser.add_argument('path',metavar='PATH', type=str)
args = parser.parse_args()

PATH = args.path
HOST = args.server
OUTPUT = args.output

streamfs.HOST = HOST
graph = pydot.Dot(graph_type='graph')

def parse_sfs(root,path):
  print path
  res = streamfs.getresponse(path)
  if res.status == 200:
    data = json.loads(res.read())
    if not 'children' in data:
      return
    children = data['children']
    for child in children:
      name = path+'/'+child
      if ' -> ' in child:
        name = name.split(' -> ')[1]
      node = graph.get_node('"'+name+'"')
      if not node:
        node = pydot.Node(name)
        graph.add_node(node)
        parse_sfs(node,name)
      else:
        node = node[0]
      graph.add_edge(pydot.Edge(root,node))

root = pydot.Node(PATH)
graph.add_node(root)
parse_sfs(root,PATH)
graph.write(OUTPUT)
graph.write_jpg(OUTPUT+".jpg",prog='fdp')
