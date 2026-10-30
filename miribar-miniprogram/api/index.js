/**
 * API 层：复用后端现有顾客端接口（与 client/src/api/index.js 顾客相关部分一一对应）
 */
const { request } = require('../utils/request')
const { BASE_URL } = require('../utils/config')

// ======== 顾客认证 ========
// 静默登录（小程序专属，后端新增）
const wxLogin = (code) => request('/auth/customer/wx-login', { method: 'POST', data: { code } })
// 手机号验证码登录（复用现有接口）
const getCaptcha = () => request('/auth/captcha')
const sendSmsCode = (phone, captchaId, captchaCode) =>
  request('/auth/customer/sms', { method: 'POST', data: { phone, captchaId, captchaCode } })
const customerLogin = (phone, code) =>
  request('/auth/customer/login', { method: 'POST', data: { phone, code } })
const getCustomerMe = () => request('/auth/customer/me')

// ======== 门店 / 桌台 ========
const getStore = (id) => request(`/store/${id}`)
const getTableByQrToken = (qrToken) => request(`/table/qr/${qrToken}`)
const getTableBill = (tableId) => request(`/table/${tableId}/bill`)

// ======== 菜单 ========
const getMenu = (storeId) => request(`/menu/store/${storeId}`)

// ======== 订单 ========
const createOrder = (data) => request('/order', { method: 'POST', data })
const getTableOrders = (tableId) => request(`/order/table/${tableId}`)
const getOrderDetail = (id) => request(`/order/${id}`)
const cancelOrder = (id) => request(`/order/${id}/cancel`, { method: 'POST' })

module.exports = {
  wxLogin, getCaptcha, sendSmsCode, customerLogin, getCustomerMe,
  getStore, getTableByQrToken, getTableBill,
  getMenu,
  createOrder, getTableOrders, getOrderDetail, cancelOrder,
  BASE_URL
}
