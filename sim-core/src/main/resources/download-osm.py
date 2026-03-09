import argparse
import osmnx as ox
import json

parser = argparse.ArgumentParser()
parser.add_argument('-n', '--north', default='31.95', type=float, help='print the north/top latitude.')
parser.add_argument('-s', '--south', default='31.85', type=float, help='print the south/bottom latitude')
parser.add_argument('-e', '--east', default='117.65', type=float, help='print the east/right longitude')
parser.add_argument('-w', '--west', default='117.50', type=float, help='print the west/left longitude')
parser.add_argument('-t', '--type', default='all', type=str, help='choose map type')

args = parser.parse_args()

# 1.9.0
# bbox = args.north, args.south, args.east, args.west
# 2.0.0b2
bbox = args.east, args.south, args.west, args.north

data_type = args.type
if args.type == "highway":
    data_type = "all"
G = ox.graph_from_bbox(bbox=bbox, network_type=data_type)

Nodes = ox.graph_to_gdfs(G, edges=False)
Edges = ox.graph_to_gdfs(G, nodes=False)
if args.type == "highway":
    Edges = Edges["highway"]

# print(Nodes.to_json())
# print(Edges.to_json())

# simplified data
simple_nodes = {}
for idx in Nodes.index:
    node = Nodes.loc[idx]
    name = str(idx)
    simple_nodes[name] = {
        "point": [str(node.y), str(node.x)]
    }

simple_edges = {}
for idx in Edges.index:
    edge = Edges.loc[idx]
    y, x = edge.geometry.xy
    coordinates = []
    for j in range(len(x)):
        coordinates.append([str(x[j]), str(y[j])])
    e_dic = {
        "source": str(idx[0]),
        "target": str(idx[1]),
        "length": str(edge.length),
        "polyline": coordinates
    }
    name = "e" + str(idx[0]) + "To" + str(idx[1])
    simple_edges[name] = e_dic

print(json.dumps(simple_nodes))
print(json.dumps(simple_edges))
