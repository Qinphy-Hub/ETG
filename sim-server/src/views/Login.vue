<script setup lang="ts">
import {onMounted, ref, reactive} from "vue";
import {ElMessage} from "element-plus";
import type {FormInstance, FormRules} from "element-plus"
import {useRouter} from "vue-router";
import {useTokenStore} from '@/stores/token'
import {registerService, loginService, verificationService, forgotPasswordService} from "@/api/user"


// base data
const router =  useRouter()
// register data
const registerForm = ref<FormInstance>()
const registerData = reactive({
  username: "",
  password: "",
  email: "",
  code: ""
})
const send = ref(false)
// password validated
const validatePassword = (rule: any, value: any, callback: any) => {
  const reg = /^(?=.*[a-zA-Z0-9].*)(?=.*[a-zA-Z.!@#$%^&*].*)(?=.*[0-9.!@#$%^&*].*).{6,32}$/;
  if (!reg.test(value)) {
    callback(new Error("two types of digits, letters, special symbols."));
  } else {
    callback();
  }
}
// form validated rules
const registerRule = reactive<FormRules<typeof registerData>>({
  username: [
    {required: true, message: "Username cannot be empty.", trigger: "blur"},
    {min: 5, max: 20, message: "Length of username is between 5 and 20.", trigger: "blur"},
  ],
  password: [
    {required: true, message: "Password cannot be empty.", trigger: "blur"},
    {min: 6, max: 32, message: "Length of password is between 6 and 32.", trigger: "blur"},
    {required: true, validator: validatePassword, trigger: "blur"},
  ],
  email: [
    {required: true, message: "Email cannot be empty.", trigger: "blur"},
    {type: 'email', message: 'Please input correct email address', trigger: ['blur', 'change']}
  ],
  code: [
    {required: true, message: "Verification code cannot be empty.", trigger: "blur"},
    {min: 6, max: 6, message: "Length of verification code is 6.", trigger: "blur"},
  ]
})
// login data
const tokenStore = useTokenStore()
const loginForm = ref<FormInstance>()
const loginData = reactive({
  email: "",
  password: "",
})
const loginRule = reactive<FormRules<typeof loginData>>({
  password: [{required: true, message: "Password cannot be empty.", trigger: "blur"}],
  email: [
    {required: true, message: "Email cannot be empty.", trigger: "blur"},
    {type: 'email', message: 'Please input correct email address', trigger: ['blur', 'change']}
  ],
})


// ----------------------- js animation -----------------------
const signUp = ref<any>(null)
const signIn = ref<any>(null)
const container = ref<any>(null)

onMounted(() => {
  // js animation
  signUp.value.addEventListener('click', () => {
    container.value.classList.add("right-panel-active");
  });

  signIn.value.addEventListener('click', () => {
    container.value.classList.remove("right-panel-active");
  });
})


// ----------------------- register -----------------------
// send email
const sendEmail = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  send.value = true
  await formEl.validateField("email", async (valid) => {
    if (valid) {
      let result = await verificationService(registerData.email) as any
      ElMessage.success({
        message: "Successfully send email to " + registerData.email + ": " + result.data,
        plain: true,
      })
    }
  })
  send.value = false
}
// register request
const register = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      await registerService(registerData)
      ElMessage.success({
        message: "Register successfully",
        plain: true,
      })
      formEl.resetFields()
      // back to login form
      container.value.classList.remove("right-panel-active");

    }
  })
}

// ----------------------- login -----------------------
// login request
const login = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      let result = await loginService(loginData) as any
      ElMessage.success({
        message: "Login success.",
        plain: true,
      })
      tokenStore.setToken(result.data)
      await router.push('/profile')
    }
  })
}
// forget password request
const sendPassword = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validateField("email", async (valid) => {
    if (valid) {
      await forgotPasswordService(loginData.email)
      ElMessage.success({
        message: "Password reset successfully!",
        plain: true,
      })
    }
  })
}

</script>

<template>
  <div class="box">
    <div class="container" id="container" ref="container">
      <!--  Sign Up  -->
      <div class="form-container sign-up-container">
        <el-form ref="registerForm" :model="registerData" method="post" :rules="registerRule">
          <h2>Create Account</h2>
          <el-form-item prop="username">
            <el-input type="text" placeholder="Username" v-model="registerData.username"/>
          </el-form-item>
          <el-form-item prop="email">
            <el-input type="text" placeholder="Email" v-model="registerData.email"/>
          </el-form-item>
          <el-form-item prop="password">
            <el-input type="password" placeholder="Password" v-model="registerData.password" autocomplete="off" show-password/>
          </el-form-item>
          <el-form-item prop="code">
            <el-input v-model="registerData.code" style="width: 50%; float: left; margin-right: 3px" placeholder="Verification Code"/>
            <el-button type="primary" :loading="send" text bg style="width: 45%; height: 100%; margin-left: 3%" @click="sendEmail(registerForm)">Send Code</el-button>
          </el-form-item>
          <el-button class="button" style="height: 50px" @click="register(registerForm)">Sign Up</el-button>
        </el-form>
      </div>
      <!--  Sign In  -->
      <div class="form-container sign-in-container">
        <el-form ref="loginForm" :model="loginData" method="post" :rules="loginRule">
          <h1>Sign in</h1>
          <el-popconfirm
              title="We will send new password to your email."
              @confirm="sendPassword(loginForm)"
          >
            <template #reference>
              <el-button type="primary" link style="height: 30px">forget password?</el-button>
            </template>
          </el-popconfirm>
          <el-form-item prop="email">
            <el-input type="text" placeholder="Email" v-model="loginData.email"/>
          </el-form-item>
          <el-form-item prop="password">
            <el-input type="password" placeholder="Password" v-model="loginData.password" show-password/>
          </el-form-item>
          <el-button class="button" style="height: 50px" @click="login(loginForm)">Sign In</el-button>
        </el-form>
      </div>
      <!--  overlay  -->
      <div class="overlay-container">
        <div class="overlay">
          <div class="overlay-panel overlay-left">
            <h1>Welcome Back!</h1>
            <p>To keep connected with us please login with your personal info</p>
            <button class="button ghost" id="signIn" ref="signIn">Sign In</button>
          </div>
          <div class="overlay-panel overlay-right">
            <h1>Hello, Friend!</h1>
            <p>Enter your personal details and start journey with us</p>
            <button class="button ghost" id="signUp" ref="signUp">Sign Up</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}
*:focus {
  outline: none;
}

body {
  background: #f6f5f7;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  font-family: 'Montserrat', sans-serif;
  height: 100vh;
  margin: 0;
}

h1 {
  font-weight: bold;
  margin: 0;
}

h2 {
  text-align: center;
}

p {
  font-size: 14px;
  font-weight: 100;
  line-height: 20px;
  letter-spacing: 0.5px;
  margin: 20px 0 30px;
}

span {
  font-size: 12px;
}

a, .link {
  color: #333;
  font-size: 14px;
  text-decoration: none;
  margin: 15px 0;
}

*:focus {
  outline: none;
}

.button {
  border-radius: 16px;
  border: 1px solid #0052d4;
  background-color: #0052d4;
  color: #FFFFFF;
  font-size: 16px;
  font-weight: bold;
  padding: 12px 45px;
  letter-spacing: 1px;
  transition: transform 80ms ease-in;
  cursor: pointer;
}

.button:active {
  transform: scale(0.95);
}

.button:focus {
  outline: none;
}

.button.ghost {
  background-color: transparent;
  border-color: #FFFFFF;
}

form {
  background-color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 0 50px;
  height: 100%;
  text-align: center;
}

:deep(.el-input__wrapper) {
  background-color: #eee;
  border: none;
  width: 100%;
}

:deep(.el-input__inner) {
  background-color: #eee;
  border: none;
  margin: 8px 0;
  width: 100%;
}

input {
  background-color: #eee;
  border: none;
  padding: 12px 15px;
  margin: 8px 0;
  width: 100%;
}

input:focus {
  outline: 1px solid #0052d4;
}

:deep(.el-form-item) {
  width: 100%;
}

.box {
  width: 100vw;
  height: 100vh;
  display: flex;
  margin: 0;
  padding: 0;
}

.container {
  margin: auto;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 14px 28px rgba(0,0,0,0.25),
  0 10px 10px rgba(0,0,0,0.22);
  position: relative;
  overflow: hidden;
  width: 768px;
  max-width: 100%;
  height: 480px;
}

.form-container {
  position: absolute;
  top: 0;
  height: 100%;
  transition: all 0.6s ease-in-out;
}

.sign-in-container {
  left: 0;
  width: 50%;
  z-index: 2;
}

.container.right-panel-active .sign-in-container {
  transform: translateX(100%);
}

.sign-up-container {
  left: 0;
  width: 50%;
  opacity: 0;
  z-index: 1;
}

.container.right-panel-active .sign-up-container {
  transform: translateX(100%);
  opacity: 1;
  z-index: 5;
  animation: show 0.6s;
}

@keyframes show {
  0%, 49.99% {
    opacity: 0;
    z-index: 1;
  }

  50%, 100% {
    opacity: 1;
    z-index: 5;
  }
}

.overlay-container {
  position: absolute;
  top: 0;
  left: 50%;
  width: 50%;
  height: 100%;
  overflow: hidden;
  transition: transform 0.6s ease-in-out;
  z-index: 100;
}

.container.right-panel-active .overlay-container{
  transform: translateX(-100%);
}

.overlay {
  background: #0052d4;
  background: -webkit-linear-gradient(to right, #4364f7, #0052d4);
  background: linear-gradient(to right, #4364f7, #0052d4);
  background-repeat: no-repeat;
  background-size: cover;
  background-position: 0 0;
  color: #FFFFFF;
  position: relative;
  left: -100%;
  height: 100%;
  width: 200%;
  transform: translateX(0);
  transition: transform 0.6s ease-in-out;
}

.container.right-panel-active .overlay {
  transform: translateX(50%);
}

.overlay-panel {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 0 40px;
  text-align: center;
  top: 0;
  height: 100%;
  width: 50%;
  transform: translateX(0);
  transition: transform 0.6s ease-in-out;
}

.overlay-left {
  transform: translateX(-20%);
}

.container.right-panel-active .overlay-left {
  transform: translateX(0);
}

.overlay-right {
  right: 0;
  transform: translateX(0);
}

.container.right-panel-active .overlay-right {
  transform: translateX(20%);
}

footer {
  background-color: #222;
  color: #fff;
  font-size: 14px;
  bottom: 0;
  position: fixed;
  left: 0;
  right: 0;
  text-align: center;
  z-index: 999;
}

footer p {
  margin: 10px 0;
}

footer i {
  color: red;
}

footer a {
  color: #3c97bf;
  text-decoration: none;
}
</style>
