<script setup lang="ts">
// router
import {useRoute} from "vue-router";
// element-plus
import {Crop, Delete, LocationInformation, Select, Upload, Failed, Plus} from "@element-plus/icons-vue";
import {ElLoading, ElMessage, ElMessageBox, ElNotification, type UploadRequestOptions} from "element-plus";
// leaflet
import * as L from "leaflet";
import {onMounted, ref} from "vue";
import {layerGroup} from "leaflet";
import "leaflet/dist/leaflet.css";
import {GeoSearchControl, OpenStreetMapProvider} from "leaflet-geosearch";
import "leaflet-geosearch/dist/geosearch.css"
// service
import {centerService, getProjectService} from "@/api/project";
import {
  deleteTrafficNetwork,
  downloadFromOSM, getEdgesData, getNodesData,
  saveTrafficNetwork,
  uploadDataService
} from "@/api/load";

// base data
const route = useRoute();
const projectId = route.query.projectId

// basic map data
const latitude = ref()
const longitude = ref()
const map = ref()
const nodesGroup = layerGroup()
const edgesGroup = layerGroup()
const canvas = layerGroup()
let nodes: any
let edges: any
let rectangle: any


// dialog flag
const downloadTypeVisible = ref(false)
const downloadType = ref("all")
const uploadVisible = ref(false)
const uploadType = ref("nodes")

// --------------------------------- initial function -------------------------------
onMounted(async () => {
  let result = await getProjectService(projectId) as any
  latitude.value = result.data.latitude
  longitude.value = result.data.longitude
  initMap(latitude.value, longitude.value)
  await updateData()
})
// initial map data
const initMap = (lat: number, lng: number) => {
  map.value = L.map('map', {
    center: new L.LatLng(lat, lng),
    zoom: 15,
    zoomControl: false,
    preferCanvas: true
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
    "Traffic Roads": edgesGroup
  }
  canvas.addTo(map.value)
  L.control.layers(baseLayers, featureLayers).addTo(map.value)

  // search function
  const search = GeoSearchControl({
    provider: new OpenStreetMapProvider(),
    style: 'bar'
  })
  map.value.addControl(search)

  map.value.on("zoomend", () => {
    if (map.value.hasLayer(nodesGroup)) {
      map.value.removeLayer(nodesGroup)
      map.value.addLayer(nodesGroup)
    }
    if (map.value.hasLayer(edgesGroup)) {
      map.value.removeLayer(edgesGroup)
      map.value.addLayer(edgesGroup)
    }
  })

  map.value.getContainer().style.cursor = "default"
}
// get traffic network data from backside
const updateData = async () => {
  clearData()
  let nodesRet = await getNodesData(projectId) as any
  let edgesRet = await getEdgesData(projectId) as any
  console.log(nodesRet.data)
  if (nodesRet.data.nodes !== null) {
    nodes = nodesRet.data.nodes
    setNodes(nodes)
  }
  if (edgesRet.data.edges !== null) {
    edges = edgesRet.data.edges
    setEdges(edges)
  }
}
// set every node some functions
const setNodes = (nodes: any) => {
  for (const key in nodes) {
    const node = L.marker(nodes[key].point).on("contextmenu", () => {
      removeNode(node, key)
    }).bindPopup("id: " + key)
    nodesGroup.addLayer(node)
  }
}
// set every edge some functions
const setEdges = (edges: any) => {
  for (const key in edges) {
    const edge = L.polyline(edges[key].polyline, {weight: 5}).on("contextmenu", () => {
      edgesGroup.removeLayer(edge)
      delete edges[key]
    }).bindPopup("id: " + key)
    edgesGroup.addLayer(edge)
  }
}
// remove node and edges which are relative about the node
const removeNode = (node: any, id: string) => {
  nodesGroup.removeLayer(node)
  delete nodes[id]
  edgesGroup.clearLayers()
  for (const key in edges) {
    if (edges[key].source === id || edges[key].target === id) {
      delete edges[key]
    }
  }
  setEdges(edges)
}

// --------------------- main function of download traffic data --------------------
const setCenter = () => {
  map.value.getContainer().style.cursor = "pointer"
  map.value.off("click")
  map.value.off("mousemove")
  map.value.on("click", async (e: any) => {
    let result = await centerService(projectId, e.latlng.lat, e.latlng.lng) as any
    ElMessage.success({
      message: result.message ? result.message : "set center successfully.",
      plain: true,
    })
    map.value.off("click")
    map.value.getContainer().style.cursor = "default"
  })
}
// select an area
const selectBox = () => {
  canvas.clearLayers()
  map.value.getContainer().style.cursor = "pointer"
  let startPoint: any
  let endPoint: any
  map.value.on("click", (e: any)=> {
    if (startPoint === undefined) {
      startPoint = L.latLng(e.latlng.lat, e.latlng.lng)
      map.value.on("mousemove", (ev: any) => {
        endPoint = L.latLng(ev.latlng.lat, ev.latlng.lng)
        let rect = L.rectangle(L.latLngBounds(startPoint, endPoint), {weight: 2, color: "#FF7722"})
        canvas.clearLayers()
        canvas.addLayer(rect)
      })
    } else if (endPoint !== undefined) {
      endPoint = L.latLng(e.latlng.lat, e.latlng.lng)
      rectangle = L.rectangle(L.latLngBounds(startPoint, endPoint), {weight: 2, color: "#FF7722"}).on("contextmenu", () => {
        rectangle = undefined
        canvas.clearLayers()
      }).on("click", () => {
        downloadTypeVisible.value = true
      })
      ElNotification({
        title: 'Info',
        message: "Left-click to download traffic data. Right-click to delete this rectangle.",
        type: 'info',
      })
      canvas.clearLayers()
      canvas.addLayer(rectangle)
      map.value.off("click")
      map.value.off("mousemove")
      map.value.getContainer().style.cursor = "default"
    }
  })
}
// download traffic data from osm through backside
const downloadTrafficData = async () => {
  downloadTypeVisible.value = false
  // if user didn't set center, now set center coordinate automatically
  if (latitude.value === 0 && longitude.value === 0) {
    const x = (rectangle._bounds._northEast.lat + rectangle._bounds._southWest.lat) / 2
    const y = (rectangle._bounds._northEast.lng + rectangle._bounds._southWest.lng) / 2
    await centerService(projectId, x, y)
  }
  const downData = {
    "projectId": projectId,
    "north": rectangle._bounds._northEast.lat,
    "south": rectangle._bounds._southWest.lat,
    "east": rectangle._bounds._northEast.lng,
    "west": rectangle._bounds._southWest.lng,
    "type": downloadType.value
  }
  nodesGroup.clearLayers()
  edgesGroup.clearLayers()
  const loading = ElLoading.service({
    lock: true,
    text: 'Loading',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  let result = await downloadFromOSM(downData) as any
  if (result.data.nodes !== null) {
    if (nodes !== undefined && nodes !== null) nodes = {...nodes, ...result.data.nodes}
    else nodes = result.data.nodes
    setNodes(nodes)
  }
  if (result.data.edges !== null) {
    if (edges !== undefined && edges !== null) edges = {...edges, ...result.data.edges}
    else edges = result.data.edges
    setEdges(edges)
  }
  ElMessage.success({
    message: "Successfully download traffic data!",
    plain: true,
  })
  loading.close()
}

// save all data
const saveData = async () => {
  const data = {
    "projectId": projectId,
    "nodes": JSON.stringify(nodes),
    "edges": JSON.stringify(edges)
  }
  await saveTrafficNetwork(data)
  ElMessage.success({
    message: "save all data successfully!",
    plain: true,
  })
  clearData()
  await updateData()
}
// clear data on front-side
const clearData = () => {
  nodes = undefined
  edges = undefined
  nodesGroup.clearLayers()
  edgesGroup.clearLayers()
}
// upload personal data of traffic network
const uploadData = async (param: UploadRequestOptions) => {
  await uploadDataService(projectId, uploadType.value, param.file)
  ElMessage.success({
    message: "Upload and update personal data successfully.",
    plain: true,
  })
  uploadVisible.value = false
  window.location.reload()
}
// delete all traffic data from backside
const deleteData = () => {
  ElMessageBox.confirm('It will delete traffic data file from database. Continue?', 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(() => {
    deleteTrafficNetwork(projectId)
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
    <el-tooltip effect="dark" content="Move to the center where you prefer" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="setCenter" :icon="LocationInformation" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="select an area to download traffic data" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="selectBox" :icon="Crop" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="Save all data" placement="right">
      <el-button style="margin: 4px; " type="success" @click="saveData" :icon="Select" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="clear all data temporarily" placement="right">
      <el-button style="margin: 4px; " type="warning" @click="clearData" :icon="Failed" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="delete data include history" placement="right">
      <el-button style="margin: 4px; " type="danger" @click="deleteData" :icon="Delete" circle size="large"/>
    </el-tooltip><br>
  </div>
  <div id="map"></div>

<!-- choose download type -->
  <el-dialog v-model="downloadTypeVisible" title="Choose Traffic Type" width="500">
    <el-select v-model="downloadType" size="large" style="width: 240px">
      <el-option label="All Road" value="all"/>
      <el-option label="Highway" value="highway"/>
      <el-option label="Drive" value="drive"/>
      <el-option label="Bike" value="bike"/>
      <el-option label="Walk" value="walk"/>
    </el-select>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="downloadTypeVisible = false">Cancel</el-button>
        <el-button type="primary" @click="downloadTrafficData">
          Confirm
        </el-button>
      </div>
    </template>
  </el-dialog>

<!-- upload personal data -->
  <el-dialog v-model="uploadVisible" title="Upload your personal data" width="800">
    <el-form-item label="File type:">
      <el-select v-model="uploadType" placeholder="Select" size="large" style="width: 240px">
        <el-option label="Junctions" value="nodes"/>
        <el-option label="Roads" value="edges"/>
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
  z-index: 10;
  position: absolute;
  left: 0;
  background-color: rgba(255, 255, 255, 0.5);
}
#map {
  width: 100vw;
  height: 100vh;
  position: absolute;
  z-index: 0;
}
</style>
