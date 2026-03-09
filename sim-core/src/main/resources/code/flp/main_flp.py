import json
import argparse
import os

# pip install geopy
from geopy.distance import geodesic

# user-defined algorithm
from covered_demands_0 import *


# ================================== Get Args ===================================
parser = argparse.ArgumentParser()
parser.add_argument('-i', '--id', default='1', type=int, help='print the algorithm id.')
args = parser.parse_args()

algorithm_id = args.id
# ================================== Get Args ===================================


# ==================================== Tools ====================================
""" get the distance between two coordinate (km)
@:param p1 [lattitude, longitude]
@:param p2 [lattitude, longitude]
"""
def get_distance(p1, p2):
    return geodesic(p1, p2).kilometers
# ==================================== Tools ====================================


# ================================= Inital data =================================
""" read data
@param: node - the potential site of charging stations
@param: demands - communities which need charging stations
"""
if os.path.exists("../data/nodes.json"):
    with open("../data/nodes.json") as file:
        nodes = json.load(file)
if os.path.exists("../data/point-demands.json"):
    with open("../data/point-demands.json") as file:
        demands = json.load(file)
if os.path.exists("../data/devices.json"):
    with open("../data/devices.json") as file:
        devices = json.load(file)
if os.path.exists("../data/lines.json"):
    with open("../data/lines.json") as file:
        lines = json.load(file)
if os.path.exists("../data/sites.json"):
    with open("../data/sites.json") as file:
        sites = json.load(file)


""" data preprocess
@param: I - the set of communities
@param: J - the set of potential charging stations site
@param: d[i][j] - the map of distance from community i to charging station j
@param: h[i] - the demand level of community i
"""
I = []
J = []
d = {}
h = {}
# potential sites set
for j in sites.keys():
    J.append(j)
# demands set
for i in demands.keys():
    I.append(i)
    h[i] = float(demands[i]["value"])
    d[i] = {}
    p1 = demands[i]["point"]
    for j in J:
        p2 = nodes[j]["point"]
        l = get_distance(p1, p2)
        d[i][j] = l
# ================================= Inital data =================================


# ================================= User-defined ================================
""" define the covered demand of a station
@:return N[i] - the set of charging stations i covered demands
"""
N = set_covered_demands(I, J, d, h)

""" user-defined the FLP solver
@:return stations - the set of stations
"""
stations = cslp_solver(I, J, d, h, N)
# ================================= User-defined ================================


# ================================ Result process ===============================
for i in stations.keys():
    stations[i]["point"] = nodes[i]["point"]

with open("../result/stations_" + str(algorithm_id) + ".json", 'w') as f:
    json.dump(stations, f)

print(json.dumps(stations))
