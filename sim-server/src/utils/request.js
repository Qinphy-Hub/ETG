import axios from 'axios';
import { useTokenStore } from "@/stores/token.js";
import { ElMessage } from "element-plus";
import router from "@/router";

const baseURL = '/api';
const instance = axios.create({baseURL})

// 请求拦截器
instance.interceptors.request.use(
    (config) => {
        // 配置token
        let tokenStore = useTokenStore();
        if (tokenStore.token) {
            config.headers.Authorization = tokenStore.token;
        }
        return config;
    },
    (err) => {
        return Promise.reject(err);
    }
)

// 相应拦截器
instance.interceptors.response.use(
    (result)=>{
        if (result.data.code === undefined || result.data.code === 0) {
            return result.data;
        }
        ElMessage.error(result.data.message || "Server Error");
        return Promise.reject(result.data);
    },
    (err)=>{
        // 权限拦截，提示登录
        console.log(err)
        if (err.response.status === 401) {
            router.push('/').then((r) => {
                // @ts-ignore
                ElMessage.error(r.message);
            });
            ElMessage.warning("Please login.")
        }
        else ElMessage.error("Server error!")
        return Promise.reject(err);
    }
)

export default instance;
