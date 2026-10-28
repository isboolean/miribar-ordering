<template>
  <div v-if="visible" class="clm-mask" @click.self="close">
    <div class="clm-card">
      <button class="clm-close" @click="close">✕</button>
      <h2 class="clm-title">登录 MiriBar</h2>
      <p class="clm-sub">登录后下单自动关联你的订单</p>

      <!-- 登录方式切换 -->
      <div class="clm-tabs">
        <button type="button" :class="{ active: tab === 'code' }" @click="switchTab('code')">验证码登录</button>
        <button type="button" :class="{ active: tab === 'password' }" @click="switchTab('password')">密码登录</button>
      </div>

      <!-- 方式一：手机号验证码 -->
      <form v-if="tab === 'code'" @submit.prevent="handleSmsForm">
        <input
          v-model.trim="phone"
          class="clm-input"
          type="tel"
          maxlength="11"
          placeholder="请输入手机号"
          autocomplete="tel"
        />
        <CaptchaInput ref="captchaRef" v-model="captchaCode" class="clm-captcha" />
        <template v-if="codeSent">
          <input
            v-model.trim="code"
            class="clm-input"
            type="text"
            maxlength="6"
            inputmode="numeric"
            placeholder="请输入 6 位短信验证码（开发模式：1234）"
          />
          <button class="clm-resend" type="button" :disabled="countdown > 0" @click="handleSendCode">
            {{ countdown > 0 ? `${countdown}s 后可重发` : '重新发送验证码' }}
          </button>
        </template>
        <button class="clm-btn" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : (codeSent ? '登 录' : '获取验证码') }}
        </button>
      </form>

      <!-- 方式二：手机号密码 -->
      <form v-else @submit.prevent="handlePasswordLogin">
        <input
          v-model.trim="phone"
          class="clm-input"
          type="tel"
          maxlength="11"
          placeholder="请输入手机号"
          autocomplete="tel"
        />
        <input
          v-model="password"
          class="clm-input"
          type="password"
          placeholder="请输入密码"
          autocomplete="current-password"
        />
        <CaptchaInput ref="captchaRef" v-model="captchaCode" class="clm-captcha" />
        <button class="clm-btn" type="submit" :disabled="loading || !password">
          {{ loading ? '登录中…' : '登 录' }}
        </button>
        <p class="clm-hint">未设置密码？先用「验证码登录」，登录后可设置密码</p>
      </form>

      <div v-if="errorMsg" class="clm-error">{{ errorMsg }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { sendSmsCode, customerLogin, customerPasswordLogin } from '@/api'
import { useCustomerStore } from '@/stores/customer'
import CaptchaInput from '@/components/CaptchaInput.vue'

const visible = defineModel('visible', { type: Boolean, default: false })
const emit = defineEmits(['success'])

const customerStore = useCustomerStore()

const tab = ref('code')
const phone = ref('')
const code = ref('')
const password = ref('')
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

function validatePhone() {
  if (!/^1\d{10}$/.test(phone.value)) {
    errorMsg.value = '请输入正确的手机号'
    return false
  }
  return true
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

async function handleSendCode() {
  if (!validatePhone() || !validateCaptcha()) return
  errorMsg.value = ''
  try {
    await sendSmsCode(phone.value, captchaRef.value.captchaId, captchaCode.value)
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
    await handleSendCode()
    return
  }
  if (!code.value) {
    errorMsg.value = '请输入短信验证码'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const data = await customerLogin(phone.value, code.value)
    customerStore.setLogin(data)
    emit('success', data)
    close()
  } catch (e) {
    errorMsg.value = e.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}

async function handlePasswordLogin() {
  if (!validatePhone() || !validateCaptcha()) return
  loading.value = true
  errorMsg.value = ''
  try {
    const data = await customerPasswordLogin(phone.value, password.value, captchaRef.value.captchaId, captchaCode.value)
    customerStore.setLogin(data)
    emit('success', data)
    close()
  } catch (e) {
    errorMsg.value = e.message || '登录失败，请重试'
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}

function close() {
  visible.value = false
  tab.value = 'code'
  phone.value = ''
  code.value = ''
  password.value = ''
  captchaCode.value = ''
  codeSent.value = false
  errorMsg.value = ''
  clearInterval(timer)
}
</script>

<style scoped>
.clm-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
}
.clm-card {
  position: relative;
  width: 100%;
  max-width: 340px;
  background: #fff;
  border-radius: 16px;
  padding: 30px 24px 22px;
}
.clm-close {
  position: absolute;
  top: 12px;
  right: 14px;
  border: none;
  background: transparent;
  font-size: 16px;
  color: #9ca3af;
  cursor: pointer;
}
.clm-title { margin: 0 0 4px; font-size: 20px; color: #1f2937; }
.clm-sub { margin: 0 0 16px; font-size: 12px; color: #6b7280; }
.clm-tabs {
  display: flex;
  background: #f3f4f6;
  border-radius: 10px;
  padding: 3px;
  margin-bottom: 18px;
}
.clm-tabs button {
  flex: 1;
  padding: 8px 0;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
  transition: all .2s;
}
.clm-tabs button.active { background: #8b5cf6; color: #fff; font-weight: 600; }
.clm-input {
  width: 100%;
  box-sizing: border-box;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  font-size: 16px;
  outline: none;
  margin-bottom: 14px;
}
.clm-input:focus { border-color: #8b5cf6; }
.clm-captcha { margin-bottom: 14px; }
.clm-captcha :deep(input) {
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  font-size: 16px;
  outline: none;
}
.clm-captcha :deep(input:focus) { border-color: #8b5cf6; }
.clm-captcha :deep(img) { height: 46px; }
.clm-btn {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 10px;
  background: #8b5cf6;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}
.clm-btn:disabled { opacity: .5; cursor: not-allowed; }
.clm-resend {
  display: block;
  margin: -4px auto 12px;
  border: none;
  background: transparent;
  color: #8b5cf6;
  font-size: 13px;
  cursor: pointer;
}
.clm-resend:disabled { color: #9ca3af; cursor: not-allowed; }
.clm-hint { margin: 12px 0 0; font-size: 12px; color: #9ca3af; text-align: center; }
.clm-error {
  margin-top: 12px;
  background: #fef2f2;
  color: #dc2626;
  font-size: 13px;
  padding: 8px 12px;
  border-radius: 8px;
}
</style>
