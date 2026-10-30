<template>
  <div class="staff-list">
    <div class="page-header">
      <h2>员工列表</h2>
      <div class="header-actions">
        <button class="btn btn-primary" @click="showAddDialog">+ 新增员工</button>
      </div>
    </div>

    <SearchFilterBar
      :fields="searchFields"
      :model="filters"
      @search="handleSearch"
      @reset="handleReset"
    />

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>工号</th>
          <th>姓名</th>
          <th>所属机构</th>
          <th>手机号</th>
          <th>角色</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="staff in staffList" :key="staff.id">
          <td>{{ staff.id }}</td>
          <td><span class="staff-no">{{ staff.username }}</span></td>
          <td>{{ staff.realName || '-' }}</td>
          <td>
            <span v-if="staff.orgName" class="org-cell">
              <span class="org-icon">{{ getOrgTypeIcon(staff) }}</span>{{ staff.orgName }}
            </span>
            <span v-else class="tag tag-warning">未分配</span>
          </td>
          <td>{{ staff.phone || '-' }}</td>
          <td>
            <span class="tag" :class="getRoleClass(staff.role)">
              {{ getRoleText(staff.role) }}
            </span>
          </td>
          <td>
            <span :class="staff.status === 1 ? 'tag tag-success' : 'tag tag-danger'">
              {{ staff.status === 1 ? '启用' : '禁用' }}
            </span>
          </td>
          <td>
            <button class="btn-icon" @click="showEditDialog(staff)">编辑</button>
            <span class="action-divider"></span>
            <button class="btn-icon btn-secondary" @click="handleResetPassword(staff)">重置密码</button>
            <span class="action-divider"></span>
            <button class="btn-icon btn-secondary" @click="showRoleDialog(staff)">分配角色</button>
            <span class="action-divider"></span>
            <button class="btn-icon btn-danger" @click="handleDelete(staff)">删除</button>
          </td>
        </tr>
        <tr v-if="staffList.length === 0">
          <td colspan="8" class="empty-row">暂无员工数据</td>
        </tr>
      </tbody>
    </table>

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
        <h3>{{ isEdit ? '编辑员工' : '新增员工' }}</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-item">
            <label>工号</label>
            <input
              v-if="isEdit"
              :value="form.username"
              type="text"
              readonly
              class="readonly"
              title="工号由系统生成，不可修改"
            />
            <div v-else class="form-hint-box">
              <span class="code-preview">自动生成</span>
              <span class="hint-text">由系统自动生成（10 位数字，顺序编号）</span>
            </div>
          </div>
          <div class="form-item">
            <label>姓名</label>
            <input v-model="form.realName" type="text" placeholder="真实姓名" />
          </div>
          <div class="form-item">
            <label>所属机构 <span class="req">*</span></label>
            <select v-model="form.orgId" required>
              <option :value="null" disabled>请选择所属机构</option>
              <option v-for="org in flatOrgs" :key="org.id" :value="org.id">
                {{ org.label }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>手机号</label>
            <input v-model="form.phone" type="text" placeholder="手机号（短信登录用）" />
          </div>
          <div class="form-item" v-if="!isEdit">
            <label>初始密码</label>
            <input type="text" value="123456" disabled />
            <small class="form-hint">默认密码 123456，可在字典管理中修改</small>
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

    <!-- 分配角色对话框 -->
    <div v-if="roleDialogVisible" class="dialog-mask" @click.self="closeRoleDialog">
      <div class="dialog">
        <h3>分配角色 - {{ currentStaff?.realName || currentStaff?.username }}</h3>
        <div class="role-list">
          <label v-for="role in allRoles" :key="role.id" class="role-item">
            <input 
              type="checkbox" 
              :value="role.id" 
              v-model="selectedRoleIds"
            />
            <span class="role-name">{{ role.roleName }}</span>
            <span class="role-desc">{{ role.description }}</span>
          </label>
        </div>
        <div class="dialog-footer">
          <button type="button" class="btn" @click="closeRoleDialog">取消</button>
          <button type="button" class="btn btn-primary" @click="handleAssignRoles">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getStaffList, addStaff, updateStaff, deleteStaff, resetStaffPassword, assignStaffRoles, getStaffRoles } from '@/api'
import { getOrgTree } from '@/api'
import { getEnabledRoles } from '@/api'
import SearchFilterBar from '@/components/SearchFilterBar.vue'
import PaginationBar from '@/components/PaginationBar.vue'

const staffList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({
  id: null,
  username: '',
  realName: '',
  phone: '',
  orgId: null,
  status: 1
})

// 机构树/平铺列表（用于筛选与表单选择）
const orgTree = ref([])
const orgNodeMap = ref(new Map())

function flattenOrgs(nodes, depth = 0, out = []) {
  for (const n of nodes) {
    out.push({ id: n.id, orgType: n.orgType, label: `${'　'.repeat(depth)}${n.orgName}` })
    if (n.children?.length) flattenOrgs(n.children, depth + 1, out)
  }
  return out
}

const flatOrgs = computed(() => flattenOrgs(orgTree.value))

// 树形下拉的机构图标
const orgIcons = { 1: '🏢', 2: '🏛️', 3: '🌐', 4: '📌', 5: '🗂️', 6: '🗺️', 7: '🏙️', 8: '🏪' }

function getOrgTypeIcon(staff) {
  const type = orgNodeMap.value.get(staff.orgId)?.orgType
  return { 1: '🏢', 2: '🏛️', 3: '🌐', 4: '📌', 5: '🗂️', 6: '🗺️', 7: '🏙️', 8: '🏪' }[type] || '📍'
}

const roleDialogVisible = ref(false)
const currentStaff = ref(null)
const allRoles = ref([])
const selectedRoleIds = ref([])

const roleMap = { 0: '吧台员工', 1: '后厨员工', 2: '店长', 3: '平台管理员' }
const roleClassMap = { 0: 'tag-info', 1: 'tag-warning', 2: 'tag-primary', 3: 'tag-danger' }

function getRoleText(role) {
  return roleMap[role] || '未知'
}

function getRoleClass(role) {
  return roleClassMap[role] || ''
}

// 搜索条件（各条件独立控件，可组合使用）
const filters = reactive({ keyword: '', phone: '', org: '', role: '', status: '' })
const searchFields = computed(() => [
  { key: 'keyword', label: '姓名/工号', placeholder: '按姓名或工号搜索' },
  { key: 'phone', label: '手机号', placeholder: '按手机号搜索' },
  {
    key: 'org', label: '所属机构', type: 'tree-select', width: '190px',
    nodes: orgTree.value,
    allLabel: '全部机构',
    allValue: '',
    iconOf: (n) => orgIcons[n.orgType] || '📍'
  },
  {
    key: 'role', label: '角色', type: 'select',
    options: [
      { label: '全部', value: '' },
      { label: '吧台员工', value: 0 },
      { label: '后厨员工', value: 1 },
      { label: '店长', value: 2 },
      { label: '平台管理员', value: 3 }
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
])

// 分页状态：与后端返回的 total / page / pageSize 保持一致
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

function resetFilters() {
  filters.keyword = ''
  filters.phone = ''
  filters.org = ''
  filters.role = ''
  filters.status = ''
}

// 机构条件下推到后端（含下级机构），切换机构即时刷新
async function loadOrgTree() {
  try {
    orgTree.value = await getOrgTree()
    const map = new Map()
    const walk = (nodes) => nodes.forEach(n => {
      map.set(n.id, n)
      if (n.children?.length) walk(n.children)
    })
    walk(orgTree.value)
    orgNodeMap.value = map
  } catch (e) {
    console.error('加载机构树失败:', e)
    orgTree.value = []
    orgNodeMap.value = new Map()
  }
}

async function loadStaff() {
  const params = {
    page: page.value,
    pageSize: pageSize.value,
    keyword: filters.keyword.trim(),
    phone: filters.phone.trim(),
    role: filters.role,
    status: filters.status
  }
  if (filters.org !== '' && filters.org != null) params.orgId = filters.org
  try {
    const result = await getStaffList(params)
    staffList.value = result.list || []
    total.value = result.total || 0
    // 删除后当前页无数据时自动回退一页
    if (staffList.value.length === 0 && total.value > 0 && page.value > 1) {
      page.value = page.value - 1
      await loadStaff()
    }
  } catch (e) {
    console.error('加载员工列表失败:', e)
    staffList.value = []
    total.value = 0
  }
}

// 查询条件变化后回到第一页
function handleSearch() {
  page.value = 1
  loadStaff()
}

function handleReset() {
  resetFilters()
  page.value = 1
  loadStaff()
}

function handlePageChange(target) {
  page.value = target
  loadStaff()
}

function handlePageSizeChange(size) {
  pageSize.value = size
  page.value = 1
  loadStaff()
}

function showAddDialog() {
  isEdit.value = false
  form.value = { id: null, username: '', realName: '', phone: '', orgId: null, status: 1 }
  dialogVisible.value = true
}

function showEditDialog(staff) {
  isEdit.value = true
  form.value = {
    id: staff.id,
    username: staff.username,
    realName: staff.realName || '',
    phone: staff.phone || '',
    orgId: staff.orgId ?? null,
    status: staff.status
  }
  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
}

async function handleSubmit() {
  if (!form.value.orgId) {
    alert('请选择所属机构')
    return
  }
  try {
    if (isEdit.value) {
      await updateStaff(form.value)
      alert('修改成功')
    } else {
      await addStaff(form.value)
      alert('新增成功')
      // 新员工排在末尾（工号递增），跳到最后一页便于查看
      page.value = Math.max(1, Math.ceil((total.value + 1) / pageSize.value))
    }
    closeDialog()
    loadStaff()
  } catch (e) {
    alert(e.message)
  }
}

async function handleDelete(staff) {
  if (!confirm(`确定要删除员工"${staff.realName || staff.username}"吗？`)) return
  try {
    await deleteStaff(staff.id)
    alert('删除成功')
    loadStaff()
  } catch (e) {
    alert(e.message)
  }
}

async function handleResetPassword(staff) {
  if (!confirm(`确定要重置"${staff.realName || staff.username}"的密码吗？\n重置后密码为：123456`)) return
  try {
    await resetStaffPassword(staff.id)
    alert('密码重置成功')
  } catch (e) {
    alert(e.message)
  }
}

async function showRoleDialog(staff) {
  currentStaff.value = staff
  // 加载可分配的角色（启用状态）
  allRoles.value = await getEnabledRoles()
  // 加载员工当前角色
  const staffRoles = await getStaffRoles(staff.id)
  selectedRoleIds.value = staffRoles.map(r => r.id)
  roleDialogVisible.value = true
}

function closeRoleDialog() {
  roleDialogVisible.value = false
}

async function handleAssignRoles() {
  try {
    await assignStaffRoles(currentStaff.value.id, selectedRoleIds.value)
    alert('角色分配成功')
    closeRoleDialog()
  } catch (e) {
    alert(e.message)
  }
}

onMounted(async () => {
  await loadOrgTree()
  await loadStaff()
})
</script>

<style scoped>
.staff-list {
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: 4px;
  width: 250px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--card-bg);
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

.empty-row {
  text-align: center;
  padding: 40px !important;
  color: var(--text-muted);
}

.org-cell {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.org-icon {
  font-size: 14px;
}

.req {
  color: var(--danger);
}

.staff-no {
  font-family: monospace;
  font-size: 13px;
  letter-spacing: .5px;
}

.form-item input.readonly {
  background: var(--surface-2);
  color: var(--text-muted);
  cursor: not-allowed;
  font-family: monospace;
}

.form-hint-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border: 1px dashed var(--border);
  border-radius: 4px;
  background: var(--surface-2);
}

.code-preview {
  font-family: monospace;
  font-size: 14px;
  color: var(--primary);
  letter-spacing: .5px;
}

.hint-text {
  font-size: 12px;
  color: var(--text-muted);
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
  background: var(--surface-2);
}

.tag-success {
  background: rgba(22,163,74,.1);
  color: var(--success);
}

.tag-danger {
  background: rgba(239,68,68,.1);
  color: var(--danger);
}

.tag-primary {
  background: var(--primary-soft);
  color: var(--primary);
}

.tag-info {
  background: var(--primary-soft);
  color: var(--primary);
}

.tag-warning {
  background: rgba(245,158,11,.12);
  color: var(--warning);
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

.btn-icon {
  background: none;
  border: none;
  color: var(--primary);
  cursor: pointer;
  padding: 4px 8px;
  font-size: 13px;
}

.btn-icon:hover {
  color: var(--primary-light);
}

/* 次要操作：中性色，降低操作列视觉噪音 */
.btn-icon.btn-secondary {
  color: var(--text-muted);
}
.btn-icon.btn-secondary:hover {
  color: var(--primary);
}

.btn-icon.btn-danger {
  color: var(--danger);
}

.btn-icon.btn-danger:hover {
  color: var(--danger);
}

.action-divider {
  display: inline-block;
  width: 1px;
  height: 12px;
  background: var(--border);
  vertical-align: middle;
  margin: 0 2px;
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
  min-width: 450px;
  max-width: 90vw;
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
.form-item select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: 4px;
  font-size: 14px;
}

.form-hint {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.role-list {
  max-height: 300px;
  overflow-y: auto;
}

.role-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
}

.role-item:hover {
  background: var(--surface-2);
}

.role-name {
  font-weight: 500;
  flex: 1;
}

.role-desc {
  font-size: 12px;
  color: var(--text-muted);
}
</style>
