<script setup lang="ts">
import {Delete, Edit, Failed, Pointer, Select, Upload} from "@element-plus/icons-vue";
import {onMounted, ref} from "vue";
import {ElMessage, ElMessageBox, type UploadRequestOptions} from "element-plus";
import {
  deleteODDemandsData, deleteStartEnd, getNodesData, getODPairsData, getStationsData,
  saveODDemandsData, saveStartEnd,
  uploadDataService
} from "@/api/load";
import {useRoute} from "vue-router";
import {getProjectService} from "@/api/project";
import * as L from "leaflet";
import {layerGroup} from "leaflet";
// icons
import startImg from "@/assets/start_point.svg"
import endImg from "@/assets/end_point.svg"

// base data
const route = useRoute();
const projectId = route.query.projectId

// icons
const getIcon = (url: string) => {
  return L.icon({iconUrl: url, iconSize: [38, 38], iconAnchor: [19, 19]})
}
const startIcon = getIcon(startImg)
const endIcon = getIcon(endImg)

// basic map data
const map = ref()
const nodesGroup = layerGroup()
const odsGroup = layerGroup()
const highlight = layerGroup()
let nodes: any
let odPairs: {[key: string]: any} = {}
let setODFlag = false
let propertyFlag = false
const properties = ref<{[key: string]: any}>({})
let tmpO: any
let tmpD: any

// dialog data
const uploadVisible = ref(false)
const propertiesVisible = ref(false)

// -------------------------------------- on mounted -------------------------------
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
    "OD pairs Layer": odsGroup
  }
  L.control.layers(baseLayers, featureLayers).addTo(map.value)
  highlight.addTo(map.value)

  map.value.on("zoomend", () => {
    if (map.value.hasLayer(nodesGroup)) {
      map.value.removeLayer(nodesGroup)
      map.value.addLayer(nodesGroup)
    }
    if (map.value.hasLayer(odsGroup)) {
      map.value.removeLayer(odsGroup)
      map.value.addLayer(odsGroup)
    }
  })

  map.value.getContainer().style.cursor = "default"
}
// set traffic junction data
const setNodes = (nodes: any) => {
  for (const key in nodes) {
    const node = L.marker(nodes[key].point).on("click", () => {
      if (setODFlag) {
        if (tmpO === undefined) {
          tmpO = key
          L.marker(nodes[key].point, {icon: startIcon}).addTo(highlight)
        }
        else if (tmpD === undefined) {
          highlight.clearLayers()
          tmpD = key
          const odKey = tmpO + "To" + tmpD
          odPairs = {}
          odPairs[odKey] = {
            O: tmpO,
            D: tmpD,
          }
          setOneOD(odKey)
          L.marker(nodes[key].point, {icon: endIcon}).addTo(highlight)
          L.polyline([nodes[tmpO].point, nodes[tmpD].point], {color: "#FF6622"}).addTo(highlight)

          tmpO = undefined
          tmpD = undefined
        }
      }
    }).bindPopup("id: " + key)
    nodesGroup.addLayer(node)
  }
}
// set od data from database
const setODs = (odPairs: any) => {
  for (const key in odPairs) {
    setOneOD(key)
  }
}
const setOneOD = (key: string) => {
  const D = L.marker(nodes[odPairs[key].D].point, {icon: endIcon})
  const O = L.marker(nodes[odPairs[key].O].point, {icon: startIcon}).on("contextmenu", () => {
    delete odPairs[key]
    odsGroup.removeLayer(O)
    highlight.clearLayers()
  }).on("click", () => {
    highlight.clearLayers()
    highlight.addLayer(D)
    L.polyline([nodes[odPairs[key].O].point, nodes[odPairs[key].D].point], {color: "#FF6622"}).addTo(highlight)
  }).bindPopup("id: " + key + ", weight: " + odPairs[key].weight)
  odsGroup.addLayer(O)
}

const updateData = async () => {
  clearData()
  let odDemand = await getStationsData(projectId) as any
  let nodesRet = await getNodesData(projectId) as any
  if (nodesRet.data.nodes !== null) {
    nodes = nodesRet.data.nodes
    setNodes(nodes)
  }
  if (odDemand.data.odPairs !== null) {
    odPairs = odDemand.data.odPairs
    setODs(odPairs)
  }
}

// ------------------------------------ main function -------------------------------
const uploadData = async (param: UploadRequestOptions) => {
  await uploadDataService(projectId, "start-end", param.file)
  ElMessage.success({
    message: "Upload and update personal data successfully.",
    plain: true,
  })
  uploadVisible.value = false
}

const selectOD = () => {
  tmpD = undefined
  tmpO = undefined
  highlight.clearLayers()
  setODFlag = true
  nodesGroup.addTo(map.value)
  odsGroup.clearLayers()
}

const saveData = async () => {
  setODFlag = false
  console.log(odPairs)
  const data = {
    "projectId": projectId,
    "startEnd": JSON.stringify(odPairs)
  }
  await saveStartEnd(data)
  ElMessage.success({
    message: "save all data successfully!",
    plain: true,
  })
  await updateData()
}

const clearData = () => {
  setODFlag = false
  propertyFlag = false
  odsGroup.clearLayers()
  highlight.clearLayers()
  odPairs = {}
}

const deleteData = () => {
  setODFlag = false
  propertyFlag = false
  odsGroup.clearLayers()
  clearData()
  ElMessageBox.confirm('It will delete all od pairs data file from database. Continue?', 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(() => {
    deleteStartEnd(projectId)
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
</script>

<template>
  <div class="container">
    <el-tooltip effect="dark" content="upload personal data" placement="right">
      <el-button style="margin: 10px 4px 4px 4px; " type="success" @click="uploadVisible = true" :icon="Upload" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="select a node to become an element of OD pair" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="selectOD" :icon="Pointer" circle size="large"/>
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
