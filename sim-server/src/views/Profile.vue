<script setup lang="ts">
import {getUserService} from "@/api/user"
import {onMounted, reactive, ref} from "vue";
import {useRouter} from "vue-router";
import {ElMessage} from 'element-plus';
import type {FormInstance, FormRules, UploadRequestOptions} from "element-plus";
import {usernameService, passwordService, uploadAvatarService, getAvatarFile, logoutService, deleteUserService} from "@/api/user"

// base data
const router = useRouter();
// username form data
const userForm = ref<FormInstance>()
const userData = reactive({username: ""})
const userFormRules = reactive<FormRules<typeof userData>>({
  username: [
    {required: true, message: "Username cannot be empty.", trigger: "blur"},
    {min: 5, max: 20, message: "Length of username is between 5 and 20.", trigger: "blur"},
  ],
})
// password form data
const pswForm = ref<FormInstance>()
const pswData = reactive({
  oldPassword: "",
  newPassword: "",
  repPassword: "",
})
const repeatVisible = ref(false)
const validatePassword = (rule: any, value: any, callback: any) => {
  const reg = /^(?=.*[a-zA-Z0-9].*)(?=.*[a-zA-Z.!@#$%^&*].*)(?=.*[0-9.!@#$%^&*].*).{6,32}$/;
  if (!reg.test(value)) {
    callback(new Error("two types of digits, letters, special symbols."));
  } else {
    callback();
  }
}
const pswFormRules = reactive<FormRules<typeof pswData>>({
  oldPassword: [{required: true, message: "Password cannot be empty.", trigger: "blur"},],
  newPassword: [
    {required: true, message: "Password cannot be empty.", trigger: "blur"},
    {min: 6, max: 32, message: "Length of password is between 6 and 32.", trigger: "blur"},
    {required: true, validator: validatePassword, trigger: "blur"},
  ],
  repPassword: [{required: true, message: "Password cannot be empty.", trigger: "blur"}],
})
// user avatar
const userPic = ref("https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png")
const userId = ref(0)

onMounted(async () => {
  let result = await getUserService() as any
  if (result.data.userPic !== null) {
    let picRet = await getAvatarFile(result.data.userPic) as any
    userPic.value = "data:image/png;base64," + btoa(new Uint8Array(picRet).reduce((data, byte) => data + String.fromCharCode(byte), ''))
  }
  userData.username = result.data.username
  userId.value = result.data.id
  if (result.data.id === 1) await router.push("/profile/admin")
  else await router.push("/profile/user")
})

// update username function
const modifyUsername = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      await usernameService(userData.username)
      ElMessage.success({
        message: "Modify username successfully.",
        plain: true
      })
    }
  })
}

// update password function
const openRepeat = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validateField(["oldPassword", "newPassword"], async (valid) => {
    if (valid) {
      repeatVisible.value = true
    }
  })
}
const modifyPassword = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      await passwordService(pswData)
      ElMessage.success({
        message: "Modify password successfully.",
        plain: true
      })
      resetPswForm(formEl)
      repeatVisible.value = false
      await router.push("/")
    }
  })
}
const resetPswForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
}

// update user avatar
const uploadAvatar = async (param: UploadRequestOptions) => {
  await uploadAvatarService(param.file)
  setTimeout( () => {
    ElMessage.success({
      message: "Upload and update avatar successfully.",
      plain: true,
    })
    router.go(0)
  }, 5000)
}

// logout
const logout = async () => {
  await logoutService()
  ElMessage.success({
    message: "Logout success.",
    plain: true,
  })
  await router.push("/")
}

// unregister
const unregister = async () => {
  await deleteUserService(userId.value)
  ElMessage.success({
    message: "unregister successfully. Look forward to your coming again.",
    plain: true,
  })
  await router.push("/")
}

</script>

<template>
  <div class="login-control">
    <el-form-item style="margin: 0 auto">
      <el-button type="info" text bg @click="logout">Logout</el-button>
      <el-button type="danger" text bg @click="unregister" :disabled="userId === 1">Unregister</el-button>
    </el-form-item>
  </div>

  <div class="information">
    <el-card class="info-box">
      <el-upload :http-request="uploadAvatar" :limit="1" action="Action" class="img-btn">
          <el-tooltip
              effect="dark"
              content="Upload Avatar"
              placement="top-start"
          >
            <el-image class="img-btn" :src="userPic" fit="fill" />
          </el-tooltip>
      </el-upload>

      <div class="input-box">
        <el-form :rules="userFormRules" ref="userForm" :model="userData">
          <el-form-item prop="username">
            <el-input type="text" placeholder="Username" v-model="userData.username" class="input">
              <template #prepend>Username:</template>
              <template #append>
                <el-button link type="primary" style="width: 80px" @click="modifyUsername(userForm)">Modify</el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>

        <el-form ref="pswForm" :model="pswData" :rules="pswFormRules">
          <el-form-item prop="oldPassword">
            <el-input type="password" placeholder="Old Password" v-model="pswData.oldPassword" class="input">
              <template #prepend>Old Password:</template>
            </el-input>
          </el-form-item>
          <el-form-item prop="newPassword">
            <el-input type="password" placeholder="New Password" v-model="pswData.newPassword" class="input">
              <template #prepend>New Password:</template>
            </el-input>
          </el-form-item>
          <el-dialog v-model="repeatVisible" title="Repeat Your New Password" width="500">
            <el-form-item prop="repPassword">
              <el-input type="password" v-model="pswData.repPassword"/>
            </el-form-item>
            <template #footer>
              <div class="dialog-footer">
                <el-button @click="repeatVisible = false">Cancel</el-button>
                <el-button type="primary" @click="modifyPassword(pswForm)">Submit</el-button>
              </div>
            </template>
          </el-dialog>

          <el-form-item style="margin-left: 8px">
            <el-button type="primary" @click="openRepeat(pswForm)">Modify Password</el-button>
            <el-button type="info" plain @click="resetPswForm(pswForm)">Reset</el-button>
          </el-form-item>
        </el-form>

      </div>
    </el-card>
  </div>

  <div class="function">
    <router-view></router-view>
  </div>
</template>

<style scoped>
.information {
  width: 100vw;
  height: 30vh;
  max-height: 300px;
  padding: 0;
  display: flex;
}

.login-control {
  width: 100vw;
  height: 50px;
  display: flex;
}

.img-btn {
  width: 210px;
  height: 210px;
  float: left;
  margin: 0;
  padding: 0;
}

.info-box {
  width: 700px;
  min-width: 700px;
  height: 250px;
  float: left;
  margin: auto;
  padding: 0;
}

.input-box {
  width: 430px;
  height: 240px;
  margin: 8px;
  float: left;
}

.input {
  margin-left: 8px;
  height: 35px;
}

.function {
  width: 100vw;
  height: 70vh;
  display: flex;
}
</style>
