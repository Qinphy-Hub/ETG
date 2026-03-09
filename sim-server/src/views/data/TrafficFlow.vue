<script setup lang="ts">
import {Delete, Edit, Failed, Select, Timer, Upload} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox, type UploadRequestOptions} from "element-plus";
import {
  deleteTrafficFlowData,
  getEdgesData, getNodesData,
  getTrafficFlowData,
  saveTrafficFlowData,
  uploadDataService
} from "@/api/load";
import {useRoute} from "vue-router";
import {onMounted, ref} from "vue";
import {getProjectService, timerService} from "@/api/project";
import * as L from "leaflet";
import {layerGroup} from "leaflet";

// base data
const route = useRoute();
const projectId = route.query.projectId

// basic map data
const map = ref()
const edgesGroup = layerGroup()
const flowsGroup = layerGroup()
let edges: {[key: string]: any} = {}
let flows: {[key: string]: any} = {}
let propertyFlag = false
const timer = ref(15)
const data_len = ref(0)
const timeUnit = ref("minutes")
const flowProperty = ref<{[key: string]: any}>({})
const textFlow = ref("")

// dialog data
const uploadVisible = ref(false)
const propertiesVisible = ref(false)
const timerVisible = ref(false)

// -------------------------------- on mounted --------------------------------
onMounted(async () => {
  let ret = await getProjectService(projectId) as any
  initMap(ret.data.latitude, ret.data.longitude)
  let result = await getProjectService(projectId) as any
  timer.value = result.data.granularity
  timeUnit.value = result.data.timeUnit
  data_len.value = result.data.timeLen
  await updateData()
})
const updateData = async () => {
  clearData()
  let flowRet = await getTrafficFlowData(projectId) as any
  let edgesRet = await getEdgesData(projectId) as any
  let nodesRet = await getNodesData(projectId) as any
  if (flowRet.data.trafficFlow !== null) flows = flowRet.data.trafficFlow
  if (edgesRet.data.edges !== null) {
    edges = edgesRet.data.edges
    setEdges(edges)
  }
  if (nodesRet.data.nodes !== null) setNodes(nodesRet.data.nodes)
}
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
  edgesGroup.addTo(map.value)
  flowsGroup.addTo(map.value)
  map.value.getContainer().style.cursor = "default"
}
// show the road on the map
const setEdges = (edges: any) => {
  for (const key in edges) {
    const edge = L.polyline(edges[key].polyline, {weight: 5}).on("click", () => {
      if (propertyFlag) {
        flowProperty.value["key"] = key
        flowProperty.value["value"] = []
        if (key in flows) flowProperty.value["value"] = flows[key].properties.data
        let text = ""
        for (const f of flowProperty.value["value"]) {
          text += (f + ",")
        }
        textFlow.value = text
        propertiesVisible.value = true
      }
    }).bindPopup("id: " + key)
    edgesGroup.addLayer(edge)
  }
}
const setNodes = (nodes: any) => {
  for (const key in nodes) {
    L.marker(nodes[key].point).addTo(edgesGroup)
  }
}

const saveTrafficFlow = () => {
  const key = flowProperty.value["key"]
  if (key in flows) flows[key].properties.data = textFlow.value.split(',')
  else {
    flows[key] = {
      "properties": {
        "data": textFlow.value.split(',').slice(0, data_len.value),
      }
    }
  }
  propertiesVisible.value = false
}
// ------------------------------ main function --------------------------------
const uploadData = async (param: UploadRequestOptions) => {
  await uploadDataService(projectId, "traffic-flow", param.file)
  ElMessage.success({
    message: "Upload and update personal data successfully.",
    plain: true,
  })
  uploadVisible.value = false
}
// set time granularity
const setTimer = () => {
  propertyFlag = false
  timerVisible.value = true
}

const setProperties = () => {
  propertyFlag = true
}

const addZeros = () => {
  for (const key in edges) {
    if (flows.key === undefined) {
      flows[key] = {}
    }
    if (flows[key].data === undefined) {
      flows[key].data = new Array(data_len.value).fill(0)
    }
    while (flows[key].data.length < data_len.value) {
      flows[key].data.push(0)
    }
  }
}

const saveData = async () => {
  addZeros()
  propertyFlag = false
  const data = {
    "projectId": projectId,
    "trafficFlow": JSON.stringify(flows),
  }
  await saveTrafficFlowData(data)
  ElMessage.success({
    message: "save all data successfully!",
    plain: true,
  })
  clearData()
  window.location.reload()
}

const clearData = () => {
  propertyFlag = false
  flows = {}
  flowsGroup.clearLayers()
  edgesGroup.clearLayers()
}

const deleteData = () => {
  propertyFlag = false
  ElMessageBox.confirm('It will delete traffic data file from database. Continue?', 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(() => {
    deleteTrafficFlowData(projectId)
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

const saveSettings = async () => {
  await timerService(projectId, timer.value, timeUnit.value, data_len.value)
  ElMessage.success({
    message: "Save settings successfully.",
    plain: true,
  })
  timerVisible.value = false
}
</script>

<template>
  <div class="container">
    <el-tooltip effect="dark" content="upload personal data" placement="right">
      <el-button style="margin: 10px 4px 4px 4px; " type="success" @click="uploadVisible = true" :icon="Upload" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="set data properties" placement="right">
      <el-button style="margin: 4px;" type="primary" @click="setTimer" :icon="Timer" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="set traffic flow data" placement="right">
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

<!-- set time granularity -->
  <el-dialog v-model="timerVisible" title="Set Properties" width="500">
    <el-scrollbar style="width: 100%; display: block">
      <el-descriptions style="margin: 6px" :column="1" border>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">Time granularity</div>
          </template>
          <el-input-number v-model="timer" :min="0" :max="1000" />
          <el-select v-model="timeUnit" placeholder="Time" size="default" style="width: 100px; margin-left: 8px">
            <el-option label="seconds" value="seconds"/>
            <el-option label="minutes" value="minutes"/>
            <el-option label="hours" value="hours"/>
            <el-option label="days" value="days"/>
          </el-select>
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">Time Span</div>
          </template>
          <el-input-number v-model="data_len" :min="0" :max="10000" />
          × {{timer}} {{timeUnit}}
        </el-descriptions-item>
      </el-descriptions>
    </el-scrollbar>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="timerVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveSettings">Confirm</el-button>
      </div>
    </template>
  </el-dialog>

<!-- set properties of traffic flow -->
  <el-dialog v-model="propertiesVisible" title="Set Properties" width="500">
    <el-input v-model="textFlow" type="textarea" placeholder="traffic flow split by ','"/>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="propertiesVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveTrafficFlow">Confirm</el-button>
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
