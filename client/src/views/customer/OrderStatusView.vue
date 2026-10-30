<template>
  <div class="orders-page">
    <!-- Header -->
    <header class="header">
      <h1>MiriBar 点餐</h1>
      <div class="table-info">{{ tableText }}</div>
    </header>

    <!-- Nav tabs -->
    <nav class="nav-tabs">
      <div class="nav-tab" @click="goToMenu">菜单</div>
      <div class="nav-tab active">我的订单</div>
    </nav>

    <!-- Order list -->
    <div class="order-list">
      <div v-if="loading" class="loading-wrap"><div class="spinner"></div></div>
      <div v-else-if="orders.length === 0" class="empty-state">
        <div class="icon">·</div>暂无订单
      </div>
      <div v-else>
        <div v-for="o in orders" :key="o.order.id" class="order-card">
          <div class="order-header">
            <span class="order-no">#{{ o.order.orderNo }}</span>
            <span class="order-status" :class="'status-' + o.order.status">
              {{ statusMap[o.order.status] || '未知' }}
            </span>
          </div>
          <div class="order-items">
            <div v-for="item in o.items" :key="item.id" class="order-item-row">
              <span class="item-name">{{ item.productName }}({{ item.skuName }})</span>
              <span class="item-qty">x{{ item.quantity }}</span>
              <span class="item-price">¥{{ item.subtotal }}</span>
            </div>
          </div>
          <div class="order-total-row">
            <span>合计</span>
            <span class="total-amount">¥{{ o.order.totalAmount }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getTableOrders, getTableById } from '@/api'

const router = useRouter()
const route = useRoute()

const tableId = ref(null)
const tableText = ref('')
const orders = ref([])
const loading = ref(true)
let refreshTimer = null

const statusMap = { 0: '待接单', 1: '制作中', 2: '已完成', 3: '已拒单', 4: '已取消', 5: '已结账' }

onMounted(async () => {
  tableId.value = parseInt(route.query.tableId) || 1
  await loadTableInfo()
  await loadOrders()
  refreshTimer = setInterval(loadOrders, 10000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})

async function loadTableInfo() {
  try {
    const t = await getTableById(tableId.value)
    tableText.value = `桌号: ${t.tableNumber}`
  } catch { /* ignore */ }
}

async function loadOrders() {
  try {
    orders.value = await getTableOrders(tableId.value)
  } catch { /* ignore */ }
  finally { loading.value = false }
}

function goToMenu() {
  router.push({ name: 'Menu', query: { tableId: tableId.value } })
}
</script>

<style scoped>
.orders-page { min-height: 100vh; padding-bottom: 20px; background: var(--bg); }

.header {
  background: var(--card-bg);
  border-bottom: 1px solid var(--border);
  padding: 16px 20px; position: sticky; top: 0; z-index: 100;
}
.header h1 { font-size: 18px; font-weight: 600; }
.table-info { font-size: 13px; color: var(--text-muted); margin-top: 2px; }

.nav-tabs { display: flex; background: var(--card-bg); border-bottom: 1px solid var(--border); }
.nav-tab {
  flex: 1; padding: 12px; text-align: center; font-size: 14px;
  color: var(--text-muted); cursor: pointer; border-bottom: 2px solid transparent;
}
.nav-tab.active { color: var(--primary); border-bottom-color: var(--primary); font-weight: 500; }

.order-list { padding: 16px; }
.order-card {
  background: var(--card-bg); border-radius: var(--radius); padding: 16px;
  margin-bottom: 12px; border: 1px solid var(--border); box-shadow: var(--shadow-sm);
}
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.order-no { font-size: 13px; color: var(--text-muted); }
.order-status { padding: 4px 10px; border-radius: 999px; font-size: 12px; font-weight: 500; }
.status-0 { background: rgba(245,158,11,.12); color: var(--warning); }
.status-1 { background: rgba(59,130,246,.1); color: var(--info); }
.status-2 { background: rgba(22,163,74,.1); color: var(--success); }
.status-3 { background: rgba(239,68,68,.1); color: var(--danger); }
.status-4 { background: var(--surface-2); color: var(--text-muted); }
.status-5 { background: rgba(22,163,74,.1); color: var(--success); }

.order-items { margin-bottom: 8px; }
.order-item-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 13px; }
.item-name { flex: 1; }
.item-qty { width: 50px; text-align: center; color: var(--text-muted); }
.item-price { width: 70px; text-align: right; }

.order-total-row { display: flex; justify-content: space-between; padding-top: 10px; border-top: 1px solid var(--border); font-weight: 600; }
.total-amount { color: var(--accent); }
</style>
