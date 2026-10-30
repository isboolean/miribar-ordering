import { defineStore } from 'pinia'
import { login as apiLogin, getMe, getUserMenus, getUserPermissions } from '@/api'

const TOKEN_KEY = 'miribar_token'
const STAFF_KEY = 'miribar_staff'
const PERMISSIONS_KEY = 'miribar_permissions'
const MENUS_KEY = 'miribar_menus'

function readStaff() {
  try {
    return JSON.parse(localStorage.getItem(STAFF_KEY)) || null
  } catch {
    return null
  }
}

function readPermissions() {
  try {
    return JSON.parse(localStorage.getItem(PERMISSIONS_KEY)) || []
  } catch {
    return []
  }
}

function readMenus() {
  try {
    return JSON.parse(localStorage.getItem(MENUS_KEY)) || []
  } catch {
    return []
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    staff: readStaff(),
    permissions: readPermissions(),
    menus: readMenus()
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    displayName: (state) => (state.staff?.realName || state.staff?.username || ''),
    isAdmin: (state) => [2, 3].includes(state.staff?.role),
    hasPermission: (state) => (perm) => state.permissions.includes(perm)
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
      this.permissions = data.permissions || []
      this.menus = data.menus || []
      localStorage.setItem(TOKEN_KEY, data.token)
      localStorage.setItem(STAFF_KEY, JSON.stringify(data))
      localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(this.permissions))
      localStorage.setItem(MENUS_KEY, JSON.stringify(this.menus))
    },
    async fetchUserMenus() {
      try {
        const menus = await getUserMenus()
        this.menus = menus
        localStorage.setItem(MENUS_KEY, JSON.stringify(menus))
      } catch (e) {
        console.error('Failed to fetch menus:', e)
      }
    },
    async fetchUserPermissions() {
      try {
        const permissions = await getUserPermissions()
        this.permissions = permissions
        localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(permissions))
      } catch (e) {
        console.error('Failed to fetch permissions:', e)
      }
    },
    async fetchMe() {
      if (!this.token) return
      try {
        const data = await getMe()
        this.staff = data
        this.permissions = data.permissions || []
        this.menus = data.menus || []
        localStorage.setItem(STAFF_KEY, JSON.stringify(data))
        localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(this.permissions))
        localStorage.setItem(MENUS_KEY, JSON.stringify(this.menus))
      } catch {
        this.logout()
      }
    },
    logout() {
      this.token = ''
      this.staff = null
      this.permissions = []
      this.menus = []
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(STAFF_KEY)
      localStorage.removeItem(PERMISSIONS_KEY)
      localStorage.removeItem(MENUS_KEY)
    }
  }
})
