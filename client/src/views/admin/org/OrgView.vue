<template>
  <div class="org-manage">
    <div class="page-header">
      <h2>组织管理</h2>
      <button class="btn btn-primary" @click="showAddDialog(null)">新增顶级组织</button>
    </div>

    <div class="layout">
      <!-- 左侧：组织树 -->
      <div class="org-tree-panel">
        <div class="panel-header">
          <span>组织架构</span>
        </div>
        <div class="tree-container">
          <div v-for="org in orgTree" :key="org.id" class="tree-node">
            <div 
              class="node-content" 
              :class="{ active: selectedOrg?.id === org.id }"
              @click="selectOrg(org)"
            >
              <span class="node-icon">{{ getOrgTypeIcon(org.orgType) }}</span>
              <span class="node-name">{{ org.orgName }}</span>
              <span class="node-code">[{{ org.orgCode }}]</span>
            </div>
            <div v-if="org.children && org.children.length > 0" class="tree-children">
              <OrgTreeNode 
                v-for="child in org.children" 
                :key="child.id" 
                :org="child"
                :depth="1"
                :selected-id="selectedOrg?.id"
                @select="selectOrg"
              />
            </div>
          </div>
          <div v-if="orgTree.length === 0" class="empty">暂无组织数据</div>
        </div>
      </div>

      <!-- 右侧：组织详情 -->
      <div class="org-detail-panel">
        <div v-if="selectedOrg" class="detail-content">
          <div class="detail-header">
            <h3>{{ selectedOrg.orgName }}</h3>
            <div class="detail-actions">
              <button class="btn btn-sm" @click="showAddDialog(selectedOrg)">添加子组织</button>
              <button class="btn btn-sm" @click="showEditDialog(selectedOrg)">编辑</button>
              <button class="btn btn-sm btn-danger" @click="handleDelete(selectedOrg)">删除</button>
            </div>
          </div>
          
          <div class="detail-info">
            <div class="info-item">
              <label>组织编码：</label>
              <span>{{ selectedOrg.orgCode }}</span>
            </div>
            <div class="info-item">
              <label>组织类型：</label>
              <span>{{ getOrgTypeText(selectedOrg.orgType) }}</span>
            </div>
            <div class="info-item">
              <label>负责人：</label>
              <span>{{ selectedOrg.leader || '-' }}</span>
            </div>
            <div class="info-item">
              <label>联系电话：</label>
              <span>{{ selectedOrg.phone || '-' }}</span>
            </div>
            <div class="info-item">
              <label>地址：</label>
              <span>{{ selectedOrg.address || '-' }}</span>
            </div>
            <div class="info-item">
              <label>状态：</label>
              <span :class="selectedOrg.status === 1 ? 'status-enabled' : 'status-disabled'">
                {{ selectedOrg.status === 1 ? '启用' : '禁用' }}
              </span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <div class="icon">🏢</div>
          <p>请从左侧选择一个组织查看详情</p>
        </div>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <div v-if="dialogVisible" class="dialog-mask" @click.self="closeDialog">
      <div class="dialog">
        <h3>{{ isEdit ? '编辑组织' : '新增组织' }}</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-item">
            <label>上级组织</label>
            <select v-model="form.parentId">
              <option :value="0">顶级组织</option>
              <option v-for="org in allOrgs" :key="org.id" :value="org.id">
                {{ org.orgName }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>组织名称</label>
            <input v-model="form.orgName" type="text" required placeholder="请输入组织名称" />
          </div>
          <div class="form-item">
            <label>组织编码</label>
            <input v-model="form.orgCode" type="text" required placeholder="请输入唯一编码（如 beijing）" />
          </div>
          <div class="form-item">
            <label>组织类型</label>
            <select v-model="form.orgType" required>
              <option :value="1">集团</option>
              <option :value="2">省份</option>
              <option :value="3">城市</option>
              <option :value="4">店面</option>
            </select>
          </div>
          <div class="form-item">
            <label>负责人</label>
            <input v-model="form.leader" type="text" placeholder="负责人姓名" />
          </div>
          <div class="form-item">
            <label>联系电话</label>
            <input v-model="form.phone" type="text" placeholder="联系电话" />
          </div>
          <div class="form-item">
            <label>地址</label>
            <input v-model="form.address" type="text" placeholder="详细地址" />
          </div>
          <div class="form-item">
            <label>排序</label>
            <input v-model.number="form.sortOrder" type="number" placeholder="数字越小越靠前" />
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
  </div>
</template>

<script setup>
import { ref, onMounted, defineComponent, h } from 'vue'
import { getOrgTree, addOrg, updateOrg, deleteOrg } from '@/api'

const orgTree = ref([])
const allOrgs = ref([])
const selectedOrg = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const parentOrg = ref(null)
const form = ref({
  id: null,
  parentId: 0,
  orgName: '',
  orgCode: '',
  orgType: 1,
  leader: '',
  phone: '',
  address: '',
  sortOrder: 0,
  status: 1
})

const loadData = async () => {
  orgTree.value = await getOrgTree()
  allOrgs.value = await getOrgList()
}

const getOrgTypeIcon = (type) => {
  const icons = { 1: '🏢', 2: '🗺️', 3: '🏙️', 4: '🏪' }
  return icons[type] || '📍'
}

const getOrgTypeText = (type) => {
  const texts = { 1: '集团', 2: '省份', 3: '城市', 4: '店面' }
  return texts[type] || '未知'
}

const selectOrg = (org) => {
  selectedOrg.value = org
}

const showAddDialog = (parent) => {
  isEdit.value = false
  parentOrg.value = parent
  form.value = {
    id: null,
    parentId: parent ? parent.id : 0,
    orgName: '',
    orgCode: '',
    orgType: parent ? Math.min((parent.orgType || 0) + 1, 4) : 1,
    leader: '',
    phone: '',
    address: '',
    sortOrder: 0,
    status: 1
  }
  dialogVisible.value = true
}

const showEditDialog = (org) => {
  isEdit.value = true
  parentOrg.value = null
  form.value = { ...org }
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await updateOrg(form.value)
      alert('修改成功')
    } else {
      await addOrg(form.value)
      alert('新增成功')
    }
    closeDialog()
    loadData()
  } catch (e) {
    alert(e.message)
  }
}

const handleDelete = async (org) => {
  if (!confirm(`确定要删除组织"${org.orgName}"吗？`)) return
  try {
    await deleteOrg(org.id)
    alert('删除成功')
    if (selectedOrg.value?.id === org.id) {
      selectedOrg.value = null
    }
    loadData()
  } catch (e) {
    alert(e.message)
  }
}

// 递归组织树节点组件
const OrgTreeNode = defineComponent({
  name: 'OrgTreeNode',
  props: {
    org: { type: Object, required: true },
    depth: { type: Number, default: 0 },
    selectedId: { type: Number, default: null }
  },
  emits: ['select'],
  setup(props, { emit }) {
    return () => {
      const children = []
      
      // 节点内容
      children.push(
        h('div', { 
          class: ['node-content', { active: props.selectedId === props.org.id }],
          style: { paddingLeft: `${props.depth * 20}px` },
          onClick: () => emit('select', props.org)
        }, [
          h('span', { class: 'node-icon' }, getOrgTypeIcon(props.org.orgType)),
          h('span', { class: 'node-name' }, props.org.orgName),
          h('span', { class: 'node-code' }, `[${props.org.orgCode}]`)
        ])
      )
      
      // 子节点
      if (props.org.children && props.org.children.length > 0) {
        children.push(
          h('div', { class: 'tree-children' }, 
            props.org.children.map(child => 
              h(OrgTreeNode, {
                key: child.id,
                org: child,
                depth: props.depth + 1,
                selectedId: props.selectedId,
                onSelect: (org) => emit('select', org)
              })
            )
          )
        )
      }
      
      return h('div', { class: 'tree-node' }, children)
    }
  }
})

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.org-manage {
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

.layout {
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 180px);
}

.org-tree-panel {
  width: 350px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  overflow: hidden;
}

.panel-header {
  padding: 12px 16px;
  background: #f5f5f5;
  border-bottom: 1px solid #eee;
  font-weight: 600;
}

.tree-container {
  padding: 12px;
  max-height: calc(100vh - 240px);
  overflow-y: auto;
}

.tree-node {
  margin: 2px 0;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all .2s;
}

.node-content:hover {
  background: #f0f0f0;
}

.node-content.active {
  background: #e6f7ff;
  color: #1890ff;
}

.node-icon {
  font-size: 16px;
}

.node-name {
  flex: 1;
  font-size: 14px;
}

.node-code {
  color: #999;
  font-size: 12px;
}

.tree-children {
  margin-left: 20px;
  border-left: 1px dashed #d9d9d9;
  padding-left: 8px;
}

.org-detail-panel {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  overflow: hidden;
}

.detail-content {
  padding: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.detail-header h3 {
  margin: 0;
  font-size: 18px;
}

.detail-actions {
  display: flex;
  gap: 8px;
}

.detail-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item label {
  font-weight: 500;
  color: #666;
  margin-right: 8px;
  min-width: 80px;
}

.info-item span {
  color: #333;
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
  padding: 4px 12px;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 400px;
  color: #999;
}

.empty-state .icon {
  font-size: 64px;
  margin-bottom: 16px;
}
</style>
