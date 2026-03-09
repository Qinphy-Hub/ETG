<script setup lang="ts">
import {Delete, Edit, Failed, Plus, Pointer, Select, Upload} from "@element-plus/icons-vue";
import {onMounted, ref} from "vue";
import {ElMessage, ElMessageBox, type UploadRequestOptions} from "element-plus";
import {
  deletePointDemandsData,
  getPointDemandsData,
  savePointDemandsData,
  uploadDataService
} from "@/api/load";
import {useRoute} from "vue-router";
import * as L from "leaflet";
import {layerGroup} from "leaflet";
import {getProjectService} from "@/api/project";

// base data
const route = useRoute();
const projectId = route.query.projectId

// basic map data
const map = ref()
const demandsGroup = layerGroup()
let demands: {[key: string]: any} = {}
let setDemandsFlag = false;
let propertyFlag = false;
const properties = ref<{[key: string]: any}>({})
let max_demand = 15

// dialog data
const uploadVisible = ref(false)
const propertiesVisible = ref(false)

// --------------------------------- on mounted -----------------------------------
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

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 18,
    attribution: 'OpenStreetMap'
  }).addTo(map.value)

  demandsGroup.addTo(map.value)

  map.value.getContainer().style.cursor = "default"

  map.value.on("click", (e: any) => {
    if (setDemandsFlag) {
      const lat = e.latlng.lat
      const lng = e.latlng.lng
      const key = lat.toString().slice(0, 7) + lng.toString().slice(0, 8)
      demands[key] = {
        point: [lat, lng],
        value: 15
      }
      setOneDemand(key)
    }
  })
}
// set demand point in database
const setPointDemands = (demands: any) => {
  for (const key in demands) {
    setOneDemand(key)
  }
}
// set a demand point
const setOneDemand = (key: string) => {
  const demand = L.circleMarker(demands[key].point, {
    color: "red",
    weight: 1,
    radius: 15,
    fillColor: "#ff0000",
    fillOpacity: demands[key].value / max_demand,
  }).on("contextmenu", () => {
    delete demands[key]
    demandsGroup.removeLayer(demand)
  }).on("click", () => {
    if (propertyFlag) {
      properties.value["key"] = key
      properties.value["value"] = demands[key].value
      propertiesVisible.value = true
    }
  }).bindPopup("id: " + key)
  demandsGroup.addLayer(demand)
}
// save demand level
const saveProperties = () => {
  const key = properties.value.key
  demands[key].value = properties.value.value
  propertiesVisible.value = false
}

const updateData = async () => {
  clearData()
  let ret = await getPointDemandsData(projectId) as any
  if (ret.data.pointDemands !== null) demands = ret.data.pointDemands
  for (let key in demands) {
    if (demands[key].value > max_demand) {
      max_demand = demands[key].value
    }
  }
  setPointDemands(demands)
}

// ------------------------------- main function -----------------------------------
const uploadData = async (param: UploadRequestOptions) => {
  await uploadDataService(projectId, "point-demands", param.file)
  ElMessage.success({
    message: "Upload and update personal data successfully.",
    plain: true,
  })
  uploadVisible.value = false
}

const setDemand = () => {
  setDemandsFlag = true
  propertyFlag = false
  map.value.getContainer().style.cursor = "pointer"
}

const setProperties = () => {
  setDemandsFlag = false
  propertyFlag = true
  map.value.getContainer().style.cursor = "default"
}

const saveData = () => {
  const data = {
    "projectId": projectId,
    "pointDemands": JSON.stringify(demands),
  }
  savePointDemandsData(data)
  ElMessage.success({
    message: "save all data successfully!",
    plain: true,
  })
  window.location.reload()
}

const clearData = () => {
  demands = {}
  demandsGroup.clearLayers()
}

const deleteData = () => {
  ElMessageBox.confirm('It will delete demand points data file from database. Continue?', 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(() => {
    deletePointDemandsData(projectId)
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
    <el-tooltip effect="dark" content="set a demand point on the map" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="setDemand" :icon="Pointer" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="set demand level to a demand point" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="setProperties" :icon="Edit" circle size="large"/>
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

<!-- upload personal data -->
  <el-dialog v-model="uploadVisible" title="Upload your personal data" width="800">
    <el-upload class="avatar-uploader" :http-request="uploadData" :limit="1" action="Action">
      <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
    </el-upload>
  </el-dialog>

<!-- set demand point properties -->
  <el-dialog v-model="propertiesVisible" title="Set Properties" width="500">
    <el-form-item label="Demand level">
      <el-input type="number" v-model="properties.value" placeholder="Please input demand level" oninput="if(value<0)value=0"/>
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
