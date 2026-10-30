import { ref, computed } from 'vue'

const KEY = 'miribar-theme'
// 'auto' | 'light' | 'dark'
const theme = ref(localStorage.getItem(KEY) || 'auto')

const prefersDark = () => window.matchMedia('(prefers-color-scheme: dark)').matches

function resolved() {
  return theme.value === 'auto' ? (prefersDark() ? 'dark' : 'light') : theme.value
}

const isDark = ref(resolved() === 'dark')

function applyTheme() {
  const r = resolved()
  document.documentElement.setAttribute('data-theme', r)
  isDark.value = r === 'dark'
}

function setTheme(t) {
  theme.value = t
  localStorage.setItem(KEY, t)
  applyTheme()
}

function toggleTheme() {
  setTheme(isDark.value ? 'light' : 'dark')
}

// 跟随系统（仅 auto 模式下生效）
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
  if (theme.value === 'auto') applyTheme()
})

applyTheme()

export function useTheme() {
  return {
    isDark: computed(() => isDark.value),
    theme,
    toggleTheme
  }
}
