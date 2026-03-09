# (terminal) pip install pulp
import pulp

""" data descriptions
@:param I - the set of communities(point demands)
@:param J - the set of potential sites
@:param d[i][j] - the distance from community i to potential site j
@:param N[i] - potential site i covered communities.
@:param h[i] - the demand level of community i
@:return the established (charging/swapping) stations:
stations = {
    key(in set I): {
        "type": "charging/swapping",
        "data": [   # optional
            {
                "name": string,
                "power": double,
                "num": integer
            },
        ]
    },
}
"""
# you can use nodes, devices, lines, demands data directly


def cslp_solver(I, J, d, h, N):
    sclp = pulp.LpProblem("SCLP", sense=pulp.LpMinimize)
    var_x = {}
    for x in J:
        var_x[x] = pulp.LpVariable(x, cat="Binary")
    sclp += pulp.lpSum(var_x[x] for x in J)
    for i in N.keys():
        if len(N[i]) != 0:
            sclp += pulp.lpSum(var_x[x] for x in N[i]) >= 1
    sclp.solve(pulp.PULP_CBC_CMD(msg=False))
    stations = {}
    for v in sclp.variables():
        if v.varValue == 1 and v.name in J:
            stations[v.name] = {"type": "charging", "data": [{"name": "type1", "power": 200, "num": 10}]}
    return stations

