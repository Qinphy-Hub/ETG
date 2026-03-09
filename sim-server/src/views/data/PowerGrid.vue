<script setup lang="ts">
import {Delete, Edit, Failed, Plus, Pointer, Select, Share, Upload} from "@element-plus/icons-vue";
import {onMounted, ref} from "vue";
import {ElMessage, ElMessageBox, type UploadRequestOptions} from "element-plus";
import {deleteDevicesData, getDevicesData, getStationsData, saveDevicesData, uploadDataService} from "@/api/load";
import {useRoute} from "vue-router";
import {getProjectService} from "@/api/project";
import * as L from "leaflet";
import {layerGroup} from "leaflet";
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
import {getAclineList, getTransformer3List, getTransformerList} from "@/api/grid";

// base data
const route = useRoute();
const projectId = route.query.projectId

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

// basic map data
const map = ref()
const stationsGroup = layerGroup()
const devicesGroup = layerGroup()
const linesGroup = layerGroup()
const canvas = layerGroup()
let stations: any
let devices: {[key: string]: any} = {}
let lines: {[key: string]: any} = {}
let setDevicesFlag = false
let propertyFlag = false
let connectFlag = false
let startPoint: any
let endPoint: any

// dialog data
const uploadVisible = ref(false)
const linePropertyVisible = ref(false)
const lineProperty = ref<{[key: string]: any}>({
  length: 10,
  key: ""
})
let transformers: any []
let aclines: any []
let transformers3: any []
const devicePropertyVisible = ref(false)
const deviceProperty = ref<{[key: string]: any}>({
  key: "",
  type: "",
})
const uploadType = ref("devices")

// ------------------------- on mounted -------------------------
onMounted(async () => {
  let result = await getProjectService(projectId) as any
  initMap(result.data.latitude, result.data.longitude)
  result = await getTransformerList() as any
  transformers = result.data
  result = await getAclineList() as any
  aclines = result.data
  result = await getTransformer3List() as any
  transformers3 = result.data
  await updateData()
})
// initial map data
const initMap = (lat: number, lng: number) => {
  map.value = L.map('map', {
    center: new L.LatLng(lat, lng),
    zoom: 15,
    zoomControl: false,
  })
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 18,
    attribution: 'OpenStreetMap'
  }).addTo(map.value)
  stationsGroup.addTo(map.value)
  devicesGroup.addTo(map.value)
  linesGroup.addTo(map.value)
  canvas.addTo(map.value)

  map.value.on("zoomend", () => {
    if (map.value.hasLayer(stationsGroup)) {
      map.value.removeLayer(stationsGroup)
      map.value.addLayer(stationsGroup)
    }
    if (map.value.hasLayer(devicesGroup)) {
      map.value.removeLayer(devicesGroup)
      map.value.addLayer(devicesGroup)
    }
    if (map.value.hasLayer(linesGroup)) {
      map.value.removeLayer(linesGroup)
      map.value.addLayer(linesGroup)
    }
  })

  map.value.getContainer().style.cursor = "default"

  map.value.on("click", (e: any) => {
    if (setDevicesFlag) {
      const lat = e.latlng.lat
      const lng = e.latlng.lng
      const key = lat.toString().slice(0, 7) + lng.toString().slice(0, 8)
      devices[key] = {
        point: [lat, lng],
        type: "bus",
        properties: null
      }
      setOneDevice(key)
    }
  })
}
// update data when data changed
const updateData = async () => {
  clearData()
  let devicesRet = await getDevicesData(projectId) as any
  let stationsRet = await getStationsData(projectId) as any
  if (stationsRet.data.stations !== null) {
    stations = stationsRet.data.stations
    setStations(stations)
  }
  if (devicesRet.data.devices !== null) {
    devices = devicesRet.data.devices
    setDevices(devices)
  }
  if (devicesRet.data.lines !== null) {
    lines = devicesRet.data.lines
    setLines(lines)
  }
}
// set stations on the map
const setStations = (stations: any) => {
  for (const key in stations) {
    let setIcon = chargingIcon
    if (stations[key].type === "swapping") setIcon = swappingIcon
    const station = L.marker(stations[key].point, {icon: setIcon}).on("click", () => {
      if (connectFlag) {
        connectFunction(key)
      }
    }).bindPopup("id: " + key)
    stationsGroup.addLayer(station)
  }
}
// set devices on the map
const setDevices = (devices: any) => {
  for (const key in devices) {
    setOneDevice(key)
  }
}
// set one device on the map
const setOneDevice = (key: string) => {
  let setIcon = busIcon
  if (devices[key].type === "source") setIcon = sourceIcon
  else if (devices[key].type === "solar") setIcon = solarIcon
  else if (devices[key].type === "wind") setIcon = windIcon
  else if (devices[key].type === "transformer" || devices[key].type === "transformer3") setIcon = transformerIcon
  else if (devices[key].type === "load") setIcon = loadIcon
  else if (devices[key].type === "outer") setIcon = gridIcon
  const device = L.marker(devices[key].point, {icon: setIcon}).on("contextmenu", () => {
    delete devices[key]
    devicesGroup.removeLayer(device)
  }).on("click", () => {
    if (propertyFlag) {
      clickProperty(key)
      devicePropertyVisible.value = true
    }
    if (connectFlag) {
      connectFunction(key)
    }
  }).bindPopup("id: " + key)
  devicesGroup.addLayer(device)
}

// set lines on the map
const setLines = (lines: any) => {
  for (const key in lines) {
    setOneLine(key)
  }
}
const setOneLine = (key: string) => {
  let c = "#00BFFF"
  if (lines[key].properties === null || lines[key].properties.is_service === false) c = "#808080"
  const line = L.polyline(lines[key].polyline, {weight: 5, color: c}).on("contextmenu", () => {
    delete lines[key]
    linesGroup.removeLayer(line)
  }).on("click", () => {
    if (propertyFlag) {
      clickLinesProperty(key)
      linePropertyVisible.value = true
    }
  })
  linesGroup.addLayer(line)
}
// connect two devices
const connectFunction = (key: string) => {
  if (startPoint === undefined) {
    if (key in stations) startPoint = stations[key].point
    else startPoint = devices[key].point
    map.value.on("mousemove", (e: any) => {
      const tmpPoint = [e.latlng.lat, e.latlng.lng]
      canvas.clearLayers()
      L.polyline([startPoint, tmpPoint]).addTo(canvas)
    })
  }
  else if (endPoint === undefined) {
    if (key in stations) endPoint = stations[key].point
    else endPoint = devices[key].point
    map.value.off("mousemove")
    canvas.clearLayers()
    const lineKey = startPoint[0].toString().slice(0, 7) + startPoint[0].toString().slice(0, 7) + key
    lines[lineKey] = {
      polyline: [startPoint, endPoint],
      length: calculateDistance(startPoint[0], startPoint[1], endPoint[0], endPoint[1]),
      properties: null
    }
    setOneLine(lineKey)
    startPoint = undefined
    endPoint = undefined
  }
}

const setDevicesProperties = () => {
  const key = deviceProperty.value.key
  if (deviceProperty.value.type === 'bus') {
    devices[key].type = "bus"
    devices[key].properties = JSON.parse(JSON.stringify(t_bus.value))
  } else if (deviceProperty.value.type === 'load') {
    devices[key].type = "load"
    devices[key].properties = JSON.parse(JSON.stringify(t_load.value))
  } else if (deviceProperty.value.type === 'transformer') {
    devices[key].type = "transformer"
    devices[key].properties = JSON.parse(JSON.stringify(t_transformers.value))
  } else if (deviceProperty.value.type === 'transformer3') {
    devices[key].type = "transformer3"
    devices[key].properties = JSON.parse(JSON.stringify(t_transformers3.value))
  } else if (deviceProperty.value.type === 'outer') {
    devices[key].type = "outer"
    devices[key].properties = JSON.parse(JSON.stringify(t_outer.value))
  } else if (deviceProperty.value.type === 'source') {
    devices[key].type = "source"
    devices[key].properties = JSON.parse(JSON.stringify(t_source.value))
  } else if (deviceProperty.value.type === 'wind') {
    devices[key].type = "wind"
    devices[key].properties = JSON.parse(JSON.stringify(t_source.value))
  } else if (deviceProperty.value.type === 'solar') {
    devices[key].type = "solar"
    devices[key].properties = JSON.parse(JSON.stringify(t_source.value))
  }
  devicePropertyVisible.value = false
}

const setLinesProperties = () => {
  const key = lineProperty.value.key
  lines[key].length = lineProperty.value.length
  lines[key].properties = JSON.parse(JSON.stringify(t_lines.value))
  linePropertyVisible.value = false
}

// ---------------------- main function -------------------------
const uploadData = async (param: UploadRequestOptions) => {
  await uploadDataService(projectId, uploadType.value, param.file)
  ElMessage.success({
    message: "Upload and update personal data successfully.",
    plain: true,
  })
  uploadVisible.value = false
}

const settingDevice = () => {
  setDevicesFlag = true
  propertyFlag = false
  connectFlag = false
  map.value.off("mousemove")
  canvas.clearLayers()
  map.value.getContainer().style.cursor = "pointer"
}

const connectDevices = () => {
  setDevicesFlag = false
  propertyFlag = false
  connectFlag = true
  map.value.off("mousemove")
  canvas.clearLayers()
  map.value.getContainer().style.cursor = "default"
}

const setProperties = () => {
  setDevicesFlag = false
  propertyFlag = true
  connectFlag = false
  map.value.off("mousemove")
  canvas.clearLayers()
  map.value.getContainer().style.cursor = "default"
}

const saveData = () => {
  const data = {
    "projectId": projectId,
    "devices": JSON.stringify(devices),
    "lines": JSON.stringify(lines)
  }
  saveDevicesData(data)
  ElMessage.success({
    message: "save all data successfully!",
    plain: true,
  })
  map.value.getContainer().style.cursor = "default"
  window.location.reload()
}

const clearData = () => {
  devices = {}
  lines = {}
  canvas.clearLayers()
  linesGroup.clearLayers()
  devicesGroup.clearLayers()
  map.value.getContainer().style.cursor = "default"
}

const deleteData = () => {
  ElMessageBox.confirm('It will delete power grid data file from database. Continue?', 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(() => {
    deleteDevicesData(projectId)
    clearData()
    ElMessage({
      type: 'success',
      message: 'Delete completed',
    })
  }).catch(() => {
    ElMessage({
      type: 'info',
      message: 'Delete canceled',
    })
  })
  map.value.getContainer().style.cursor = "default"
}

// --------------------- set properties table data ---------------------
const t_bus = ref({
  vn_kv: 0.0,
  max_vm_pu: 0.0,
  min_vm_pu: 0.0,
  is_service: true
})
const t_load = ref({
  p_mw: 0.0,
  q_mvar: 0.0,
  const_z_percent: 0.0,
  const_i_percent: 0.0,
  scaling: 1.0,
  type: "wye",
  controllable: true,
  max_p_mw: 0.0,
  min_p_mw: 0.0,
  max_q_mvar: 0.0,
  min_q_mvar: 0.0,
  is_service: true,
})
const t_transformers = ref({
  id: -1,
  hv_bus: "",
  lv_bus: "",
  sn_mva: 0.0,
  vn_hv_kv: 0.0,
  vn_lv_kv: 0.0,
  vk_percent: 0.0,
  vkr_percent: 0.0,
  pfe_kw: 0.0,
  i0_percent: 0.0,
  shift_degree: 0.0,
  tap_side: "hv",
  tap_neutral: 0,
  tap_min: 0,
  tap_max: 0,
  tap_step_percent: 0.0,
  tap_step_degree: 0.0,
  tap_changer_type: "Ratio",
  max_loading_percent: 0.0,
  is_service: true,
  oltc: true,
  power_station_unit: true,
  leakage_resistance_ratio_hv: 0.5,
  leakage_reactance_ratio_hv: 0.5,
})
const t_transformers3 = ref({
  id: -1,
  hv_bus: "",
  lv_bus: "",
  vn_hv_kv: 0.0,
  vn_lv_kv: 0.0,
  vn_mv_kv: 0.0,
  sn_hv_mva: 0.0,
  sn_mv_mva: 0.0,
  sn_lv_mva: 0.0,
  vk_hv_percent: 0.0,
  vk_mv_percent: 0.0,
  vk_lv_percent: 0.0,
  vkr_hv_percent: 0.0,
  vkr_mv_percent: 0.0,
  vkr_lv_percent: 0.0,
  pfe_kw: 0.0,
  i0_percent: 0.0,
  shift_mv_degree: 0.0,
  shift_lv_degree: 0.0,
  tap_side: "hv",
  tap_neutral: 0,
  tap_min: 0,
  tap_max: 0,
  tap_step_percent: 0.0,
  tap_changer_type: "Ratio",
  is_service: true,
})
// source, solar, wind
const t_source = ref({
  p_mw: 0.0,
  vm_pu: 1.0,
  scaling: 1.0,
  max_p_mw: 0.0,
  min_p_mw: 0.0,
  max_q_mvar: 0.0,
  min_q_mvar: 0.0,
  power_station_trafo: 0,
  is_service: true,
})
const t_outer = ref({
  vm_pu: 1.0,
  va_degree: 0.0,
  max_p_mw: 0.0,
  min_p_mw: 0.0,
  max_q_mvar: 0.0,
  min_q_mvar: 0.0,
  is_service: true
})

const clickProperty = (key: string) => {
  deviceProperty.value.type = devices[key].type
  deviceProperty.value.key = key
  if (devices[key].properties !== null) {
    if (devices[key].type === "bus") t_bus.value = devices[key].properties
    else if (devices[key].type === "load") t_load.value = devices[key].properties
    else if (devices[key].type === "transformer") t_transformers.value = devices[key].properties
    else if (devices[key].type === "transformer3") t_transformers3.value = devices[key].properties
    else if (devices[key].type === "source") t_source.value = devices[key].properties
    else if (devices[key].type === "wind") t_source.value = devices[key].properties
    else if (devices[key].type === "solar") t_source.value = devices[key].properties
    else if (devices[key].type === "outer") t_outer.value = devices[key].properties
  }
}

const changeTransformers = () => {
  for (const item of transformers) {
    if (item.id === t_transformers.value.id) {
      t_transformers.value.sn_mva = item.snMva
      t_transformers.value.vn_hv_kv = item.vnHvKv
      t_transformers.value.vn_lv_kv = item.vnLvKv
      t_transformers.value.vk_percent = item.vkPercent
      t_transformers.value.vkr_percent = item.vkrPercent
      t_transformers.value.pfe_kw = item.pfeKw
      t_transformers.value.i0_percent = item.i0Percent
      t_transformers.value.shift_degree = item.shiftDegree
      t_transformers.value.tap_side = item.tapSide
      t_transformers.value.tap_neutral = item.tapNeutral
      t_transformers.value.tap_min = item.tapMin
      t_transformers.value.tap_max = item.tapMax
      t_transformers.value.tap_step_percent = item.tapStepPercent
      t_transformers.value.tap_step_degree = item.tapStepDegree
      t_transformers.value.tap_changer_type = item.tapChangerType
    }
  }
}

const changeTransformers3 = () => {
  for (const item of transformers3) {
    if (item.id === t_transformers3.value.id) {
      t_transformers3.value.vn_hv_kv = item.vnHvKv
      t_transformers3.value.vn_lv_kv = item.vnLvKv
      t_transformers3.value.vn_mv_kv = item.vnMvKv
      t_transformers3.value.sn_hv_mva = item.snHvMva
      t_transformers3.value.sn_lv_mva = item.snLvMva
      t_transformers3.value.sn_mv_mva = item.snMvMva
      t_transformers3.value.vk_hv_percent = item.vkHvPercent
      t_transformers3.value.vk_mv_percent = item.vkMvPercent
      t_transformers3.value.vk_lv_percent = item.vkLvPercent
      t_transformers3.value.vkr_hv_percent = item.vkrHvPercent
      t_transformers3.value.vkr_mv_percent = item.vkrMvPercent
      t_transformers3.value.vkr_lv_percent = item.vkrLvPercent
      t_transformers3.value.pfe_kw = item.pfeKw
      t_transformers3.value.i0_percent = item.i0Percent
      t_transformers3.value.shift_mv_degree = item.shiftMvDegree
      t_transformers3.value.shift_lv_degree = item.shiftLvDegree
      t_transformers3.value.tap_side = item.tapSide
      t_transformers3.value.tap_neutral = item.tapNeutral
      t_transformers3.value.tap_min = item.tapMin
      t_transformers3.value.tap_max = item.tapMax
      t_transformers3.value.tap_step_percent = item.tapStepPercent
      t_transformers3.value.tap_changer_type = item.tapChangerType
    }
  }
}


const t_lines = ref({
  id: -1,
  r_ohm_per_km: 0.0,
  x_ohm_per_km: 0.0,
  c_nf_per_km: 0.0,
  g_us_per_km: 0.0,
  max_i_ka: 0.0,
  parallel: 1,
  df: 1.0,
  max_loading_percent: 0.0,
  q_mm2: 0,
  alpha: 0.0,
  type: "ol",
  is_service: false
})

const clickLinesProperty = (key: string) => {
  lineProperty.value.key = key
  lineProperty.value.length = lines[key].length
  if (lines[key].properties !== null) {
    t_lines.value = lines[key].properties
  }
}

const changeLines = () => {
  for (const item of aclines) {
    if (item.id === t_lines.value.id) {
      t_lines.value.id = item.id
      t_lines.value.r_ohm_per_km = item.rohmPerKm
      t_lines.value.x_ohm_per_km = item.xohmPerKm
      t_lines.value.c_nf_per_km = item.cnfPerKm
      t_lines.value.max_i_ka = item.maxIKa
      t_lines.value.type = item.type
    }
  }
}


// --------------------------- other tools -------------------------
const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number) => {
  console.log('lat1, lon1, lat2, lon2', lat1, lon1, lat2, lon2)
  let R = 6371;
  let dLat = deg2rad(lat2 - lat1)
  let dLon = deg2rad(lon2 - lon1)
  let a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
  let c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  let d = R * c;
  return d.toFixed(2);
}

const deg2rad = (deg: number) => {
  return deg * (Math.PI / 180)
}
</script>

<template>
  <div class="container">
    <el-tooltip effect="dark" content="upload personal data" placement="right">
      <el-button style="margin: 10px 4px 4px 4px; " type="success" @click="uploadVisible = true" :icon="Upload" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="select a power device on the map" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="settingDevice" :icon="Pointer" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="connect two device" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="connectDevices" :icon="Share" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="select a device to set properties" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="setProperties" :icon="Edit" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="Save all data" placement="right">
      <el-button style="margin: 4px; " type="success" @click="saveData" :icon="Select" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="clear all data temporarily" placement="right">
      <el-button style="margin: 4px; " type="warning" @click="clearData" :icon="Failed" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="delete data include database" placement="right">
      <el-button style="margin: 4px; " type="danger" @click="deleteData" :icon="Delete" circle size="large"/>
    </el-tooltip><br>
  </div>
  <div id="map"></div>

<!-- upload personal data -->
  <el-dialog v-model="uploadVisible" title="Upload your personal data" width="800">
    <el-form-item label="File type:">
      <el-select v-model="uploadType" placeholder="Select" size="large" style="width: 240px">
        <el-option label="Power Devices" value="devices"/>
        <el-option label="Power Lines" value="lines"/>
      </el-select>
    </el-form-item>
    <el-upload class="avatar-uploader" :http-request="uploadData" :limit="1" action="Action">
      <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
    </el-upload>
  </el-dialog>

<!-- set lines property -->
  <el-dialog v-model="linePropertyVisible" title="Set Property" width="500">
    <el-form-item label="Line length(km)">
      <el-input v-model="lineProperty.length" autocomplete="off" />
    </el-form-item>
    <el-form-item label="Line type">
      <el-select v-model="t_lines.id" @change="changeLines" placeholder="Select" size="large" style="width: 240px">
        <el-option label="self-defined" :value="-1"/>
        <el-option v-for="item in aclines" :label="item.name" :value="item.id"/>
      </el-select>
    </el-form-item>
    <el-form-item label="resistance of the line(Ohm/km)*">
      <el-input v-model="t_lines.r_ohm_per_km" autocomplete="off" :disabled="t_lines.id !== -1" type="number" min="0"/>
    </el-form-item>
    <el-form-item label="reactance of the line(Ohm/km)*">
      <el-input v-model="t_lines.x_ohm_per_km" autocomplete="off" :disabled="t_lines.id !== -1" type="number" min="0"/>
    </el-form-item>
    <el-form-item label="capacitance of the line(nano Farad/km)*">
      <el-input v-model="t_lines.c_nf_per_km" autocomplete="off" :disabled="t_lines.id !== -1" type="number" min="0"/>
    </el-form-item>
    <el-form-item label="dielectric conductance of the line**">
      <el-input v-model="t_lines.g_us_per_km" autocomplete="off" type="number" min="0"/>
    </el-form-item>
    <el-form-item label="maximal thermal current(kA)*">
      <el-input v-model="t_lines.max_i_ka" autocomplete="off" :disabled="t_lines.id !== -1" type="number" min="0" />
    </el-form-item>
    <el-form-item label="number of parallel line systems**">
      <el-input v-model="t_lines.parallel" autocomplete="off" type="number" min="1"/>
    </el-form-item>
    <el-form-item label="derating factor (scaling) for maximal thermal current**">
      <el-input v-model="t_lines.df" autocomplete="off" type="number" min="0" max="1" step="0.01" />
    </el-form-item>
    <el-form-item label="Type of line">
      <el-select v-model="t_lines.type" placeholder="Select" size="large" style="width: 240px" :disabled="t_lines.id !== -1">
        <el-option label="overhead line" value="ol"/>
        <el-option label="underground cable system" value="cs"/>
      </el-select>
    </el-form-item>
    <el-form-item label="Maximum loading of the line**">
      <el-input v-model="t_lines.max_loading_percent" autocomplete="off" type="number" step="0.1" min="0"/>
    </el-form-item>
    <el-form-item>
      <el-checkbox v-model="t_lines.is_service" label="Service Start*" size="large" />
    </el-form-item>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="linePropertyVisible = false">Cancel</el-button>
        <el-button type="primary" @click="setLinesProperties">Confirm</el-button>
      </div>
    </template>
  </el-dialog>

<!-- set devices property -->
  <el-dialog v-model="devicePropertyVisible" title="Set Property" width="500">
    <el-form-item label="Device type">
      <el-select v-model="deviceProperty.type" placeholder="Select" size="large" style="width: 240px">
        <el-option label="Bus" value="bus" />
        <el-option label="Load" value="load" />
        <el-option label="Transformer" value="transformer" />
        <el-option label="Three Winding Transformer" value="transformer3" />
        <el-option label="Generator" value="source" />
        <el-option label="Solar" value="solar" />
        <el-option label="Wind" value="wind" />
        <el-option label="External Grid" value="outer" />
      </el-select>
    </el-form-item>
    <div v-if="deviceProperty.type === 'bus'">
      <el-form-item label="Bus rated voltage (kV)*">
        <el-input v-model="t_bus.vn_kv" autocomplete="off" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="Maximum voltage (kV)**">
        <el-input v-model="t_bus.max_vm_pu" autocomplete="off" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="Minimum voltage (kV)**">
        <el-input v-model="t_bus.min_vm_pu" autocomplete="off" min="0" type="number"/>
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="t_bus.is_service" label="Service Start*" size="large" />
      </el-form-item>
    </div>
    <div v-else-if="deviceProperty.type === 'load'">
      <el-form-item label="active power of the load(MW)*">
        <el-input v-model="t_load.p_mw" autocomplete="off" min="0" type="number" />
      </el-form-item>
      <el-form-item label="reactive power of the load(MVar)*">
        <el-input v-model="t_load.q_mvar" autocomplete="off" type="number" />
      </el-form-item>
      <el-form-item label-position="top" label="percentage of active and reactive power that is associated to constant impedance load at rated voltage(%)*">
        <el-input v-model="t_load.const_z_percent" autocomplete="off" min="0" max="100" type="number"/>
      </el-form-item>
      <el-form-item label-position="top" label="percentage of active and reactive power that is associated to constant current load at rated voltage(%)*">
        <el-input v-model="t_load.const_i_percent" autocomplete="off" min="0" max="100" type="number" />
      </el-form-item>
      <el-form-item label="scaling factor for active and reactive power*">
        <el-input v-model="t_load.scaling" autocomplete="off" min="0" type="number" />
      </el-form-item>
      <el-form-item label="Maximum active power**">
        <el-input v-model="t_load.max_p_mw" autocomplete="off" type="number"/>
      </el-form-item>
      <el-form-item label="Minimum active power**">
        <el-input v-model="t_load.min_p_mw" autocomplete="off" type="number"/>
      </el-form-item>
      <el-form-item label="Maximum reactive power**">
        <el-input v-model="t_load.max_q_mvar" autocomplete="off" type="number"/>
      </el-form-item>
      <el-form-item label="Minimum reactive power**">
        <el-input v-model="t_load.min_q_mvar" autocomplete="off" type="number"/>
      </el-form-item>
      <el-form-item label="Connection Type of 3 Phase Load*">
        <el-select v-model="t_load.type" placeholder="Select" size="large" style="width: 240px">
          <el-option label="wye" value="wye"/>
          <el-option label="delta" value="delat"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="t_load.controllable" label="Controllabel**" size="large" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="t_load.is_service" label="Service Start*" size="large" />
      </el-form-item>
    </div>
    <div v-else-if="deviceProperty.type === 'outer'">
      <el-form-item label="voltage set point(p.u)*">
        <el-input v-model="t_outer.vm_pu" autocomplete="off" min="0" type="number" />
      </el-form-item>
      <el-form-item label="angle set point(degree)*">
        <el-input v-model="t_outer.va_degree" autocomplete="off" />
      </el-form-item>
      <el-form-item label="Maximum active power**">
        <el-input v-model="t_outer.max_p_mw" autocomplete="off" type="number" />
      </el-form-item>
      <el-form-item label="Minimum active power**">
        <el-input v-model="t_outer.min_p_mw" autocomplete="off" type="number" />
      </el-form-item>
      <el-form-item label="Maximum reactive power**">
        <el-input v-model="t_outer.max_q_mvar" autocomplete="off" type="number" />
      </el-form-item>
      <el-form-item label="Minimum reactive power**">
        <el-input v-model="t_outer.min_q_mvar" autocomplete="off" type="number" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="t_outer.is_service" label="Service Start*" size="large" />
      </el-form-item>
    </div>
    <div v-else-if="deviceProperty.type === 'transformer'">
      <el-form-item label="Transformer type">
        <el-select v-model="t_transformers.id" @change="changeTransformers" size="large" style="width: 240px">
          <el-option label="Self-defined" :value="-1"/>
          <el-option v-for="item in transformers" :label="item.name" :value="item.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="high voltage bus index of the transformer*">
        <el-input v-model="t_transformers.hv_bus" autocomplete="off" type="text" />
      </el-form-item>
      <el-form-item label="low voltage bus index of the transformer*">
        <el-input v-model="t_transformers.lv_bus" autocomplete="off" type="text" />
      </el-form-item>
      <el-form-item label="rated apparent power of the transformer(MVA)*">
        <el-input v-model="t_transformers.sn_mva" autocomplete="off" :disabled="t_transformers.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="rated voltage at high voltage bus(kV)*">
        <el-input v-model="t_transformers.vn_hv_kv" autocomplete="off" :disabled="t_transformers.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="rated voltage at low voltage bus(kV)*">
        <el-input v-model="t_transformers.vn_lv_kv" autocomplete="off" :disabled="t_transformers.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="short circuit voltage(%)*">
        <el-input v-model="t_transformers.vk_percent" autocomplete="off" :disabled="t_transformers.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="real component of short circuit voltage(%)*">
        <el-input v-model="t_transformers.vkr_percent" autocomplete="off" :disabled="t_transformers.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="iron losses(kW)*">
        <el-input v-model="t_transformers.pfe_kw" autocomplete="off" :disabled="t_transformers.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="open loop losses in(%)*">
        <el-input v-model="t_transformers.i0_percent" autocomplete="off" :disabled="t_transformers.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="transformer phase shift angle*">
        <el-input v-model="t_transformers.shift_degree" autocomplete="off" :disabled="t_transformers.id !== -1" type="number"/>
      </el-form-item>
      <el-form-item label="defines if tap changer is at the high- or low voltage side">
        <el-select v-model="t_transformers.tap_side" placeholder="Select" size="large" style="width: 240px" :disabled="t_transformers.id !== -1">
          <el-option label="hv" value="hv" />
          <el-option label="lv" value="lv" />
        </el-select>
      </el-form-item>
      <el-form-item label="rated tap position">
        <el-input-number v-model="t_transformers.tap_neutral" autocomplete="off" :disabled="t_transformers.id !== -1"/>
      </el-form-item>
      <el-form-item label="minimum tap position">
        <el-input-number v-model="t_transformers.tap_min" autocomplete="off" :disabled="t_transformers.id !== -1"/>
      </el-form-item>
      <el-form-item label="maximum tap position">
        <el-input-number v-model="t_transformers.tap_max" autocomplete="off" :disabled="t_transformers.id !== -1"/>
      </el-form-item>
      <el-form-item label="tap step size for voltage magnitude(%)">
        <el-input v-model="t_transformers.tap_step_percent" autocomplete="off" :disabled="t_transformers.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="tap step size for voltage angle">
        <el-input v-model="t_transformers.tap_step_degree" autocomplete="off" :disabled="t_transformers.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="specifies the tap changer type">
        <el-select v-model="t_transformers.tap_changer_type" placeholder="Select" size="large" style="width: 240px" :disabled="t_transformers.id !== -1">
          <el-option label="Ratio" value="Ratio" />
          <el-option label="Symmetrical" value="Symmetrical" />
          <el-option label="Ideal" value="Ideal" />
          <el-option label="Tabular" value="Tabular" />
        </el-select>
      </el-form-item>
      <el-form-item label-position="top" label="Maximum loading of the transformer with respect to sn_mva and its corresponding current at 1.0 p.u.**">
        <el-input v-model="t_transformers.max_loading_percent" autocomplete="off" min="0" type="number" />
      </el-form-item>
      <el-form-item label-position="top" label="ratio of transformer short-circuit resistance on HV side*">
        <el-input v-model="t_transformers.leakage_resistance_ratio_hv" autocomplete="off" type="number" min="0" max="1" step="0.1"/>
      </el-form-item>
      <el-form-item label-position="top" label="ratio of transformer short-circuit reactance on HV side*">
        <el-input v-model="t_transformers.leakage_reactance_ratio_hv" autocomplete="off" type="number" min="0" max="1" step="0.1"/>
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="t_transformers.oltc" label="specifies if the transformer has an OLTC*" size="large" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="t_transformers.power_station_unit" label="specifies if the transformer is part of a power_station_unit*" size="large" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="t_transformers.is_service" label="Service Start*" size="large" />
      </el-form-item>
    </div>
    <div v-else-if="deviceProperty.type === 'transformer3'">
      <el-form-item label="Three Winding Transformer type">
        <el-select v-model="t_transformers3.id" @change="changeTransformers3" size="large" style="width: 240px">
          <el-option label="Self-defined" :value="-1"/>
          <el-option v-for="item in transformers3" :label="item.name" :value="item.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="high voltage bus index of the transformer*">
        <el-input v-model="t_transformers3.hv_bus" autocomplete="off" type="text" />
      </el-form-item>
      <el-form-item label="low voltage bus index of the transformer*">
        <el-input v-model="t_transformers3.lv_bus" autocomplete="off" type="text" />
      </el-form-item>
      <el-form-item label="rated voltage at high voltage bus(kV)*">
        <el-input v-model="t_transformers3.vn_hv_kv" autocomplete="off" :disabled="t_transformers3.id !== -1" type="number"/>
      </el-form-item>
      <el-form-item label="rated voltage at medium voltage bus(kV)*">
        <el-input v-model="t_transformers3.vn_mv_kv" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="rated voltage at low voltage bus(kV)*">
        <el-input v-model="t_transformers3.vn_lv_kv" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="rated apparent power on high voltage side(kVA)*">
        <el-input v-model="t_transformers3.sn_hv_mva" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="rated apparent power on medium voltage side(kVA)*">
        <el-input v-model="t_transformers3.sn_mv_mva" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="rated apparent power on low voltage side(kVA)*">
        <el-input v-model="t_transformers3.sn_lv_mva" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="short circuit voltage from high to medium voltage(%)*">
        <el-input v-model="t_transformers3.vk_hv_percent" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="short circuit voltage from medium to low voltage(%)*">
        <el-input v-model="t_transformers3.vk_mv_percent" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="short circuit voltage from high to low voltage(%)*">
        <el-input v-model="t_transformers3.vk_lv_percent" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label-position="top" label="real part of short circuit voltage from high to medium voltage(%)*">
        <el-input v-model="t_transformers3.vkr_hv_percent" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label-position="top" label="real part of short circuit voltage from medium to low voltage(%)*">
        <el-input v-model="t_transformers3.vkr_mv_percent" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label-position="top" label="real part of short circuit voltage from high to low voltage(%)*">
        <el-input v-model="t_transformers3.vkr_lv_percent" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="iron losses(kW)*">
        <el-input v-model="t_transformers3.pfe_kw" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="open loop losses in(%)*">
        <el-input v-model="t_transformers3.i0_percent" autocomplete="off" :disabled="t_transformers3.id !== -1" min="0" type="number"/>
      </el-form-item>
      <el-form-item label="transformer phase shift angle at the MV side">
        <el-input v-model="t_transformers3.shift_mv_degree" autocomplete="off" :disabled="t_transformers3.id !== -1" type="number"/>
      </el-form-item>
      <el-form-item label="transformer phase shift angle at the LV side">
        <el-input v-model="t_transformers3.shift_lv_degree" autocomplete="off" :disabled="t_transformers3.id !== -1" type="number"/>
      </el-form-item>
      <el-form-item label="defines if tap changer is at the high- or low voltage side">
        <el-select v-model="t_transformers3.tap_side" placeholder="Select" size="large" style="width: 240px" :disabled="t_transformers3.id !== -1">
          <el-option label="hv" value="hv" />
          <el-option label="mv" value="mv" />
          <el-option label="lv" value="lv" />
        </el-select>
      </el-form-item>
      <el-form-item label="rated tap position">
        <el-input-number v-model="t_transformers3.tap_neutral" autocomplete="off" :disabled="t_transformers3.id !== -1"/>
      </el-form-item>
      <el-form-item label="minimum tap position">
        <el-input-number v-model="t_transformers3.tap_min" autocomplete="off" :disabled="t_transformers3.id !== -1"/>
      </el-form-item>
      <el-form-item label="maximum tap position">
        <el-input-number v-model="t_transformers3.tap_max" autocomplete="off" :disabled="t_transformers3.id !== -1"/>
      </el-form-item>
      <el-form-item label="tap step size for voltage magnitude(%)">
        <el-input v-model="t_transformers3.tap_step_percent" autocomplete="off" :disabled="t_transformers3.id !== -1" type="number" min="0"/>
      </el-form-item>
      <el-form-item label="specifies the tap changer type">
        <el-select v-model="t_transformers3.tap_changer_type" placeholder="Select" size="large" style="width: 240px" :disabled="t_transformers3.id !== -1">
          <el-option label="Ratio" value="Ratio" />
          <el-option label="Symmetrical" value="Symmetrical" />
          <el-option label="Ideal" value="Ideal" />
          <el-option label="Tabular" value="Tabular" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="t_transformers3.is_service" label="Service Start*" size="large" />
      </el-form-item>
    </div>
    <div v-else>
      <el-form-item label="the real power of the generator(MW)*">
        <el-input v-model="t_source.p_mw" autocomplete="off" max="0" type="number"/>
      </el-form-item>
      <el-form-item label="voltage set point of the generator(p.u)*">
        <el-input v-model="t_source.vm_pu" autocomplete="off" type="number"/>
      </el-form-item>
      <el-form-item label="scaling factor for the active power*">
        <el-input v-model="t_source.scaling" autocomplete="off" type="number" />
      </el-form-item>
      <el-form-item label="Maximum active power**">
        <el-input v-model="t_source.max_p_mw" autocomplete="off" type="number" />
      </el-form-item>
      <el-form-item label="Minimum active power**">
        <el-input v-model="t_source.min_p_mw" autocomplete="off" type="number" />
      </el-form-item>
      <el-form-item label="Maximum reactive power**">
        <el-input v-model="t_source.max_q_mvar" autocomplete="off" type="number" />
      </el-form-item>
      <el-form-item label="Minimum reactive power**">
        <el-input v-model="t_source.min_q_mvar" autocomplete="off" type="number" />
      </el-form-item>
      <el-form-item label="index of the power station trafo*">
        <el-input-number v-model="t_source.power_station_trafo" autocomplete="off"/>
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="t_source.is_service" label="Service Start*" size="large" />
      </el-form-item>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="devicePropertyVisible = false">Cancel</el-button>
        <el-button type="primary" @click="setDevicesProperties">Confirm</el-button>
      </div>
    </template>
  </el-dialog>
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
</style>
