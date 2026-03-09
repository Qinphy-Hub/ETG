import numpy as np

""" set distribution of every real demand node
@:param properties (dict) - the parameters of ditribution, which set by user.
@:param size (Integer) - the demand point vehicle's quantity.
@:param granularity (Ineger) - time granularity.
@:param time_len (Integer) - the length of data.
@:return vehicles departure schedule (dict) - {time: num}
"""

def get_distribution_data(properties, size, granularity, time_len):
    data = np.random.normal(properties["mu"], properties["sigma"], size)
    time_list = {}
    for t in range(0, time_len, granularity):
        cnt = 0
        for e in data:
            if t <= e < t + granularity:
                cnt += 1
        time_list[t + 1] = cnt
    return time_list
