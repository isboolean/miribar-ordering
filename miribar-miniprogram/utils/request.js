/**
 * 网络请求封装（对标 client/src/api/index.js 的 request）
 * 约定：后端统一返回 { code, message, data }，code === 200 为成功
 * 顾客 token 与员工 token 区分：小程序只使用顾客 token
 */
const { BASE_URL } = require('./config')

const TOKEN_KEY = 'miribar_customer_token'

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || ''
}

function setToken(token) {
  token ? wx.setStorageSync(TOKEN_KEY, token) : wx.removeStorageSync(TOKEN_KEY)
}

function clearToken() {
  wx.removeStorageSync(TOKEN_KEY)
}

/**
 * @param {string} url   以 / 开头的接口路径，如 /menu/store/1
 * @param {object} options { method, data, header }
 * @returns {Promise<any>} 成功时 resolve 后端 data 字段
 */
function request(url, options = {}) {
  const token = getToken()
  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.header || {})
      },
      success(res) {
        const body = res.data
        if (res.statusCode !== 200 || !body || body.code !== 200) {
          const err = new Error((body && body.message) || '请求失败')
          err.code = body && body.code
          // 401：登录态失效，清除 token 静默重登（下次请求自动带上新 token）
          if (err.code === 401) {
            clearToken()
            const { silentLogin } = require('./auth')
            silentLogin().catch(() => {})
          }
          reject(err)
          return
        }
        resolve(body.data)
      },
      fail() {
        reject(new Error('网络异常，请检查网络后重试'))
      }
    })
  })
}

module.exports = { request, getToken, setToken, clearToken, TOKEN_KEY }
