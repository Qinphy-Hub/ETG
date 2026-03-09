<script setup lang="ts">
import Editor from "@/components/Editor.vue";
import {onMounted, ref} from "vue";
import {getModifyTrafficFlow, initialModifyTrafficFlow, saveModifyTrafficFlow} from "@/api/program";
import {ElMessage, ElMessageBox} from "element-plus";
import {Select, RefreshLeft} from "@element-plus/icons-vue";
import {useRoute} from "vue-router";

// base data
const route = useRoute();
const projectId = route.query.projectId

// initial code data
onMounted(async () => {
  let result = await getModifyTrafficFlow(projectId) as any
  originCode.value = result.data
})

const language = ref("python")
const originCode = ref("")
const code =  ref('')
const textChange = (text: string) => {
  code.value = text
}

const saveCode = async () => {
  let result = await saveModifyTrafficFlow(projectId, code.value) as any
  ElMessage.success(result.message?result.message:"save data successfully.")
}

const initProgram = async () => {
  ElMessageBox.confirm('It will delete your data before. Continue?', 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(async () => {
    let result = await initialModifyTrafficFlow(projectId) as any
    originCode.value = result.data
    ElMessage({
      type: 'success',
      message: 'Initial completed',
    })
  }).catch(() => {
    ElMessage({
      type: 'info',
      message: 'Initial canceled',
    })
  })

}
</script>

<template>
  <div class="body">
    <div class="container">
      <Editor :text="originCode" :language="language" @change="textChange"/>
    </div>
  </div>

  <div class="btn-box">
    <el-tooltip effect="dark" content="initial program" placement="left">
      <el-button style="margin: 10px 4px 4px 4px;" type="primary" @click="initProgram" :icon="RefreshLeft" circle size="large"/>
    </el-tooltip><br>
    <el-tooltip effect="dark" content="save program" placement="left">
      <el-button style="margin: 4px; " type="danger" @click="saveCode" :icon="Select" circle size="large"/>
    </el-tooltip><br>
  </div>
</template>

<style scoped>
.body {
  width: 100vw;
  height: 100vh;
  display: flex;
  background: lightblue;
}

.container {
  width: 60vw;
  height: 100vh;
  margin: 0 auto;
}

.btn-box {
  width: 70px;
  height: 100vh;
  z-index: 10;
  position: absolute;
  right: 0;
  top: 0;
  background-color: rgba(255, 255, 255, 0.5);
}
</style>
