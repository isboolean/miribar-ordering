<template>
  <div class="menu-manage">
    <div class="page-header">
      <h2>菜单管理</h2>
      <button class="btn btn-primary" @click="showAddDialog(null)">新增顶级菜单</button>
    </div>

    <SearchFilterBar
      :fields="searchFields"
      :model="filters"
      @search="loadData"
      @reset="resetFilters"
    />

    <div class="tree-grid">
      <!-- 表头 -->
      <div class="grid-row grid-head">
        <div class="col-name"><span class="head-spacer"></span>菜单名称</div>
        <div class="col-type">类型</div>
        <div class="col-path">路由路径</div>
        <div class="col-perm">权限编码</div>
        <div class="col-sort">排序</div>
        <div class="col-actions">操作</div>
      </div>

      <!-- 树形行 -->
      <div
        v-for="row in visibleRows"
        :key="row.menu.id"
        class="grid-row"
        :class="{ 'row-dir': row.menu.menuType === 0 }"
      >
        <!-- 左：菜单信息 -->
        <div class="col-name" :style="{ paddingLeft: `${16 + row.depth * 24}px` }">
          <span
            v-if="hasChildren(row.menu)"
            class="expand-toggle"
            :class="{ expanded: isExpanded(row.menu.id) }"
            @click="toggleExpand(row.menu.id)"
          >›</span>
          <span v-else class="expand-placeholder"></span>
          <span
            class="node-icon"
            :class="`type-${row.menu.menuType}`"
            :title="row.menu.icon ? `图标：${row.menu.icon}` : ''"
          >{{ iconText(row.menu) }}</span>
          <span class="node-name">{{ row.menu.menuName }}</span>
          <span v-if="row.menu.visible === 0" class="badge badge-muted badge-mini">隐藏</span>
          <span v-if="row.menu.status === 0" class="badge badge-danger badge-mini">禁用</span>
        </div>

        <div class="col-type">
          <span class="badge" :class="typeBadge(row.menu.menuType)">{{ typeText(row.menu.menuType) }}</span>
        </div>

        <div class="col-path">{{ row.menu.path || '—' }}</div>
        <div class="col-perm">{{ row.menu.permissionCode || '—' }}</div>
        <div class="col-sort">{{ row.menu.sortOrder }}</div>

        <!-- 右：操作区 -->
        <div class="col-actions">
          <button
            v-if="row.menu.menuType !== 2"
            class="btn btn-outline btn-sm"
            @click="showAddDialog(row.menu)"
          >新增子菜单</button>
          <button class="btn btn-outline btn-sm" @click="showEditDialog(row.menu)">编辑</button>
          <button class="btn-text danger" @click="handleDelete(row.menu)">删除</button>
        </div>
      </div>

      <div v-if="menuTree.length === 0" class="empty">暂无菜单数据</div>
      <div v-else-if="isFiltering && visibleRows.length === 0" class="empty">无匹配的菜单</div>
    </div>

    <!-- 新增/编辑对话框 -->
    <div v-if="dialogVisible" class="dialog-mask" @click.self="closeDialog">
      <div class="dialog">
        <h3>{{ isEdit ? '编辑菜单' : '新增菜单' }}</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-item">
            <label>菜单类型</label>
            <select v-model="form.menuType" required>
              <option :value="0">目录</option>
              <option :value="1">菜单</option>
              <option :value="2">按钮/权限</option>
            </select>
          </div>
          <div class="form-item">
            <label>菜单名称</label>
            <input v-model="form.menuName" type="text" required placeholder="请输入菜单名称" />
          </div>
          <div v-if="form.menuType !== 2" class="form-item">
            <label>路由路径</label>
            <input v-model="form.path" type="text" placeholder="如：/staff/list" />
          </div>
          <div v-if="form.menuType === 1" class="form-item">
            <label>组件路径</label>
            <input v-model="form.component" type="text" placeholder="如：staff/StaffList" />
          </div>
          <div v-if="form.menuType === 2" class="form-item">
            <label>权限编码</label>
            <input v-model="form.permissionCode" type="text" placeholder="如：staff:add" />
          </div>
          <div class="form-row">
            <div class="form-item">
              <label>图标</label>
              <input v-model="form.icon" type="text" placeholder="图标名称" />
            </div>
            <div class="form-item">
              <label>排序</label>
              <input v-model.number="form.sortOrder" type="number" placeholder="数字越小越靠前" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-item">
              <label>是否可见</label>
              <select v-model="form.visible">
                <option :value="1">显示</option>
                <option :value="0">隐藏</option>
              </select>
            </div>
            <div class="form-item">
              <label>状态</label>
              <select v-model="form.status">
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </div>
          </div>
          <div class="dialog-footer">
            <button type="button" class="btn btn-outline" @click="closeDialog">取消</button>
            <button type="submit" class="btn btn-primary">确定</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getMenuList, addMenu, updateMenu, deleteMenu } from '@/api'
import SearchFilterBar from '@/components/SearchFilterBar.vue'

const menuTree = ref([])

// 搜索条件（各条件独立控件，可组合使用；名称条件同时匹配路由路径与权限编码）
const filters = reactive({ keyword: '', menuType: '', status: '' })
const searchFields = [
  { key: 'keyword', label: '名称/路径', placeholder: '菜单名称、路由路径或权限编码' },
  {
    key: 'menuType', label: '类型', type: 'select',
    options: [
      { label: '全部', value: '' },
      { label: '目录', value: 0 },
      { label: '菜单', value: 1 },
      { label: '按钮', value: 2 }
    ]
  },
  {
    key: 'status', label: '状态', type: 'select',
    options: [
      { label: '全部', value: '' },
      { label: '启用', value: 1 },
      { label: '禁用', value: 0 }
    ]
  }
]

const isFiltering = computed(() =>
  filters.keyword.trim() !== '' || filters.menuType !== '' || filters.status !== ''
)

function resetFilters() {
  filters.keyword = ''
  filters.menuType = ''
  filters.status = ''
}

// 递归过滤：节点自身匹配，或存在匹配的后代时保留（祖先链用于维持树形结构）
function filterTree(nodes) {
  const kw = filters.keyword.trim().toLowerCase()
  const result = []
  for (const menu of nodes) {
    const selfMatch =
      (!kw ||
        (menu.menuName || '').toLowerCase().includes(kw) ||
        (menu.path || '').toLowerCase().includes(kw) ||
        (menu.permissionCode || '').toLowerCase().includes(kw)) &&
      (filters.menuType === '' || menu.menuType === filters.menuType) &&
      (filters.status === '' || menu.status === filters.status)
    const matchedChildren = hasChildren(menu) ? filterTree(menu.children) : []
    if (selfMatch || matchedChildren.length > 0) {
      result.push(matchedChildren.length > 0 ? { ...menu, children: matchedChildren } : menu)
    }
  }
  return result
}

const filteredTree = computed(() => (isFiltering.value ? filterTree(menuTree.value) : menuTree.value))
// 展开/收起状态：默认收起，点击箭头才展开（搜索时自动全展开）
const expandedIds = ref(new Set())
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({
  id: null,
  parentId: 0,
  menuType: 1,
  menuName: '',
  path: '',
  component: '',
  permissionCode: '',
  icon: '',
  sortOrder: 0,
  visible: 1,
  status: 1
})

const hasChildren = (menu) => Array.isArray(menu.children) && menu.children.length > 0
const isExpanded = (id) => expandedIds.value.has(id)
const toggleExpand = (id) => {
  const next = new Set(expandedIds.value)
  next.has(id) ? next.delete(id) : next.add(id)
  expandedIds.value = next
}

// 按展开状态展平树为行列表（搜索时自动全展开）
const visibleRows = computed(() => {
  const rows = []
  const walk = (nodes, depth) => {
    for (const menu of nodes) {
      rows.push({ menu, depth })
      if (hasChildren(menu) && (isFiltering.value || expandedIds.value.has(menu.id))) {
        walk(menu.children, depth + 1)
      }
    }
  }
  walk(filteredTree.value, 0)
  return rows
})

const typeText = (t) => ({ 0: '目录', 1: '菜单', 2: '按钮' }[t] ?? '未知')
const typeBadge = (t) => ({ 0: 'badge-info', 1: 'badge-success', 2: 'badge-muted' }[t] ?? 'badge-muted')
// 图标标记固定用中文单字；数据库 icon 名（如 User）过长，仅作悬浮提示展示
const iconText = (menu) => ({ 0: '目', 1: '菜', 2: '按' }[menu.menuType] ?? '·')

const loadData = async () => {
  menuTree.value = await getMenuList()
}

const showAddDialog = (parent) => {
  isEdit.value = false
  form.value = {
    id: null,
    parentId: parent ? parent.id : 0,
    menuType: parent ? 1 : 0,
    menuName: '',
    path: '',
    component: '',
    permissionCode: '',
    icon: '',
    sortOrder: 0,
    visible: 1,
    status: 1
  }
  dialogVisible.value = true
}

const showEditDialog = (menu) => {
  isEdit.value = true
  form.value = { ...menu }
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await updateMenu(form.value)
      alert('修改成功')
    } else {
      await addMenu(form.value)
      alert('新增成功')
    }
    closeDialog()
    loadData()
  } catch (e) {
    alert(e.message)
  }
}

const handleDelete = async (menu) => {
  if (hasChildren(menu) && !confirm(`菜单"${menu.menuName}"包含子菜单，删除将一并移除，确定继续吗？`)) return
  if (!hasChildren(menu) && !confirm(`确定要删除菜单"${menu.menuName}"吗？`)) return
  try {
    await deleteMenu(menu.id)
    alert('删除成功')
    loadData()
  } catch (e) {
    alert(e.message)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.menu-manage {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

/* ===== 树形表格 ===== */
.tree-grid {
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.grid-row {
  display: grid;
  /* 列宽统一由 grid 模板控制，表头与数据行逐列对齐；操作列固定宽度 */
  grid-template-columns: minmax(0, 2.2fr) minmax(0, .7fr) minmax(0, 1.4fr) minmax(0, 1.2fr) minmax(0, .5fr) 190px;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
  transition: background .15s;
}

.grid-row:last-of-type { border-bottom: none; }
.grid-row:hover { background: var(--surface-2); }

.grid-head {
  background: var(--surface-2);
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 500;
  padding: 10px 16px;
}

/* 列样式（宽度由 grid-template-columns 统一控制） */
.col-name   { display: flex; align-items: center; gap: 8px; min-width: 0; }
.col-path   { color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.col-perm   { color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.col-sort   { color: var(--text-muted); }
.col-actions { display: flex; justify-content: flex-end; align-items: center; gap: 8px; min-width: 0; }

.grid-head .col-path,
.grid-head .col-perm { color: var(--text-muted); }

/* 表头占位：与数据行的「箭头+图标」等宽，保证表头与名称垂直对齐 */
.head-spacer { width: 58px; flex-shrink: 0; }

/* 展开箭头 */
.expand-toggle {
  width: 18px; height: 18px;
  display: inline-flex; align-items: center; justify-content: center;
  color: var(--text-muted);
  font-size: 15px; font-weight: 600;
  cursor: pointer; user-select: none;
  transition: transform .2s;
  border-radius: 4px;
  flex-shrink: 0;
}
.expand-toggle:hover { background: var(--border); color: var(--text); }
.expand-toggle.expanded { transform: rotate(90deg); }
.expand-placeholder { width: 18px; flex-shrink: 0; }

/* 类型图标：字母标记，样式随类型 */
.node-icon {
  width: 24px; height: 24px;
  display: inline-flex; align-items: center; justify-content: center;
  border-radius: 6px;
  font-size: 12px; font-weight: 600;
  flex-shrink: 0;
}
.node-icon.type-0 { background: var(--primary-soft); color: var(--primary); }
.node-icon.type-1 { background: rgba(22, 163, 74, .1); color: var(--success); }
.node-icon.type-2 { background: var(--surface-2); color: var(--text-muted); }

.node-name { font-weight: 500; white-space: nowrap; }
.row-dir > .col-name .node-name { font-weight: 600; }

.badge-mini { font-size: 10px; padding: 1px 6px; margin-left: 2px; }

/* 删除：文字按钮，弱化视觉 */
.btn-text {
  background: none; border: none;
  padding: 5px 6px; font-size: 12px;
  color: var(--danger);
  border-radius: 6px;
}
.btn-text:hover { background: rgba(239, 68, 68, .08); }

.empty {
  text-align: center;
  padding: 48px 0;
  color: var(--text-muted);
}

/* ===== 对话框 ===== */
.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(16, 24, 40, .4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  padding: 24px;
  min-width: 420px;
  max-width: 90vw;
  box-shadow: var(--shadow-md);
}

.dialog h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
}

.form-item {
  margin-bottom: 16px;
  flex: 1;
}

.form-item label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--text-muted);
}

.form-item input,
.form-item select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  background: var(--card-bg);
  color: var(--text);
  transition: border-color .15s, box-shadow .15s;
}

.form-item input:focus,
.form-item select:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-soft);
}

.form-row { display: flex; gap: 12px; }

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style>
