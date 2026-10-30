/**
 * 扫码落地页（入口页）
 *
 * 三种进入方式：
 * 1. 扫「小程序码」：scene 参数携带桌码 token（桌台小程序码由后端生成，scene = qrToken），
 *    小程序自动拉起本页，onLoad options.scene 为 URL 编码后的值，需 decodeURIComponent；
 * 2. 扫「普通链接二维码」（存量 H5 桌码）：需在 mp 后台配置「扫普通链接二维码打开小程序」
 *    规则，URL 会通过 options.q（URL 编码）传入，从中解析出 qrToken；
 * 3. 站内扫码：首页点「扫码点餐」按钮调 wx.scanCode，result 为二维码内容字符串。
 *
 * 解析成功后：GET /table/qr/{qrToken} 复用现有桌码接口换取桌台（含 storeId），
 * 再拉门店信息，写入全局桌位上下文，跳转菜单页。
 */
const api = require('../../api/index')
const context = require('../../utils/context')

Page({
  data: {
    status: 'idle', // idle-待扫码 loading-解析中 ok-跳转中 error-桌码无效
    errorMsg: '',
    tableLabel: ''
  },

  onLoad(options) {
    // 优先小程序码 scene，其次普通链接二维码 q，最后兼容直接传 qrToken
    let token = ''
    if (options && options.scene) {
      token = decodeURIComponent(options.scene)
    } else if (options && options.q) {
      // q = 完整原始 URL 的 encodeURIComponent 结果
      const url = decodeURIComponent(options.q)
      token = this.extractTokenFromUrl(url)
    } else if (options && options.qrToken) {
      token = options.qrToken
    }
    if (token) {
      this.resolveTable(token)
    }
  },

  /** 从二维码 URL 中提取桌码 token：约定 URL 形如 https://host/t/{qrToken} 或 ?qrToken=xxx */
  extractTokenFromUrl(url) {
    const pathMatch = url.match(/\/t\/([A-Za-z0-9_-]+)/)
    if (pathMatch) return pathMatch[1]
    const queryMatch = url.match(/[?&]qrToken=([A-Za-z0-9_-]+)/)
    if (queryMatch) return queryMatch[1]
    return ''
  },

  /** 站内扫码按钮：wx.scanCode 拿到二维码内容后统一走解析 */
  onScanTap() {
    wx.scanCode({
      onlyFromCamera: false,
      success: (res) => {
        let token = ''
        const raw = res.result || ''
        if (res.scanType === 'WX_CODE') {
          // 扫小程序码返回内容形如 "pages/index/index?scene=xxx"
          const m = raw.match(/[?&]scene=([^&]+)/)
          token = m ? decodeURIComponent(m[1]) : ''
        }
        if (!token) {
          token = /https?:\/\//.test(raw) ? this.extractTokenFromUrl(raw) : raw
        }
        if (!token) {
          this.setData({ status: 'error', errorMsg: '二维码无法识别，请扫描桌台上的点餐码' })
          return
        }
        this.resolveTable(token)
      },
      fail: () => wx.showToast({ title: '已取消扫码', icon: 'none' })
    })
  },

  /** 解析桌码 → 校验 → 写入上下文 → 进菜单 */
  resolveTable(qrToken) {
    this.setData({ status: 'loading', errorMsg: '' })
    api.getTableByQrToken(qrToken)
      .then((table) => {
        // 桌台已被清台时提示确认（status: 0-空闲 1-已开台 2-已清台）
        if (table.status === 2) {
          this.setData({ status: 'error', errorMsg: '该桌台已清台，请联系店员确认桌号' })
          return
        }
        return api.getStore(table.storeId).then((store) => {
          if (!store || store.status !== 1) {
            this.setData({ status: 'error', errorMsg: '门店暂停营业，请稍后再来' })
            return
          }
          context.setContext({ store, table })
          this.setData({ status: 'ok', tableLabel: `${store.name} · ${table.tableNumber}` })
          // 跳转菜单页，关闭落地页避免返回循环
          wx.redirectTo({ url: '/pages/menu/menu' })
        })
      })
      .catch((err) => {
        this.setData({
          status: 'error',
          errorMsg: err.code === 404 ? '桌码无效或已过期，请联系店员' : (err.message || '解析失败，请重试')
        })
      })
  },

  /** 恢复上次桌位（从下拉历史进入、非扫码场景） */
  onResumeTap() {
    if (!context.hasContext()) {
      wx.showToast({ title: '暂无点餐记录，请先扫码', icon: 'none' })
      return
    }
    wx.redirectTo({ url: '/pages/menu/menu' })
  },

  goOrders() {
    if (!context.hasContext()) {
      wx.showToast({ title: '请先扫描桌台二维码', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/orders/orders' })
  },

  onShareAppMessage() {
    const ctx = context.getContext()
    return {
      title: ctx ? `我在${ctx.store.name}点餐，同桌一起来~` : 'MiriBar 扫码点餐',
      path: '/pages/index/index'
    }
  }
})
