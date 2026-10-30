<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <div class="brand-icon">🍸</div>
        <h1>MiriBar</h1>
        <p>员工登录</p>
      </div>

      <!-- 登录方式切换 -->
      <div class="tab-switch">
        <button type="button" :class="{ active: tab === 'password' }" @click="switchTab('password')">账号密码登录</button>
        <button type="button" :class="{ active: tab === 'sms' }" @click="switchTab('sms')">手机号验证码登录</button>
      </div>

      <!-- 方式一：账号密码 -->
      <form v-if="tab === 'password'" @submit.prevent="handleLogin">
        <div class="field">
          <label>工号</label>
          <input v-model.trim="username" type="text" placeholder="请输入10位员工工号（管理员用 admin）" autocomplete="username" required />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" autocomplete="current-password" required />
        </div>
        <div class="field">
          <label>图形验证码</label>
          <CaptchaInput ref="captchaRef" v-model="captchaCode" />
        </div>
        <div v-if="errorMsg" class="error">{{ errorMsg }}</div>
        <button class="login-btn" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登 录' }}
        </button>
      </form>

      <!-- 方式二：手机号验证码 -->
      <form v-else @submit.prevent="handleSmsForm">
        <div class="field">
          <label>手机号</label>
          <input v-model.trim="phone" type="tel" maxlength="11" placeholder="请输入已绑定的手机号" autocomplete="tel" />
        </div>
        <div class="field">
          <label>图形验证码</label>
          <CaptchaInput ref="captchaRef" v-model="captchaCode" />
        </div>
        <template v-if="codeSent">
          <div class="field">
            <label>短信验证码</label>
            <input v-model.trim="smsCode" type="text" maxlength="6" inputmode="numeric" placeholder="请输入短信验证码" />
          </div>
          <button class="resend-btn" type="button" :disabled="countdown > 0" @click="handleSendSms">
            {{ countdown > 0 ? `${countdown}s 后可重发` : '重新发送验证码' }}
          </button>
        </template>
        <div v-if="errorMsg" class="error">{{ errorMsg }}</div>
        <button class="login-btn" type="submit" :disabled="loading || (codeSent && !smsCode)">
          {{ loading ? '登录中…' : (codeSent ? '登 录' : '获取验证码') }}
        </button>
      </form>

      <div class="tips">
        <p>管理员：admin / admin123　员工：10 位工号（如 0000000001）/ 初始密码 123456</p>
        <p>短信登录：13800000001（admin）等，图形/短信验证码开发模式均为 1234</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { sendStaffSms, staffSmsLogin } from '@/api'
import CaptchaInput from '@/components/CaptchaInput.vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const tab = ref('password')
const username = ref('')
const password = ref('')
const phone = ref('')
const smsCode = ref('')
const captchaCode = ref('')
const captchaRef = ref(null)
const codeSent = ref(false)
const errorMsg = ref('')
const loading = ref(false)
const countdown = ref(0)
let timer = null

function switchTab(t) {
  tab.value = t
  errorMsg.value = ''
  captchaCode.value = ''
  codeSent.value = false
  clearInterval(timer)
}

function startCountdown() {
  countdown.value = 60
  clearInterval(timer)
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

function validateCaptcha() {
  if (!captchaCode.value) {
    errorMsg.value = '请输入图形验证码'
    return false
  }
  return true
}

async function refreshCaptcha() {
  captchaCode.value = ''
  await captchaRef.value?.refresh()
}

function redirectTo(staff) {
  router.replace(route.query.redirect || ([2, 3].includes(staff.role) ? '/admin' : '/kitchen'))
}

async function handleLogin() {
  if (!validateCaptcha()) return
  loading.value = true
  errorMsg.value = ''
  try {
    const staff = await auth.login(username.value, password.value, captchaRef.value.captchaId, captchaCode.value)
    redirectTo(staff)
  } catch (e) {
    errorMsg.value = e.message || '登录失败，请重试'
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}

async function handleSendSms() {
  if (!/^1\d{10}$/.test(phone.value)) {
    errorMsg.value = '请输入正确的手机号'
    return
  }
  if (!validateCaptcha()) return
  errorMsg.value = ''
  try {
    await sendStaffSms(phone.value, captchaRef.value.captchaId, captchaCode.value)
    codeSent.value = true
    startCountdown()
    await refreshCaptcha()
  } catch (e) {
    errorMsg.value = e.message || '验证码发送失败'
    await refreshCaptcha()
  }
}

async function handleSmsForm() {
  if (!codeSent.value) {
    await handleSendSms()
    return
  }
  if (!smsCode.value) {
    errorMsg.value = '请输入短信验证码'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const staff = await staffSmsLogin(phone.value, smsCode.value)
    auth.persistLogin(staff)
    redirectTo(staff)
  } catch (e) {
    errorMsg.value = e.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  padding: 20px;
}
.login-card {
  width: 100%;
  max-width: 400px;
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  padding: 36px 32px 26px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-md);
}
.brand { text-align: center; margin-bottom: 22px; }
.brand-icon { font-size: 40px; }
.brand h1 { margin: 8px 0 4px; font-size: 22px; color: var(--text); letter-spacing: 2px; font-weight: 600; }
.brand p { margin: 0; font-size: 13px; color: var(--text-muted); }
.tab-switch {
  display: flex;
  background: var(--surface-2);
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 22px;
}
.tab-switch button {
  flex: 1;
  padding: 9px 0;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-size: 13px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all .2s;
}
.tab-switch button.active { background: var(--primary); color: #fff; font-weight: 600; }
.field { margin-bottom: 16px; }
.field label { display: block; font-size: 13px; color: var(--text); margin-bottom: 6px; }
.field input {
  width: 100%;
  box-sizing: border-box;
  padding: 11px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  transition: border-color .2s;
}
.field input:focus { border-color: var(--primary); }
.error {
  background: rgba(239,68,68,.08);
  color: var(--danger);
  font-size: 13px;
  padding: 8px 12px;
  border-radius: 8px;
  margin-bottom: 14px;
}
.login-btn {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 8px;
  background: var(--primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background .2s;
}
.login-btn:hover { background: var(--primary-dark); }
.login-btn:disabled { opacity: .6; cursor: not-allowed; }
.resend-btn {
  display: block;
  margin: -6px auto 14px;
  border: none;
  background: transparent;
  color: var(--primary);
  font-size: 13px;
  cursor: pointer;
}
.resend-btn:disabled { color: var(--text-muted); cursor: not-allowed; }
.tips {
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px dashed var(--border);
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.8;
}
.tips p { margin: 0; }
</style>
