const { silentLogin } = require('./utils/auth')

App({
  onLaunch() {
    // 静默登录：wx.login -> 服务端 code2Session -> 自定义登录态 token
    // 登录失败不阻塞点餐（游客态也可下单），仅在需要会员能力时再提示
    silentLogin().catch((err) => {
      console.warn('[app] 静默登录失败，游客模式继续', err)
    })
  },

  globalData: {
    // 当前桌位上下文（扫码后由 pages/index 写入）
    store: null, // { id, name, ... }
    table: null  // { id, tableNumber, storeId, status, ... }
  }
})
