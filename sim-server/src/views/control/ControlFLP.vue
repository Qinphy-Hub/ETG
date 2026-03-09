<script setup lang="ts">
import {onMounted, ref} from "vue";
import {layerGroup} from "leaflet";
import * as L from "leaflet";
import {getProjectService} from "@/api/project";
import {useRoute} from "vue-router";
import {getNodesData, getODPairsData, getPointDemandsData, getSitesData, uploadDataService} from "@/api/load";
// icons
import startImg from "@/assets/start_point.svg"
import endImg from "@/assets/end_point.svg"
import {DataAnalysis, MessageBox, Failed, Download, UploadFilled, Plus} from "@element-plus/icons-vue";
import {flpProgramListService, getFlpHistory, getFlpResult} from "@/api/program";
import {ElLoading, ElMessage, ElNotification, type UploadRequestOptions} from "element-plus";
const getIcon = (url: string) => {
  return L.icon({iconUrl: url, iconSize: [38, 38], iconAnchor: [19, 19]})
}
const startIcon = getIcon(startImg)
const endIcon = getIcon(endImg)


// base data
const route = useRoute();
const projectId = route.query.projectId


// data
let ods: {[key: string]: any} = {}
let points: {[key: string]: any} = {}
let sites: {[key: string]: any} = {}
let nodes: {[key: string]: any} = {}
let stations: {[key: string]: any} = {}

// based map data
const map = ref()
const canvas = layerGroup()
const retMap = layerGroup()
const sitesGroup = layerGroup()
const pointsGroup = layerGroup()
const odsGroup = layerGroup()


// program data
let programList = ref<any[]>([])


// onmount
onMounted(async () => {
  let result = await getProjectService(projectId) as any
  initMap(result.data.latitude, result.data.longitude)
  let programRet = await flpProgramListService(projectId) as any
  programList.value = programRet.data
  let historyRet = await getFlpHistory(projectId) as any
  stations = historyRet.data
  await updateData();
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
    "Potential site": sitesGroup,
    "Point demands": pointsGroup,
    "OD demands": odsGroup,
  }
  L.control.layers(baseLayers, featureLayers).addTo(map.value)
  canvas.addTo(map.value)
  retMap.addTo(map.value)

  map.value.on("zoomend", () => {
    if (map.value.hasLayer(sitesGroup)) {
      map.value.removeLayer(sitesGroup)
      map.value.addLayer(sitesGroup)
    }
    if (map.value.hasLayer(pointsGroup)) {
      map.value.removeLayer(pointsGroup)
      map.value.addLayer(pointsGroup)
    }
    if (map.value.hasLayer(odsGroup)) {
      map.value.removeLayer(odsGroup)
      map.value.addLayer(odsGroup)
    }
  })

  map.value.getContainer().style.cursor = "default"
}


// drawing function
const updateData = async () => {
  sitesGroup.clearLayers()
  pointsGroup.clearLayers()
  odsGroup.clearLayers()
  let odsRet = await getODPairsData(projectId) as any
  let pointsRet = await getPointDemandsData(projectId) as any
  let sitesRet = await getSitesData(projectId) as any
  let nodesRet = await getNodesData(projectId) as any
  if (odsRet.data.odPairs !== null) ods = odsRet.data.odPairs
  if (pointsRet.data.pointDemands !== null) points = pointsRet.data.pointDemands
  if (sitesRet.data.sites !== null) sites = sitesRet.data.sites
  if (nodesRet.data.nodes !== null) nodes = nodesRet.data.nodes
  for (let key in points) {
    if (points[key].value > max_demand) {
      max_demand = points[key].value
    }
  }
  setSites(sites)
  setPoints(points)
  setOds(ods)
}

const setSites = (sites: any) => {
  for (const key in sites) {
    const site = L.circle(sites[key].point, {radius: 30, color: "#808080"}).on("click", () => {
      countAlgorithm(key)
    }).bindPopup("potential site id: " + key + ", cost: " + sites[key].cost)
    sitesGroup.addLayer(site)
  }
}

const countAlgorithm = (key: string) => {
  let msg = ""
  for (const i in programList.value) {
    const id = programList.value[i].id
    if (key in stations[id]) msg += (programList.value[i].name + ", ")
  }
  if (msg === "") msg = "No one!"
  ElNotification({
    title: 'These algorithms choose the Potential site ' + key,
    message: msg,
  })
}

let max_demand = 15
const setPoints = (points: any) => {
  for (const key in points) {
    const point = L.circle(points[key].point, {
      color: "#ff0000",
      weight: 0.5,
      radius: 30,
      fillColor: "#ff0000",
      fillOpacity: points[key].value / max_demand,
    }).bindPopup("point demand id: " + key + ", value: " + points[key].value)
    pointsGroup.addLayer(point)
  }
}

const setOds = (ods: any) => {
  for (const key in ods) {
    const D = L.marker(nodes[ods[key].D].point, {icon: endIcon})
    const O = L.marker(nodes[ods[key].O].point, {icon: startIcon}).on("click", () => {
      canvas.clearLayers()
      canvas.addLayer(D)
      L.polyline([nodes[ods[key].O].point, nodes[ods[key].D].point], {color: "#FF6622"}).addTo(canvas)
    }).bindPopup("od pairs id: " + key + ", value: " + ods[key].value)
    odsGroup.addLayer(O)
  }
}


const runFlpMain = async () => {
  const loading = ElLoading.service({
    lock: true,
    text: 'Loading',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  clearRes()
  let result = await getFlpResult(projectId) as any
  stations = result.data
  ElMessage.success({
    message: "Successfully run FLP solver!",
    plain: true,
  })
  loading.close()
}

const setStations = (stationList: any, c: string, id: number) => {
  for (const key in stationList) {
    const station = L.circle(stationList[key].point, {radius: 50, color: c, fillOpacity: 0.7}).on("click", () => {
      showStationMsg(id, key)
    }).bindPopup("station id: " + key)
    retMap.addLayer(station)
  }
}

const properties = ref<{[key: string]: any}>({})
const showStationMsg = (id: number, key: string) => {
  properties.value = stations[id][key]
  propertyVisible.value = true
}

let showResList:number[] = []
const chooseProgram = (index: number) => {
  const id = programList.value[index].id
  if (stations[id] == null) ElMessage('This algorithm did not run.')
  else if (!(showResList.some(ele => ele === id))) {
    showResList.push(id)
    setStations(stations[id], programList.value[index].color, id)
  }
}

const clearRes = () => {
  retMap.clearLayers()
  showResList = []
}

const downloadRes = (index: number) => {
  const id = programList.value[index].id
  const blob = new Blob([JSON.stringify(stations[id])], {type: "application/json"})
  const url = URL.createObjectURL(blob)
  const link = document.createElement("a")
  link.href = url
  link.download = `stations-${id}.json`
  link.click()
  URL.revokeObjectURL(url)
}

const downloadVisible = ref(false)
const propertyVisible =  ref(false)
const uploadVisible = ref(false)
const uploadType = ref("nodes")

const uploadData = async (param: UploadRequestOptions) => {
  await uploadDataService(projectId, uploadType.value, param.file)
  ElMessage.success({
    message: "Upload and update personal data successfully.",
    plain: true,
  })
  uploadVisible.value = false
}
</script>

<template>
  <div class="container">
    <div class="scroll-bar">
      <el-tooltip effect="dark" content="upload personal data" placement="right">
        <el-button style="margin: 10px 20px 1px 4px; float: none" type="success" @click="uploadVisible = true" :icon="UploadFilled" circle size="large"/>
      </el-tooltip><br>
      <el-tooltip effect="dark" content="calc" placement="right">
        <el-button style="margin: 10px 20px 1px 4px; float: none" @click="runFlpMain" type="danger" :icon="DataAnalysis" circle size="large"/>
      </el-tooltip><br>
      <el-tooltip effect="dark" content="clear result on the map" placement="right">
        <el-button style="margin: 10px 20px 1px 4px; float: none" @click="clearRes" type="warning" :icon="Failed" circle size="large"/>
      </el-tooltip><br>
      <el-tooltip effect="dark" content="download the result(stations info file)" placement="right">
        <el-button style="margin: 10px 20px 1px 4px; float: none" @click="downloadVisible = true" type="success" :icon="Download" circle size="large"/>
      </el-tooltip><br>
      <el-divider />
      <el-tooltip v-for="(p, index) in programList" effect="dark" :content="p.name" placement="right">
        <el-button style="margin: 10px 20px 1px 4px; float: none" @click="chooseProgram(index)" :color="p.color" :icon="MessageBox" circle size="large"/>
      </el-tooltip><br>
    </div>
  </div>
  <div id="map"></div>

<!-- download box -->
  <el-dialog v-model="downloadVisible" title="Choose your algorithm" width="500">
    <el-table :data="programList" border style="width: 100%">
      <el-table-column fixed prop="id" label="Id" width="50" />
      <el-table-column prop="name" label="Program Name" width="200" />
      <el-table-column prop="color" label="Color" width="120" />
      <el-table-column fixed="right" label="Download" min-width="60">
        <template #default="scope">
          <el-tooltip content="Download" placement="top" effect="dark">
            <el-button type="primary" :icon="Download" @click="downloadRes(scope.$index)" circle />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="downloadVisible = false">Cancel</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- show station info -->
  <el-dialog v-model="propertyVisible" title="Station Information" width="500">
    <el-table :data="properties.data" style="width: 100%" max-height="240">
      <el-table-column prop="name" label="Battery/Pile Type Name" align="center"/>
      <el-table-column prop="power" label="Rated Power" align="center"/>
      <el-table-column prop="num" label="Number" align="center"/>
    </el-table>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="propertyVisible = false">Cancel</el-button>
      </div>
    </template>
  </el-dialog>

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
</template>

<style scoped>
.container {
  width: 70px;
  height: 100vh;
  position: absolute;
  left: 0;
  overflow: hidden;
}

.scroll-bar {
  width: 87px;
  height: 100vh;
  z-index: 10;
  position: absolute;
  left: 0;
  overflow: auto;
  background-color: rgba(255, 255, 255, 0.5);
}

#map {
  width: 100vw;
  height: 100vh;
  position: absolute;
  z-index: 1;
}
</style>