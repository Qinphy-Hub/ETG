""" calculate the traffic flow at one time
@:param flow(double:0~1) - the edge's traffic flow at one time.
@:param edge - the default properties are: edge["length", "source", "target", ...]
             - the user defined properties in edge["properties"]
@:param vehicles(integer) - the number of new cars on that edge.
@:return the next time's traffic flow.
@note we strongly sugest add the property capacity of the road.
"""


# default model is BPR
def get_traffic_flow(flow, edge, vehicles):
    L = edge["length"]
    # capacity = edge["capacity"]
    capacity = L * 3 / 5
    return flow + vehicles / capacity

