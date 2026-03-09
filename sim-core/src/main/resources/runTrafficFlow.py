
import json

# get data from file system
with open("../data/traffic-flow.json", 'r') as f:
    flowDict = json.load(f)

for edge in flowDict:
    arr = flowDict[edge]["properties"]["data"]
    flowDict[edge]["properties"]["data"] = modifyTrafficFlowFromEdge(arr)

print(json.dumps(flowDict))
