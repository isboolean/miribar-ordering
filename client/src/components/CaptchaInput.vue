<template>
  <div class="captcha-row">
    <input
      class="captcha-input"
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value.trim())"
      placeholder="图形验证码"
      maxlength="4"
      autocomplete="off"
    />
    <img
      v-if="image"
      :src="image"
      class="captcha-img"
      title="点击刷新验证码"
      @click="refresh"
    />
    <span v-else class="captcha-img captcha-loading">加载中…</span>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCaptcha } from '@/api'

const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue'])

const captchaId = ref('')
const image = ref('')

async function refresh() {
  emit('update:modelValue', '')
  try {
    const data = await getCaptcha()
    captchaId.value = data.captchaId
    image.value = data.image
  } catch { /* 静默失败，用户可点击重试 */ }
}

onMounted(refresh)
defineExpose({ refresh, captchaId })
</script>

<style scoped>
.captcha-row { display: flex; gap: 10px; align-items: stretch; }
.captcha-input {
  flex: 1;
  min-width: 0;
  padding: 11px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  transition: border-color .2s;
}
.captcha-input:focus { border-color: #8b5cf6; }
.captcha-img {
  height: 44px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  cursor: pointer;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 110px;
  font-size: 12px;
  color: #9ca3af;
  background: #f3f4f6;
  object-fit: cover;
}
.captcha-loading { padding: 0 12px; }
</style>
