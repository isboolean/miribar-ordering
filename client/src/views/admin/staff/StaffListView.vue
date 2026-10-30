<template>
  <div class="staff-list">
    <div class="page-header">
      <h2>员工列表</h2>
      <div class="header-actions">
        <input 
          v-model="keyword" 
          type="text" 
          placeholder="搜索姓名/账号/手机号" 
          @keyup.enter="loadStaff"
          class="search-input"
        />
        <button class="btn btn-primary" @click="showAddDialog">+ 新增员工</button>
      </div>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>账号</th>
          <th>姓名</th>
          <th>手机号</th>
          <th>角色</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="staff in staffList" :key="staff.id">
          <td>{{ staff.id }}</td>
          <td>{{ staff.username }}</td>
          <td>{{ staff.realName || '-' }}</td>
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
            <button class="btn-icon" @click="handleResetPassword(staff)">重置密码</button>
            <button class="btn-icon" @click="showRoleDialog(staff)">分配角色</button>
            <button class="btn-icon btn-danger" @click="handleDelete(staff)">删除</button>
          </td>
        </tr>
        <tr v-if="staffList.length === 0">
          <td colspan="7" class="empty-row">暂无员工数据</td>
        </tr>
      </tbody>
    </table>

    <!-- 新增/编辑对话框 -->
    <div v-if="dialogVisible" class="dialog-mask" @click.self="closeDialog">
      <div class="dialog">
        <h3>{{ isEdit ? '编辑员工' : '新增员工' }}</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-item">
            <label>账号</label>
            <input v-model="form.username" type="text" required :disabled="isEdit" placeholder="登录账号" />
          </div>
          <div class="form-item">
            <label>姓名</label>
            <input v-model="form.realName" type="text" placeholder="真实姓名" />
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
import { ref, onMounted } from 'vue'
import { getStaffList, addStaff, updateStaff, deleteStaff, resetStaffPassword, assignStaffRoles, getStaffRoles } from '@/api'
import { getRoleList } from '@/api'

const staffList = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({
  id: null,
  username: '',
  realName: '',
  phone: '',
  status: 1
})

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

async function loadStaff() {
  try {
    const params = {}
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    staffList.value = await getStaffList(params)
  } catch (e) {
    console.error('加载员工列表失败:', e)
    staffList.value = []
  }
}

function showAddDialog() {
  isEdit.value = false
  form.value = { id: null, username: '', realName: '', phone: '', status: 1 }
  dialogVisible.value = true
}

function showEditDialog(staff) {
  isEdit.value = true
  form.value = {
    id: staff.id,
    username: staff.username,
    realName: staff.realName || '',
    phone: staff.phone || '',
    status: staff.status
  }
  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
}

async function handleSubmit() {
  try {
    if (isEdit.value) {
      await updateStaff(form.value)
      alert('修改成功')
    } else {
      await addStaff(form.value)
      alert('新增成功')
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
  // 加载所有角色
  allRoles.value = await getRoleList()
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

onMounted(() => {
  loadStaff()
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
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  width: 250px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background: #fafafa;
  font-weight: 600;
}

.empty-row {
  text-align: center;
  padding: 40px !important;
  color: #999;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
  background: #f5f5f5;
}

.tag-success {
  background: #f6ffed;
  color: #52c41a;
}

.tag-danger {
  background: #fff2f0;
  color: #f5222d;
}

.tag-primary {
  background: #e6f7ff;
  color: #1890ff;
}

.tag-info {
  background: #f0f5ff;
  color: #2f54eb;
}

.tag-warning {
  background: #fffbe6;
  color: #fa8c16;
}

.btn {
  padding: 6px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  background: #fff;
}

.btn-primary {
  background: #1890ff;
  color: #fff;
  border-color: #1890ff;
}

.btn-icon {
  background: none;
  border: none;
  color: #1890ff;
  cursor: pointer;
  padding: 4px 8px;
  font-size: 13px;
}

.btn-icon:hover {
  color: #40a9ff;
}

.btn-icon.btn-danger {
  color: #f5222d;
}

.btn-icon.btn-danger:hover {
  color: #ff4d4f;
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
  background: #fff;
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
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

.form-hint {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #999;
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
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
}

.role-item:hover {
  background: #fafafa;
}

.role-name {
  font-weight: 500;
  flex: 1;
}

.role-desc {
  font-size: 12px;
  color: #999;
}
</style>
