import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import router from '@/router'
import { createPinia } from "pinia";
import { createPersistedState } from "pinia-persistedstate-plugin";
import 'element-plus/dist/index.css'
import VNetworkGraph from "v-network-graph";
import "v-network-graph/lib/style.css"
import './style.css'
import App from './App.vue'
import * as echarts from 'echarts'
import Echarts from 'vue-echarts'


const pinia = createPinia()
const persist = createPersistedState()
pinia.use(persist)

const app = createApp(App)
app.component('v-chart', Echarts)
app.config.globalProperties.$echarts = echarts
app.use(ElementPlus)
    .use(router)
    .use(VNetworkGraph)
    .use(pinia)
    .mount('#app')
