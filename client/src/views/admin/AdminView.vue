<template>
  <div class="admin-page">
    <header class="header">
      <h1>⚙️ MiriBar 管理后台</h1>
      <div class="header-user">
        <span style="font-size:13px;opacity:.8">{{ auth.displayName }} | {{ roleText }}</span>
        <button class="btn btn-outline btn-sm" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <div class="layout">
      <nav class="sidebar">
        <div v-for="tab in tabs" :key="tab.key" class="nav-item" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
          {{ tab.icon }} {{ tab.label }}
        </div>
      </nav>

      <main class="content">
        <!-- 桌台管理 -->
        <div v-if="activeTab === 'tables'" class="section">
          <div class="section-header">
            <h2>🪑 桌台管理</h2>
            <button class="btn btn-primary" @click="openTableModal()">+ 新增桌台</button>
          </div>
          <div class="table-grid">
            <div v-for="t in tables" :key="t.id" class="table-card" :class="'status-' + t.status">
              <div class="table-num">{{ t.tableNumber }}</div>
              <div class="table-seats">{{ t.seats }}人桌</div>
              <div class="table-status">
                <span class="badge" :class="tableStatusBadge(t.status)">{{ tableStatusText(t.status) }}</span>
              </div>
              <div class="table-actions">
                <button v-if="t.status === 1" class="btn btn-success btn-sm" @click="handleClearTable(t.id)">清台</button>
                <button class="btn btn-outline btn-sm" @click="openTableModal(t)">编辑</button>
                <button class="btn btn-danger btn-sm" @click="handleDeleteTable(t.id)">删除</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 分类管理 -->
        <div v-if="activeTab === 'categories'" class="section">
          <div class="section-header">
            <h2>📂 分类管理</h2>
            <button class="btn btn-primary" @click="openCategoryModal()">+ 新增分类</button>
          </div>
          <table class="data-table">
            <thead><tr><th>图标</th><th>名称</th><th>排序</th><th>状态</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="c in categories" :key="c.id">
                <td style="font-size:20px">{{ c.icon }}</td>
                <td>{{ c.name }}</td>
                <td>{{ c.sortOrder }}</td>
                <td><span class="badge" :class="c.status === 1 ? 'badge-success' : 'badge-danger'">{{ c.status === 1 ? '启用' : '禁用' }}</span></td>
                <td>
                  <button class="btn btn-outline btn-sm" @click="openCategoryModal(c)">编辑</button>
                  <button class="btn btn-danger btn-sm" @click="handleDeleteCategory(c.id)">删除</button>
                </td>
              </tr>
              <tr v-if="categories.length === 0"><td colspan="5" class="empty-row">暂无分类</td></tr>
            </tbody>
          </table>
        </div>

        <!-- 商品管理 -->
        <div v-if="activeTab === 'products'" class="section">
          <div class="section-header">
            <h2>📦 商品管理</h2>
            <button class="btn btn-primary" @click="openProductModal()">+ 新增商品</button>
          </div>
          <table class="data-table">
            <thead><tr><th>商品名称</th><th>分类</th><th>类型</th><th>标签</th><th>规格/价格</th><th>状态</th><th>操作</th></tr></thead>
            <tbody>
              <template v-for="group in products" :key="group.category.id">
                <tr v-for="p in group.products" :key="p.product.id">
                  <td><strong>{{ p.product.name }}</strong></td>
                  <td>{{ group.category.name }}</td>
                  <td>{{ typeMap[p.product.productType] || '酒水' }}</td>
                  <td>{{ p.product.tags || '-' }}</td>
                  <td><div class="sku-text" v-for="s in p.skus" :key="s.id">{{ s.skuName }}: ¥{{ s.price }}</div></td>
                  <td><span class="badge" :class="p.product.status === 1 ? 'badge-success' : 'badge-danger'">{{ p.product.status === 1 ? '上架' : '下架' }}</span></td>
                  <td>
                    <button class="btn btn-outline btn-sm" @click="handleToggleStatus(p.product.id, p.product.status === 1 ? 0 : 1)">{{ p.product.status === 1 ? '下架' : '上架' }}</button>
                    <button class="btn btn-danger btn-sm" @click="handleDeleteProduct(p.product.id)">删除</button>
                  </td>
                </tr>
              </template>
              <tr v-if="products.length === 0"><td colspan="7" class="empty-row">暂无商品</td></tr>
            </tbody>
          </table>
        </div>

        <!-- 结账管理 -->
        <div v-if="activeTab === 'checkout'" class="section">
          <div class="section-header">
            <h2>💰 结账管理</h2>
            <button class="btn btn-outline" @click="loadBills">🔄 刷新</button>
          </div>
          <div v-if="bills.length === 0" class="empty-state"><div class="icon">💰</div>暂无开台的桌台</div>
          <div v-for="bill in bills" :key="bill.table.id" class="bill-card">
            <div class="bill-header">
              <div>
                <span class="bill-table">🪑 {{ bill.table.tableNumber }}</span>
                <span class="badge badge-warning" style="margin-left:8px">{{ bill.orderCount || 0 }}单</span>
              </div>
              <div style="text-align:right">
                <div class="bill-total">¥{{ bill.totalAmount }}</div>
                <button class="btn btn-success" style="margin-top:6px" @click="handleCheckout(bill.table.id)">💰 结账</button>
              </div>
            </div>
            <div v-for="od in bill.orders" :key="od.order.id" class="bill-order">
              <div class="bill-order-header">
                <span>#{{ od.order.orderNo }}</span>
                <span>{{ orderStatusMap[od.order.status] || '未知' }}</span>
              </div>
              <div v-for="item in od.items" :key="item.id" class="bill-item">
                <span>{{ item.productName }}({{ item.skuName }}) x{{ item.quantity }}</span>
                <span>¥{{ item.subtotal }}</span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- Table Modal -->
    <div v-if="tableModal.show" class="modal-overlay" @click.self="tableModal.show = false">
      <div class="modal-box">
        <h3>{{ tableModal.id ? '编辑桌台' : '新增桌台' }}</h3>
        <div class="form-group">
          <label class="form-label">桌号</label>
          <input class="form-input" v-model="tableModal.tableNumber" placeholder="如 A03">
        </div>
        <div class="form-group">
          <label class="form-label">座位数</label>
          <input class="form-input" type="number" v-model.number="tableModal.seats">
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="tableModal.show = false">取消</button>
          <button class="btn btn-primary" @click="handleSaveTable">保存</button>
        </div>
      </div>
    </div>

    <!-- Category Modal -->
    <div v-if="categoryModal.show" class="modal-overlay" @click.self="categoryModal.show = false">
      <div class="modal-box">
        <h3>{{ categoryModal.id ? '编辑分类' : '新增分类' }}</h3>
        <div class="form-group">
          <label class="form-label">分类名称</label>
          <input class="form-input" v-model="categoryModal.name" placeholder="如 啤酒">
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">图标(emoji)</label>
            <input class="form-input" v-model="categoryModal.icon" placeholder="如 🍺">
          </div>
          <div class="form-group">
            <label class="form-label">排序</label>
            <input class="form-input" type="number" v-model.number="categoryModal.sortOrder">
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="categoryModal.show = false">取消</button>
          <button class="btn btn-primary" @click="handleSaveCategory">保存</button>
        </div>
      </div>
    </div>

    <!-- Product Modal -->
    <div v-if="productModal.show" class="modal-overlay" @click.self="productModal.show = false">
      <div class="modal-box">
        <h3>新增商品</h3>
        <div class="form-group">
          <label class="form-label">商品名称</label>
          <input class="form-input" v-model="productModal.name" placeholder="商品名称">
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">分类</label>
            <select class="form-input" v-model.number="productModal.categoryId">
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.icon }} {{ c.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">类型</label>
            <select class="form-input" v-model.number="productModal.productType">
              <option :value="0">酒水</option>
              <option :value="1">小食</option>
              <option :value="2">套餐</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">描述</label>
          <input class="form-input" v-model="productModal.description" placeholder="商品描述">
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">标签(逗号分隔)</label>
            <input class="form-input" v-model="productModal.tags" placeholder="热门,新品,推荐">
          </div>
          <div class="form-group">
            <label class="form-label">含酒精</label>
            <select class="form-input" v-model.number="productModal.containsAlcohol">
              <option :value="1">是</option>
              <option :value="0">否</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">规格 (格式: 名称:价格:库存，多个用分号)</label>
          <input class="form-input" v-model="productModal.skuStr" placeholder="如 标准杯:48:100;大杯:68:50">
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="productModal.show = false">取消</button>
          <button class="btn btn-primary" @click="handleSaveProduct">保存</button>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div v-if="toast" class="toast-container"><div class="toast-msg">{{ toast }}</div></div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  getTables, addTable, updateTable, deleteTable, clearTable,
  adminGetCategories, adminAddCategory, adminUpdateCategory, adminDeleteCategory,
  adminGetProducts, adminAddProduct, adminUpdateProductStatus, adminDeleteProduct,
  getTableBill, checkoutTable
} from '@/api'

const router = useRouter()
const auth = useAuthStore()
const roleMap = { 0: '吧台员工', 1: '后厨员工', 2: '店长', 3: '平台管理员' }
const roleText = ref(roleMap[auth.staff?.role] || '员工')

function handleLogout() {
  auth.logout()
  router.replace('/login')
}

const STORE_ID = 1
const typeMap = { 0: '酒水', 1: '小食', 2: '套餐' }
const orderStatusMap = { 0: '待接单', 1: '制作中', 2: '已完成', 3: '已拒单', 4: '已取消', 5: '已结账' }

const tabs = [
  { key: 'tables', icon: '🪑', label: '桌台管理' },
  { key: 'categories', icon: '📂', label: '分类管理' },
  { key: 'products', icon: '📦', label: '商品管理' },
  { key: 'checkout', icon: '💰', label: '结账管理' }
]

const activeTab = ref('tables')
const tables = ref([])
const categories = ref([])
const products = ref([])
const bills = ref([])
const toast = ref('')

// Modals
const tableModal = reactive({ show: false, id: null, tableNumber: '', seats: 4 })
const categoryModal = reactive({ show: false, id: null, name: '', icon: '', sortOrder: 0 })
const productModal = reactive({ show: false, name: '', categoryId: null, productType: 0, description: '', tags: '', containsAlcohol: 1, skuStr: '' })

// Load data on tab change
watch(activeTab, (tab) => {
  if (tab === 'tables') loadTables()
  else if (tab === 'categories') loadCategories()
  else if (tab === 'products') loadProducts()
  else if (tab === 'checkout') loadBills()
})

onMounted(() => loadTables())

// ========== Tables ==========
async function loadTables() { tables.value = await getTables(STORE_ID).catch(() => []) }

function tableStatusText(s) { return s === 0 ? '空闲' : s === 1 ? '已开台' : '已清台' }
function tableStatusBadge(s) { return s === 0 ? 'badge-success' : s === 1 ? 'badge-warning' : 'badge-muted' }

function openTableModal(t) {
  tableModal.id = t?.id || null
  tableModal.tableNumber = t?.tableNumber || ''
  tableModal.seats = t?.seats || 4
  tableModal.show = true
}

async function handleSaveTable() {
  if (!tableModal.tableNumber.trim()) { alert('请输入桌号'); return }
  try {
    if (tableModal.id) {
      await updateTable(tableModal.id, { tableNumber: tableModal.tableNumber, seats: tableModal.seats })
    } else {
      await addTable({ storeId: STORE_ID, tableNumber: tableModal.tableNumber, seats: tableModal.seats, status: 0 })
    }
    tableModal.show = false
    showToast('保存成功')
    loadTables()
  } catch (e) { alert(e.message) }
}

async function handleDeleteTable(id) {
  if (!confirm('确认删除该桌台？')) return
  try { await deleteTable(id); showToast('删除成功'); loadTables() } catch (e) { alert(e.message) }
}

async function handleClearTable(id) {
  if (!confirm('确认清台？')) return
  try { await clearTable(id); showToast('清台成功'); loadTables() } catch (e) { alert(e.message) }
}

// ========== Categories ==========
async function loadCategories() { categories.value = await adminGetCategories(STORE_ID).catch(() => []) }

function openCategoryModal(c) {
  categoryModal.id = c?.id || null
  categoryModal.name = c?.name || ''
  categoryModal.icon = c?.icon || ''
  categoryModal.sortOrder = c?.sortOrder || 0
  categoryModal.show = true
}

async function handleSaveCategory() {
  if (!categoryModal.name.trim()) { alert('请输入分类名称'); return }
  try {
    const body = { storeId: STORE_ID, name: categoryModal.name, icon: categoryModal.icon, sortOrder: categoryModal.sortOrder, status: 1 }
    if (categoryModal.id) {
      await adminUpdateCategory(categoryModal.id, body)
    } else {
      await adminAddCategory(body)
    }
    categoryModal.show = false
    showToast('保存成功')
    loadCategories()
  } catch (e) { alert(e.message) }
}

async function handleDeleteCategory(id) {
  if (!confirm('确认删除该分类？')) return
  try { await adminDeleteCategory(id); showToast('删除成功'); loadCategories() } catch (e) { alert(e.message) }
}

// ========== Products ==========
async function loadProducts() { products.value = await adminGetProducts(STORE_ID).catch(() => []) }

function openProductModal() {
  productModal.name = ''; productModal.description = ''; productModal.tags = ''
  productModal.productType = 0; productModal.containsAlcohol = 1; productModal.skuStr = ''
  if (categories.value.length > 0) productModal.categoryId = categories.value[0].id
  productModal.show = true
}

async function handleSaveProduct() {
  if (!productModal.name.trim()) { alert('请输入商品名称'); return }
  if (!productModal.skuStr.trim()) { alert('请至少添加一个规格'); return }
  const skus = productModal.skuStr.split(';').filter(Boolean).map(s => {
    const [skuName, price, stock] = s.trim().split(':')
    return { skuName, price: parseFloat(price), stock: parseInt(stock), soldOut: 0 }
  })
  try {
    await adminAddProduct({
      product: {
        storeId: STORE_ID, categoryId: productModal.categoryId, name: productModal.name,
        description: productModal.description, productType: productModal.productType,
        tags: productModal.tags, containsAlcohol: productModal.containsAlcohol, status: 1
      },
      skus
    })
    productModal.show = false
    showToast('添加成功')
    loadProducts()
  } catch (e) { alert(e.message) }
}

async function handleToggleStatus(id, status) {
  try { await adminUpdateProductStatus(id, status); showToast('操作成功'); loadProducts() } catch (e) { alert(e.message) }
}

async function handleDeleteProduct(id) {
  if (!confirm('确认删除该商品？')) return
  try { await adminDeleteProduct(id); showToast('删除成功'); loadProducts() } catch (e) { alert(e.message) }
}

// ========== Checkout ==========
async function loadBills() {
  try {
    const allTables = await getTables(STORE_ID)
    const occupied = allTables.filter(t => t.status === 1)
    const result = []
    for (const t of occupied) {
      try {
        const bill = await getTableBill(t.id)
        result.push(bill)
      } catch { /* skip */ }
    }
    bills.value = result
  } catch { bills.value = [] }
}

async function handleCheckout(tableId) {
  if (!confirm('确认结账？结账后将自动清台。')) return
  try { await checkoutTable(tableId); showToast('结账成功'); loadBills(); loadTables() } catch (e) { alert(e.message) }
}

function showToast(msg) {
  toast.value = msg
  setTimeout(() => { toast.value = '' }, 2000)
}
</script>

<style scoped>
.admin-page { min-height: 100vh; }

.header {
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  padding: 14px 24px; display: flex; align-items: center; justify-content: space-between;
}
.header h1 { font-size: 20px; }

.layout { display: flex; min-height: calc(100vh - 56px); }
.sidebar { width: 200px; background: var(--card-bg); border-right: 1px solid var(--border); padding: 16px 0; flex-shrink: 0; }
.nav-item { padding: 12px 24px; cursor: pointer; font-size: 14px; color: var(--text-muted); transition: all .2s; border-left: 3px solid transparent; }
.nav-item:hover { background: rgba(255,255,255,.05); color: var(--text); }
.nav-item.active { background: rgba(108,52,131,.2); color: var(--primary-light); border-left-color: var(--primary-light); }

.content { flex: 1; padding: 24px; overflow-y: auto; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-header h2 { font-size: 20px; font-weight: 600; }

/* Table grid */
.table-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px; }
.table-card { background: var(--card-bg); border-radius: 12px; padding: 16px; border: 1px solid var(--border); text-align: center; transition: all .2s; }
.table-card:hover { border-color: var(--primary); transform: translateY(-2px); }
.table-card.status-0 { border-color: rgba(46,204,113,.3); }
.table-card.status-1 { border-color: rgba(243,156,18,.3); }
.table-num { font-size: 22px; font-weight: 700; margin-bottom: 6px; }
.table-seats { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; }
.table-actions { margin-top: 10px; display: flex; gap: 6px; justify-content: center; flex-wrap: wrap; }

.empty-row { text-align: center; padding: 40px !important; color: var(--text-muted); }
.sku-text { font-size: 12px; color: var(--text-muted); padding: 2px 0; }

/* Bill */
.bill-card { background: var(--card-bg); border-radius: 12px; padding: 20px; border: 1px solid var(--border); margin-bottom: 16px; }
.bill-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--border); }
.bill-table { font-size: 20px; font-weight: 700; }
.bill-total { font-size: 24px; font-weight: 700; color: var(--accent); }
.bill-order { padding: 10px 0; border-bottom: 1px solid rgba(255,255,255,.05); }
.bill-order-header { display: flex; justify-content: space-between; font-size: 13px; color: var(--text-muted); margin-bottom: 6px; }
.bill-item { display: flex; justify-content: space-between; font-size: 13px; padding: 2px 0; }
</style>
