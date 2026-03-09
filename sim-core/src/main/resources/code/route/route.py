"""
start_n: start node
end_n: end node
adj_matrix: adjacent matrix of traffic network
flow_slice: every edge's traffic flow at one time
"""
import heapq


def get_default_path(start_n, end_n, adj_matrix, flow_slice, edges):
    distance = {vertex: float('infinity') for vertex in adj_matrix}
    distance[start_n] = 0
    parent = {vertex: None for vertex in adj_matrix}
    priority_queue = [(0, start_n)]
    while priority_queue:
        current_distance, current_vertex = heapq.heappop(priority_queue)
        if current_distance > distance[current_vertex]:
            continue
        for neighbor in adj_matrix[current_vertex]:
            edge = adj_matrix[current_vertex][neighbor]
            dist = current_distance + flow_slice[edge]
            if dist < distance[neighbor]:
                distance[neighbor] = dist
                parent[neighbor] = current_vertex
                heapq.heappush(priority_queue, (dist, neighbor))
    paths = []
    current = end_n
    while current is not None:
        paths.append(current)
        current = parent[current]
    paths.reverse()
    return paths
