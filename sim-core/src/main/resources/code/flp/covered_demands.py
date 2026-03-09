
""" calculate the station covered demands
@:param I - the set of communities(point demands)
@:param J - the set of potential sites
@:param d[i][j] - the distance from community i to potential site j
@:param h[i] - the demand level of community i
@:return N[i] - station i covered demands
"""


def set_covered_demands(I, J, d, h):
    N = {}
    # user-defined the critical coverage distance (km)
    D = 1
    for i in I:
        N[i] = []
        for j in J:
            if d[i][j] < D:
                N[i].append(j)
    return N

