const BASE = '/api'
const TOKEN_KEY = 'miribar_token'
const CUSTOMER_TOKEN_KEY = 'miribar_customer_token'

function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

function getCustomerToken() {
  return localStorage.getItem(CUSTOMER_TOKEN_KEY) || ''
}

function pickToken(kind) {
  return kind === 'customer' ? getCustomerToken() : getToken()
}

function handle401() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem('miribar_staff')
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

async function request(url, options = {}) {
  const { auth, ...rest } = options
  const token = pickToken(auth)
  const res = await fetch(BASE + url, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...rest.headers
    },
    ...rest
  })
  const data = await res.json()
  if (data.code !== 200) {
    if (data.code === 401) {
      handle401()
    }
    throw new Error(data.message || '请求失败')
  }
  return data.data
}

// ========== 员工认证 ==========
export const getCaptcha = () => request('/auth/captcha')
export const login = (username, password, captchaId, captchaCode) =>
  request('/auth/login', { method: 'POST', body: JSON.stringify({ username, password, captchaId, captchaCode }) })
export const sendStaffSms = (phone, captchaId, captchaCode) =>
  request('/auth/staff/sms', { method: 'POST', body: JSON.stringify({ phone, captchaId, captchaCode }) })
export const staffSmsLogin = (phone, code) =>
  request('/auth/staff/sms-login', { method: 'POST', body: JSON.stringify({ phone, code }) })
export const getMe = () => request('/auth/me')

// ========== 顾客认证 ==========
export const sendSmsCode = (phone, captchaId, captchaCode) =>
  request('/auth/customer/sms', { method: 'POST', body: JSON.stringify({ phone, captchaId, captchaCode }) })
export const customerLogin = (phone, code) =>
  request('/auth/customer/login', { method: 'POST', body: JSON.stringify({ phone, code }) })
export const customerPasswordLogin = (phone, password, captchaId, captchaCode) =>
  request('/auth/customer/password-login', { method: 'POST', body: JSON.stringify({ phone, password, captchaId, captchaCode }) })
export const setCustomerPassword = (password) =>
  request('/auth/customer/password/set', { method: 'POST', auth: 'customer', body: JSON.stringify({ password }) })
export const getCustomerMe = () => request('/auth/customer/me', { auth: 'customer' })

// ========== 门店 ==========
export const getStore = (id) => request(`/store/${id}`)

// ========== 菜单 ==========
export const getMenu = (storeId) => request(`/menu/store/${storeId}`)
export const getCategories = (storeId) => request(`/menu/store/${storeId}/categories`)
export const getProductsByCategory = (categoryId) => request(`/menu/category/${categoryId}`)
export const getProductDetail = (productId) => request(`/menu/product/${productId}`)

// ========== 桌台 ==========
export const getTables = (storeId) => request(`/table/store/${storeId}`)
export const getTableByQrToken = (token) => request(`/table/qr/${token}`)
export const getTableById = (id) => request(`/table/${id}`)
export const addTable = (data) => request('/table', { method: 'POST', body: JSON.stringify(data) })
export const updateTable = (id, data) => request(`/table/${id}`, { method: 'PUT', body: JSON.stringify(data) })
export const deleteTable = (id) => request(`/table/${id}`, { method: 'DELETE' })
export const openTable = (id) => request(`/table/${id}/open`, { method: 'POST' })
export const clearTable = (id) => request(`/table/${id}/clear`, { method: 'POST' })
export const getTableBill = (id) => request(`/table/${id}/bill`)
export const checkoutTable = (id, params = '') => request(`/table/${id}/checkout${params ? '?' + params : ''}`, { method: 'POST' })

// ========== 订单 ==========
export const createOrder = (data) => request('/order', { method: 'POST', auth: 'customer', body: JSON.stringify(data) })
export const getOrderDetail = (id) => request(`/order/${id}`)
export const getTableOrders = (tableId) => request(`/order/table/${tableId}`)
export const getStoreActiveOrders = (storeId) => request(`/order/store/${storeId}/active`)
export const getStoreOrdersByStatus = (storeId, status) => request(`/order/store/${storeId}/status/${status}`)
export const acceptOrder = (id) => request(`/order/${id}/accept`, { method: 'POST' })
export const completeOrder = (id) => request(`/order/${id}/complete`, { method: 'POST' })
export const rejectOrder = (id, reason) => request(`/order/${id}/reject?reason=${encodeURIComponent(reason)}`, { method: 'POST' })
export const cancelOrder = (id) => request(`/order/${id}/cancel`, { method: 'POST' })

// ========== 商品管理 ==========
export const adminGetCategories = (storeId) => request(`/admin/product/category/store/${storeId}`)
export const adminAddCategory = (data) => request('/admin/product/category', { method: 'POST', body: JSON.stringify(data) })
export const adminUpdateCategory = (id, data) => request(`/admin/product/category/${id}`, { method: 'PUT', body: JSON.stringify(data) })
export const adminDeleteCategory = (id) => request(`/admin/product/category/${id}`, { method: 'DELETE' })
export const adminGetProducts = (storeId) => request(`/admin/product/store/${storeId}`)
export const adminGetProduct = (id) => request(`/admin/product/${id}`)
export const adminAddProduct = (data) => request('/admin/product', { method: 'POST', body: JSON.stringify(data) })
export const adminUpdateProduct = (id, data) => request(`/admin/product/${id}`, { method: 'PUT', body: JSON.stringify(data) })
export const adminUpdateProductStatus = (id, status) => request(`/admin/product/${id}/status?status=${status}`, { method: 'POST' })
export const adminDeleteProduct = (id) => request(`/admin/product/${id}`, { method: 'DELETE' })

// ========== 菜单权限管理 ==========
export const getMenuList = () => request('/admin/menu/list')
export const getMenuDetail = (id) => request(`/admin/menu/${id}`)
export const addMenu = (data) => request('/admin/menu', { method: 'POST', body: JSON.stringify(data) })
export const updateMenu = (data) => request('/admin/menu', { method: 'PUT', body: JSON.stringify(data) })
export const deleteMenu = (id) => request(`/admin/menu/${id}`, { method: 'DELETE' })

// ========== 角色管理 ==========
export const getRoleList = () => request('/admin/role/list')
export const getEnabledRoles = () => request('/admin/role/enabled')
export const getRoleDetail = (id) => request(`/admin/role/${id}`)
export const addRole = (data) => request('/admin/role', { method: 'POST', body: JSON.stringify(data) })
export const updateRole = (data) => request('/admin/role', { method: 'PUT', body: JSON.stringify(data) })
export const deleteRole = (id) => request(`/admin/role/${id}`, { method: 'DELETE' })
export const assignRoleMenus = (id, menuIds) => request(`/admin/role/${id}/menus`, { method: 'POST', body: JSON.stringify(menuIds) })
export const getRoleMenus = (id) => request(`/admin/role/${id}/menus`)

// ========== 用户菜单权限 ==========
export const getUserMenus = () => request('/auth/menu')
export const getUserPermissions = () => request('/auth/permission')

// ========== 员工管理 ==========
export const getStaffList = (params = {}) => {
  const query = new URLSearchParams(params).toString()
  return request(`/admin/staff/list${query ? '?' + query : ''}`)
}
export const getStaffDetail = (id) => request(`/admin/staff/${id}`)
export const addStaff = (data) => request('/admin/staff', { method: 'POST', body: JSON.stringify(data) })
export const updateStaff = (data) => request('/admin/staff', { method: 'PUT', body: JSON.stringify(data) })
export const deleteStaff = (id) => request(`/admin/staff/${id}`, { method: 'DELETE' })
export const resetStaffPassword = (id) => request(`/admin/staff/${id}/reset-password`, { method: 'POST' })
export const assignStaffRoles = (id, roleIds) => request(`/admin/staff/${id}/roles`, { method: 'POST', body: JSON.stringify(roleIds) })
export const getStaffRoles = (id) => request(`/admin/staff/${id}/roles`)

// ========== 组织管理 ==========
export const getOrgTree = () => request('/admin/org/tree')
export const getOrgList = () => request('/admin/org/list')
export const getEnabledOrgs = () => request('/admin/org/enabled')
export const getOrgDetail = (id) => request(`/admin/org/${id}`)
export const addOrg = (data) => request('/admin/org', { method: 'POST', body: JSON.stringify(data) })
export const updateOrg = (data) => request('/admin/org', { method: 'PUT', body: JSON.stringify(data) })
export const deleteOrg = (id) => request(`/admin/org/${id}`, { method: 'DELETE' })
export const getOrgChildren = (id) => request(`/admin/org/${id}/children`)
export const searchOrg = (keyword) => request(`/admin/org/search?keyword=${encodeURIComponent(keyword)}`)

// ========== 字典类型管理 ==========
export const getDictTypeList = () => request('/admin/dict/type/list')
export const getEnabledDictTypes = () => request('/admin/dict/type/enabled')
export const getDictTypeDetail = (id) => request(`/admin/dict/type/${id}`)
export const getDictTypeByCode = (dictType) => request(`/admin/dict/type/code/${dictType}`)
export const addDictType = (data) => request('/admin/dict/type', { method: 'POST', body: JSON.stringify(data) })
export const updateDictType = (data) => request('/admin/dict/type', { method: 'PUT', body: JSON.stringify(data) })
export const deleteDictType = (id) => request(`/admin/dict/type/${id}`, { method: 'DELETE' })
export const searchDictType = (keyword) => request(`/admin/dict/type/search?keyword=${encodeURIComponent(keyword)}`)

// ========== 字典数据管理 ==========
export const getDictDataList = () => request('/admin/dict/data/list')
export const getDictDataByType = (dictType) => request(`/admin/dict/data/type/${dictType}`)
export const getEnabledDictData = () => request('/admin/dict/data/enabled')
export const getDictDataDetail = (id) => request(`/admin/dict/data/${id}`)
export const getDictDataDefault = (dictType) => request(`/admin/dict/data/default/${dictType}`)
export const getDictDataLabel = (dictType, dictValue) => request(`/admin/dict/data/label?dictType=${encodeURIComponent(dictType)}&dictValue=${encodeURIComponent(dictValue)}`)
export const addDictData = (data) => request('/admin/dict/data', { method: 'POST', body: JSON.stringify(data) })
export const updateDictData = (data) => request('/admin/dict/data', { method: 'PUT', body: JSON.stringify(data) })
export const deleteDictData = (id) => request(`/admin/dict/data/${id}`, { method: 'DELETE' })
export const deleteDictDataByType = (dictType) => request(`/admin/dict/data/type/${dictType}`, { method: 'DELETE' })
export const searchDictData = (keyword) => request(`/admin/dict/data/search?keyword=${encodeURIComponent(keyword)}`)

// ========== 客户管理 ==========
export const getCustomerList = (params = {}) => {
  const query = new URLSearchParams(params).toString()
  return request(`/admin/customer/list${query ? '?' + query : ''}`)
}
export const getCustomerDetail = (id) => request(`/admin/customer/${id}`)
export const addCustomer = (data) => request('/admin/customer', { method: 'POST', body: JSON.stringify(data) })
export const updateCustomer = (data) => request('/admin/customer', { method: 'PUT', body: JSON.stringify(data) })
export const deleteCustomer = (id) => request(`/admin/customer/${id}`, { method: 'DELETE' })
