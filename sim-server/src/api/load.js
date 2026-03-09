import request from "@/utils/request.js"


// upload any data
export const uploadDataService = (id, fileType, file) => {
    const formData = new FormData()
    formData.append("id", id)
    formData.append("fileType", fileType)
    formData.append("file", file)
    return request.patch('/load/files', formData)
}


// ------------------------------- get traffic network data -----------------------------
// download traffic data from osm
export const downloadFromOSM = (downData) => {
    const params = new URLSearchParams()
    for (const key in downData) {
        params.append(key, downData[key])
    }
    return request.post("/load/osm", params)
}
export const getNodesData = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/nodes", params)
}
export const getEdgesData = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/edges", params)
}
export const saveTrafficNetwork = (data) => {
    const params = new URLSearchParams()
    for (const key in data) {
        params.append(key, data[key])
    }
    return request.post("/load/save-traffic", params)
}
export const deleteTrafficNetwork = (projectId) => {
    return request.delete(`/load/delete-traffic/${projectId}`)
}
// ------------------------------- get traffic network data -----------------------------


// ------------------------------ set stations information data -------------------------
export const getStationsData = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/stations", params)
}
export const saveStationsData = (data) => {
    const params = new URLSearchParams()
    for (const key in data) {
        params.append(key, data[key])
    }
    return request.post("/load/save-station", params)
}
export const deleteStationsData = (projectId) => {
    return request.delete(`/load/delete-stations/${projectId}`)
}
// ------------------------------ set stations information data -------------------------


// ------------------------------ set traffic flow data ---------------------------------
export const getTrafficFlowData = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/traffic-flow", params)
}
export const saveTrafficFlowData = (data) => {
    const params = new URLSearchParams()
    for (const key in data) {
        params.append(key, data[key])
    }
    return request.post("/load/save-traffic-flow", params)
}
export const deleteTrafficFlowData = (projectId) => {
    return request.delete(`/load/delete-traffic-flow/${projectId}`)
}
// ----------------------------- set traffic flow data -----------------------------------


// ----------------------- set station's potential site data  ----------------------------
export const getSitesData = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/sites", params)
}
export const saveSitesData = (data) => {
    const params = new URLSearchParams()
    for (const key in data) {
        params.append(key, data[key])
    }
    return request.post("/load/save-site", params)
}
export const deleteSitesData = (projectId) => {
    return request.delete(`/load/delete-site/${projectId}`)
}
// ----------------------- set station's potential site data  ----------------------------


// ---------------------------- set point demands data  ---------------------------------
export const getPointDemandsData = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/point-demands", params)
}
export const savePointDemandsData = (data) => {
    const params = new URLSearchParams()
    for (const key in data) {
        params.append(key, data[key])
    }
    return request.post("/load/save-pointDemand", params)
}
export const deletePointDemandsData = (projectId) => {
    return request.delete(`/load/delete-pointDemand/${projectId}`)
}
// ---------------------------- set point demands data  ---------------------------------


// -------------------------- set od pairs demands data  --------------------------------
export const getODPairsData = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/od-pairs", params)
}
export const saveODDemandsData = (data) => {
    const params = new URLSearchParams()
    for (const key in data) {
        params.append(key, data[key])
    }
    return request.post("/load/save-odDemand", params)
}
export const deleteODDemandsData = (projectId) => {
    return request.delete(`/load/delete-odDemand/${projectId}`)
}
// -------------------------- set od pairs demands data  --------------------------------


// ------------------------------ set stations information data -------------------------
export const getRealDemandsData = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/real-demands", params)
}
export const saveRealDemandsData = (data) => {
    const params = new URLSearchParams()
    for (const key in data) {
        params.append(key, data[key])
    }
    return request.post("/load/save-real-demands", params)
}
export const deleteRealDemandsData = (projectId) => {
    return request.delete(`/load/delete-real-demands/${projectId}`)
}
// ------------------------------ set stations information data -------------------------


// ------------------------------- get and set power grid data --------------------------
export const getDevicesData = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/devices", params)
}
export const saveDevicesData = (data) => {
    const params = new URLSearchParams()
    for (const key in data) {
        params.append(key, data[key])
    }
    return request.post("/load/save-device", params)
}
export const deleteDevicesData = (projectId) => {
    return request.delete(`/load/delete-device/${projectId}`)
}
// ------------------------------- get and set power grid data --------------------------











/** get data from backside
 * getTrafficNetwork: get traffic network topology data from file system.
 */



export const getPowerFlowData = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/power-flow", params)
}

export const getStartEnd = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/start-end", params)
}

export const getAllData = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/all-data", params)
}

export const getSimRet = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/load/sim-result", params)
}

/** save data into backside file system
 * saveTrafficNetwork
 */



export const saveStartEnd = (data) => {
    const params = new URLSearchParams()
    for (const key in data) {
        params.append(key, data[key])
    }
    return request.post("/load/save-start-end", params)
}

/** delete data from backside file system **/



export const deleteStartEnd = (projectId) => {
    return request.delete(`/load/delete-start-end/${projectId}`)
}





