/**
 * 会员登录页（可选，非强制）
 * 静默登录已在 app.js 完成（wx.login → /auth/customer/wx-login）；
 * 本页用于绑定手机号（解锁积分/优惠券/再来一单等会员能力，P1），
 * 完全复用现有接口：GET /auth/captcha → POST /auth/customer/sms → POST /auth/customer/login
 */
const api = require('../../api/index')
const { saveCustomer, isLoggedIn } = require('../../utils/auth')

Page({
  data: {
    phone: '',
    smsCode: '',
    captchaId: '',
    captchaImage: '',
    captchaCode: '',
    countdown: 0,
    submitting: false
  },

  onLoad() {
    if (isLoggedIn()) {
      // 已登录直接返回（登录非强制，游客态可完成点餐）
      wx.navigateBack({ fail: () => wx.switchTab && wx.switchTab({}) })
    }
    this.refreshCaptcha()
  },

  onUnload() {
    if (this._timer) clearInterval(this._timer)
  },

  refreshCaptcha() {
    api.getCaptcha()
      .then(({ captchaId, image }) => {
        this.setData({ captchaId, captchaImage: image, captchaCode: '' })
      })
      .catch((err) => wx.showToast({ title: err.message || '验证码加载失败', icon: 'none' }))
  },

  onCaptchaReload() {
    this.refreshCaptcha()
  },

  onPhoneInput(e) { this.setData({ phone: e.detail.value }) },
  onCaptchaInput(e) { this.setData({ captchaCode: e.detail.value }) },
  onSmsInput(e) { this.setData({ smsCode: e.detail.value }) },

  /** 发送短信验证码：图形验证码校验通过后下发 */
  onSendSms() {
    const { phone, captchaId, captchaCode, countdown } = this.data
    if (countdown > 0) return
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    if (!captchaCode) {
      wx.showToast({ title: '请输入图形验证码', icon: 'none' })
      return
    }
    api.sendSmsCode(phone, captchaId, captchaCode)
      .then(() => {
        wx.showToast({ title: '验证码已发送', icon: 'none' })
        this.setData({ countdown: 60 })
        this._timer = setInterval(() => {
          const c = this.data.countdown - 1
          this.setData({ countdown: c })
          if (c <= 0) clearInterval(this._timer)
        }, 1000)
      })
      .catch((err) => {
        wx.showToast({ title: err.message || '发送失败', icon: 'none' })
        this.refreshCaptcha()
      })
  },

  onSubmit() {
    const { phone, smsCode, submitting } = this.data
    if (submitting) return
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    if (!smsCode) {
      wx.showToast({ title: '请输入短信验证码', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    api.customerLogin(phone, smsCode)
      .then((vo) => {
        saveCustomer(vo)
        wx.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 600)
      })
      .catch((err) => {
        this.setData({ submitting: false })
        wx.showToast({ title: err.message || '登录失败', icon: 'none' })
        this.refreshCaptcha()
      })
  }
})
