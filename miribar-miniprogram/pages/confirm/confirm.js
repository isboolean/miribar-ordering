/**
 * 订单确认页
 * 数据源：购物车（utils/cart）；提交：POST /order（顾客 token 携带时服务端自动关联 customer_id）
 * 规则：提交 3 秒防重（幂等保护）；下单成功清空购物车并跳转订单跟踪页。
 */
const api = require('../../api/index')
const context = require('../../utils/context')
const cart = require('../../utils/cart')
const { formatPrice } = require('../../utils/format')

Page({
  data: {
    tableNumber: '',
    items: [],       // [{ skuId, productName, skuName, unitPrice, quantity, remark, itemRemarkEditing }]
    remark: '',
    totalAmount: '0.00',
    totalCount: 0,
    submitting: false,
    paymentModeLabel: '线下结账' // P0 线下结账；在线支付为 P1 预留
  },

  onLoad() {
    const ctx = context.getContext()
    this.setData({ tableNumber: ctx ? ctx.store.name + ' · ' + ctx.table.tableNumber : '' })
    this.refresh()
  },

  onShow() {
    this.refresh()
  },

  refresh() {
    const items = cart.getItems().map((i) => ({ ...i, unitPriceLabel: formatPrice(i.unitPrice) }))
    this.setData({
      items,
      remark: cart.getRemark(),
      totalAmount: cart.totalAmount().toFixed(2),
      totalCount: cart.totalCount()
    })
  },

  onRemarkInput(e) {
    cart.setRemark(e.detail.value)
  },

  /** 单品备注（弹键盘输入） */
  onItemRemark(e) {
    cart.setItemRemark(e.currentTarget.dataset.skuId, e.detail.value)
    this.refresh()
  },

  /** 数量调整（确认页只允许减少和返回加购页增加，保持简单） */
  onMinus(e) {
    cart.removeItem(e.currentTarget.dataset.skuId)
    this.refresh()
  },

  /** 提交订单（3 秒防重） */
  onSubmit() {
    if (this.data.submitting || this.data.totalCount === 0) return
    const ctx = context.getContext()
    if (!ctx) {
      wx.showToast({ title: '桌位信息丢失，请重新扫码', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    const payload = {
      storeId: ctx.store.id,
      tableId: ctx.table.id,
      remark: this.data.remark || '',
      items: cart.toOrderItems()
    }
    api.createOrder(payload)
      .then((orderVO) => {
        cart.clear()
        wx.showToast({ title: '下单成功', icon: 'success' })
        setTimeout(() => {
          wx.redirectTo({ url: '/pages/orders/orders?highlight=' + orderVO.order.id })
        }, 600)
      })
      .catch((err) => {
        this.setData({ submitting: false })
        // 提交失败不锁死，允许重试（服务端二次校验商品有效性/库存/价格）
        wx.showModal({
          title: '下单失败',
          content: err.message || '请稍后重试',
          showCancel: false
        })
      })
  },

  goBackMenu() {
    wx.navigateBack()
  }
})
