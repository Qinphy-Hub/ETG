<script setup lang="ts">
import {Pointer, Edit, Delete, Failed, Plus, Select, Upload} from "@element-plus/icons-vue";
import {onMounted, ref} from "vue";
import {ElMessage, ElMessageBox, type UploadRequestOptions} from "element-plus";
import {
  deleteStationsData,
  getNodesData,
  getStationsData,
  saveStationsData,
  uploadDataService
} from "@/api/load";
import {useRoute} from "vue-router";
import {getProjectService} from "@/api/project";
import * as L from "leaflet";
import {layerGroup} from "leaflet";
// icons
import chargingImg from "@/assets/charging-station.svg"
import swappingImg from "@/assets/swapping-station.svg"

// base data
const route = useRoute();
const projectId = route.query.projectId

// icons
const getIcon = (url: string) => {
  return L.icon({iconUrl: url, iconSize: [38, 38], iconAnchor: [19, 19]})
}
const chargingIcon = getIcon(chargingImg)
const swappingIcon = getIcon(swappingImg)

// basic map data
const map = ref()
const nodesGroup = layerGroup()
const stationsGroup = layerGroup()
let nodes: any
let stations: {[key: string]: any} = {}
let setStationFlag = false
let propertyFlag = false
// set properties' middle variable
const properties = ref<{[key: string]: any}>({})

// dialog data
const uploadVisible = ref(false)
const propertiesVisible = ref(false)

// ------------------------------------- on mounted --------------------------------------
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
    "Traffic Junctions": nodesGroup,
    "Stations": stationsGroup
  }
  L.control.layers(baseLayers, featureLayers).addTo(map.value)

  map.value.on("zoomend", () => {
    if (map.value.hasLayer(nodesGroup)) {
      map.value.removeLayer(nodesGroup)
      map.value.addLayer(nodesGroup)
    }
    if (map.value.hasLayer(stationsGroup)) {
      map.value.removeLayer(stationsGroup)
      map.value.addLayer(stationsGroup)
    }
  })

  map.value.getContainer().style.cursor = "default"
}
// set nodes to locate the stations
const setNodes = (nodes: any) => {
  for (const key in nodes) {
    const node = L.marker(nodes[key].point).on("click", () => {
      if (setStationFlag && !(key in stations)) {
        stations[key] = {
          "point": nodes[key].point,
          "type": "charging",
          "data": []
        }
        setOneStation(key)
      }
    }).bindPopup("id: " + key)
    nodesGroup.addLayer(node)
  }
}
// set all stations on the map
const setStations = (stations: any) => {
  for (const key in stations) {
    setOneStation(key)
  }
}
// set a station
const setOneStation = (key: string) => {
  let setIcon = chargingIcon
  if (stations[key].type === "swapping") setIcon = swappingIcon
  const station = L.marker(stations[key].point, {icon: setIcon}).on("contextmenu", () => {
    delete stations[key]
    stationsGroup.removeLayer(station)
  }).on("click", () => {
    if (propertyFlag) {
      properties.value["key"] = key
      properties.value["type"] = stations[key].type
      properties.value["data"] = stations[key].data
      propertiesVisible.value = true
    }
  })
  stationsGroup.addLayer(station)
}
// save settings
const saveProperties = () => {
  const key = properties.value.key
  stations[key].type = properties.value.type
  stations[key].data = properties.value.data
  propertiesVisible.value = false
}

const updateData = async () => {
  let stationsRet = await getStationsData(projectId) as any
  let nodesRet = await getNodesData(projectId) as any
  if (nodesRet.data.nodes !== null) nodes = nodesRet.data.nodes
  if (stationsRet.data.stations !== null) stations = stationsRet.data.stations
  setNodes(nodes)
  setStations(stations)
}

// -------------------------------------- main function ----------------------------------
const uploadData = async (param: UploadRequestOptions) => {
  await uploadDataService(projectId, "stations", param.file)
  ElMessage.success({
    message: "Upload and update personal data successfully.",
    plain: true,
  })
  uploadVisible.value = false
  window.location.reload()
}

const selectStation = () => {
  map.value.addLayer(nodesGroup)
  map.value.addLayer(stationsGroup)
  setStationFlag = true
  propertyFlag = false
}

const saveData = () => {
  const data = {
   "projectId": projectId,
   "stations": JSON.stringify(stations),
  }
  saveStationsData(data)
  ElMessage.success({
    message: "save all data successfully!",
    plain: true,
  })
  window.location.reload()
}

const clearData = () => {
  stations = {}
  stationsGroup.clearLayers()
}

const deleteData = () => {
  ElMessageBox.confirm('It will delete stations data file from database. Continue?', 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(() => {
    deleteStationsData(projectId)
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
}

const setProperties = () => {
  map.value.removeLayer(nodesGroup)
  map.value.addLayer(stationsGroup)
  setStationFlag = false
  propertyFlag = true
}

const addItem = () => {
  properties.value.data.push({
    "name": "type2",
    "power": 0,
    "num": 0
  })
}
</script>

<template>
  <div class="container">
    <el-tooltip effect="dark" content="upload personal data" placement="right">
      <el-button style="margin: 10px 4px 4px 4px; " type="success" @click="uploadVisible = true" :icon="Upload" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="select a node to become a station" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="selectStation" :icon="Pointer" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="select a station to set properties" placement="right">
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
    <el-upload class="avatar-uploader" :http-request="uploadData" :limit="1" action="Action">
      <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
    </el-upload>
  </el-dialog>

<!-- set properties -->
  <el-dialog v-model="propertiesVisible" title="Settings" width="500">
    <el-select v-model="properties.type" size="large" style="width: 240px">
      <el-option key="charging" label="charging" value="charging"/>
      <el-option key="swapping" label="swapping" value="swapping"/>
    </el-select> &nbsp; Station
    <el-table :data="properties.data" style="width: 100%" max-height="240">
      <el-table-column prop="name" label="Battery/Pile Type Name" align="center">
        <template #default="scope">
          <el-input v-model="properties.data[scope.$index].name" />
        </template>
      </el-table-column>
      <el-table-column prop="power" label="Rated Power" align="center">
        <template #default="scope">
          <el-input-number v-model="properties.data[scope.$index].power" :precision="2" :min="0" :max="100000" style="width: 100%;" />
        </template>
      </el-table-column>
      <el-table-column prop="num" label="Number" align="center">
        <template #default="scope">
          <el-input-number v-model="properties.data[scope.$index].num" :min="0" :max="100000" style="width: 100%;" />
        </template>
      </el-table-column>
    </el-table>
    <el-button style="width: 100%" @click="addItem">Add Type</el-button>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="propertiesVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveProperties">Confirm</el-button>
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
