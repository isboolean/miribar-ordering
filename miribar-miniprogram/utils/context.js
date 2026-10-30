/**
 * 桌位上下文：扫码成功后写入当前门店/桌台，全局页面共享并持久化
 * （对标 H5 端 MenuView 通过路由 query 传 storeId/tableId，小程序用 storage 更稳，
 *   顾客杀掉小程序重新扫码/从下拉历史进入时仍可恢复）
 */
const KEY = 'miribar_table_context'

function setContext({ store, table }) {
  const ctx = {
    store: {
      id: store.id,
      name: store.name,
      address: store.address || '',
      businessHours: store.businessHours || '',
      paymentMode: store.paymentMode
    },
    table: {
      id: table.id,
      tableNumber: table.tableNumber,
      storeId: table.storeId,
      status: table.status
    }
  }
  wx.setStorageSync(KEY, JSON.stringify(ctx))
  return ctx
}

function getContext() {
  try {
    return JSON.parse(wx.getStorageSync(KEY) || 'null')
  } catch {
    return null
  }
}

function clearContext() {
  wx.removeStorageSync(KEY)
}

/** 当前桌位是否有效（有门店与桌台） */
function hasContext() {
  const ctx = getContext()
  return !!(ctx && ctx.store && ctx.store.id && ctx.table && ctx.table.id)
}

module.exports = { setContext, getContext, clearContext, hasContext }
