import request from "@/utils/request.js"


// simulation
export const simulation = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/simulation', params)
}


// set initial program data
export const initialScheduling = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/init/scheduling', params)
}


// get exists program data

export const getScheduling = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/scheduling', params)
}


// modify traffic flow data

export const modifyScheduling = (projectId, code) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("code", code)
    return request.post('/program/save/scheduling', params)
}

// run program
export const runTrafficFlow = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/run/traffic-flow', params)
}

export const runScheduling = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/run/scheduling', params)
}

export const runShortest = (projectId, start, end) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("start", start)
    params.append("end", end)
    return request.post('/program/run/shortest-path', params)
}






// set relation between traffic flow and vehicle speed program




// set user vehicle routes
export const getUserRoutes = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/getUserRoutes', params)
}

export const saveUserRoutes = (projectId, code) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("code", code)
    return request.post('/program/saveUserRoutes', params)
}

export const initialUserRoutes = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/initialUserRoutes', params)
}

// give some noise into the original traffic flow data
export const getNoiseFlowCode = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/code-noise-flow', params)
}

export const saveNoiseFlowCode = (projectId, code) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("code", code)
    return request.post('/program/save-noise-flow', params)
}

export const initialNoiseFlowCode = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/init-noise-flow', params)
}
// --------------------------------------- Program Manage ----------------------------------------
// 1. One Car dispatching Program
export const routeProgramCreateService = (projectId, createData) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    for (const key in createData) {
        params.append(key, createData[key])
    }
    return request.post('/program/route-create', params)
}
export const routeProgramListService = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/program/route-list", params)
}
export const routeProgramUpdateService = (updateData) => {
    const params = new URLSearchParams()
    for (const key in updateData) {
        params.append(key, updateData[key])
    }
    return request.patch('/program/route-update', params)
}
export const routeProgramDeleteService = (id) => {
    return request.delete(`/program/route/${id}`)
}

// 2. FLP Program
export const flpProgramCreateService = (projectId, createData) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    for (const key in createData) {
        params.append(key, createData[key])
    }
    return request.post('/program/flp-create', params)
}
export const flpProgramListService = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post("/program/flp-list", params)
}
export const flpProgramUpdateService = (updateData) => {
    const params = new URLSearchParams()
    for (const key in updateData) {
        params.append(key, updateData[key])
    }
    return request.patch('/program/flp-update', params)
}
export const flpProgramDeleteService = (id) => {
    return request.delete(`/program/flp/${id}`)
}
// ---------------------------------------- Program Manager -----------------------------------------



// ---------------------------------------- Program Editor ------------------------------------------
// 1. one car scheduling program
export const getRouteCode = (projectId, programId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("programId", programId)
    return request.post('/program/code-route', params)
}
export const saveRouteCode = (projectId, programId, code) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("code", code)
    params.append("programId", programId)
    return request.post('/program/save-route', params)
}
export const initialRouteCode = (projectId, programId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("programId", programId)
    return request.post('/program/init-route', params)
}

// 2. flp program
export const getFlpCode = (projectId, programId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("programId", programId)
    return request.post('/program/code-flp', params)
}
export const saveFlpCode = (projectId, programId, code) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("code", code)
    params.append("programId", programId)
    return request.post('/program/save-flp', params)
}
export const initialFlpCode = (projectId, programId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("programId", programId)
    return request.post('/program/init-flp', params)
}

// 3. flp program
export const getCoveredDemandsCode = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/code-covered-demands', params)
}
export const saveCoveredDemandsCode = (projectId, code) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("code", code)
    return request.post('/program/save-covered-demands', params)
}
export const initialCoveredDemandsCode = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/init-covered-demands', params)
}

// 4. vehicle speed calculate
export const getFlowSpeedCode = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/code-speed', params)
}
export const saveFlowSpeedCode = (projectId, code) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("code", code)
    return request.post('/program/save-speed', params)
}
export const initialFlowSpeedCode = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/init-speed', params)
}

// 5. set the algorithm of traffic flow calculate
export const getSetTrafficFlowCode = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/code-flow', params)
}
export const saveSetTrafficFlowCode = (projectId, code) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("code", code)
    return request.post('/program/save-flow', params)
}
export const initialSetTrafficFlowCode = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/init-flow', params)
}

// 6. give some noise into the original traffic flow data
export const getModifyTrafficFlow = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/code-noise-flow', params)
}

export const saveModifyTrafficFlow = (projectId, code) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("code", code)
    return request.post('/program/save-noise-flow', params)
}

export const initialModifyTrafficFlow = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/init-noise-flow', params)
}

// 7. set distribution
export const getDistribution = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/code-distribution', params)
}

export const saveDistribution = (projectId, code) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    params.append("code", code)
    return request.post('/program/save-distribution', params)
}

export const initialDistribution = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/init-distribution', params)
}
// ---------------------------------------- Program Editor ------------------------------------------


// ----------------------------------------- Calc Result --------------------------------------------
export const getFlpResult = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/program/calc/flp', params)
}
export const getFlpHistory = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/load/res/flp', params)
}
export const getRouteRes = (projectId) => {
    const params = new URLSearchParams()
    params.append("projectId", projectId)
    return request.post('/load/res/route', params)
}
