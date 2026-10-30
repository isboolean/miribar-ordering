<template>
  <div class="menu-page">
    <!-- Header -->
    <header class="header">
      <h1>MiriBar 点餐</h1>
      <div class="table-info">{{ tableText }}</div>
      <div class="user-row">
        <button class="theme-toggle" :aria-label="isDark ? '切换到浅色模式' : '切换到深色模式'" @click="toggleTheme">
          {{ isDark ? '☀️' : '🌙' }}
        </button>
        <button v-if="!customer.isLoggedIn" class="login-chip" @click="showLogin = true">登录</button>
        <span v-else class="user-chip" @click="handleCustomerLogout">{{ customer.displayName }} ▾</span>
      </div>
      <div class="warning-banner">未成年人禁止饮酒 · 理性饮酒，切勿酗酒</div>
    </header>

    <!-- Nav tabs -->
    <nav class="nav-tabs">
      <div class="nav-tab" :class="{ active: activeTab === 'menu' }" @click="activeTab = 'menu'">菜单</div>
      <div class="nav-tab" :class="{ active: activeTab === 'orders' }" @click="goToOrders">我的订单</div>
    </nav>

    <!-- Category tabs -->
    <div class="category-tabs" ref="tabsRef">
      <div
        v-for="(group, idx) in menuData" :key="group.category.id"
        class="tab-item" :class="{ active: activeCategory === idx }"
        @click="scrollToCategory(idx)"
      >
        {{ group.category.icon || '' }} {{ group.category.name }}
      </div>
    </div>

    <!-- Product list -->
    <div class="product-list" ref="listRef" @scroll="onScroll">
      <div v-if="loading" class="skeleton-wrap">
        <div v-for="i in 4" :key="i" class="skeleton-card">
          <div class="skeleton skeleton-img"></div>
          <div class="skeleton-lines">
            <div class="skeleton skeleton-line w-60"></div>
            <div class="skeleton skeleton-line w-90"></div>
            <div class="skeleton skeleton-line w-40"></div>
          </div>
        </div>
      </div>
      <div v-else-if="menuData.length === 0" class="empty-state">
        <div class="icon">🍹</div>暂无菜单数据
      </div>
      <template v-else>
        <div v-for="(group, idx) in menuData" :key="group.category.id" :ref="el => setCategoryRef(el, idx)" class="category-section">
          <div class="category-title">
            <span class="icon" v-if="group.category.icon">{{ group.category.icon }}</span>
            {{ group.category.name }}
          </div>
          <ProductCard
            v-for="p in group.products" :key="p.product.id"
            :product="p.product" :skus="p.skus"
          />
        </div>
      </template>
    </div>

    <!-- Cart -->
    <CartBar :visible="cartOpen" @toggle="cartOpen = !cartOpen" @close="cartOpen = false" @submit="handleSubmit" />

    <!-- Confirm modal -->
    <div v-if="showConfirm" class="modal-overlay" @click.self="!submitting && (showConfirm = false)">
      <div class="modal-box">
        <h3>确认下单</h3>
        <p>共{{ cart.totalCount }}件商品，合计 <strong class="confirm-amount">¥{{ cart.totalAmount.toFixed(2) }}</strong>，确认下单？</p>
        <p v-if="cart.orderRemark.trim()" class="confirm-remark">备注：{{ cart.orderRemark.trim() }}</p>
        <div class="modal-actions">
          <button class="btn btn-outline" :disabled="submitting" @click="showConfirm = false">取消</button>
          <button class="btn btn-primary" :disabled="submitting" @click="doSubmit">
            <span v-if="submitting" class="btn-spinner" aria-hidden="true"></span>
            {{ submitting ? '提交中…' : '确认下单' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div v-if="toastMsg" class="toast-container"><div class="toast-msg">{{ toastMsg }}</div></div>

    <!-- 顾客登录弹窗 -->
    <CustomerLoginModal v-model:visible="showLogin" @success="onCustomerLogin" />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useCustomerStore } from '@/stores/customer'
import { useTheme } from '@/composables/useTheme'
import { getMenu, getTableByQrToken, getTableById, createOrder } from '@/api'
import ProductCard from '@/components/ProductCard.vue'
import CartBar from '@/components/CartBar.vue'
import CustomerLoginModal from '@/components/CustomerLoginModal.vue'

const router = useRouter()
const route = useRoute()
const cart = useCartStore()
const customer = useCustomerStore()
const { isDark, toggleTheme } = useTheme()
const showLogin = ref(false)

function onCustomerLogin() {
  showToast(`欢迎，${customer.displayName}`)
}

function handleCustomerLogout() {
  customer.logout()
  showToast('已退出登录')
}

const storeId = ref(1)
const tableId = ref(null)
const tableText = ref('桌号: 加载中...')
const menuData = ref([])
const loading = ref(true)
const activeTab = ref('menu')
const activeCategory = ref(0)
const cartOpen = ref(false)
const showConfirm = ref(false)
const submitting = ref(false)
const toastMsg = ref('')

const tabsRef = ref(null)
const listRef = ref(null)
const categoryRefs = {}

function setCategoryRef(el, idx) {
  if (el) categoryRefs[idx] = el
}

onMounted(async () => {
  const q = route.query
  if (q.token) {
    try {
      const table = await getTableByQrToken(q.token)
      tableId.value = table.id
      storeId.value = table.storeId
    } catch {
      showToast('桌码无效，请联系店员')
      return
    }
  } else if (q.tableId) {
    tableId.value = parseInt(q.tableId)
    storeId.value = parseInt(q.storeId) || 1
  } else {
    tableId.value = 1
    storeId.value = 1
  }

  await loadTableInfo()
  await loadMenu()
})

async function loadTableInfo() {
  try {
    const t = await getTableById(tableId.value)
    tableText.value = `桌号: ${t.tableNumber} | ${t.seats}人桌`
  } catch { /* ignore */ }
}

async function loadMenu() {
  loading.value = true
  try {
    menuData.value = await getMenu(storeId.value)
  } catch {
    showToast('加载菜单失败')
  } finally {
    loading.value = false
  }
}

function scrollToCategory(idx) {
  activeCategory.value = idx
  const el = categoryRefs[idx]
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function onScroll() {
  // Update active category based on scroll position
  const scrollTop = listRef.value?.scrollTop || 0
  for (let i = Object.keys(categoryRefs).length - 1; i >= 0; i--) {
    const el = categoryRefs[i]
    if (el && el.offsetTop <= scrollTop + 120) {
      activeCategory.value = i
      break
    }
  }
}

function goToOrders() {
  router.push({ name: 'OrderStatus', query: { tableId: tableId.value, storeId: storeId.value } })
}

function handleSubmit() {
  if (cart.totalCount === 0 || submitting.value) return
  showConfirm.value = true
}

async function doSubmit() {
  if (submitting.value) return
  submitting.value = true
  try {
    await createOrder({
      storeId: storeId.value,
      tableId: tableId.value,
      items: cart.toOrderItems(),
      remark: cart.orderRemark.trim()
    })
    cart.clear()
    cartOpen.value = false
    showConfirm.value = false
    showToast('下单成功！')
    goToOrders()
  } catch (e) {
    showToast(e.message || '下单失败，请重试')
  } finally {
    submitting.value = false
  }
}

function showToast(msg) {
  toastMsg.value = msg
  setTimeout(() => { toastMsg.value = '' }, 2000)
}
</script>

<style scoped>
.menu-page { padding-bottom: 88px; background: var(--bg); min-height: 100vh; }

.header {
  background: var(--card-bg);
  border-bottom: 1px solid var(--border);
  padding: 16px 20px; position: sticky; top: 0; z-index: 100;
}
.header h1 { font-size: 18px; font-weight: 600; }
.table-info { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
.user-row { position: absolute; top: 16px; right: 16px; display: flex; align-items: center; gap: 8px; }
.theme-toggle {
  width: 36px; height: 36px; border-radius: 50%;
  border: none; background: var(--surface-2);
  font-size: 15px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background .2s, transform .2s;
}
.theme-toggle:hover { background: var(--border); transform: scale(1.08); }
.login-chip, .user-chip {
  background: var(--surface-2); border: none;
  color: var(--text); border-radius: 999px; padding: 6px 14px; font-size: 12px; cursor: pointer;
}
.login-chip:hover, .user-chip:hover { background: var(--border); }
.warning-banner {
  background: rgba(245,158,11,.08);
  border-radius: var(--radius-sm); padding: 7px 12px; margin-top: 12px;
  font-size: 12px; color: var(--warning);
}

.nav-tabs { display: flex; background: var(--card-bg); border-bottom: 1px solid var(--border); }
.nav-tab {
  flex: 1; padding: 12px; text-align: center; font-size: 14px; color: var(--text-muted);
  cursor: pointer; border-bottom: 2px solid transparent; transition: color .2s, border-color .2s;
}
.nav-tab.active { color: var(--primary); border-bottom-color: var(--primary); font-weight: 500; }

.category-tabs {
  display: flex; overflow-x: auto; padding: 12px 16px; gap: 8px;
  background: var(--card-bg); position: sticky; top: 108px; z-index: 90;
  border-bottom: 1px solid var(--border);
}
.category-tabs::-webkit-scrollbar { display: none; }
.tab-item {
  flex-shrink: 0; padding: 7px 16px; border-radius: 999px;
  background: var(--surface-2); color: var(--text-muted);
  font-size: 13px; cursor: pointer; transition: background .2s, color .2s; border: none;
}
.tab-item.active { background: var(--primary); color: #fff; }

.product-list { padding: 16px; overflow-y: auto; max-height: calc(100vh - 216px); }
.category-section { margin-bottom: 28px; }
.category-title {
  font-size: 15px; font-weight: 600; margin-bottom: 12px;
  display: flex; align-items: center; gap: 6px;
}
.category-title .icon { font-size: 16px; }

/* 骨架屏 */
.skeleton-wrap { padding: 0; }
.skeleton-card {
  display: flex; align-items: flex-start; gap: 14px;
  background: var(--card-bg); border: 1px solid var(--border);
  border-radius: var(--radius); padding: 14px; margin-bottom: 10px;
}
.skeleton {
  background: linear-gradient(90deg, var(--surface-2) 25%, var(--border) 50%, var(--surface-2) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease infinite;
  border-radius: var(--radius-sm);
}
.skeleton-img { width: 90px; height: 90px; flex-shrink: 0; }
.skeleton-lines { flex: 1; display: flex; flex-direction: column; gap: 10px; padding-top: 4px; }
.skeleton-line { height: 13px; }
.w-60 { width: 60%; }
.w-90 { width: 90%; }
.w-40 { width: 40%; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.confirm-amount { color: var(--accent); font-size: 17px; }
.confirm-remark {
  font-size: 12px; color: var(--text-muted);
  background: var(--surface-2); border-radius: var(--radius-sm);
  padding: 8px 10px; margin-top: 8px;
}
.btn[disabled] { opacity: .6; cursor: not-allowed; pointer-events: none; }
.btn-spinner {
  width: 14px; height: 14px; border: 2px solid rgba(255,255,255,.4);
  border-top-color: #fff; border-radius: 50%;
  animation: spin .8s linear infinite; display: inline-block;
}
</style>
