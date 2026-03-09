
import copy
import json
import math
import argparse
import geopy
from geopy.distance import geodesic
from flowSpeed import get_vehicle_speed

# 数据处理的相关方法
def get_adj_matrix(nodes, edges):
    # initial
    net = {}
    for n in nodes:
        net[n] = {}
    # calc
    for edge in edges:
        p1 = edges[edge]["source"]
        p2 = edges[edge]["target"]
        net[p1][p2] = edge
    return net

def get_flow_slice(timer, flows):
    flow_slice = {}
    for key in flows:
        flow_slice[key] = float(flows[key]["properties"]["data"][timer])
    return flow_slice

def get_length_of_lines(remain_lines):
    length = 0
    for i in range(len(remain_lines) - 1):
        length += geodesic(remain_lines[i], remain_lines[i + 1]).kilometers
    return length

def get_position(p1, p2, l):
    lat1 = float(p1[0]) * math.pi / 180
    lng1 = float(p1[1]) * math.pi / 180
    lat2 = float(p2[0]) * math.pi / 180
    lng2 = float(p2[1]) * math.pi / 180
    y = math.sin(lng2 - lng1) * math.cos(lat2)
    x = math.cos(lat1) * math.sin(lat2) - math.sin(lat1) * math.cos(lat2) * math.cos(lng2 - lng1)
    bearing = float((math.atan2(y, x) / math.pi * 180 + 360.0) % 360)
    start = geopy.Point(p1[0], p1[1])
    p_aim = geodesic(kilometers=l).destination(start, bearing)
    return [p_aim.latitude, p_aim.longitude]

def get_positions_of_one_edge(time, speed, edge):
    edge_lines = edge["polyline"]
    lines = []
    remain_lines = []
    for i in range(len(edge_lines) - 1):
        length = get_length_of_lines([edge_lines[i], edge_lines[i + 1]])
        if (length / speed * 3600) <= time:
            lines.append(edge_lines[i])
            time -= (length / speed * 3600)
        else:
            l = speed * time / 3600
            p = get_position(edge_lines[i], edge_lines[i + 1], l)
            lines.append(p)
            remain_lines.append(p)
            remain_lines += edge_lines[i + 1:]
            return lines, remain_lines

def get_one_line_posistion(remain_lines, remain_speed, timer):
    lines = []
    t = 0
    lines.append(remain_lines[0])
    remain_lines = remain_lines[1:]
    while timer - t > 0:
        t = get_length_of_lines([lines[-1], remain_lines[0]]) / remain_speed * 3600
        if timer - t < 0:
            break
        else:
            lines.append(remain_lines[0])
            remain_lines = remain_lines[1:]
            timer -= t
    l = remain_speed * timer / 3600
    p = get_position(lines[-1], remain_lines[0], l)
    lines.append(p)
    remain_lines = [p] + remain_lines
    return remain_lines, lines


parser = argparse.ArgumentParser()
parser.add_argument('-t', '--time', default='0', type=int, help='all simulation times')
parser.add_argument('-g', '--granularity', default='1', type=int, help='granularity')
args = parser.parse_args()

with open("./data/start-end.json", 'r') as f:
    positions = json.load(f)

all_time = args.time
granularity = args.granularity
start_n = positions[list(positions.keys())[0]]["O"]
end_n = positions[list(positions.keys())[0]]["D"]

# 预备数据
with open("./data/nodes.json", 'r') as f:
    nodes = json.load(f)
with open("./data/edges.json", 'r') as f:
    edges = json.load(f)
with open("./data/traffic-flow.json", 'r') as f:
    flows = json.load(f)

for key in edges:
    lines = []
    for f in edges[key]["polyline"]:
        lines.append([float(f[0]), float(f[1])])
    edges[key]["polyline"] = lines

for key in flows:
    data = []
    for f in flows[key]["properties"]["data"]:
        data.append(float(f))
    flows[key]["properties"]["data"] = data

adj_matrix = get_adj_matrix(nodes, edges)

# 计算每一秒钟，决策的路径和汽车实际经过的路径
ret = {
    "route": [],
    "lines": []
}
route = [start_n, end_n]
remain_lines = []
remain_speed = 60
for t in range(all_time):
    # 计算t时间的交通流数据的切片
    flow_slice = get_flow_slice(int(t / granularity), flows)
    # 计算当前t对应的决策路径
    if len(route) < 2:
        break
    route = get_default_path(route[0], route[-1], edges, adj_matrix, flow_slice)
    remain_time = (get_length_of_lines(remain_lines) / remain_speed * 3600)
    if remain_time >= 1:
        x, y = get_one_line_posistion(remain_lines, remain_speed, 1)
        remain_lines = x
        ret["lines"].append(y)
        ret["route"].append(route)
    else:
        tmp_lines = []
        T = 1 - remain_time
        while (get_length_of_lines(tmp_lines) / remain_speed * 3600) < T:
            remain_lines += tmp_lines
            T -= (get_length_of_lines(tmp_lines) / remain_speed * 3600)
            if len(route) < 2:
                break
            route = get_default_path(route[0], route[-1], edges, adj_matrix, flow_slice)
            edge = edges[adj_matrix[route[0]][route[1]]]
            tmp_lines = edge["polyline"]
            route = route[1:]
            remain_speed = get_vehicle_speed(edge, flow_slice)
        x, y = get_one_line_posistion(tmp_lines, remain_speed, T)
        remain_lines += y
        ret["lines"].append(remain_lines)
        remain_lines = x
        ret["route"].append(route)

print(json.dumps(ret))
