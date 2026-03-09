<script setup lang="ts">
import {Delete, Edit, Failed, Pointer, Select, Upload} from "@element-plus/icons-vue";
import {onMounted, ref} from "vue";
import {ElMessage, ElMessageBox, type UploadRequestOptions} from "element-plus";
import {
  deleteSitesData, getNodesData,
  getSitesData,
  saveSitesData,
  uploadDataService
} from "@/api/load";
import {useRoute} from "vue-router";
import {getProjectService} from "@/api/project";
import * as L from "leaflet";
import {layerGroup} from "leaflet";

// base data
const route = useRoute();
const projectId = route.query.projectId

// map data
const map = ref()
const nodesGroup = layerGroup()
const sitesGroup = layerGroup()
let nodes: any
let sites: {[key: string]: any} = {}
let setSitesFlag = false
let propertyFlag = false
const properties = ref<{[key: string]: any}>({})

// dialog data
const uploadVisible = ref(false)
const propertiesVisible = ref(false)

// --------------------------------- on mounted ----------------------------------
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
    "Potential Sites": sitesGroup
  }
  L.control.layers(baseLayers, featureLayers).addTo(map.value)

  map.value.on("zoomend", () => {
    if (map.value.hasLayer(nodesGroup)) {
      map.value.removeLayer(nodesGroup)
      map.value.addLayer(nodesGroup)
    }
    if (map.value.hasLayer(sitesGroup)) {
      map.value.removeLayer(sitesGroup)
      map.value.addLayer(sitesGroup)
    }
  })

  map.value.getContainer().style.cursor = "default"
}
// set junctions
const setNodes = (nodes: any) => {
  for (const key in nodes) {
    const node = L.marker(nodes[key].point).on("click", () => {
      if (setSitesFlag && !(key in sites)) {
        sites[key] = {
          "point": nodes[key].point,
          "cost": 0
        }
        setOneSite(key)
      }
    }).bindPopup("id: " + key)
    nodesGroup.addLayer(node)
  }
}
// set potential sites on the map
const setSites = (sites: any) => {
  for (const key in sites) {
    setOneSite(key)
  }
}
// set one site on the map
const setOneSite = (key: string) => {
  const site = L.circleMarker(sites[key].point, {radius: 15, color: "#FF7722"}).on("contextmenu", () => {
    delete sites[key]
    sitesGroup.removeLayer(site)
  }).on("click", () => {
    if (propertyFlag) {
      properties.value["key"] = key
      properties.value["cost"] = sites[key].cost
      propertiesVisible.value = true
    }
  }).bindPopup("id: " + key + ", cost: " + sites[key].cost)
  sitesGroup.addLayer(site)
}
const updateData = async () => {
  clearData()
  let nodesRet = await getNodesData(projectId) as any
  let sitesRet = await getSitesData(projectId) as any
  if (nodesRet.data.nodes !== null) {
    nodes = nodesRet.data.nodes
    setNodes(nodes)
  }
  if (sitesRet.data.sites !== null) {
    sites = sitesRet.data.sites
    setSites(sites)
  }
}

const saveProperties = () => {
  const key = properties.value.key
  sites[key].cost = properties.value.cost
  propertiesVisible.value = false
}


// ------------------------------- main function ---------------------------------
const uploadData = async (param: UploadRequestOptions) => {
  await uploadDataService(projectId, "potential-sites", param.file)
  ElMessage.success({
    message: "Upload and update personal data successfully.",
    plain: true,
  })
  uploadVisible.value = false
}

const selectSite = () => {
  setSitesFlag = true
  propertyFlag = false
  map.value.addLayer(nodesGroup)
  map.value.addLayer(sitesGroup)
}

const setProperties = () => {
  setSitesFlag = false
  propertyFlag = true
  map.value.removeLayer(nodesGroup)
  map.value.addLayer(sitesGroup)
}

const saveData = () => {
  const data = {
    "projectId": projectId,
    "sites": JSON.stringify(sites)
  }
  saveSitesData(data)
  ElMessage.success({
    message: "save all data successfully!",
    plain: true,
  })
  updateData()
}

const clearData = () => {
  sites = {}
  nodes = {}
  nodesGroup.clearLayers()
  sitesGroup.clearLayers()
  setSitesFlag = false
  propertyFlag = false
}

const deleteData = () => {
  ElMessageBox.confirm('It will delete demand points data file from database. Continue?', 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(() => {
    deleteSitesData(projectId)
    clearData()
    ElMessage({
      type: 'success',
      message: 'Delete completed',
    })
    window.location.reload()
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
    <el-tooltip effect="dark" content="select a node to become a potential site" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="selectSite" :icon="Pointer" circle size="large"/>
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

<!-- set demand point properties -->
  <el-dialog v-model="propertiesVisible" title="Set Properties" width="500">
    <el-form-item label="cost">
      <el-input type="number" v-model="properties.cost" placeholder="Please input demand level" oninput="if(value<0)value=0"/>
    </el-form-item>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="propertiesVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveProperties">
          Confirm
        </el-button>
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
