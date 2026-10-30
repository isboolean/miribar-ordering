<template>
  <div class="admin-layout">
    <header class="header">
      <h1>MiriBar 管理后台</h1>
      <div class="header-user">
        <span style="font-size:13px;opacity:.8">{{ auth.displayName }} | {{ roleText }}</span>
        <button class="btn btn-outline btn-sm" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <div class="layout">
      <nav class="sidebar">
        <!-- 动态菜单 -->
        <template v-for="menu in visibleMenus" :key="menu.id">
          <!-- 目录类型（有子菜单） -->
          <div v-if="menu.menuType === 0 && menu.children && menu.children.length > 0" class="nav-group">
            <div class="nav-item nav-group-title" @click="toggleGroup(menu.id)">
              {{ getMenuIcon(menu.icon) }} {{ menu.menuName }}
              <span class="arrow" :class="{ expanded: expandedGroups.includes(menu.id) }">▶</span>
            </div>
            <div v-show="expandedGroups.includes(menu.id)" class="nav-group-children">
              <div 
                v-for="child in menu.children" 
                :key="child.id" 
                class="nav-item nav-child"
                :class="{ active: activeMenu === child.path }"
                @click="navigateTo(child)"
              >
                {{ getMenuIcon(child.icon) }} {{ child.menuName }}
              </div>
            </div>
          </div>
          <!-- 菜单类型（无子菜单） -->
          <div 
            v-else-if="menu.menuType === 1" 
            class="nav-item"
            :class="{ active: activeMenu === menu.path }"
            @click="navigateTo(menu)"
          >
            {{ getMenuIcon(menu.icon) }} {{ menu.menuName }}
          </div>
        </template>
        
        <!-- 固定菜单（如果用户没有动态菜单，显示默认菜单） -->
        <template v-if="visibleMenus.length === 0">
          <div class="nav-item" :class="{ active: activeMenu === '/admin/tables' }" @click="activeMenu = '/admin/tables'; loadContent('tables')">
            🪑 桌台管理
          </div>
          <div class="nav-item" :class="{ active: activeMenu === '/admin/categories' }" @click="activeMenu = '/admin/categories'; loadContent('categories')">
            📂 分类管理
          </div>
          <div class="nav-item" :class="{ active: activeMenu === '/admin/products' }" @click="activeMenu = '/admin/products'; loadContent('products')">
            📦 商品管理
          </div>
          <div class="nav-item" :class="{ active: activeMenu === '/admin/checkout' }" @click="activeMenu = '/admin/checkout'; loadContent('checkout')">
            💰 结账管理
          </div>
        </template>
      </nav>

      <main class="content">
        <!-- 动态组件渲染 -->
        <component :is="currentComponent" v-if="currentComponent" />
        
        <!-- 默认内容（当没有选择动态菜单时） -->
        <div v-else-if="activeContent === 'tables'" class="section">
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

        <div v-else-if="activeContent === 'categories'" class="section">
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

        <div v-else-if="activeContent === 'products'" class="section">
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

        <div v-else-if="activeContent === 'checkout'" class="section">
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

        <div v-else class="empty-state">
          <div class="icon">👋</div>
          <p>欢迎使用 MiriBar 管理后台</p>
          <p style="font-size:13px;color:#999">请从左侧菜单选择功能</p>
        </div>
      </main>
    </div>

    <!-- 省略原有的 Modal 代码，保持简洁 -->
    <!-- Toast -->
    <div v-if="toast" class="toast-container"><div class="toast-msg">{{ toast }}</div></div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, markRaw, defineAsyncComponent } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  getTables, addTable, updateTable, deleteTable, clearTable,
  adminGetCategories, adminAddCategory, adminUpdateCategory, adminDeleteCategory,
  adminGetProducts, adminAddProduct, adminUpdateProductStatus, adminDeleteProduct,
  getTableBill, checkoutTable
} from '@/api'

// 导入动态组件
import StaffListView from './staff/StaffListView.vue'
import RoleView from './role/RoleView.vue'
import MenuView from './menu/MenuView.vue'
import OrgView from './org/OrgView.vue'
import DictView from './dict/DictView.vue'
import CustomerListView from './customer/CustomerListView.vue'

const router = useRouter()
const auth = useAuthStore()
const roleMap = { 0: '吧台员工', 1: '后厨员工', 2: '店长', 3: '平台管理员' }
const roleText = ref(roleMap[auth.staff?.role] || '员工')

const activeMenu = ref('')
const activeContent = ref('')
const currentComponent = ref(null)
const expandedGroups = ref([])

// 计算可见菜单（过滤掉隐藏的和按钮类型）
const visibleMenus = computed(() => {
  return (auth.menus || []).filter(m => (m.visible === 1 || m.visible === null || m.visible === undefined) && m.menuType !== 2)
})

function handleLogout() {
  auth.logout()
  router.replace('/login')
}

function getMenuIcon(icon) {
  // 简约风格：统一使用中性标记点，不再使用 emoji 图标
  return ''
}

function toggleGroup(groupId) {
  const idx = expandedGroups.value.indexOf(groupId)
  if (idx >= 0) {
    expandedGroups.value.splice(idx, 1)
  } else {
    expandedGroups.value.push(groupId)
  }
}

function navigateTo(menu) {
  activeMenu.value = menu.path
  currentComponent.value = null
  
  // 根据组件路径加载对应组件
  const componentMap = {
    'staff/StaffList': StaffListView,
    'staff/RoleManage': RoleView,
    'staff/OrgManage': OrgView,
    'system/MenuManage': MenuView,
    'system/DictManage': DictView,
    'customer/CustomerList': CustomerListView
  }
  
  if (componentMap[menu.component]) {
    currentComponent.value = markRaw(componentMap[menu.component])
  } else {
    // 其他组件根据 path 加载内容（业务管理）
    const contentKey = menu.path.split('/').pop()
    loadContent(contentKey)
  }
}

function loadContent(key) {
  activeContent.value = key
  currentComponent.value = null
  
  if (key === 'tables') loadTables()
  else if (key === 'categories') loadCategories()
  else if (key === 'products') loadProducts()
  else if (key === 'checkout') loadBills()
}

const STORE_ID = 1
const typeMap = { 0: '酒水', 1: '小食', 2: '套餐' }
const orderStatusMap = { 0: '待接单', 1: '制作中', 2: '已完成', 3: '已拒单', 4: '已取消', 5: '已结账' }

const tables = ref([])
const categories = ref([])
const products = ref([])
const bills = ref([])
const toast = ref('')

// Modals
const tableModal = reactive({ show: false, id: null, tableNumber: '', seats: 4 })
const categoryModal = reactive({ show: false, id: null, name: '', icon: '', sortOrder: 0 })
const productModal = reactive({ show: false, name: '', categoryId: null, productType: 0, description: '', tags: '', containsAlcohol: 1, skuStr: '' })

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

onMounted(() => {
  // 默认加载桌台
  loadTables()
})
</script>

<style scoped>
.admin-layout { min-height: 100vh; }

.header {
  background: var(--card-bg);
  border-bottom: 1px solid var(--border);
  padding: 14px 24px; display: flex; align-items: center; justify-content: space-between;
}
.header h1 { font-size: 18px; font-weight: 600; }

.layout { display: flex; min-height: calc(100vh - 56px); }
.sidebar { 
  width: 220px; 
  background: var(--card-bg); 
  border-right: 1px solid var(--border); 
  padding: 16px 0; 
  flex-shrink: 0; 
  overflow-y: auto;
}
.nav-item { 
  padding: 12px 24px; 
  cursor: pointer; 
  font-size: 14px; 
  color: var(--text-muted); 
  transition: all .2s; 
  border-left: 3px solid transparent; 
}
.nav-item:hover { background: var(--surface-2); color: var(--text); }
.nav-item.active { background: var(--primary-soft); color: var(--primary); border-left-color: var(--primary); font-weight: 500; }

.nav-group-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.arrow {
  font-size: 10px;
  transition: transform .2s;
}

.arrow.expanded {
  transform: rotate(90deg);
}

.nav-group-children {
  background: var(--surface-2);
}

.nav-child {
  padding-left: 40px;
  font-size: 13px;
}

.content { flex: 1; padding: 24px; overflow-y: auto; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-header h2 { font-size: 18px; font-weight: 600; }

/* Table grid */
.table-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px; }
.table-card { background: var(--card-bg); border-radius: 12px; padding: 16px; border: 1px solid var(--border); text-align: center; transition: all .2s; }
.table-card:hover { border-color: var(--primary); transform: translateY(-2px); }
.table-card.status-0 { border-color: rgba(22,163,74,.3); }
.table-card.status-1 { border-color: rgba(245,158,11,.3); }
.table-num { font-size: 22px; font-weight: 700; margin-bottom: 6px; }
.table-seats { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; }
.table-actions { margin-top: 10px; display: flex; gap: 6px; justify-content: center; flex-wrap: wrap; }

.empty-row { text-align: center; padding: 40px !important; color: var(--text-muted); }
.sku-text { font-size: 12px; color: var(--text-muted); padding: 2px 0; }

/* Bill */
.bill-card { background: var(--card-bg); border-radius: 12px; padding: 20px; border: 1px solid var(--border); margin-bottom: 16px; }
.bill-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--border); }
.bill-table { font-size: 20px; font-weight: 700; }
.bill-total { font-size: 22px; font-weight: 700; color: var(--accent); }
.bill-order { padding: 10px 0; border-bottom: 1px solid var(--border); }
.bill-order-header { display: flex; justify-content: space-between; font-size: 13px; color: var(--text-muted); margin-bottom: 6px; }
.bill-item { display: flex; justify-content: space-between; font-size: 13px; padding: 2px 0; }

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-muted);
}

.empty-state .icon {
  font-size: 48px;
  margin-bottom: 16px;
}
</style>
