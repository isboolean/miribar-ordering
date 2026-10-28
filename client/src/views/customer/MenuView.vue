<template>
  <div class="menu-page">
    <!-- Header -->
    <header class="header">
      <h1>🍸 MiriBar 点餐</h1>
      <div class="table-info">{{ tableText }}</div>
      <div class="warning-banner">⚠️ 未成年人禁止饮酒 | 理性饮酒，切勿酗酒</div>
    </header>

    <!-- Nav tabs -->
    <nav class="nav-tabs">
      <div class="nav-tab" :class="{ active: activeTab === 'menu' }" @click="activeTab = 'menu'">📋 菜单</div>
      <div class="nav-tab" :class="{ active: activeTab === 'orders' }" @click="goToOrders">📦 我的订单</div>
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
      <div v-if="loading" class="loading-wrap"><div class="spinner"></div></div>
      <div v-else-if="menuData.length === 0" class="empty-state">
        <div class="icon">📋</div>暂无菜单数据
      </div>
      <template v-else>
        <div v-for="(group, idx) in menuData" :key="group.category.id" :ref="el => setCategoryRef(el, idx)" class="category-section">
          <div class="category-title">
            <span class="icon">{{ group.category.icon || '📌' }}</span>
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
    <div v-if="showConfirm" class="modal-overlay" @click.self="showConfirm = false">
      <div class="modal-box">
        <h3>确认下单</h3>
        <p>共{{ cart.totalCount }}件商品，合计 ¥{{ cart.totalAmount.toFixed(2) }}，确认下单？</p>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="showConfirm = false">取消</button>
          <button class="btn btn-primary" @click="doSubmit">确认</button>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div v-if="toastMsg" class="toast-container"><div class="toast-msg">{{ toastMsg }}</div></div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { getMenu, getTableByQrToken, getTableById, createOrder } from '@/api'
import ProductCard from '@/components/ProductCard.vue'
import CartBar from '@/components/CartBar.vue'

const router = useRouter()
const route = useRoute()
const cart = useCartStore()

const storeId = ref(1)
const tableId = ref(null)
const tableText = ref('桌号: 加载中...')
const menuData = ref([])
const loading = ref(true)
const activeTab = ref('menu')
const activeCategory = ref(0)
const cartOpen = ref(false)
const showConfirm = ref(false)
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
  if (cart.totalCount === 0) return
  showConfirm.value = true
}

async function doSubmit() {
  showConfirm.value = false
  try {
    await createOrder({
      storeId: storeId.value,
      tableId: tableId.value,
      items: cart.toOrderItems(),
      remark: ''
    })
    cart.clear()
    cartOpen.value = false
    showToast('下单成功！')
    goToOrders()
  } catch (e) {
    showToast(e.message || '下单失败')
  }
}

function showToast(msg) {
  toastMsg.value = msg
  setTimeout(() => { toastMsg.value = '' }, 2000)
}
</script>

<style scoped>
.menu-page { padding-bottom: 80px; }

.header {
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  padding: 16px 20px; position: sticky; top: 0; z-index: 100;
}
.header h1 { font-size: 18px; font-weight: 600; }
.table-info { font-size: 13px; opacity: .85; margin-top: 4px; }
.warning-banner {
  background: rgba(231,76,60,.2); border: 1px solid var(--danger);
  border-radius: 6px; padding: 8px 12px; margin-top: 10px;
  font-size: 12px; color: #FF6B6B;
}

.nav-tabs { display: flex; background: var(--card-bg); border-bottom: 1px solid var(--border); }
.nav-tab {
flex: 1; padding: 12px; text-align: center; font-size: 14px; color: var(--text-muted); cursor: pointer; border-bottom: 2px solid transparent; transition: all .2s; }
.nav-tab.active { color: var(--primary-light); border-bottom-color: var(--primary); }

.category-tabs {
  display: flex; overflow-x: auto; padding: 12px 16px; gap: 8px;
  background: var(--card-bg); position: sticky; top: 80px; z-index: 90;
}
.category-tabs::-webkit-scrollbar { display: none; }
.tab-item {
  flex-shrink: 0; padding: 8px 16px; border-radius: 20px;
  background: rgba(255,255,255,.08); color: var(--text-muted);
  font-size: 14px; cursor: pointer; transition: all .2s; border: 1px solid transparent;
}
.tab-item.active { background: var(--primary); color: #fff; border-color: var(--primary-light); }

.product-list { padding: 12px 16px; overflow-y: auto; max-height: calc(100vh - 200px); }
.category-section { margin-bottom: 24px; }
.category-title { font-size: 16px; font-weight: 600; margin-bottom: 12px; padding-left: 4px; display: flex; align-items: center; gap: 8px; }
.category-title .icon { font-size: 20px; }
</style>
