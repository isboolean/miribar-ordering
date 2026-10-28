<template>
  <div class="kitchen-page">
    <!-- Header -->
    <header class="header">
      <div>
        <h1>🍳 订单看板</h1>
        <div class="store-info">MiriBar 旗舰店</div>
      </div>
      <div class="header-right">
        <div class="mode-toggle">
          <button class="mode-btn" :class="{ active: mode === 'bar' }" @click="mode = 'bar'">🍸 吧台</button>
          <button class="mode-btn" :class="{ active: mode === 'kitchen' }" @click="mode = 'kitchen'">🍳 后厨</button>
        </div>
        <div class="ws-status">
          <div class="ws-dot" :class="{ connected: wsConnected }"></div>
          <span>{{ wsConnected ? '已连接' : '未连接' }}</span>
        </div>
      </div>
    </header>

    <!-- Stats -->
    <div class="stats-bar">
      <div class="stat-item stat-pending"><div class="stat-num">{{ pending.length }}</div><div class="stat-label">待接单</div></div>
      <div class="stat-item stat-cooking"><div class="stat-num">{{ cooking.length }}</div><div class="stat-label">制作中</div></div>
      <div class="stat-item stat-done"><div class="stat-num">{{ done.length }}</div><div class="stat-label">已完成</div></div>
    </div>

    <!-- Board -->
    <div class="board">
      <!-- Pending -->
      <div class="column col-pending">
        <div class="column-header"><span>⏳ 待接单</span><span class="column-count">{{ pending.length }}</span></div>
        <div class="column-body">
          <div v-if="pending.length === 0" class="empty-state"><div class="icon">📋</div>暂无待接单</div>
          <div v-for="o in pending" :key="o.order.id" class="order-card" :class="{ urgent: isUrgent(o) }">
            <OrderCard :order-vo="o" :mode="mode" @accept="handleAccept" @reject="openReject" @complete="handleComplete" />
          </div>
        </div>
      </div>
      <!-- Cooking -->
      <div class="column col-cooking">
        <div class="column-header"><span>🔥 制作中</span><span class="column-count">{{ cooking.length }}</span></div>
        <div class="column-body">
          <div v-if="cooking.length === 0" class="empty-state"><div class="icon">👨‍🍳</div>暂无制作中</div>
          <div v-for="o in cooking" :key="o.order.id" class="order-card">
            <OrderCard :order-vo="o" :mode="mode" @accept="handleAccept" @reject="openReject" @complete="handleComplete" />
          </div>
        </div>
      </div>
      <!-- Done -->
      <div class="column col-done">
        <div class="column-header"><span>✅ 已完成</span><span class="column-count">{{ done.length }}</span></div>
        <div class="column-body">
          <div v-if="done.length === 0" class="empty-state"><div class="icon">🎉</div>暂无已完成</div>
          <div v-for="o in done" :key="o.order.id" class="order-card">
            <OrderCard :order-vo="o" :mode="mode" @accept="handleAccept" @reject="openReject" @complete="handleComplete" />
          </div>
        </div>
      </div>
    </div>

    <!-- Notification -->
    <Transition name="slide-right">
      <div v-if="notification" class="notification">{{ notification }}</div>
    </Transition>

    <!-- Reject Modal -->
    <div v-if="rejectModal.show" class="modal-overlay" @click.self="rejectModal.show = false">
      <div class="modal-box">
        <h3>拒单原因</h3>
        <div class="reject-reasons">
          <button v-for="r in rejectReasons" :key="r" class="reason-tag" :class="{ selected: rejectModal.reason === r }" @click="rejectModal.reason = r">{{ r }}</button>
        </div>
        <textarea class="form-input" v-model="rejectModal.reason" placeholder="请输入拒单原因..." rows="3" style="margin-bottom:12px;resize:none"></textarea>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="rejectModal.show = false">取消</button>
          <button class="btn btn-danger" @click="confirmReject">确认拒单</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, defineComponent, h } from 'vue'
import { getStoreActiveOrders, acceptOrder as apiAccept, completeOrder as apiComplete, rejectOrder as apiReject } from '@/api'
import { useWebSocket } from '@/composables/useWebSocket'

const STORE_ID = 1
const BAR_TYPES = [0, 2]
const KITCHEN_TYPES = [1]

const mode = ref('bar')
const allOrders = ref([])
const notification = ref('')
const rejectModal = ref({ show: false, orderId: null, reason: '' })
const rejectReasons = ['商品已售罄', '暂时无法制作', '库存不足', '其他原因']
let refreshTimer = null

// WebSocket
const { connected: wsConnected, connect, disconnect, on } = useWebSocket(STORE_ID)

onMounted(async () => {
  await loadOrders()
  connect('bar')
  on('NEW_ORDER', () => {
    showNotification('🔔 新订单')
    playSound()
    loadOrders()
  })
  on('ORDER_STATUS_CHANGE', () => loadOrders())
  refreshTimer = setInterval(loadOrders, 15000)
})

onUnmounted(() => {
  disconnect()
  if (refreshTimer) clearInterval(refreshTimer)
})

async function loadOrders() {
  try {
    allOrders.value = await getStoreActiveOrders(STORE_ID)
  } catch { /* ignore */ }
}

function filterByMode(orders) {
  const types = mode.value === 'bar' ? BAR_TYPES : KITCHEN_TYPES
  return orders.map(o => {
    const items = o.items.filter(i => types.includes(i.itemType))
    if (items.length === 0) return null
    const amount = items.reduce((s, i) => s + parseFloat(i.subtotal), 0)
    return { ...o, items, _amount: amount }
  }).filter(Boolean)
}

const filteredOrders = computed(() => filterByMode(allOrders.value))
const pending = computed(() => filteredOrders.value.filter(o => o.order.status === 0))
const cooking = computed(() => filteredOrders.value.filter(o => o.order.status === 1))
const done = computed(() => filteredOrders.value.filter(o => o.order.status === 2))

function isUrgent(o) {
  if (o.order.status !== 0) return false
  const mins = (Date.now() - new Date(o.order.createdAt).getTime()) / 60000
  return mins >= 2
}

async function handleAccept(orderId) {
  if (!confirm('确认接单？')) return
  try {
    await apiAccept(orderId)
    showNotification('订单已接单')
    await loadOrders()
  } catch (e) { alert(e.message) }
}

async function handleComplete(orderId) {
  if (!confirm('确认制作完成？')) return
  try {
    await apiComplete(orderId)
    showNotification('订单制作完成')
    await loadOrders()
  } catch (e) { alert(e.message) }
}

function openReject(orderId) {
  rejectModal.value = { show: true, orderId, reason: '' }
}

async function confirmReject() {
  const { orderId, reason } = rejectModal.value
  if (!reason.trim()) { alert('请填写拒单原因'); return }
  try {
    await apiReject(orderId, reason)
    rejectModal.value.show = false
    showNotification('订单已拒单')
    await loadOrders()
  } catch (e) { alert(e.message) }
}

function showNotification(text) {
  notification.value = text
  setTimeout(() => { notification.value = '' }, 3000)
}

function playSound() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.connect(gain); gain.connect(ctx.destination)
    osc.frequency.value = 800; gain.gain.value = 0.3
    osc.start(); osc.stop(ctx.currentTime + 0.15)
  } catch { /* ignore */ }
}

// OrderCard inline component
const OrderCard = defineComponent({
  name: 'OrderCard',
  props: { orderVo: Object, mode: String },
  emits: ['accept', 'reject', 'complete'],
  setup(props, { emit }) {
    const elapsed = computed(() => {
      return Math.floor((Date.now() - new Date(props.orderVo.order.createdAt).getTime()) / 60000)
    })
    return () => {
      const o = props.orderVo
      const order = o.order
      return h('div', {}, [
        h('div', { class: 'card-header' }, [
          h('div', {}, [
            h('span', { class: 'card-table' }, `🪑 ${o.tableNumber || '?'}`),
            h('span', { class: ['card-source', order.source === 1 ? 'add-order' : ''] }, order.source === 1 ? '加单' : '扫码')
          ]),
          h('div', { class: 'card-time' }, `${elapsed.value}分钟前`)
        ]),
        h('div', { class: 'card-items' },
          o.items.map(item =>
            h('div', { class: 'card-item' }, [
              h('span', { class: 'item-name' }, `${item.productName}(${item.skuName})`),
              h('span', { class: 'item-qty' }, `x${item.quantity}`),
              item.remark ? h('span', { class: 'item-remark' }, `📝${item.remark}`) : null
            ])
          )
        ),
        order.remark ? h('div', { class: 'card-remark' }, `💬 ${order.remark}`) : null,
        h('div', { class: 'card-amount' }, `¥${o._amount.toFixed(2)}`),
        h('div', { class: 'card-actions' },
          order.status === 0
            ? [h('button', { class: 'action-btn btn-accept', onClick: () => emit('accept', order.id) }, '✅ 接单'),
               h('button', { class: 'action-btn btn-reject', onClick: () => emit('reject', order.id) }, '❌ 拒单')]
            : order.status === 1
            ? [h('button', { class: 'action-btn btn-complete', style: 'flex:1', onClick: () => emit('complete', order.id) }, '✅ 完成制作')]
            : [h('div', { class: 'done-text' }, '✅ 制作完成')]
        )
      ])
    }
  }
})
</script>

<style scoped>
.kitchen-page { min-height: 100vh; display: flex; flex-direction: column; }

.header {
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  padding: 12px 24px; display: flex; align-items: center; justify-content: space-between;
}
.header h1 { font-size: 20px; font-weight: 600; }
.store-info { font-size: 13px; opacity: .85; }
.header-right { display: flex; align-items: center; gap: 16px; }
.mode-toggle { display: flex; border-radius: 8px; overflow: hidden; border: 1px solid var(--border); }
.mode-btn { padding: 6px 16px; font-size: 13px; background: transparent; border: none; color: var(--text-muted); cursor: pointer; }
.mode-btn.active { background: var(--primary); color: #fff; }
.ws-status { display: flex; align-items: center; gap: 6px; font-size: 12px; }
.ws-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--danger); }
.ws-dot.connected { background: var(--success); }

.stats-bar { display: flex; gap: 16px; padding: 12px 24px; background: var(--card-bg); border-bottom: 1px solid var(--border); }
.stat-item { display: flex; align-items: center; gap: 8px; }
.stat-num { font-size: 24px; font-weight: 700; }
.stat-label { font-size: 12px; color: var(--text-muted); }
.stat-pending .stat-num { color: var(--accent); }
.stat-cooking .stat-num { color: var(--info); }
.stat-done .stat-num { color: var(--success); }

.board { display: flex; gap: 16px; padding: 16px 24px; flex: 1; }
.column { flex: 1; background: rgba(255,255,255,.02); border-radius: 12px; border: 1px solid var(--border); display: flex; flex-direction: column; min-width: 300px; }
.column-header { padding: 14px 18px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; font-weight: 600; font-size: 15px; }
.column-count { background: rgba(255,255,255,.1); padding: 2px 10px; border-radius: 12px; font-size: 13px; }
.col-pending .column-header { border-top: 3px solid var(--accent); }
.col-cooking .column-header { border-top: 3px solid var(--info); }
.col-done .column-header { border-top: 3px solid var(--success); }
.column-body { flex: 1; overflow-y: auto; padding: 12px; display: flex; flex-direction: column; gap: 10px; }

.order-card { background: var(--card-bg); border-radius: 10px; padding: 14px; border: 1px solid var(--border); transition: all .2s; }
.order-card:hover { border-color: var(--primary); }
.order-card.urgent { border-color: var(--danger); animation: pulse 2s infinite; }
@keyframes pulse { 0%,100% { box-shadow: 0 0 0 0 rgba(231,76,60,.3); } 50% { box-shadow: 0 0 12px 2px rgba(231,76,60,.3); } }

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.card-table { font-size: 18px; font-weight: 700; }
.card-time { font-size: 11px; color: var(--text-muted); }
.card-source { font-size: 10px; padding: 2px 8px; border-radius: 4px; background: rgba(255,255,255,.08); color: var(--text-muted); margin-left: 8px; }
.card-source.add-order { background: rgba(46,204,113,.15); color: var(--success); }

.card-items { margin-bottom: 10px; }
.card-item { display: flex; justify-content: space-between; padding: 3px 0; font-size: 13px; }
.card-item .item-name { flex: 1; }
.card-item .item-qty { width: 40px; text-align: center; color: var(--text-muted); }
.card-item .item-remark { font-size: 11px; color: var(--accent); max-width: 100px; text-align: right; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.card-remark { background: rgba(243,156,18,.1); border: 1px solid rgba(243,156,18,.3); border-radius: 6px; padding: 6px 10px; font-size: 12px; color: var(--accent); margin-bottom: 10px; }
.card-amount { text-align: right; font-size: 13px; color: var(--accent); margin-bottom: 10px; font-weight: 600; }

.card-actions { display: flex; gap: 8px; }
.action-btn { flex: 1; padding: 8px; border-radius: 8px; border: none; font-size: 13px; font-weight: 600; cursor: pointer; }
.action-btn:active { transform: scale(.95); }
.btn-accept { background: var(--info); color: #fff; }
.btn-complete { background: var(--success); color: #fff; }
.btn-reject { background: var(--danger); color: #fff; }
.done-text { text-align: center; width: 100%; color: var(--success); font-size: 13px; padding: 4px; }

.notification { position: fixed; top: 20px; right: 20px; background: var(--primary); color: #fff; padding: 14px 24px; border-radius: 10px; font-size: 14px; font-weight: 600; z-index: 999; box-shadow: 0 4px 20px rgba(108,52,131,.4); }
.slide-right-enter-active, .slide-right-leave-active { transition: all .3s ease; }
.slide-right-enter-from, .slide-right-leave-to { opacity: 0; transform: translateX(100%); }

.reject-reasons { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.reason-tag { padding: 6px 12px; border-radius: 16px; border: 1px solid var(--border); background: transparent; color: var(--text); font-size: 12px; cursor: pointer; }
.reason-tag.selected { border-color: var(--danger); background: rgba(231,76,60,.15); color: var(--danger); }
</style>
