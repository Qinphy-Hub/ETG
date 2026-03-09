""" set vehicle speed by edge's properties and traffic flow
@:param edge - the default properties are: edge["length", "source", "target", ...]
             - the user defined properties in edge["properties"]
@:param flow(double:0~1) - the edge's traffic flow at one time
@:return vehicle speed at one time
@note we strongly sugest user to set max_speed property into every edge.
"""

# default speed model is BPR
def get_vehicle_speed(edge, flow):
    L = edge["length"]
    # v_0 = edge["max_speed"]
    v_0 = 60
    alpha = 0.15
    beta = 4.0
    return L / v_0 * (1 + alpha * (flow ** beta))
