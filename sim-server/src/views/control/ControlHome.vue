<script setup lang="ts">
import {
  Delete,
  Van,
  MapLocation,
  UploadFilled,
  Guide,
  Download,
  Plus,
  VideoPlay,
  VideoPause,
  Back,
  EditPen,
  Promotion
} from "@element-plus/icons-vue";
import {onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {layerGroup} from "leaflet";
import {ElLoading, ElMessage, type UploadRequestOptions} from "element-plus";
import {getAllData, uploadDataService} from "@/api/load";
import {getProjectService} from "@/api/project";
import * as L from "leaflet";
// icons
import chargingImg from "@/assets/charging-station.svg"
import swappingImg from "@/assets/swapping-station.svg"
import transformerImg from "@/assets/transformer.svg"
import loadImg from "@/assets/load.svg"
import sourceImg from "@/assets/source.svg"
import solarImg from "@/assets/solar.svg"
import windImg from "@/assets/wind.svg"
import gridImg from "@/assets/grid.svg"
import busImg from "@/assets/bus.svg"
import startImg from "@/assets/start_point.svg"
import endImg from "@/assets/end_point.svg"
import colormap from "colormap";
import {runScheduling, runTrafficFlow, runShortest} from "@/api/program";

// base data
const route = useRoute();
const router = useRouter();
const projectId = route.query.projectId
const colors = colormap({
  colormap: 'autumn',
  nshades: 100,
  format: 'hex',
  alpha: 1
})

// icons
const getIcon = (url: string) => {
  return L.icon({iconUrl: url, iconSize: [38, 38], iconAnchor: [19, 19]})
}
const chargingIcon = getIcon(chargingImg)
const swappingIcon = getIcon(swappingImg)
const transformerIcon = getIcon(transformerImg)
const loadIcon = getIcon(loadImg)
const sourceIcon = getIcon(sourceImg)
const solarIcon = getIcon(solarImg)
const windIcon = getIcon(windImg)
const gridIcon = getIcon(gridImg)
const busIcon = getIcon(busImg)
const startIcon = getIcon(startImg)
const endIcon = getIcon(endImg)

// basic map data
const map = ref()
const trafficGroup = layerGroup()
const powerGridGroup = layerGroup()
const trafficFlowGroup = layerGroup()
const powerFlowGroup = layerGroup()
const demandsGroup = layerGroup()
const canvas = layerGroup()
let nodes: {[key: string]: any} = {}
let edges: {[key: string]: any} = {}
let trafficFlow: {[key: string]: any} = {}
let stations: {[key: string]: any} = {}
let stationTimeList: {[key: string]: any} = {}


// dialog data
const uploadVisible = ref(false)
const uploadType = ref("nodes")
const startTimer = ref(false)
let timeCounter = 0
let timer: any
let endTimer: number
const stationDrawer = ref(false)
const stationInfo = ref<{[key: string]: any}>({})


// ----------------------------- on mounted ----------------------------------
onMounted(async () => {
  let result = await getProjectService(projectId) as any
  initMap(result.data.latitude, result.data.longitude)
  await updateData()
})
// initial map data
const initMap = (lat: number, lng: number) => {
  map.value = L.map('map', {
    center: new L.LatLng(lat, lng),
    zoom: 15,
    zoomControl: false,
  })
  const osmMap = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 18,
    attribution: 'OpenStreetMap'
  }).addTo(map.value)
  const baseLayers = {
    "OpenStreetMap": osmMap
  }
  const featureLayers = {
    "Traffic Network": trafficGroup,
    "Power Grid": powerGridGroup,
    "Demand": demandsGroup,
    "Traffic Flow": trafficFlowGroup,
    "Power Flow": powerFlowGroup,
  }
  L.control.layers(baseLayers, featureLayers).addTo(map.value)
  canvas.addTo(map.value)

  map.value.on("zoomend", () => {
    if (map.value.hasLayer(trafficGroup)) {
      map.value.removeLayer(trafficGroup)
      map.value.addLayer(trafficGroup)
    }
    if (map.value.hasLayer(powerGridGroup)) {
      map.value.removeLayer(powerGridGroup)
      map.value.addLayer(powerGridGroup)
    }
    if (map.value.hasLayer(trafficFlowGroup)) {
      map.value.removeLayer(trafficFlowGroup)
      map.value.addLayer(trafficFlowGroup)
    }
    if (map.value.hasLayer(powerFlowGroup)) {
      map.value.removeLayer(powerFlowGroup)
      map.value.addLayer(powerFlowGroup)
    }
  })

  map.value.getContainer().style.cursor = "default"
}
const updateData = async () => {
  trafficGroup.clearLayers()
  powerGridGroup.clearLayers()
  trafficFlowGroup.clearLayers()
  powerFlowGroup.clearLayers()
  demandsGroup.clearLayers()
  let result = await getAllData(projectId) as any
  nodes = result.data.nodes
  edges = result.data.edges
  trafficFlow = result.data.trafficFlow
  stationTimeList = result.data.stationInfo
  stations = result.data.stations
  setTrafficNetwork(nodes, edges)
  setPowerGrid(result.data.devices, result.data.stations, result.data.lines)
  setDemands(result.data.pointDemands, result.data.odPairs)
  // end Timer
  for (const key in trafficFlow) {
    endTimer = trafficFlow[key].properties.data.length
    break
  }
}
// set all data on the map
const setTrafficNetwork = (nodes: any, edges: any) => {
  // for (const key in nodes) {
  //   L.marker(nodes[key].point).bindPopup("id: " + key).addTo(trafficGroup)
  // }
  for (const key in edges) {
    L.polyline(edges[key].polyline).bindPopup("id: " + key).on("click", () => {
      // TODO: add data chart
    }).addTo(trafficGroup)
  }
}
const setPowerGrid = (devices: any, stations: any, lines: any) => {
  let setIcon = busIcon
  for (const key in devices) {
    if (devices[key].type === "source") setIcon = sourceIcon
    else if (devices[key].type === "solar") setIcon = solarIcon
    else if (devices[key].type === "wind") setIcon = windIcon
    else if (devices[key].type === "transformer") setIcon = transformerIcon
    else if (devices[key].type === "load") setIcon = loadIcon
    else if (devices[key].type == "grid") setIcon = gridIcon
    L.marker(devices[key].point, {icon: setIcon}).bindPopup("id: " + key).addTo(powerGridGroup)
  }
  for (const key in stations) {
    if (stations[key].properties.type === "swapping") setIcon = swappingIcon
    else setIcon = chargingIcon
    L.marker(stations[key].point, {icon: setIcon}).on("click", () => {
      stationDrawer.value = true
      stationInfo.value["key"] = key
      stationInfo.value["type"] = stations[key].properties.type
    }).bindPopup("id: " + key).addTo(powerGridGroup)
  }
  for (const key in lines) {
    L.polyline(lines[key].polyline, {color: "#FF7722"}).bindPopup("id: " + key).addTo(powerGridGroup)
  }
}
const setDemands = (points: any, ods: any) => {
  for (const key in points) {
    L.circleMarker(points[key].point, {radius: points[key].radius}).bindPopup("id: " + key).addTo(demandsGroup)
  }
  for (const key in ods) {
    const D = L.marker(nodes[ods[key].D].point, {icon: endIcon})
    L.marker(nodes[ods[key].O].point, {icon: startIcon}).on("click", () => {
      canvas.clearLayers()
      canvas.addLayer(D)
      L.polyline([nodes[ods[key].O].point, nodes[ods[key].D].point], {color: "#FF6622"}).addTo(canvas)
    }).bindPopup("id: " + key + ", weight: " + ods[key].weight).addTo(demandsGroup)
  }
}
const setTrafficFlowByTimer = (flows: any, timer: number) => {
  trafficFlowGroup.clearLayers()
  console.log(flows)
  for (const key in flows) {
    let data = 0
    if (flows[key].properties.data.length > timer) data = parseFloat(flows[key].properties.data[timer]) * 10
    console.log(data)
    let flowColor = colors[0]
    if (data >= 100) flowColor = colors[99]
    else flowColor = colors[(100 - data).toFixed()]
    L.polyline(edges[key].polyline, {weight: 5, color: flowColor}).addTo(trafficFlowGroup)
  }
}

const setStationInfoByTimer = (timer: number) => {
  // const key = stationInfo.value["key"]
  // stationInfo.value["data"] = stationTimeList[key]["data"][timer]
  // console.log(stationTimeList)
  // let voltage = 0
  // if (stationInfo.value["type"] === 'charging') {
  //   for (let i = 0; i < stationInfo.value["data"].length; i++) {
  //     if (parseInt(stationInfo.value["data"][i]) === 1) {
  //       voltage += parseFloat(stations[key]["properties"]["capacity"][i])
  //     }
  //   }
  // }
  // else {
  //   for (let i = 0; i < stationInfo.value["data"].length; i++) {
  //     if (parseFloat(stationInfo.value["data"][i]) <= 0.9) {
  //       voltage += 120
  //     }
  //   }
  // }
  // stationInfo.value["voltage"] = voltage
}
// ---------------------------- main function --------------------------------
const uploadData = async (param: UploadRequestOptions) => {
  await uploadDataService(projectId, uploadType.value, param.file)
  ElMessage.success({
    message: "Upload and update personal data successfully.",
    plain: true,
  })
  uploadVisible.value = false
}

const downloadData = () => {

}

const putCar = () => {

}

const controlProgram = () => {

}

const locateProgram = () => {

}

const deleteData = () => {

}

const startControl = () => {
  startTimer.value = true
  timer = setInterval(function () {
    timeCounter += 1
    console.log(timeCounter)
    if (timeCounter >= endTimer) {
      clearInterval(timer)
      startTimer.value = false
      timeCounter = 0
      ElMessage.success("simulation ending.")
    }
    setTrafficFlowByTimer(trafficFlow, timeCounter)
    setStationInfoByTimer(timeCounter)
  }, 5000)
}

const endControl = () => {
  startTimer.value = false
  if (timer !== undefined) {
    clearInterval(timer)
    startTimer.value = false
    timer = undefined
    timeCounter = 0
  }
}

const backControl = () => {
  startTimer.value = false
  if (timer !== undefined) {
    clearInterval(timer)
    startTimer.value = false
    timer = undefined
    timeCounter = 0
  }
  timeCounter = 0
}

// ---------------------- addition -----------------------
const editTrafficFlowProgram = () => {
  window.open(router.resolve({
    path: "/program/traffic-flow",
    query: {projectId: projectId},
  }).href)
}

const modifyTrafficFlow = async () => {
  const loading = ElLoading.service({
    lock: true,
    text: 'Loading',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  let result = await runTrafficFlow(projectId) as any
  trafficFlow = result.data.trafficFlow
  for (const key in trafficFlow) {
    endTimer = trafficFlow[key].properties.data.length
    break
  }
  timeCounter = 0
  loading.close()
}

const editSchedulingProgram = () => {
  window.open(router.resolve({
    path: "/program/scheduling",
    query: {projectId: projectId},
  }).href)
}

const modifyScheduling = async () => {
  const loading = ElLoading.service({
    lock: true,
    text: 'Loading',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  let result = await runScheduling(projectId) as any
  // scheduling = result.data.scheduling
  timeCounter = 0
  loading.close()
}

const runShortestPath = async () => {
  const loading = ElLoading.service({
    lock: true,
    text: 'Loading',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  // let result = await runShortest(projectId, start, end) as any

}

</script>

<template>
  <div class="container">
    <el-tooltip effect="dark" content="upload personal data" placement="right">
      <el-button style="margin: 10px 4px 4px 4px; " type="success" @click="uploadVisible = true" :icon="UploadFilled" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="download all data from database" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="downloadData" :icon="Download" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="Edit control program" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="controlProgram" :icon="Guide" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="Edit location program" placement="right">
      <el-button style="margin: 4px; " type="primary" @click="locateProgram" :icon="MapLocation" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="set a car on the map" placement="right">
      <el-button style="margin: 4px; " type="primary" @click="putCar" :icon="Van" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="delete all data include database" placement="right">
      <el-button style="margin: 4px; " type="danger" @click="deleteData" :icon="Delete" circle size="large"/>
    </el-tooltip><br>
    <div v-if="startTimer === false">
      <el-tooltip effect="dark" content="Start simulation" placement="right">
        <el-button style="margin: 4px; " type="primary" @click="startControl" :icon="VideoPlay" circle size="large"/>
      </el-tooltip><br>
    </div>
    <div v-else>
      <el-tooltip effect="dark" content="Pause simulation" placement="right">
        <el-button style="margin: 4px; " type="primary" @click="endControl" :icon="VideoPause" circle size="large"/>
      </el-tooltip><br>
      <el-tooltip effect="dark" content="Back to start" placement="right">
        <el-button style="margin: 4px; " type="primary" @click="backControl" :icon="Back" circle size="large"/>
      </el-tooltip><br>
    </div>
    <el-divider></el-divider>
    <el-tooltip effect="dark" content="edit your program about traffic flow" placement="left">
      <el-button style="margin: 4px;" type="warning" @click="editTrafficFlowProgram" :icon="EditPen" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="modify traffic flow by your program" placement="left">
      <el-button style="margin: 4px;" type="warning" @click="modifyTrafficFlow" :icon="Promotion" circle size="large"/>
    </el-tooltip><br>
    <el-divider></el-divider>
    <el-tooltip effect="dark" content="edit your program about scheduling" placement="left">
      <el-button style="margin: 4px;" type="warning" @click="editSchedulingProgram" :icon="EditPen" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="modify scheduling by your program" placement="left">
      <el-button style="margin: 4px;" type="warning" @click="modifyScheduling" :icon="Promotion" circle size="large"/>
    </el-tooltip><br>
    <el-divider></el-divider>
    <el-tooltip effect="dark" content="run shortest path" placement="right">
      <el-button style="margin: 4px; " type="danger" @click="runShortestPath" :icon="EditPen" circle size="large"/>
    </el-tooltip><br>

  </div>
  <div id="map"></div>

<!-- upload personal data -->
  <el-dialog v-model="uploadVisible" title="Upload your personal data" width="800">
    <el-form-item label="File type:">
      <el-select v-model="uploadType" placeholder="Select" size="large" style="width: 240px">
        <el-option label="Junctions" value="nodes"/>
        <el-option label="Roads" value="edges"/>
        <el-option label="Power Devices" value="devices"/>
        <el-option label="Power Lines" value="lines"/>
        <el-option label="Stations" value="stations"/>
        <el-option label="Point Demands" value="pointDemands"/>
        <el-option label="OD Pairs" value="odPairs"/>
        <el-option label="Traffic Flow" value="trafficFlow"/>
        <el-option label="Power Flow" value="power flow"/>
      </el-select>
    </el-form-item>
    <el-upload class="avatar-uploader" :http-request="uploadData" :limit="1" action="Action">
      <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
    </el-upload>
  </el-dialog>

<!-- station information drawer -->
  <el-drawer v-model="stationDrawer" title="Station Information" :with-header="true" :modal="false" :show-close="false">
    <template #header="{ close, titleId, titleClass }">
      <h4 :id="titleId" :class="titleClass">{{ stationInfo.type }} station</h4>
      <el-button type="danger" @click="close">
        <el-icon class="el-icon--left"><CircleCloseFilled /></el-icon>
        Close
      </el-button>
    </template>

    Power: {{ stationInfo.voltage }}
    <div v-if="stationInfo.type === 'charging'">
      <div v-for="status in stationInfo.data" class="charging">
        <div v-if="status === 1" class="charging-img">
          <img src="../../assets/charger-ing.svg" width="50" height="50" alt="charging" />
        </div>
        <div v-else class="charging-img">
          <img src="../../assets/charger-free.svg" width="50" height="50" alt="free" />
        </div>
      </div>
    </div>

    <div v-else>
      <div v-for="battery in stationInfo.data" class="charging">
        <div v-if="battery >= 0.9" class="charging-img">
          <img src="../../assets/battery100.svg" width="50" height="50" alt="battery" />
        </div>
        <div v-else-if="battery >= 0.8" class="charging-img">
          <img src="../../assets/battery85.svg" width="50" height="50" alt="battery" />
        </div>
        <div v-else-if="battery >= 0.6" class="charging-img">
          <img src="../../assets/battery65.svg" width="50" height="50" alt="battery" />
        </div>
        <div v-else-if="battery >= 0.4" class="charging-img">
          <img src="../../assets/battery45.svg" width="50" height="50" alt="battery" />
        </div>
        <div v-else-if="battery >= 0.2" class="charging-img">
          <img src="../../assets/battery20.svg" width="50" height="50" alt="battery" />
        </div>
        <div v-else class="charging-img">
          <img src="../../assets/battery10.svg" width="50" height="50" alt="battery" />
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<style scoped>
.container {
  width: 70px;
  height: 100vh;
  z-index: 10;
  position: absolute;
  left: 0;
  background-color: rgba(255, 255, 255, 0.5);
}
#map {
  width: 100vw;
  height: 100vh;
  position: absolute;
  z-index: 1;
}

.charging {
  width: 100%;
  height: 100%;
}

.charging-img {
  float: left;
}
</style>
