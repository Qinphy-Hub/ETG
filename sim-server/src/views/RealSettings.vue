<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadRequestOptions} from "element-plus";
import {uploadDataService} from "@/api/load";
import {
  getProjectService,
} from "@/api/project";
import {
  routeProgramCreateService,
  routeProgramListService,
  routeProgramUpdateService,
  routeProgramDeleteService,
  flpProgramUpdateService, flpProgramDeleteService, flpProgramCreateService, flpProgramListService
} from '@/api/program';
import {Delete, Edit, FolderAdd, Tools} from "@element-plus/icons-vue";

// base data
const route = useRoute();
const router = useRouter();
const projectId = route.query.projectId

onMounted(async () => {
  let result = await getProjectService(projectId) as any
  proType.value = result.data.proType
  await updateProject()
})

// ------------------ main function --------------------------
const clickFunction = (str: string) => {
  router.push({path: '/data/' + str, query: {projectId: projectId}});
}

const programFunction = (str: string) => {
  router.push({path: '/program/' + str, query: {projectId: projectId}});
}

// ------------------- settings data --------------------------
const proType = ref("Single-Schedule")

const uploadData = async (param: UploadRequestOptions) => {
  await uploadDataService(projectId, "multiple-cars", param.file)
  ElMessage.success({
    message: "Upload and update personal data successfully.",
    plain: true,
  })
}


// ------------------ set program of one car's route -------------------
// common function
const updateProject = async () => {
  let routeRet = await routeProgramListService(projectId) as any
  routeProgramList.value = routeRet.data
  let flpRet = await flpProgramListService(projectId) as any
  flpProgramList.value = flpRet.data
}
const formatBoolean = (row: any, index: any, callValue: any) => {
  if (row.active === true) {
    return "Yes"
  } else {
    return "No"
  }
}
// one car route
const routeTableVisible = ref(false)
// program data
interface routeProgramType {
  id: number;
  carColor: string;
  routeColor: string;
  name: string;
  active: boolean;
}
const routeProgramList = ref<routeProgramType[]>([]);
const createRouteVisible = ref(false)
// edit project data
const editRouteVisible = ref(false)
const editRouteForm = ref<FormInstance>()
const editRouteData = reactive({
  id: 0,
  name: "",
  carColor: "",
  routeColor: '',
  active: true,
})
const editRouteProgram = (index: number) => {
  editRouteData.id = routeProgramList.value[index].id
  editRouteData.carColor = routeProgramList.value[index].carColor
  editRouteData.routeColor = routeProgramList.value[index].routeColor
  editRouteData.name = routeProgramList.value[index].name
  editRouteData.active = routeProgramList.value[index].active
  editRouteVisible.value = true
}
const confirmRouteEdit = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      await routeProgramUpdateService(editRouteData)
      ElMessage.success({
        message: "Successfully updated program",
        plain: true,
      })
      await updateProject()
      editRouteVisible.value = false
    }
  })
}
const deleteRouteProgram = async (index: number) => {
  ElMessageBox.confirm(
      'It will be delete from database and file system. Are you sure?',
      'Warning',
      {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning',
      }
  ).then(async () => {
    await routeProgramDeleteService(routeProgramList.value[index].id)
    ElMessage({
      type: 'success',
      message: 'Delete completed',
    })
    await updateProject()
  }).catch(() => {
    ElMessage({
      type: 'info',
      message: 'Delete canceled',
    })
  })
}
const createRouteForm = ref<FormInstance>()
const createRouteData = reactive({
  id: 0,
  name: "",
  carColor: "",
  routeColor: '',
  active: true,
})
const routeProjectRule = reactive<FormRules<typeof createRouteData>>({
  name: [
    {required: true, message: "Name cannot be empty.", trigger: "blur"},
    {max: 20, message: "Length should be less than 20.", trigger: "blur"},
  ],
  carColor: [
    {required: true, message: "Map type cannot be empty.", trigger: "blur"},
  ],
  routeColor: [
    {required: true, message: "Project type cannot be empty.", trigger: "blur"},
  ]
})
const createRouteProgram = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      await routeProgramCreateService(projectId, createRouteData)
      ElMessage.success({
        message: "Create Program success.",
        plain: true
      })
      await updateProject()
      createRouteVisible.value = false
    }
  })
}
const editRouteCode = (index: number) => {
  router.push({path: '/program/code-route', query: {projectId: projectId, programId: routeProgramList.value[index].id}});
}


// -------------------------------- FLP Program ----------------------------------
const flpTableVisible = ref(false)
const createFlpVisible = ref(false)
interface flpProgramType {
  id: number;
  color: string;
  name: string;
  active: boolean;
}
const flpProgramList = ref<flpProgramType[]>([]);
const editFlpVisible = ref(false)
const editFlpForm = ref<FormInstance>()
const editFlpData = reactive({
  id: 0,
  name: "",
  color: "",
  active: true,
})
const editFlpProgram = (index: number) => {
  editFlpData.id = flpProgramList.value[index].id
  editFlpData.color = flpProgramList.value[index].color
  editFlpData.name = flpProgramList.value[index].name
  editFlpData.active = flpProgramList.value[index].active
  editFlpVisible.value = true
}
const confirmFlpEdit = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      await flpProgramUpdateService(editFlpData)
      ElMessage.success({
        message: "Successfully updated program",
        plain: true,
      })
      await updateProject()
      editFlpVisible.value = false
    }
  })
}
const deleteFlpProgram = async (index: number) => {
  ElMessageBox.confirm(
      'It will be delete from database and file system. Are you sure?',
      'Warning',
      {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning',
      }
  ).then(async () => {
    await flpProgramDeleteService(flpProgramList.value[index].id)
    ElMessage({
      type: 'success',
      message: 'Delete completed',
    })
    await updateProject()
  }).catch(() => {
    ElMessage({
      type: 'info',
      message: 'Delete canceled',
    })
  })
}
const createFlpForm = ref<FormInstance>()
const createFlpData = reactive({
  id: 0,
  name: "",
  color: "",
  active: true,
})
const flpProjectRule = reactive<FormRules<typeof createFlpData>>({
  name: [
    {required: true, message: "Name cannot be empty.", trigger: "blur"},
    {max: 20, message: "Length should be less than 20.", trigger: "blur"},
  ],
  color: [
    {required: true, message: "Map type cannot be empty.", trigger: "blur"},
  ]
})
const createFlpProgram = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      await flpProgramCreateService(projectId, createFlpData)
      ElMessage.success({
        message: "Create Program success.",
        plain: true
      })
      await updateProject()
      createFlpVisible.value = false
    }
  })
}
const editFlpCode = (index: number) => {
  router.push({path: '/program/code-flp', query: {projectId: projectId, programId: flpProgramList.value[index].id}});
}

</script>

<template>
  <div class="full">
    <div class="container">
      <div class="split">
        <el-card style="width: 500px; height: 100px; display: flex; align-items: center; justify-content: center" shadow="never">
          <el-text class="mx-1" type="primary" size="large" tag="b">Data Input</el-text>
        </el-card>
        <el-scrollbar style="height: calc(100vh - 150px); width: 100%; display: block">
          <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="clickFunction('traffic')" shadow="hover">
            <img src="@/assets/traffic.svg" style="width: 60px; float: left" alt="traffic"/>
            <div class="detail">
              <el-text class="mx-1" type="success">Traffic Network Data</el-text>
              (Junctions and Roads) download from OSM or upload by user. It also give you some tools to modify the data.
            </div>
          </el-card>
          <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="clickFunction('station')" shadow="hover">
            <img src="@/assets/stations.svg" style="width: 60px; float: left" alt="stations"/>
            <div class="detail">
              Set the location and properties of the <el-text class="mx-1" type="success">Charging Station</el-text>. <el-text class="mx-1" type="warning">Make sure the map data is complete before setting</el-text>.
            </div>
          </el-card>
          <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="clickFunction('power-grid')" shadow="hover">
            <img src="@/assets/power-grid.svg" style="width: 60px; float: left" alt="power grid"/>
            <div class="detail">
              Upload or set your <el-text class="mx-1" type="success">Power Grid Data</el-text>(Devices and Lines) and their properties. <el-text class="mx-1" type="info">The functionality is not yet perfect.</el-text>
            </div>
          </el-card>
          <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="clickFunction('traffic-flow')" shadow="hover">
            <img src="@/assets/traffic-flow.svg" style="width: 60px; float: left" alt="traffic flow"/>
            <div class="detail">
              Upload or set your <el-text class="mx-1" type="success">Traffic Flow Data</el-text>. <el-text class="mx-1" type="warning">Make sure the map data is complete before setting</el-text> and the data length of each road is consistent.
            </div>
          </el-card>
          <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="clickFunction('power-flow')" shadow="hover">
            <img src="@/assets/power-flow.svg" style="width: 60px; float: left" alt="power flow"/>
            <div class="detail">
              Upload or set your <el-text class="mx-1" type="success">Power Flow Data</el-text>. Please check your power grid data is complete. <el-text class="mx-1" type="info">The functionality is not yet perfect.</el-text>
            </div>
          </el-card>

          <div v-if="proType === 'Single-Schedule'">
            <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="clickFunction('start-end')" shadow="hover">
              <img src="@/assets/start-end.svg" style="width: 60px; float: left" alt="od pairs"/>
              <div class="detail">
                <el-text class="mx-1" type="success">Set start point and end point</el-text>: when you scheduling one vehicle you need to set it. <el-text class="mx-1" type="warning">Make sure the map data is complete before setting</el-text>.
              </div>
            </el-card>
          </div>
          <div v-else-if="proType === 'Multi-Schedule'">
            <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="clickFunction('real-demands')" shadow="hover">
              <img src="@/assets/start-end.svg" style="width: 60px; float: left" alt="real demands"/>
              <div class="detail">
                <el-text class="mx-1" type="success">Set one point to produce the charging demand</el-text>: This is an important message. <el-text class="mx-1" type="warning">Make sure the map data is complete before setting</el-text>.
              </div>
            </el-card>
          </div>
          <div v-else>
            <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="clickFunction('sites')" shadow="hover">
              <img src="@/assets/potential-site.svg" style="width: 60px; float: left" alt="potential site"/>
              <div class="detail">
                Upload or set your <el-text class="mx-1" type="success">Potential Site Data</el-text>. <el-text class="mx-1" type="warning">Make sure the map data is complete before setting</el-text>. You can use these point data in your program.
              </div>
            </el-card>
            <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="clickFunction('pointDemand')" shadow="hover">
              <img src="@/assets/point-demand.svg" style="width: 60px; float: left" alt="point demand"/>
              <div class="detail">
                Upload or set your <el-text class="mx-1" type="success">Point Demand Data</el-text>. You can use these self-defined point data which can be anywhere in your program.
              </div>
            </el-card>
            <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="clickFunction('od-pairs')" shadow="hover">
              <img src="@/assets/od-pairs.svg" style="width: 60px; float: left" alt="od pairs"/>
              <div class="detail">
                Upload or set your <el-text class="mx-1" type="success">OD Pairs Data</el-text>. <el-text class="mx-1" type="warning">Make sure the map data is complete before setting</el-text>. This is very typical traffic data.
              </div>
            </el-card>
          </div>
        </el-scrollbar>
      </div>

      <div class="split">
        <el-card style="width: 500px; height: 100px; display: flex; align-items: center; justify-content: center" shadow="never">
          <el-text class="mx-1" type="primary" size="large" tag="b">Control Program</el-text>
        </el-card>
        <el-scrollbar style="height: calc(100vh - 150px); width: 100%; display: block">
          <div v-if="proType === 'FLP'">
            <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="flpTableVisible = true" shadow="hover">
              <img src="@/assets/point-demand-program.svg" style="width: 60px; float: left" alt="point demand algorithm"/>
              <div class="detail">
                <el-text class="mx-1" type="danger" tag="b">Facility Location Problem</el-text>(Point Demand)<br/>
                Find the best location for your facility(charging stations) based on the point demand.
              </div>
            </el-card>
            <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="programFunction('covered-demands')" shadow="hover">
              <img src="@/assets/od-pair-program.svg" style="width: 60px; float: left" alt="covered demands algorithm"/>
              <div class="detail">
                <el-text class="mx-1" type="danger" tag="b">Defined covered demands</el-text><br/>
                User set the algorithm to defined one station covered the demands.
              </div>
            </el-card>
          </div>
          <div v-else>
            <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="programFunction('flow-speed')" shadow="hover">
              <img src="@/assets/speed.svg" style="width: 60px; float: left" alt="speed"/>
              <div class="detail">
                <el-text class="mx-1" type="danger" tag="b">Vehicle Speed Control</el-text><br/>
                Set the relation between traffic flow and vehicle speed. <el-text class="mx-1" type="info">we set the default speed is 60km/h.</el-text>
              </div>
            </el-card>
            <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="programFunction('traffic-flow')" shadow="hover">
              <img src="@/assets/modify-traffic-flow.svg" style="width: 60px; float: left" alt="modify traffic flow"/>
              <div class="detail">
                <el-text class="mx-1" type="danger" tag="b">Modify Traffic Flow Data</el-text><br/>
                You can change the original data of traffic flow. If you don't set it, we will use the original data.
              </div>
            </el-card>

            <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="routeTableVisible = true" shadow="hover">
              <img src="@/assets/car-dispatch.svg" style="width: 60px; float: left" alt="car dispatch"/>
              <div class="detail">
                <el-text class="mx-1" type="danger" tag="b">Default Vehicle Routes</el-text><br/>
                Set the algorithm for the vehicle to find the path. We use the dijkstra algorithm by default.
              </div>
            </el-card>
          </div>
          <div v-if="proType === 'Multi-Schedule'">
            <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="programFunction('set-traffic-flow')" shadow="hover">
              <img src="@/assets/sim-traffic-flow.svg" style="width: 60px; float: left" alt="modify traffic flow"/>
              <div class="detail">
                <el-text class="mx-1" type="danger" tag="b">Set Traffic Flow Algorithm</el-text><br/>
                Set the algorithm of calculate the traffic flow when simulation is start.
              </div>
            </el-card>
            <el-card style="width: 498px; height: 110px; display: block; margin-top: 3px; cursor: pointer" @click="programFunction('set-distribution')" shadow="hover">
              <img src="@/assets/multi-cars.svg" style="width: 60px; float: left" alt="distribution"/>
              <div class="detail">
                <el-text class="mx-1" type="danger" tag="b">Set Distribution</el-text><br/>
                Set the algorithm of calculate the traffic flow when simulation is start.
              </div>
            </el-card>
          </div>

        </el-scrollbar>
      </div>
    </div>
  </div>

<!-- scheduling program list -->
  <el-drawer v-model="routeTableVisible" size="50%" title="Multiple Vehicles Scheduling Program List">
    <div class="create-box">
      <el-button type="danger" style="float:left; height: 40px" @click="createRouteVisible = true">
        <el-icon class="el-icon--left"><FolderAdd /></el-icon>New Program
      </el-button>
    </div>
    <el-card>
      <el-table :data="routeProgramList" border style="width: 100%">
        <el-table-column fixed prop="id" label="Id" width="50" />
        <el-table-column prop="name" label="Program Name" width="200" />
        <el-table-column prop="carColor" label="Vehicle Color" width="120" />
        <el-table-column prop="routeColor" label="Route Color" width="120" />
        <el-table-column prop="active" label="Active" :formatter="formatBoolean" :show-overflow-tooltip="true" width="80" />
        <el-table-column fixed="right" label="Operations" min-width="150">
          <template #default="scope">
            <el-tooltip content="Settings" placement="top" effect="dark">
              <el-button type="primary" :icon="Tools" @click="editRouteProgram(scope.$index)" circle />
            </el-tooltip>
            <el-tooltip content="Edit Code" placement="top" effect="dark">
              <el-button type="primary" :icon="Edit" @click="editRouteCode(scope.$index)" circle />
            </el-tooltip>
            <el-tooltip content="Delete" placement="top" effect="dark">
              <el-button type="danger" :icon="Delete" @click="deleteRouteProgram(scope.$index)" circle />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </el-drawer>
  <el-dialog v-model="createRouteVisible" title="Create a new project" width="500">
    <el-form :model="createRouteData" :rules="routeProjectRule" ref="createRouteForm">
      <el-form-item label="Program name" prop="name">
        <el-input type="text" v-model="createRouteData.name" autocomplete="off" placeholder="Program name"/>
      </el-form-item>
      <el-form-item label="Vehicle Color" prop="carColor">
        <el-color-picker v-model="createRouteData.carColor" color-format="hex" />
      </el-form-item>
      <el-form-item label="Route Color" prop="routeColor">
        <el-color-picker v-model="createRouteData.routeColor" color-format="hex"/>
      </el-form-item>
      <el-form-item label="Active" prop="active">
        <el-switch
            v-model="createRouteData.active"
            inline-prompt
            style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
            active-text="Y"
            inactive-text="N"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="createRouteVisible = false">Cancel</el-button>
        <el-button type="primary" @click="createRouteProgram(createRouteForm)">
          Confirm
        </el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="editRouteVisible" title="Edit program" width="500">
    <el-form :model="editRouteData" :rules="routeProjectRule" ref="editRouteForm">
      <el-form-item label="Program name" prop="name">
        <el-input type="text" v-model="editRouteData.name" autocomplete="off" placeholder="Program name"/>
      </el-form-item>
      <el-form-item label="Vehicle Color" prop="carColor">
        <el-color-picker v-model="editRouteData.carColor" />
      </el-form-item>
      <el-form-item label="Route Color" prop="routeColor">
        <el-color-picker v-model="editRouteData.routeColor" />
      </el-form-item>
      <el-form-item label="Active" prop="active">
        <el-switch
            v-model="editRouteData.active"
            inline-prompt
            style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
            active-text="Y"
            inactive-text="N"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="editRouteVisible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmRouteEdit(editRouteForm)">
          Confirm
        </el-button>
      </div>
    </template>
  </el-dialog>

<!-- FLP  -->
  <el-drawer v-model="flpTableVisible" size="50%" title="FLP Program List">
    <div class="create-box">
      <el-button type="danger" style="float:left; height: 40px" @click="createFlpVisible = true">
        <el-icon class="el-icon--left"><FolderAdd /></el-icon>New Program
      </el-button>
    </div>
    <el-card>
      <el-table :data="flpProgramList" border style="width: 100%">
        <el-table-column fixed prop="id" label="Id" width="50" />
        <el-table-column prop="name" label="Program Name" width="200" />
        <el-table-column prop="color" label="Station Color" width="150" />
        <el-table-column prop="active" label="Active" :formatter="formatBoolean" :show-overflow-tooltip="true" width="80" />
        <el-table-column fixed="right" label="Operations" min-width="150">
          <template #default="scope">
            <el-tooltip content="Settings" placement="top" effect="dark">
              <el-button type="primary" :icon="Tools" @click="editFlpProgram(scope.$index)" circle />
            </el-tooltip>
            <el-tooltip content="Edit Code" placement="top" effect="dark">
              <el-button type="primary" :icon="Edit" @click="editFlpCode(scope.$index)" circle />
            </el-tooltip>
            <el-tooltip content="Delete" placement="top" effect="dark">
              <el-button type="danger" :icon="Delete" @click="deleteFlpProgram(scope.$index)" circle />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </el-drawer>
  <el-dialog v-model="createFlpVisible" title="Create a new project" width="500">
    <el-form :model="createFlpData" :rules="flpProjectRule" ref="createFlpForm">
      <el-form-item label="Program name" prop="name">
        <el-input type="text" v-model="createFlpData.name" autocomplete="off" placeholder="Program name"/>
      </el-form-item>
      <el-form-item label="Vehicle Color" prop="carColor">
        <el-color-picker v-model="createFlpData.color" color-format="hex" />
      </el-form-item>
      <el-form-item label="Active" prop="active">
        <el-switch
            v-model="createFlpData.active"
            inline-prompt
            style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
            active-text="Y"
            inactive-text="N"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="createFlpVisible = false">Cancel</el-button>
        <el-button type="primary" @click="createFlpProgram(createFlpForm)">
          Confirm
        </el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="editFlpVisible" title="Edit program" width="500">
    <el-form :model="editFlpData" :rules="flpProjectRule" ref="editFlpForm">
      <el-form-item label="Program name" prop="name">
        <el-input type="text" v-model="editFlpData.name" autocomplete="off" placeholder="Program name"/>
      </el-form-item>
      <el-form-item label="Vehicle Color" prop="carColor">
        <el-color-picker v-model="editFlpData.color" />
      </el-form-item>
      <el-form-item label="Active" prop="active">
        <el-switch
            v-model="editFlpData.active"
            inline-prompt
            style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
            active-text="Y"
            inactive-text="N"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="editFlpVisible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmFlpEdit(editFlpForm)">
          Confirm
        </el-button>
      </div>
    </template>
  </el-dialog>

</template>

<style scoped>
.full {
  width: 100vw;
  height: 90vh;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0;
  padding: 0;
  min-width: 1500px;
  min-height: 400px;
}

.container {
  width: 1000px;
  height: 90vh;
  margin: 0 auto;
  display: block;
}

.split {
  width: 500px;
  height: 90vh;
  float: left;
}

.detail {
  width: 370px;
  height: 100%;
  float: left;
  margin-left: 10px;
  text-align: left;
}

.create-box {
  width: 700px;
  height: 50px;
  margin: 0;
  padding: 0;
}
</style>
