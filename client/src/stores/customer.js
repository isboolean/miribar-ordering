import { defineStore } from 'pinia'

const TOKEN_KEY = 'miribar_customer_token'
const CUSTOMER_KEY = 'miribar_customer'

function readCustomer() {
  try {
    return JSON.parse(localStorage.getItem(CUSTOMER_KEY)) || null
  } catch {
    return null
  }
}

export const useCustomerStore = defineStore('customer', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    customer: readCustomer()
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    displayName: (state) => (state.customer?.nickname || state.customer?.phone || '')
  },
  actions: {
    setLogin(data) {
      this.token = data.token
      this.customer = data
      localStorage.setItem(TOKEN_KEY, data.token)
      localStorage.setItem(CUSTOMER_KEY, JSON.stringify(data))
    },
    async fetchMe() {
      if (!this.token) return
      try {
        const data = await getCustomerMe()
        this.customer = data
        localStorage.setItem(CUSTOMER_KEY, JSON.stringify(data))
      } catch {
        this.logout()
      }
    },
    logout() {
      this.token = ''
      this.customer = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(CUSTOMER_KEY)
    }
  }
})

import { getCustomerMe } from '@/api'
