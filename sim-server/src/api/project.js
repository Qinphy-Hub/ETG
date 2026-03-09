import request from "@/utils/request.js"
import {par} from "colormap/colorScale";

export const projectListService = () => {
    return request.get("/project/list")
}

export const projectCreateService = (createData) => {
    const params = new URLSearchParams()
    for (const key in createData) {
        params.append(key, createData[key])
    }
    return request.post('/project/create', params)
}

export const projectUpdateService = (updateData) => {
    const params = new URLSearchParams()
    for (const key in updateData) {
        params.append(key, updateData[key])
    }
    return request.patch('/project/update', params)
}

export const projectDeleteService = (id) => {
    return request.delete(`/project/${id}`)
}

export const getProjectService = (id) => {
    return request.get(`/project/info/${id}`)
}

export const getProgramService = (id) => {
    return request.get(`/program/info/${id}`)
}

export const centerService = (id, lat, lng) => {
    const params = new URLSearchParams()
    params.append("id", id)
    params.append("latitude", lat)
    params.append("longitude", lng)
    return request.patch("/project/coordinate", params)
}

export const timerService = (id, granularity, timeUnit, timeLen) => {
    const params = new URLSearchParams()
    params.append("id", id)
    params.append("granularity", granularity)
    params.append("timeUnit", timeUnit)
    params.append("timeLen", timeLen)
    return request.patch("/project/timer", params)
}