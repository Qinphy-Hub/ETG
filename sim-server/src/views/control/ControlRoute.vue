<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import {layerGroup} from "leaflet";
import * as L from "leaflet";
import {getProjectService} from "@/api/project";
import {useRoute} from "vue-router";
import {
  getDevicesData,
  getStationsData,
  uploadDataService
} from "@/api/load";
import Chart from "@/components/Chart.vue";
// icons
import chargingImg from "@/assets/charging-station.svg"
import swappingImg from "@/assets/swapping-station.svg"
import {
  DataAnalysis,
  Failed,
  Download,
  UploadFilled,
  Plus,
  VideoPlay,
  VideoPause,
  Back, DArrowRight, DArrowLeft
} from "@element-plus/icons-vue";
import {getFlpResult, getRouteRes} from "@/api/program";
import {ElLoading, ElMessage, type UploadRequestOptions} from "element-plus";
import transformerImg from "@/assets/transformer.svg";
import loadImg from "@/assets/load.svg";
import sourceImg from "@/assets/source.svg";
import solarImg from "@/assets/solar.svg";
import windImg from "@/assets/wind.svg";
import gridImg from "@/assets/grid.svg";
import busImg from "@/assets/bus.svg";
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

// base data
const route = useRoute();
const projectId = route.query.projectId


// data
let stations: {[key: string]: any} = {}
let devices: {[key: string]: any} = {}
let lines: {[key: string]: any} = {}
let positions: {[key: string]: any} = {}
let stationsPower: {[key: string]: any} = {}

// based map data
const map = ref()
const canvas = layerGroup()
const stationGroup = layerGroup()
const powerGroup = layerGroup()

// onmount
onMounted(async () => {
  let result = await getProjectService(projectId) as any
  initMap(result.data.latitude, result.data.longitude)
  let ret = await getRouteRes(projectId) as any
  positions = ret.data.positions
  stationsPower = ret.data.station_power
  let granularity = result.data.granularity
  let timeUnit = result.data.timeUnit
  let timeLen = result.data.timeLen
  let span = 1
  if (timeUnit === "hours") span = 60 * 60
  else if (timeUnit === "minutes") span = 60
  else if (timeUnit === "days") span = 24 * 60 * 60
  endTimer = span * granularity * timeLen
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
    "Stations": stationGroup,
    "Power Grid": powerGroup
  }
  L.control.layers(baseLayers, featureLayers).addTo(map.value)
  canvas.addTo(map.value)

  map.value.on("zoomend", () => {
    if (map.value.hasLayer(stationGroup)) {
      map.value.removeLayer(stationGroup)
      map.value.addLayer(stationGroup)
    }
    if (map.value.hasLayer(powerGroup)) {
      map.value.removeLayer(powerGroup)
      map.value.addLayer(powerGroup)
    }
  })

  map.value.getContainer().style.cursor = "default"
}


// drawing function
const updateData = async () => {
  stationGroup.clearLayers()
  powerGroup.clearLayers()
  let stationRet = await getStationsData(projectId) as any
  let powerRet = await getDevicesData(projectId) as any
  if (stationRet.data.stations !== null) stations = stationRet.data.stations
  if (powerRet.data.devices !== null) devices = powerRet.data.devices
  if (powerRet.data.lines !== null) lines = powerRet.data.lines
  setStations(stations)
  setDevices(devices)
  setLines(lines)
}

const setStations = (stations: any) => {
  for (const key in stations) {
    let setIcon = chargingIcon
    if (stations[key].type === "swapping") setIcon = swappingIcon
    const station = L.marker(stations[key].point, {icon: setIcon}).on("contextmenu", () => {
      delete stations[key]
      stationGroup.removeLayer(station)
    }).on("click", () => {
      calcStation(key)
      stationVisible.value = true
    })
    stationGroup.addLayer(station)
  }
}

// set devices on the map
const setDevices = (devices: any) => {
  for (const key in devices) {
    let setIcon = busIcon
    if (devices[key].type === "source") setIcon = sourceIcon
    else if (devices[key].type === "solar") setIcon = solarIcon
    else if (devices[key].type === "wind") setIcon = windIcon
    else if (devices[key].type === "transformer" || devices[key].type === "transformer3") setIcon = transformerIcon
    else if (devices[key].type === "load") setIcon = loadIcon
    else if (devices[key].type === "outer") setIcon = gridIcon
    const device = L.marker(devices[key].point, {icon: setIcon}).on("click", () => {
      // set data table
    }).bindPopup("id: " + key)
    powerGroup.addLayer(device)
  }
}

// set lines on the map
const setLines = (lines: any) => {
  for (const key in lines) {
    let c = "#00BFFF"
    if (lines[key].properties === null || lines[key].properties.is_service === false) c = "#808080"
    const line = L.polyline(lines[key].polyline, {weight: 5, color: c}).on("click", () => {
      // set data
    })
    powerGroup.addLayer(line)
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

const properties = ref<{[key: string]: any}>({})

const clearRes = () => {
  canvas.clearLayers()
}

const downloadRes = (index: number) => {
  // const id = programList.value[index].id
  // const blob = new Blob([JSON.stringify(stations[id])], {type: "application/json"})
  // const url = URL.createObjectURL(blob)
  // const link = document.createElement("a")
  // link.href = url
  // link.download = `stations-${id}.json`
  // link.click()
  // URL.revokeObjectURL(url)
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


// ------------------------------------ simulation ------------------------------------
let timer: any
const timeCounter = ref(0)
let endTimer = 0
const playStart = ref(false)
const playVelocity = ref(1)

const getSimRetSlice = (timer: number) => {
  canvas.clearLayers()
  if (timer in positions && positions[timer].length !== 0) {
    for (const polyline of positions[timer]) {
      L.polyline(polyline, {color: "#FFEE00", weight: 8}).addTo(canvas)
    }
  }
}

const startSimulation = () => {
  if (timer !== undefined) {
    clearInterval(timer)
    timer = undefined
  }
  timer = setInterval(function () {
    playStart.value = true
    timeCounter.value += playVelocity.value
    if (timeCounter.value >= endTimer) {
      clearInterval(timer)
      timeCounter.value = 0
      playStart.value = false
      ElMessage.success("simulation ending.")
    }
    // TODO: get and show simulation data
    getSimRetSlice(timeCounter.value)
  }, 1000 / playVelocity.value)
}
const pauseSimulation = () => {
  if (timer !== undefined) {
    playStart.value = false
    clearInterval(timer)
    timer = undefined
  }
}
const returnSimulation = () => {
  if (timer !== undefined) {
    clearInterval(timer)
    timer = undefined
    timeCounter.value = 0
  }
  playStart.value = false
  timeCounter.value = 0
}
const quickSimulation = () => {
  if (playVelocity.value < 24 * 60 * 60) {
    playVelocity.value += 1
    clearInterval(timer)
    timer = undefined
    startSimulation()
  }
}
const slowSimulation = () => {
  if (playVelocity.value > 1) {
    playVelocity.value -= 1
    clearInterval(timer)
    timer = undefined
    startSimulation()
  }
}

const calcStation = (key: string) => {
  let x = []
  let y = []
  for (let i = 0; i < endTimer; i++) {
    x.push(i + 1)
    let cnt = 0
    let j = i - 300
    if (j < 0) j = 0
    for (; j < i; j++) {
      if (j in stationsPower[key]) cnt++
    }
    y.push(cnt * 60)
  }
  options.value.xAxis.data = x
  options.value.series[0].data = y
}

const stationVisible = ref(false)
const options = ref({
  xAxis: {
    type: 'time',
    data: [1, 2, 3, 4, 5],
  },
  yAxis: {
    type: 'power',
  },
  series: [{
    data: [1, 2, 3, 4, 5],
    type: 'line',
  }]
})
</script>

<template>
  <div class="container">
    <div class="scroll-bar">
<!--      <el-tooltip effect="dark" content="simulate" placement="right">-->
<!--        <el-button style="margin: 10px 20px 1px 4px; float: none" type="danger" @click="simulationSwitch" :icon="Coin" circle size="large"/>-->
<!--      </el-tooltip><br>-->
      <div>T:{{timeCounter}}s</div>
      <div v-if="playStart === false">
        <el-tooltip effect="dark" content="start simulation" placement="right">
          <el-button style="margin: 10px 20px 1px 4px; float: none" type="success" @click="startSimulation" :icon="VideoPlay" circle size="large"/>
        </el-tooltip><br>
      </div>
      <div v-else>
        <el-tooltip effect="dark" content="pause simulation" placement="right">
          <el-button style="margin: 10px 20px 1px 4px; float: none " type="success" @click="pauseSimulation" :icon="VideoPause" circle size="large"/>
        </el-tooltip><br>
      </div>
      <div v-if="timeCounter === 0">
        <el-tooltip effect="dark" content="back to start" placement="right">
          <el-button style="margin: 10px 20px 1px 4px; float: none" type="info" :icon="Back" circle size="large"/>
        </el-tooltip><br>
      </div>
      <div v-else>
        <el-tooltip effect="dark" content="back to start" placement="right">
          <el-button style="margin: 10px 20px 1px 4px; float: none" type="primary" @click="returnSimulation" :icon="Back" circle size="large"/>
        </el-tooltip><br>
      </div>
      <el-popover placement="right" title="speed" :width="200" trigger="hover">
        <template #reference>
          <el-button style="margin: 10px 20px 1px 4px; float: none" type="primary" @click="quickSimulation" :icon="DArrowRight" circle size="large"/>
        </template>
        <el-input-number v-model="playVelocity" :min="1" :max="24 * 60 * 60" />
      </el-popover>
      <el-popover placement="right" title="speed" :width="200" trigger="hover">
        <template #reference>
          <el-button style="margin: 10px 20px 1px 4px; float: none" type="primary" @click="slowSimulation" :icon="DArrowLeft" circle size="large"/>
        </template>
        <el-input-number v-model="playVelocity" :min="1" :max="24 * 60 * 60" />
      </el-popover>

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
      <el-tooltip effect="dark" content="download the result(stations info file)" placement="right">
        <el-button style="margin: 10px 20px 1px 4px; float: none" @click="stationVisible = true" type="success" :icon="Download" circle size="large"/>
      </el-tooltip><br>
      <el-divider />
    </div>
  </div>
  <div id="map"></div>

<!--  &lt;!&ndash; download box &ndash;&gt;-->
<!--  <el-dialog v-model="downloadVisible" title="Choose your algorithm" width="500">-->
<!--    <el-table :data="programList" border style="width: 100%">-->
<!--      <el-table-column fixed prop="id" label="Id" width="50" />-->
<!--      <el-table-column prop="name" label="Program Name" width="200" />-->
<!--      <el-table-column prop="color" label="Color" width="120" />-->
<!--      <el-table-column fixed="right" label="Download" min-width="60">-->
<!--        <template #default="scope">-->
<!--          <el-tooltip content="Download" placement="top" effect="dark">-->
<!--            <el-button type="primary" :icon="Download" @click="downloadRes(scope.$index)" circle />-->
<!--          </el-tooltip>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--    </el-table>-->
<!--    <template #footer>-->
<!--      <div class="dialog-footer">-->
<!--        <el-button @click="downloadVisible = false">Cancel</el-button>-->
<!--      </div>-->
<!--    </template>-->
<!--  </el-dialog>-->

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

<!-- stations information -->
  <el-drawer v-model="stationVisible" title="Station Power Data">
    <Chart :rawData="options"></Chart>
  </el-drawer>
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