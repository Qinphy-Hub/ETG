import request from "@/utils/request.js";

export const registerService = (registerData) => {
    const params = new URLSearchParams()
    for (const key in registerData) {
        params.append(key, registerData[key])
    }
    return request.post('/user/register', params)
}

export const loginService = (loginData) => {
    const params = new URLSearchParams()
    for (const key in loginData) {
        params.append(key, loginData[key])
    }
    return request.post('/user/login', params)
}

export const verificationService = (email) => {
    const params = new URLSearchParams()
    params.append("email", email)
    return request.post('/user/verification-code', params)
}

export const forgotPasswordService = (email) => {
    const params = new URLSearchParams()
    params.append("email", email)
    return request.post('/user/forget-password', params)
}

export const getUserService = () => {
    return request.get('/user/info')
}

export const usernameService = (username) => {
    const params = new URLSearchParams()
    params.append("username", username)
    return request.patch('/user/modify-username', params)
}

export const passwordService = (pswData) => {
    return request.patch('/user/modify-password', pswData)
}

export const uploadAvatarService = (file) => {
    const formData = new FormData()
    formData.append("file", file)
    return request.patch('/user/upload-avatar', formData)
}

export const getAvatarFile = (url) => {
    return request.get(url, {responseType: 'arraybuffer'})
}

export const logoutService = () => {
    return request.get('/user/logout')
}

export const deleteUserService = (id) => {
    return request.delete(`/user/${id}`)
}

export const getUserListService = () => {
    return request.get('/user/list')
}
