<template>
  <div class="customer-list">
    <div class="page-header">
      <h2>客户列表</h2>
      <div class="header-actions">
        <input 
          v-model="keyword" 
          type="text" 
          placeholder="搜索昵称/手机号/OpenID" 
          @keyup.enter="loadCustomers"
          class="search-input"
        />
        <button class="btn btn-primary" @click="showAddDialog">+ 新增客户</button>
      </div>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>头像</th>
          <th>昵称</th>
          <th>手机号</th>
          <th>OpenID</th>
          <th>注册时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="customer in customerList" :key="customer.id">
          <td>{{ customer.id }}</td>
          <td>
            <img 
              v-if="customer.avatarUrl" 
              :src="customer.avatarUrl" 
              alt="头像" 
              class="avatar"
            />
            <span v-else>-</span>
          </td>
          <td>{{ customer.nickname || '-' }}</td>
          <td>{{ customer.phone || '-' }}</td>
          <td><code class="openid">{{ customer.openid || '-' }}</code></td>
          <td>{{ formatDate(customer.createdAt) }}</td>
          <td>
            <button class="btn-icon" @click="showEditDialog(customer)">编辑</button>
            <button class="btn-icon btn-danger" @click="handleDelete(customer)">删除</button>
          </td>
        </tr>
        <tr v-if="customerList.length === 0">
          <td colspan="7" class="empty-row">暂无客户数据</td>
        </tr>
      </tbody>
    </table>

    <!-- 新增/编辑对话框 -->
    <div v-if="dialogVisible" class="dialog-mask" @click.self="closeDialog">
      <div class="dialog">
        <h3>{{ isEdit ? '编辑客户' : '新增客户' }}</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-item">
            <label>昵称</label>
            <input v-model="form.nickname" type="text" placeholder="客户昵称" />
          </div>
          <div class="form-item">
            <label>手机号</label>
            <input v-model="form.phone" type="text" placeholder="手机号" />
          </div>
          <div class="form-item">
            <label>OpenID</label>
            <input v-model="form.openid" type="text" placeholder="微信OpenID" />
          </div>
          <div class="form-item">
            <label>头像URL</label>
            <input v-model="form.avatarUrl" type="text" placeholder="头像图片链接" />
          </div>
          <div class="dialog-footer">
            <button type="button" class="btn" @click="closeDialog">取消</button>
            <button type="submit" class="btn btn-primary">确定</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCustomerList, addCustomer, updateCustomer, deleteCustomer } from '@/api'

const customerList = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({
  id: null,
  nickname: '',
  phone: '',
  openid: '',
  avatarUrl: ''
})

async function loadCustomers() {
  try {
    const params = {}
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    customerList.value = await getCustomerList(params)
  } catch (e) {
    console.error('加载客户列表失败:', e)
    customerList.value = []
  }
}

function showAddDialog() {
  isEdit.value = false
  form.value = { id: null, nickname: '', phone: '', openid: '', avatarUrl: '' }
  dialogVisible.value = true
}

function showEditDialog(customer) {
  isEdit.value = true
  form.value = {
    id: customer.id,
    nickname: customer.nickname || '',
    phone: customer.phone || '',
    openid: customer.openid || '',
    avatarUrl: customer.avatarUrl || ''
  }
  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
}

async function handleSubmit() {
  try {
    if (isEdit.value) {
      await updateCustomer(form.value)
      alert('修改成功')
    } else {
      await addCustomer(form.value)
      alert('新增成功')
    }
    closeDialog()
    loadCustomers()
  } catch (e) {
    alert(e.message)
  }
}

async function handleDelete(customer) {
  if (!confirm(`确定要删除客户"${customer.nickname || customer.phone}"吗？`)) return
  try {
    await deleteCustomer(customer.id)
    alert('删除成功')
    loadCustomers()
  } catch (e) {
    alert(e.message)
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadCustomers()
})
</script>

<style scoped>
.customer-list {
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

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.openid {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: monospace;
  font-size: 12px;
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

.form-item input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
</style>
