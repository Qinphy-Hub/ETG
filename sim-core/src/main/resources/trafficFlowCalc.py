""" calc traffic flow
@param: lanes - the number of lanes.
@param: length - the length of the road.
@param: vehicles - the number of vehicles which running on the road.
"""


def calc_traffic_flow(lanes, length, vehicles):
    capacity = lanes * length / 2.0
    return vehicles / capacity * 10
