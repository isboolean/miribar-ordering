/**
 * 菜单页（核心页）
 * 数据：GET /menu/store/{storeId} -> [{ category, products: [{ product, skus }] }]（仅上架）
 * 交互：左分类侧栏 + 右商品列表联动；单规格点击直接加购，多规格弹出选择弹窗；
 *       底部购物车栏（数量/合计）+ 购物车明细弹层（改数量/单品备注）。
 */
const api = require('../../api/index')
const context = require('../../utils/context')
const cart = require('../../utils/cart')
const { formatPrice } = require('../../utils/format')

Page({
  data: {
    storeName: '',
    tableNumber: '',
    loading: true,
    loadError: '',
    categories: [],      // [{ id, name, icon }]
    activeIndex: 0,
    groups: [],          // [{ category, products }] 渲染用
    cartCount: 0,
    cartAmount: '0.00',
    showCart: false,
    cartItems: [],       // 购物车弹层
    skuPopup: null       // { product, skus } 规格弹窗
  },

  onLoad() {
    const ctx = context.getContext()
    if (!context.hasContext()) {
      wx.redirectTo({ url: '/pages/index/index' })
      return
    }
    this.storeId = ctx.store.id
    this.setData({ storeName: ctx.store.name, tableNumber: ctx.table.tableNumber })
    this.loadMenu()
    // 购物车变化时同步底部栏
    this._unsub = cart.subscribe(() => this.syncCart())
    this.syncCart()
  },

  onUnload() {
    if (this._unsub) this._unsub()
  },

  onShow() {
    // 每次回到菜单页刷新库存/售罄状态（E-07：不做高频请求，onShow 一次性刷新）
    if (this.storeId && !this.data.loading) this.loadMenu(true)
  },

  /** 拉取菜单；silent=true 时静默刷新不显示 loading */
  loadMenu(silent = false) {
    if (!silent) this.setData({ loading: true, loadError: '' })
    api.getMenu(this.storeId)
      .then((menu) => {
        // 处理渲染字段：价格格式化、售罄判断、标签数组化
        const groups = (menu || []).map((g) => ({
          categoryId: g.category.id,
          categoryName: g.category.name,
          anchor: 'cat-' + g.category.id,
          products: (g.products || []).map((vo) => {
            const p = vo.product
            const skus = (vo.skus || []).map((s) => ({
              ...s,
              available: !(s.soldOut === 1 || (s.stock !== null && s.stock <= 0))
            }))
            const anyAvailable = skus.some((s) => s.available)
            const minPrice = skus.length
              ? Math.min(...skus.filter((s) => s.available).map((s) => Number(s.price)), ...skus.map((s) => Number(s.price)))
              : 0
            return {
              productId: p.id,
              name: p.name,
              image: p.imageUrl || '',
              description: p.description || '',
              tags: p.tags ? String(p.tags).split(',').filter(Boolean) : [],
              isAlcohol: p.containsAlcohol === 1,
              multiSpec: skus.length > 1,
              anyAvailable,
              skus,
              priceLabel: skus.length === 1 ? formatPrice(skus[0].price) : formatPrice(minPrice) + ' 起'
            }
          })
        }))
        const categories = groups.map((g) => ({ id: g.categoryId, name: g.categoryName, anchor: g.anchor }))
        this.setData({ groups, categories, loading: false })
      })
      .catch((err) => {
        this.setData({ loading: false, loadError: err.message || '菜单加载失败' })
      })
  },

  /** 侧栏分类点击 → 滚动到对应分组 */
  onCategoryTap(e) {
    const index = e.currentTarget.dataset.index
    this.setData({ activeIndex: index })
    wx.pageScrollTo({ selector: '#' + this.data.groups[index].anchor, duration: 200, offsetTop: -10 })
  },

  /** 商品点击：单规格直接加购，多规格弹窗 */
  onProductTap(e) {
    const { groupIndex, index } = e.currentTarget.dataset
    const product = this.data.groups[groupIndex].products[index]
    if (!product.anyAvailable) {
      wx.showToast({ title: '暂时售罄', icon: 'none' })
      return
    }
    if (product.multiSpec) {
      this.setData({ skuPopup: product })
    } else {
      const sku = product.skus[0]
      const ok = cart.addItem({
        skuId: sku.id, productId: product.productId, productName: product.name,
        skuName: sku.skuName, unitPrice: Number(sku.price), itemType: 0, stock: sku.stock || 0
      })
      if (ok) wx.showToast({ title: '已加入购物车', icon: 'none', duration: 800 })
    }
  },

  /** 规格弹窗选择 */
  onSkuSelect(e) {
    const skuIndex = e.currentTarget.dataset.index
    const product = this.data.skuPopup
    const sku = product.skus[skuIndex]
    if (!sku.available) {
      wx.showToast({ title: '该规格已售罄', icon: 'none' })
      return
    }
    cart.addItem({
      skuId: sku.id, productId: product.productId, productName: product.name,
      skuName: sku.skuName, unitPrice: Number(sku.price), itemType: 0, stock: sku.stock || 0
    })
    this.setData({ skuPopup: null })
    wx.showToast({ title: '已加入购物车', icon: 'none', duration: 800 })
  },

  onSkuClose() {
    this.setData({ skuPopup: null })
  },

  // ======== 购物车弹层 ========
  syncCart() {
    const items = cart.getItems()
    this.setData({
      cartItems: items,
      cartCount: cart.totalCount(),
      cartAmount: cart.totalAmount().toFixed(2)
    })
  },

  onCartBarTap() {
    if (this.data.cartCount === 0) return
    this.setData({ showCart: true })
  },

  onCartClose() {
    this.setData({ showCart: false })
  },

  onCartPlus(e) {
    cart.increaseQty(e.currentTarget.dataset.skuId)
  },

  onCartMinus(e) {
    cart.removeItem(e.currentTarget.dataset.skuId)
  },

  onCartClear() {
    wx.showModal({
      title: '清空购物车',
      content: '确定清空已选商品吗？',
      success: ({ confirm }) => {
        if (confirm) {
          cart.clear()
          this.setData({ showCart: false })
        }
      }
    })
  },

  onItemRemark(e) {
    cart.setItemRemark(e.currentTarget.dataset.skuId, e.detail.value)
  },

  onRemarkInput(e) {
    cart.setRemark(e.detail.value)
  },

  // ======== 下单 / 查单入口 ========
  goConfirm() {
    if (this.data.cartCount === 0) return
    this.setData({ showCart: false })
    wx.navigateTo({ url: '/pages/confirm/confirm' })
  },

  goOrders() {
    wx.navigateTo({ url: '/pages/orders/orders' })
  },

  onPullDownRefresh() {
    this.loadMenu(true)
    wx.stopPullDownRefresh()
  },

  onShareAppMessage() {
    const ctx = context.getContext()
    return {
      title: ctx ? `${ctx.store.name} · ${ctx.table.tableNumber} 同桌点餐` : 'MiriBar 扫码点餐',
      path: '/pages/index/index'
    }
  }
})
