/**
 * 购物车（对标 client/src/stores/cart.js，改为 storage 持久化 + 观察者通知）
 * - 同桌多设备各自维护购物车，提交后生成独立订单（与 H5 规则一致）
 * - 结构：items: [{ skuId, productId, productName, skuName, unitPrice, quantity, remark, itemType, stock }]
 */
const KEY = 'miribar_cart'

let listeners = []

function _read() {
  try {
    return JSON.parse(wx.getStorageSync(KEY) || 'null') || { items: [], orderRemark: '' }
  } catch {
    return { items: [], orderRemark: '' }
  }
}

function _write(cart) {
  wx.setStorageSync(KEY, JSON.stringify(cart))
  listeners.forEach((fn) => fn(cart))
}

/** 页面 onShow/onLoad 时订阅，onUnload 时用返回的解绑函数取消 */
function subscribe(fn) {
  listeners.push(fn)
  return () => {
    listeners = listeners.filter((f) => f !== fn)
  }
}

function getItems() {
  return _read().items
}

function getRemark() {
  return _read().orderRemark
}

function setRemark(text) {
  const cart = _read()
  cart.orderRemark = text || ''
  _write(cart)
}

/** 加购一件商品规格（同 skuId 数量 +1）；库存上限由 stock 控制 */
function addItem({ skuId, productId, productName, skuName, unitPrice, itemType, stock }) {
  const cart = _read()
  const existing = cart.items.find((i) => i.skuId === skuId)
  if (existing) {
    if (stock > 0 && existing.quantity >= stock) {
      wx.showToast({ title: '已达库存上限', icon: 'none' })
      return false
    }
    existing.quantity++
  } else {
    cart.items.push({
      skuId, productId, productName, skuName,
      unitPrice, quantity: 1, remark: '', itemType,
      stock: stock || 0
    })
  }
  _write(cart)
  return true
}

/** 减一件（减到 0 移除） */
function removeItem(skuId) {
  const cart = _read()
  const idx = cart.items.findIndex((i) => i.skuId === skuId)
  if (idx < 0) return
  if (cart.items[idx].quantity > 1) {
    cart.items[idx].quantity--
  } else {
    cart.items.splice(idx, 1)
  }
  _write(cart)
}

function increaseQty(skuId) {
  const cart = _read()
  const item = cart.items.find((i) => i.skuId === skuId)
  if (!item) return
  if (item.stock > 0 && item.quantity >= item.stock) {
    wx.showToast({ title: '已达库存上限', icon: 'none' })
    return
  }
  item.quantity++
  _write(cart)
}

function setItemRemark(skuId, remark) {
  const cart = _read()
  const item = cart.items.find((i) => i.skuId === skuId)
  if (item) {
    item.remark = remark || ''
    _write(cart)
  }
}

function totalCount() {
  return getItems().reduce((s, i) => s + i.quantity, 0)
}

function totalAmount() {
  return getItems().reduce((s, i) => s + i.unitPrice * i.quantity, 0)
}

function clear() {
  _write({ items: [], orderRemark: '' })
}

/** 转换为下单接口的 items 结构（对标 OrderItemRequest: skuId/quantity/remark） */
function toOrderItems() {
  return getItems().map((i) => ({ skuId: i.skuId, quantity: i.quantity, remark: i.remark }))
}

module.exports = {
  subscribe, getItems, getRemark, setRemark,
  addItem, removeItem, increaseQty, setItemRemark,
  totalCount, totalAmount, clear, toOrderItems
}
