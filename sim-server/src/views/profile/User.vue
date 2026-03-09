<script setup lang="ts">
import {FolderAdd, Delete, Edit, Tools} from "@element-plus/icons-vue"
import {onMounted, reactive, ref} from "vue";
import {useRouter} from "vue-router";
import {projectCreateService, projectDeleteService, projectListService, projectUpdateService} from "@/api/project"
import {ElLoading, ElMessage, ElMessageBox, type FormInstance, type FormRules} from "element-plus";
import {simulation} from "@/api/program";


// base data
const router = useRouter()
// operation to project
const createVisible = ref(false)
const createForm = ref<FormInstance>()
const createData = reactive({
  wksName: "",
  wksDesc: "",
  wksType: '',
  proType: '',
})
const validateWksName = (rule: any, value: any, callback: any) => {
  const reg = /^[A-Za-z0-9]+$/;
  if (!reg.test(value)) {
    callback(new Error("Only numbers and letters."));
  } else {
    callback();
  }
}
const projectRule = reactive<FormRules<typeof createData>>({
  wksName: [
    {required: true, message: "Project name cannot be empty.", trigger: "blur"},
    {max: 20, message: "Length should be less than 20.", trigger: "blur"},
    {validator: validateWksName, trigger: "blur"}
  ],
  wksDesc: [
    {max: 300, message: "Length should be less than 300.", trigger: "blur"},
  ],
  wksType: [
    {required: true, message: "Map type cannot be empty.", trigger: "blur"},
  ],
  proType: [
    {required: true, message: "Project type cannot be empty.", trigger: "blur"},
  ]
})
// edit project data
const editVisible = ref(false)
const editForm = ref<FormInstance>()
const editData = reactive({
  id: 0,
  wksName: '',
  wksDesc: '',
})
// project data
interface projectType {
  id: number;
  wksName: string;
  wksDesc: string;
  createDate: string;
  wksType: string;
  proType: string;
}
const projectList = ref<projectType[]>([]);

onMounted(async () => {
  await updateProject()
})

// base function
const updateProject = async () => {
  let result = await projectListService() as any
  projectList.value = result.data
}
// create project
const createProject = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      await projectCreateService(createData)
      ElMessage.success({
        message: "Create Project success.",
        plain: true
      })
      await updateProject()
      createVisible.value = false
    }
  })
}
// edit project information
const editProject = (index: number) => {
  editData.id = projectList.value[index].id
  editData.wksName = projectList.value[index].wksName
  editData.wksDesc = projectList.value[index].wksDesc
  editVisible.value = true
}
const confirmEdit = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      await projectUpdateService(editData)
      ElMessage.success({
        message: "Successfully updated project",
        plain: true,
      })
      await updateProject()
      editVisible.value = false
    }
  })
}
// delete project
const deleteProject = (index: number) => {
  ElMessageBox.confirm(
      'It will be delete from database and file system. Are you sure?',
      'Warning',
      {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning',
      }
  ).then(async () => {
    await projectDeleteService(projectList.value[index].id)
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

// open project
const openProject = async (index: number) => {
  // show result page
  if (projectList.value[index].proType === "FLP") {
    await router.push({path: '/control/flp', query: {projectId: projectList.value[index].id}})
  } else {
    await router.push({path: '/control/route', query: {projectId: projectList.value[index].id}})
  }
}
// data settings
const dataManage = (index: number) => {
  window.open(router.resolve({
    path: "/data",
    query: {projectId: projectList.value[index].id},
  }).href)
}
</script>

<template>
  <div class="projects-box">
    <div class="create-box">
      <el-button type="danger" style="float:left; height: 40px" @click="createVisible = true">
        <el-icon class="el-icon--left"><FolderAdd /></el-icon>New Project
      </el-button>
    </div>
    <el-card>
      <el-table :data="projectList" border style="width: 100%">
        <el-table-column fixed prop="id" label="Id" width="50" />
        <el-table-column prop="createDate" label="Date" width="110" />
        <el-table-column prop="wksName" label="Project Name" width="180" />
        <el-table-column prop="wksType" label="Map Type" width="100" />
        <el-table-column prop="proType" label="Project Type" width="150" />
        <el-table-column prop="wksDesc" label="Description" width="500" />
        <el-table-column fixed="right" label="Operations" min-width="150">
          <template #default="scope">
            <el-tooltip content="Edit" placement="top" effect="dark">
              <el-button type="primary" :icon="Edit" @click="editProject(scope.$index)" circle />
            </el-tooltip>

            <el-tooltip content="Settings" placement="top" effect="dark">
              <el-button type="primary" :icon="Tools" @click="dataManage(scope.$index)" circle />
            </el-tooltip>

            <el-tooltip content="Delete" placement="top" effect="dark">
              <el-button type="danger" :icon="Delete" @click="deleteProject(scope.$index)" circle />
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="Simulation" min-width="110">
          <template #default="scope">
            <el-button type="success" @click="openProject(scope.$index)" >Play</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>

  <!-- other invisible form -->
  <el-dialog v-model="createVisible" title="Create a new project" width="500">
    <el-form :model="createData" :rules="projectRule" ref="createForm">
      <el-form-item label="Project name" prop="wksName">
        <el-input type="text" v-model="createData.wksName" autocomplete="off" placeholder="Project name"/>
      </el-form-item>
      <el-form-item label="Map type" prop="wksType">
        <el-radio-group v-model="createData.wksType">
          <el-radio value="real" size="large">Real World</el-radio>
          <el-radio value="free" size="large">Artificial World</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Project type" prop="proType">
        <el-select v-model="createData.proType" placeholder="Select" size="large" style="width: 240px">
          <el-option label="Single Vehicle Scheduling" value="Single-Schedule"/>
          <el-option label="Multiple Vehicles Scheduling" value="Multi-Schedule"/>
          <el-option label="Facilities Location Problem" value="FLP"/>
        </el-select>
      </el-form-item>
      <el-form-item label="Project Description" prop="wksDesc">
        <el-input type="textarea" v-model="createData.wksDesc" placeholder="Project Descriptions" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="createVisible = false">Cancel</el-button>
        <el-button type="primary" @click="createProject(createForm)">
          Confirm
        </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="editVisible" title="Edit project information" width="500">
    <el-form :model="editData" :rules="projectRule" ref="editForm">
      <el-form-item label="Project name" prop="wksName">
        <el-input v-model="editData.wksName" autocomplete="off" placeholder="Project name"/>
      </el-form-item>
      <el-form-item label="Project Description" prop="wksDesc">
        <el-input v-model="editData.wksDesc" placeholder="Project Descriptions" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="editVisible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmEdit(editForm)">
          Confirm
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.projects-box {
  width: 700px;
  height: 600px;
  min-width: 700px;
  margin: 0 auto;
  padding: 0;
}

.create-box {
  width: 700px;
  height: 50px;
  margin: 0;
  padding: 0;
}


</style>
