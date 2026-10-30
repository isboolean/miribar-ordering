<template>
  <div class="org-manage">
    <div class="page-header">
      <h2>组织管理</h2>
      <button class="btn btn-primary" @click="showAddDialog(null)">新增顶级组织</button>
    </div>

    <div class="layout">
      <!-- 左侧：机构树 -->
      <div class="org-tree-panel">
        <div class="panel-header">
          <span>组织架构</span>
        </div>

        <div class="maintenance-bar">
          <label class="maintenance-toggle">
            <input type="checkbox" v-model="maintenance" />
            <span>组织维护</span>
          </label>
          <span class="maintenance-hint">{{ maintenance ? '节点后显示操作' : '仅浏览' }}</span>
        </div>

        <div class="tree-container">
          <div
            v-for="node in treeRows"
            :key="node.id"
            class="node-content"
            :class="{ active: selectedOrg?.id === node.id }"
            :style="{ paddingLeft: `${8 + node.depth * 18}px` }"
            @click="selectOrg(node)"
          >
            <span
              v-if="node.children?.length"
              class="node-toggle"
              :class="{ expanded: isExpanded(node.id) }"
              title="展开/收起"
              @click.stop="toggleExpand(node)"
            >▸</span>
            <span v-else class="node-toggle placeholder"></span>
            <span class="node-icon">{{ getOrgTypeIcon(node.orgType) }}</span>
            <span class="node-name">{{ node.orgName }}</span>
            <span v-if="maintenance" class="node-actions">
              <button class="node-act" title="查看" @click.stop="openDetail(node)">查</button>
              <button class="node-act" title="新增下级机构" @click.stop="showAddDialog(node)">加</button>
              <button class="node-act" title="编辑" @click.stop="showEditDialog(node)">改</button>
            </span>
          </div>
          <div v-if="orgTree.length === 0" class="empty">暂无组织数据</div>
        </div>
      </div>

      <!-- 右侧：下级机构列表 -->
      <div class="org-child-panel">
        <template v-if="selectedOrg">
          <div class="child-head">
            <div class="head-info">
              <h3>{{ selectedOrg.orgName }}</h3>
              <div class="head-sub">
                <code class="org-code">{{ selectedOrg.orgCode }}</code>
                <span>{{ getOrgTypeText(selectedOrg.orgType) }}</span>
                <span>下级机构 {{ childCount }} 个</span>
              </div>
            </div>
            <div class="head-actions">
              <button class="btn btn-sm" @click="openDetail(selectedOrg)">查看详情</button>
              <button class="btn btn-sm btn-primary" @click="showAddDialog(selectedOrg)">新增下级机构</button>
            </div>
          </div>

          <div class="child-body">
            <SearchFilterBar
              :fields="childSearchFields"
              :model="childFilters"
              @search="reloadTree"
              @reset="resetChildFilters"
            />

            <table class="data-table">
              <thead>
                <tr>
                  <th>机构名称</th>
                  <th>机构编码</th>
                  <th class="col-type">类型</th>
                  <th class="col-leader">负责人</th>
                  <th class="col-status">状态</th>
                  <th class="col-actions">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="child in filteredChildren" :key="child.id">
                  <td>
                    <span class="row-name" @click="selectOrg(child)">
                      <span class="row-icon">{{ getOrgTypeIcon(child.orgType) }}</span>
                      {{ child.orgName }}
                    </span>
                  </td>
                  <td><code class="org-code">{{ child.orgCode }}</code></td>
                  <td class="col-type">{{ getOrgTypeText(child.orgType) }}</td>
                  <td class="col-leader">{{ child.leader || '-' }}</td>
                  <td class="col-status">
                    <span class="tag" :class="child.status === 1 ? 'tag-success' : 'tag-danger'">
                      {{ child.status === 1 ? '启用' : '禁用' }}
                    </span>
                  </td>
                  <td class="col-actions">
                    <button class="btn-icon" @click="openDetail(child)">查看</button>
                    <span class="action-divider"></span>
                    <button class="btn-icon btn-secondary" @click="showEditDialog(child)">编辑</button>
                    <span class="action-divider"></span>
                    <button class="btn-icon btn-danger" @click="handleDelete(child)">删除</button>
                  </td>
                </tr>
                <tr v-if="filteredChildren.length === 0">
                  <td colspan="6" class="empty-row">
                    {{ childCount === 0 ? '该机构下暂无下级机构' : '无匹配的下级机构' }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>

        <div v-else class="empty-state">
          <div class="icon">🏢</div>
          <p>请从左侧选择一个机构查看下级机构</p>
        </div>
      </div>
    </div>

    <!-- 机构详情（查看） -->
    <div v-if="detailVisible" class="dialog-mask" @click.self="detailVisible = false">
      <div class="dialog dialog-wide">
        <div class="dialog-head">
          <h3>{{ detailOrg?.orgName }}</h3>
          <button class="btn btn-sm" @click="detailVisible = false">关闭</button>
        </div>
        <div class="detail-grid">
          <div class="info-item">
            <label>组织编码</label>
            <span><code class="org-code">{{ detailOrg?.orgCode }}</code></span>
          </div>
          <div class="info-item">
            <label>组织类型</label>
            <span>{{ getOrgTypeText(detailOrg?.orgType) }}</span>
          </div>
          <div class="info-item">
            <label>上级机构</label>
            <span>{{ parentNameOf(detailOrg) }}</span>
          </div>
          <div class="info-item">
            <label>负责人</label>
            <span>{{ detailOrg?.leader || '-' }}</span>
          </div>
          <div class="info-item">
            <label>联系电话</label>
            <span>{{ detailOrg?.phone || '-' }}</span>
          </div>
          <div class="info-item">
            <label>排序</label>
            <span>{{ detailOrg?.sortOrder ?? 0 }}</span>
          </div>
          <div class="info-item">
            <label>地址</label>
            <span>{{ detailOrg?.address || '-' }}</span>
          </div>
          <div class="info-item">
            <label>状态</label>
            <span class="tag" :class="detailOrg?.status === 1 ? 'tag-success' : 'tag-danger'">
              {{ detailOrg?.status === 1 ? '启用' : '禁用' }}
            </span>
          </div>
          <div class="info-item">
            <label>下级机构</label>
            <span>{{ detailOrg?.children?.length || 0 }} 个</span>
          </div>
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
              <option v-for="org in parentOptions" :key="org.id" :value="org.id">
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
            <input
              v-if="isEdit"
              :value="form.orgCode"
              type="text"
              readonly
              class="readonly"
              title="机构编码由系统生成，不可修改"
            />
            <div v-else class="form-hint">
              <span class="code-preview">{{ nextCodeHint }}</span>
              <span class="hint-text">由系统自动生成（10 位数字，顺序编号）</span>
            </div>
          </div>
          <div class="form-item">
            <label>组织类型</label>
            <select v-model="form.orgType" required>
              <option v-for="t in ORG_TYPES" :key="t.value" :value="t.value">{{ t.label }}</option>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { getOrgTree, getOrgList, addOrg, updateOrg, deleteOrg } from '@/api'
import SearchFilterBar from '@/components/SearchFilterBar.vue'

const orgTree = ref([])
const allOrgs = ref([])
const selectedOrg = ref(null)
const maintenance = ref(false)
const detailVisible = ref(false)
const detailOrg = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
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

// 下级机构搜索条件（各条件独立控件，可组合使用）
const childFilters = reactive({ orgName: '', orgCode: '' })
const childSearchFields = [
  { key: 'orgName', label: '机构名称', placeholder: '按机构名称搜索' },
  { key: 'orgCode', label: '机构编码', placeholder: '按机构编码搜索' }
]

const childCount = computed(() => selectedOrg.value?.children?.length || 0)

const filteredChildren = computed(() => {
  const name = childFilters.orgName.trim().toLowerCase()
  const code = childFilters.orgCode.trim().toLowerCase()
  return (selectedOrg.value?.children || []).filter(c => {
    if (name && !(c.orgName || '').toLowerCase().includes(name)) return false
    if (code && !(c.orgCode || '').toLowerCase().includes(code)) return false
    return true
  })
})

function resetChildFilters() {
  childFilters.orgName = ''
  childFilters.orgCode = ''
}

const reloadTree = async () => {
  const currentId = selectedOrg.value?.id
  orgTree.value = await getOrgTree()
  allOrgs.value = await getOrgList()
  if (currentId) {
    selectedOrg.value = findNodeById(orgTree.value, currentId) || null
  }
}

// 在树中按 id 查找节点（用于刷新后保持选中状态）
function findNodeById(nodes, id) {
  for (const node of nodes) {
    if (node.id === id) return node
    if (node.children?.length) {
      const hit = findNodeById(node.children, id)
      if (hit) return hit
    }
  }
  return null
}

// 组织类型：1=集团 2=总部 3=分部 4=区域 5=总部机构 6=省份 7=城市 8=店面
const ORG_TYPES = [
  { value: 1, label: '集团', icon: '🏢' },
  { value: 2, label: '总部', icon: '🏛️' },
  { value: 3, label: '分部', icon: '🌐' },
  { value: 4, label: '区域', icon: '📌' },
  { value: 5, label: '总部机构', icon: '🗂️' },
  { value: 6, label: '省份', icon: '🗺️' },
  { value: 7, label: '城市', icon: '🏙️' },
  { value: 8, label: '店面', icon: '🏪' }
]

// 新增子机构时按父级推导默认类型：集团下多为分部，总部下为总部机构，分部/区域下为省份
const CHILD_TYPE = { 1: 3, 2: 5, 3: 6, 4: 6, 5: 8, 6: 7, 7: 8, 8: 8 }

const typeMeta = (type) => ORG_TYPES.find(t => t.value === type) || { label: '未知', icon: '📍' }
const getOrgTypeIcon = (type) => typeMeta(type).icon
const getOrgTypeText = (type) => typeMeta(type).label

// 展开/收起状态：默认全部收起，点击节点或箭头才展开
const expandedIds = ref(new Set())

const isExpanded = (id) => expandedIds.value.has(id)

const toggleExpand = (org) => {
  const set = new Set(expandedIds.value)
  if (set.has(org.id)) {
    set.delete(org.id)
  } else {
    set.add(org.id)
  }
  expandedIds.value = set
}

const selectOrg = (org) => {
  selectedOrg.value = org
  // 有下级机构时，点击节点自动展开
  if (org.children?.length && !expandedIds.value.has(org.id)) {
    toggleExpand(org)
  }
  resetChildFilters()
}

const openDetail = (org) => {
  detailOrg.value = org
  detailVisible.value = true
}

const parentNameOf = (org) => {
  if (!org || !org.parentId) return '顶级组织'
  const parent = allOrgs.value.find(o => o.id === org.parentId)
  return parent ? parent.orgName : '顶级组织'
}

// 编辑时不可把上级设为自身或自身的下级，避免形成环
const isSelfOrDescendant = (candidateId, orgId) => {
  if (!orgId) return false
  if (candidateId === orgId) return true
  const byParent = new Map()
  for (const o of allOrgs.value) {
    const list = byParent.get(o.parentId) || []
    list.push(o)
    byParent.set(o.parentId, list)
  }
  const stack = [orgId]
  while (stack.length) {
    const current = stack.pop()
    for (const child of byParent.get(current) || []) {
      if (child.id === candidateId) return true
      stack.push(child.id)
    }
  }
  return false
}

const parentOptions = computed(() =>
  allOrgs.value.filter(o => !isSelfOrDescendant(o.id, form.value.id))
)

// 新增时预览下一个编码（后端最终生成，此处仅作提示）
const nextCodeHint = computed(() => {
  let max = 0
  for (const o of allOrgs.value) {
    const code = String(o.orgCode || '')
    if (/^\d{1,10}$/.test(code)) max = Math.max(max, Number(code))
  }
  return String(max + 1).padStart(10, '0')
})

const showAddDialog = (parent) => {
  isEdit.value = false
  form.value = {
    id: null,
    parentId: parent ? parent.id : 0,
    orgName: '',
    orgCode: '',
    orgType: parent ? (CHILD_TYPE[parent.orgType] || 1) : 1,
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
  form.value = {
    id: org.id,
    parentId: org.parentId ?? 0,
    orgName: org.orgName,
    orgCode: org.orgCode,
    orgType: org.orgType,
    leader: org.leader || '',
    phone: org.phone || '',
    address: org.address || '',
    sortOrder: org.sortOrder ?? 0,
    status: org.status
  }
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
    reloadTree()
  } catch (e) {
    alert(e.message)
  }
}

const handleDelete = async (org) => {
  // 前置校验：存在下级机构时不允许删除（后端同样有校验，此处提前给出明确提示）
  const children = org.children?.length || 0
  if (children > 0) {
    alert(`"${org.orgName}"下存在 ${children} 个下级机构，请先删除下级机构后再删除。`)
    return
  }
  if (!confirm(`确定要删除机构"${org.orgName}"吗？\n删除后不可恢复。`)) return
  try {
    await deleteOrg(org.id)
    alert('删除成功')
    if (detailOrg.value?.id === org.id) detailVisible.value = false
    await reloadTree()
  } catch (e) {
    alert(e.message)
  }
}

// 展平树为带层级的行（层级仅用于缩进展示；仅展开已点击展开的节点）
const treeRows = computed(() => {
  const rows = []
  const walk = (nodes, depth) => {
    for (const node of nodes) {
      rows.push({ ...node, depth })
      if (node.children?.length && expandedIds.value.has(node.id)) {
        walk(node.children, depth + 1)
      }
    }
  }
  walk(orgTree.value, 0)
  return rows
})

onMounted(() => {
  reloadTree()
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
  align-items: flex-start;
}

/* ===== 左侧机构树 ===== */
.org-tree-panel {
  width: 360px;
  flex-shrink: 0;
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 8px;
  overflow: hidden;
}

.panel-header {
  padding: 12px 16px;
  background: var(--surface-2);
  border-bottom: 1px solid var(--border);
  font-weight: 600;
  font-size: 14px;
}

.maintenance-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  border-bottom: 1px solid var(--border);
}

.maintenance-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  cursor: pointer;
  user-select: none;
}

.maintenance-toggle input {
  accent-color: var(--primary);
  cursor: pointer;
}

.maintenance-hint {
  font-size: 12px;
  color: var(--text-muted);
}

.tree-container {
  padding: 10px 8px;
  max-height: calc(100vh - 280px);
  overflow-y: auto;
}

.tree-node {
  margin: 2px 0;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background .15s;
}

.node-content:hover {
  background: var(--surface-2);
}

.node-content.active {
  background: var(--primary-soft);
  color: var(--primary);
}

.node-toggle {
  width: 14px;
  flex-shrink: 0;
  font-size: 11px;
  line-height: 1;
  color: var(--text-muted);
  cursor: pointer;
  user-select: none;
  transition: transform .15s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.node-toggle:hover {
  color: var(--primary);
}

.node-toggle.expanded {
  transform: rotate(90deg);
}

.node-toggle.placeholder {
  cursor: default;
}

.node-icon {
  font-size: 15px;
  flex-shrink: 0;
}

.node-name {
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.node-actions {
  display: flex;
  gap: 3px;
  margin-left: auto;
  flex-shrink: 0;
}

.node-act {
  width: 22px;
  height: 22px;
  padding: 0;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--card-bg);
  color: var(--text-muted);
  font-size: 11px;
  line-height: 1;
  cursor: pointer;
  transition: all .15s;
}

.node-act:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-soft);
}

/* ===== 右侧下级机构列表 ===== */
.org-child-panel {
  flex: 1;
  min-width: 0;
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 8px;
  overflow: hidden;
  min-height: calc(100vh - 160px);
}

.child-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  background: var(--surface-2);
  border-bottom: 1px solid var(--border);
}

.head-info h3 {
  margin: 0 0 4px 0;
  font-size: 17px;
}

.head-sub {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: var(--text-muted);
}

.head-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.child-body {
  padding: 16px;
}

.org-code {
  background: var(--surface-2);
  padding: 2px 6px;
  border-radius: 3px;
  font-family: monospace;
  font-size: 12px;
  color: var(--text-muted);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 11px 12px;
  text-align: left;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
}

.data-table th {
  background: var(--surface-2);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  white-space: nowrap;
}

.data-table tbody tr:hover {
  background: var(--surface-2);
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.row-name {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-weight: 500;
}

.row-name:hover {
  color: var(--primary);
}

.row-icon {
  font-size: 15px;
}

.col-type { width: 88px; color: var(--text-muted); }
.col-leader { width: 110px; color: var(--text-muted); }
.col-status { width: 88px; }
.col-actions { width: 168px; white-space: nowrap; }

.empty-row {
  text-align: center;
  padding: 40px !important;
  color: var(--text-muted);
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
  background: var(--surface-2);
  color: var(--text-muted);
}

.tag-success {
  background: rgba(22, 163, 74, .1);
  color: var(--success);
}

.tag-danger {
  background: rgba(239, 68, 68, .1);
  color: var(--danger);
}

/* ===== 按钮 ===== */
.btn {
  padding: 6px 16px;
  border: 1px solid var(--border);
  border-radius: 4px;
  cursor: pointer;
  background: var(--card-bg);
  color: var(--text);
  font-size: 14px;
}

.btn-primary {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.btn-sm {
  padding: 5px 12px;
  font-size: 13px;
}

.btn-icon {
  background: none;
  border: none;
  color: var(--primary);
  cursor: pointer;
  padding: 3px 6px;
  font-size: 13px;
}

.btn-icon:hover {
  color: var(--primary-light);
}

.btn-icon.btn-secondary {
  color: var(--text-muted);
}

.btn-icon.btn-secondary:hover {
  color: var(--primary);
}

.btn-icon.btn-danger {
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

/* ===== 对话框 ===== */
.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .5);
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
  max-height: 90vh;
  overflow-y: auto;
}

.dialog-wide {
  width: 620px;
  min-width: 0;
}

.dialog-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dialog h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
}

.dialog-head h3 {
  margin: 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.info-item label {
  color: var(--text-muted);
  min-width: 72px;
  flex-shrink: 0;
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
  background: var(--card-bg);
  color: var(--text);
}

.form-item input.readonly {
  background: var(--surface-2);
  color: var(--text-muted);
  cursor: not-allowed;
  font-family: monospace;
}

.form-hint {
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.empty {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: var(--text-muted);
}

.empty-state .icon {
  font-size: 64px;
  margin-bottom: 16px;
}
</style>
