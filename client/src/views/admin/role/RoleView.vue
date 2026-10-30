<template>
  <div class="role-manage">
    <div class="page-header">
      <h2>角色管理</h2>
      <button class="btn btn-primary" @click="showAddDialog">新增角色</button>
    </div>

    <SearchFilterBar
      :fields="searchFields"
      :model="filters"
      @search="handleSearch"
      @reset="handleReset"
    />

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>角色名称</th>
            <th>角色编码</th>
            <th>描述</th>
            <th>数据范围</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="role in roles" :key="role.id">
            <td>{{ role.id }}</td>
            <td>{{ role.roleName }}</td>
            <td>{{ role.roleCode }}</td>
            <td>{{ role.description }}</td>
            <td>{{ getDataScopeText(role.dataScope) }}</td>
            <td>
              <span :class="role.status === 1 ? 'status-enabled' : 'status-disabled'">
                {{ role.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>
              <button class="btn btn-sm" @click="showEditDialog(role)">编辑</button>
              <button class="btn btn-sm" @click="showPermissionDialog(role)">分配权限</button>
              <button class="btn btn-sm btn-danger" @click="handleDelete(role)">删除</button>
            </td>
          </tr>
          <tr v-if="roles.length === 0">
            <td colspan="7" class="empty">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <PaginationBar
      :total="total"
      :page="page"
      :page-size="pageSize"
      @update:page="handlePageChange"
      @update:page-size="handlePageSizeChange"
    />

    <!-- 新增/编辑对话框 -->
    <div v-if="dialogVisible" class="dialog-mask" @click.self="closeDialog">
      <div class="dialog">
        <h3>{{ isEdit ? '编辑角色' : '新增角色' }}</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-item">
            <label>角色名称</label>
            <input v-model="form.roleName" type="text" required placeholder="请输入角色名称" />
          </div>
          <div class="form-item">
            <label>角色编码</label>
            <input v-model="form.roleCode" type="text" required placeholder="请输入角色编码（如 admin）" />
          </div>
          <div class="form-item">
            <label>描述</label>
            <textarea v-model="form.description" placeholder="请输入描述"></textarea>
          </div>
          <div class="form-item">
            <label>数据范围</label>
            <select v-model="form.dataScope">
              <option :value="1">全部数据</option>
              <option :value="2">本组织及下级</option>
              <option :value="3">仅本组织</option>
            </select>
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="form.status">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </div>
          <div class="dialog-footer">
            <button type="button" class="btn" @click="closeDialog">取消</button>
            <button type="submit" class="btn btn-primary">确定</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 分配权限对话框 -->
    <div v-if="permissionDialogVisible" class="dialog-mask" @click.self="closePermissionDialog">
      <div class="dialog dialog-large">
        <div class="dialog-head">
          <h3>分配权限 - {{ currentRole?.roleName }}</h3>
          <span class="selected-count">已选 {{ selectedMenuIds.length }} 项</span>
        </div>
        <div class="menu-tree-container">
          <PermissionTreeNode
            v-for="menu in menuTree"
            :key="menu.id"
            :node="menu"
            :depth="0"
            :checked-ids="selectedMenuIds"
            @toggle="handleToggle"
          />
          <div v-if="menuTree.length === 0" class="empty">暂无菜单数据</div>
        </div>
        <div class="dialog-footer">
          <button type="button" class="btn" @click="closePermissionDialog">取消</button>
          <button type="button" class="btn btn-primary" @click="handleSavePermission">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRoleList, addRole, updateRole, deleteRole, assignRoleMenus, getRoleMenus, getMenuList } from '@/api'
import SearchFilterBar from '@/components/SearchFilterBar.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import PermissionTreeNode from './PermissionTreeNode.vue'

const roles = ref([])

// 搜索条件（各条件独立控件，可组合使用）
const filters = reactive({ roleName: '', roleCode: '', dataScope: '', status: '' })
const searchFields = [
  { key: 'roleName', label: '角色名称', placeholder: '按角色名称搜索' },
  { key: 'roleCode', label: '角色编码', placeholder: '按角色编码搜索' },
  {
    key: 'dataScope', label: '数据范围', type: 'select',
    options: [
      { label: '全部', value: '' },
      { label: '全部数据', value: 1 },
      { label: '本组织及下级', value: 2 },
      { label: '仅本组织', value: 3 }
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

// 分页状态：与后端返回的 total / page / pageSize 保持一致
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

function resetFilters() {
  filters.roleName = ''
  filters.roleCode = ''
  filters.dataScope = ''
  filters.status = ''
}
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  dataScope: 1,
  status: 1
})

const permissionDialogVisible = ref(false)
const currentRole = ref(null)
const menuTree = ref([])
const selectedMenuIds = ref([])

const loadData = async () => {
  const params = {
    page: page.value,
    pageSize: pageSize.value,
    roleName: filters.roleName.trim(),
    roleCode: filters.roleCode.trim(),
    dataScope: filters.dataScope,
    status: filters.status
  }
  try {
    const result = await getRoleList(params)
    roles.value = result.list || []
    total.value = result.total || 0
    // 删除等操作导致当前页无数据时，自动回退一页
    if (roles.value.length === 0 && total.value > 0 && page.value > 1) {
      page.value = page.value - 1
      await loadData()
    }
  } catch (e) {
    console.error('加载角色列表失败:', e)
    roles.value = []
    total.value = 0
  }
}

// 查询条件变化后回到第一页
function handleSearch() {
  page.value = 1
  loadData()
}

function handleReset() {
  resetFilters()
  page.value = 1
  loadData()
}

function handlePageChange(target) {
  page.value = target
  loadData()
}

function handlePageSizeChange(size) {
  pageSize.value = size
  page.value = 1
  loadData()
}

const getDataScopeText = (scope) => {
  const map = { 1: '全部数据', 2: '本组织及下级', 3: '仅本组织' }
  return map[scope] || '未知'
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    roleName: '',
    roleCode: '',
    description: '',
    dataScope: 1,
    status: 1
  }
  dialogVisible.value = true
}

const showEditDialog = (role) => {
  isEdit.value = true
  form.value = { ...role }
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await updateRole(form.value)
      alert('修改成功')
    } else {
      await addRole(form.value)
      alert('新增成功')
      // 新增记录排在列表末尾，跳到最后一页便于查看
      page.value = Math.max(1, Math.ceil((total.value + 1) / pageSize.value))
    }
    closeDialog()
    loadData()
  } catch (e) {
    alert(e.message)
  }
}

const handleDelete = async (role) => {
  if (!confirm(`确定要删除角色"${role.roleName}"吗？`)) return
  try {
    await deleteRole(role.id)
    alert('删除成功')
    loadData()
  } catch (e) {
    alert(e.message)
  }
}

const showPermissionDialog = async (role) => {
  currentRole.value = role
  // 加载菜单树
  menuTree.value = await getMenuList()
  // 加载已分配的菜单
  const assignedIds = await getRoleMenus(role.id)
  selectedMenuIds.value = [...assignedIds]
  permissionDialogVisible.value = true
}

const closePermissionDialog = () => {
  permissionDialogVisible.value = false
  currentRole.value = null
  selectedMenuIds.value = []
}

// 收集节点及其全部后代的 id
const collectIds = (node) => [node.id, ...(node.children || []).flatMap(collectIds)]

// 建立「节点 → 父节点」索引，用于父级勾选状态联动
const buildParentMap = () => {
  const map = new Map()
  const walk = (nodes, parent) => {
    for (const node of nodes) {
      map.set(node.id, parent)
      if (node.children?.length) walk(node.children, node)
    }
  }
  walk(menuTree.value, null)
  return map
}

const handleToggle = (node, checked) => {
  const set = new Set(selectedMenuIds.value)
  const ids = collectIds(node)
  if (checked) ids.forEach(id => set.add(id))
  else ids.forEach(id => set.delete(id))

  // 父级联动：子项全选时自动选中父级，未全选则取消父级
  const parentMap = buildParentMap()
  let parent = parentMap.get(node.id)
  while (parent) {
    const allChecked = (parent.children || []).every(child => set.has(child.id))
    if (allChecked) set.add(parent.id)
    else set.delete(parent.id)
    parent = parentMap.get(parent.id)
  }

  selectedMenuIds.value = [...set]
}

const handleSavePermission = async () => {
  try {
    await assignRoleMenus(currentRole.value.id, selectedMenuIds.value)
    alert('权限分配成功')
    closePermissionDialog()
  } catch (e) {
    alert(e.message)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.role-manage {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.table-container {
  background: var(--card-bg);
  border-radius: 8px;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid var(--border);
}

.data-table th {
  background: var(--surface-2);
  font-weight: 600;
}

.status-enabled {
  color: var(--success);
}

.status-disabled {
  color: var(--danger);
}

.btn {
  padding: 6px 16px;
  border: 1px solid var(--border);
  border-radius: 4px;
  cursor: pointer;
  background: var(--card-bg);
}

.btn-primary {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.btn-danger {
  color: var(--danger);
  border-color: var(--danger);
}

.btn-sm {
  padding: 4px 8px;
  font-size: 12px;
}

.dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: var(--card-bg);
  border-radius: 8px;
  padding: 24px;
  min-width: 400px;
  max-width: 90vw;
}

.dialog-large {
  width: 600px;
}

.dialog h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
}

.form-item input,
.form-item textarea,
.form-item select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: 4px;
  font-size: 14px;
}

.form-item textarea {
  min-height: 80px;
  resize: vertical;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.menu-tree-container {
  max-height: 420px;
  overflow-y: auto;
  border: 1px solid var(--border);
  border-radius: 4px;
  padding: 8px;
}

.dialog-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 16px;
}

.dialog-head h3 {
  margin: 0;
}

.selected-count {
  font-size: 12px;
  color: var(--text-muted);
}

.empty {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
}
</style>
