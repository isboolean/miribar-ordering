<template>
  <div class="role-manage">
    <div class="page-header">
      <h2>角色管理</h2>
      <button class="btn btn-primary" @click="showAddDialog">新增角色</button>
    </div>

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
        <h3>分配权限 - {{ currentRole?.roleName }}</h3>
        <div class="menu-tree-container">
          <div v-for="menu in menuTree" :key="menu.id" class="menu-tree-item">
            <label>
              <input 
                type="checkbox" 
                :value="menu.id" 
                v-model="selectedMenuIds"
                @change="handleMenuCheck(menu)"
              />
              {{ menu.menuName }}
            </label>
            <div v-if="menu.children && menu.children.length > 0" class="menu-children">
              <div v-for="child in menu.children" :key="child.id" class="menu-tree-item">
                <label>
                  <input 
                    type="checkbox" 
                    :value="child.id" 
                    v-model="selectedMenuIds"
                    @change="handleMenuCheck(child)"
                  />
                  {{ child.menuName }}
                </label>
                <div v-if="child.children && child.children.length > 0" class="menu-children">
                  <label v-for="grandChild in child.children" :key="grandChild.id" class="menu-tree-item">
                    <input 
                      type="checkbox" 
                      :value="grandChild.id" 
                      v-model="selectedMenuIds"
                    />
                    {{ grandChild.menuName }}
                    <span v-if="grandChild.permissionCode" class="permission-code">({{ grandChild.permissionCode }})</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
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
import { ref, onMounted } from 'vue'
import { getRoleList, addRole, updateRole, deleteRole, assignRoleMenus, getRoleMenus, getMenuList } from '@/api'

const roles = ref([])
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
  roles.value = await getRoleList()
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

const handleMenuCheck = (menu) => {
  // 如果选中父菜单，自动选中子菜单
  if (menu.children && menu.children.length > 0) {
    const checkAll = (items) => {
      for (const item of items) {
        if (!selectedMenuIds.value.includes(item.id)) {
          selectedMenuIds.value.push(item.id)
        }
        if (item.children) {
          checkAll(item.children)
        }
      }
    }
    checkAll(menu.children)
  }
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
  background: #fff;
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
  border-bottom: 1px solid #eee;
}

.data-table th {
  background: #f5f5f5;
  font-weight: 600;
}

.status-enabled {
  color: #52c41a;
}

.status-disabled {
  color: #f5222d;
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

.btn-danger {
  color: #f5222d;
  border-color: #f5222d;
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
  background: #fff;
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
  border: 1px solid #d9d9d9;
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
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 12px;
}

.menu-tree-item {
  padding: 6px 0;
}

.menu-tree-item label {
  cursor: pointer;
}

.menu-children {
  margin-left: 24px;
  border-left: 1px dashed #d9d9d9;
  padding-left: 12px;
}

.permission-code {
  color: #999;
  font-size: 12px;
  margin-left: 4px;
}

.empty {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
