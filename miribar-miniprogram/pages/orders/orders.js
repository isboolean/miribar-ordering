/**
 * 订单跟踪页
 * 数据：GET /order/table/{tableId}（本桌全部订单，含同桌其他设备下的单，按桌聚合）
 * 状态：0-待接单 1-制作中 2-已完成 3-已拒单 4-已取消 5-已结账
 * 实时性：15 秒轮询刷新（后端 WebSocket 看板侧已覆盖，顾客端轮询足够 P0 使用）；
 *         onUnload 清理定时器（防内存泄漏）。
 * 加单：返回菜单页继续点，新订单自动并入本桌账单。
 */
const api = require('../../api/index')
const context = require('../../utils/context')
const { orderStatusInfo, formatPrice, formatTime } = require('../../utils/format')

const POLL_INTERVAL = 15000

Page({
  data: {
    tableNumber: '',
    orders: [],        // 渲染用订单列表
    billTotal: null,   // 桌台账单合计（有未结账订单时展示）
    highlightId: null,
    loading: true
  },

  onLoad(options) {
    const ctx = context.getContext()
    if (!context.hasContext()) {
      wx.redirectTo({ url: '/pages/index/index' })
      return
    }
    this.tableId = ctx.table.id
    this.setData({
      tableNumber: ctx.store.name + ' · ' + ctx.table.tableNumber,
      highlightId: options && options.highlight ? Number(options.highlight) : null
    })
    this.refresh()
  },

  onShow() {
    this.startPolling()
  },

  onHide() {
    this.stopPolling()
  },

  onUnload() {
    this.stopPolling()
  },

  startPolling() {
    this.stopPolling()
    this._timer = setInterval(() => this.refresh(true), POLL_INTERVAL)
  },

  stopPolling() {
    if (this._timer) {
      clearInterval(this._timer)
      this._timer = null
    }
  },

  refresh(silent = false) {
    if (!silent) this.setData({ loading: true })
    Promise.all([
      api.getTableOrders(this.tableId),
      api.getTableBill(this.tableId).catch(() => null)
    ])
      .then(([orders, bill]) => {
        // 新订单在前（后端按创建时间排序，这里双保险倒序）
        const list = (orders || []).slice().reverse().map((vo) => {
          const o = vo.order
          const st = orderStatusInfo(o.status)
          return {
            id: o.id,
            orderNo: o.orderNo,
            status: o.status,
            statusText: st.text,
            statusCls: st.cls,
            total: formatPrice(o.totalAmount),
            remark: o.remark || '',
            time: formatTime(o.createdAt),
            items: (vo.items || []).map((it) => ({
              name: it.productName,
              skuName: it.skuName,
              quantity: it.quantity,
              remark: it.remark || '',
              label: `${it.productName}（${it.skuName}）× ${it.quantity}` + (it.remark ? ` · ${it.remark}` : '')
            })),
            canCancel: o.status === 0
          }
        })
        this.setData({
          orders: list,
          billTotal: bill && bill.totalAmount != null ? formatPrice(bill.totalAmount) : null,
          loading: false
        })
      })
      .catch((err) => {
        this.setData({ loading: false })
        if (!silent) wx.showToast({ title: err.message || '加载失败', icon: 'none' })
      })
  },

  /** 待接单订单可取消（与后端规则一致：仅待支付/待接单可取消） */
  onCancelTap(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '取消订单',
      content: '确定取消这笔订单吗？',
      success: ({ confirm }) => {
        if (!confirm) return
        api.cancelOrder(id)
          .then(() => {
            wx.showToast({ title: '已取消', icon: 'success' })
            this.refresh(true)
          })
          .catch((err) => wx.showToast({ title: err.message || '取消失败', icon: 'none' }))
      }
    })
  },

  /** 加单：回到菜单页（购物车逻辑独立，不影响已提交订单） */
  onAddOrder() {
    wx.navigateBack({
      fail: () => wx.redirectTo({ url: '/pages/menu/menu' })
    })
  },

  onPullDownRefresh() {
    this.refresh(true)
    wx.stopPullDownRefresh()
  }
})
