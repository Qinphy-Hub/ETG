import request from "@/utils/request.js"


// AC Lines
export const getAclineList = () => {
    return request.post("/grid/aclines")
}

export const getAcline = (id) => {
    return request.post(`/grid/acline/${id}`)
}


// DC Lines
export const getDclineList = () => {
    return request.post("/grid/dclines")
}

export const getDcline = (id) => {
    return request.post(`/grid/dcline/${id}`)
}


// Transformers
export const getTransformerList = () => {
    return request.post("/grid/transformers")
}

export const getTransformer = (id) => {
    return request.post(`/grid/transformer/${id}`)
}


// Transformers3
export const getTransformer3List = () => {
    return request.post("/grid/transformers3")
}

export const getTransformer3 = (id) => {
    return request.post(`/grid/transformer3/${id}`)
}