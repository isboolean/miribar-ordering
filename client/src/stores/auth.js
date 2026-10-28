import { defineStore } from 'pinia'
import { login as apiLogin, getMe } from '@/api'

const TOKEN_KEY = 'miribar_token'
const STAFF_KEY = 'miribar_staff'

function readStaff() {
  try {
    return JSON.parse(localStorage.getItem(STAFF_KEY)) || null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    staff: readStaff()
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    displayName: (state) => (state.staff?.realName || state.staff?.username || ''),
    isAdmin: (state) => [2, 3].includes(state.staff?.role)
  },
  actions: {
    async login(username, password, captchaId, captchaCode) {
      const data = await apiLogin(username, password, captchaId, captchaCode)
      this.persistLogin(data)
      return data
    },
    persistLogin(data) {
      this.token = data.token
      this.staff = data
      localStorage.setItem(TOKEN_KEY, data.token)
      localStorage.setItem(STAFF_KEY, JSON.stringify(data))
    },
    async fetchMe() {
      if (!this.token) return
      try {
        const data = await getMe()
        this.staff = data
        localStorage.setItem(STAFF_KEY, JSON.stringify(data))
      } catch {
        this.logout()
      }
    },
    logout() {
      this.token = ''
      this.staff = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(STAFF_KEY)
    }
  }
})
