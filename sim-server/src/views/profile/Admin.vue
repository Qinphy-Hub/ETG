<script setup lang="ts">
import {Delete} from "@element-plus/icons-vue";
import {deleteUserService, getUserListService} from "@/api/user";

import {onMounted, ref} from "vue";
import {ElMessage} from "element-plus";

const userList = ref<any []>([])

onMounted(async () => {
  let result = await getUserListService() as any
  userList.value = result.data
})

const deleteUser = async (index: number) => {
  await deleteUserService(userList.value[index].id)
  ElMessage.success({
    message: "delete successfully.",
    plain: true,
  })
  location.reload()
}
</script>

<template>
  <el-card class="users-box">
    <el-table :data="userList" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="50"></el-table-column>
      <el-table-column prop="username" label="Name" width="180"></el-table-column>
      <el-table-column prop="email" label="Email" width="240"></el-table-column>
      <el-table-column fixed="right" label="Operation">
        <template #default="scope">
          <el-tooltip content="Delete" placement="top" effect="light">
            <el-button type="danger" :disabled="userList[scope.$index].id === 1" :icon="Delete" @click="deleteUser(scope.$index)" circle />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.users-box {
  width: 700px;
  height: 500px;
  min-width: 700px;
  margin: 0 auto;
  padding: 0;
}
</style>
