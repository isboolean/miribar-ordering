/**
 * 登录态管理
 * 1. 静默登录：wx.login 拿 code -> POST /auth/customer/wx-login（服务端 code2Session，
 *    按 openid 查/建 t_customer，签发顾客 JWT）。前端只存 token，不碰 session_key。
 * 2. 手机号登录（可选）：复用现有 /auth/customer/sms + /auth/customer/login。
 */
const { request, setToken, getToken, clearToken, TOKEN_KEY } = require('./request')
const CUSTOMER_KEY = 'miribar_customer'

// ======== 静默登录（小程序专属） ========
function silentLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success({ code }) {
        if (!code) {
          reject(new Error('wx.login 未返回 code'))
          return
        }
        request('/auth/customer/wx-login', { method: 'POST', data: { code } })
          .then((data) => {
            setToken(data.token)
            wx.setStorageSync(CUSTOMER_KEY, JSON.stringify(data))
            resolve(data)
          })
          .catch(reject)
      },
      fail: reject
    })
  })
}

// ======== 顾客信息缓存 ========
function getCustomer() {
  try {
    return JSON.parse(wx.getStorageSync(CUSTOMER_KEY) || 'null')
  } catch {
    return null
  }
}

function saveCustomer(vo) {
  // 手机号登录成功后，后端返回 CustomerVO（与静默登录返回结构一致）
  setToken(vo.token)
  wx.setStorageSync(CUSTOMER_KEY, JSON.stringify(vo))
}

function isLoggedIn() {
  return !!getToken()
}

function logout() {
  clearToken()
  wx.removeStorageSync(CUSTOMER_KEY)
}

module.exports = { silentLogin, getCustomer, saveCustomer, isLoggedIn, logout, TOKEN_KEY }
