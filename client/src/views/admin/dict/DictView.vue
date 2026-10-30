<template>
  <div class="dict-manage">
    <div class="page-header">
      <h2>字典管理</h2>
      <button class="btn btn-primary" @click="showAddTypeDialog">+ 新增字典类型</button>
    </div>

    <SearchFilterBar
      :fields="typeSearchFields"
      :model="typeFilters"
      @search="handleTypeSearch"
      @reset="handleTypeReset"
    />

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th>字典名称</th>
            <th>字典编码</th>
            <th>备注</th>
            <th class="col-status">状态</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="type in dictTypes" :key="type.id">
            <td class="col-id">{{ type.id }}</td>
            <td><strong class="dict-name">{{ type.dictName }}</strong></td>
            <td><code class="dict-code">{{ type.dictType }}</code></td>
            <td class="remark-cell">{{ type.remark || '-' }}</td>
            <td class="col-status">
              <span class="tag" :class="type.status === 1 ? 'tag-success' : 'tag-danger'">
                {{ type.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="col-actions">
              <button class="btn-icon" @click="openDataDialog(type)">数据管理</button>
              <span class="action-divider"></span>
              <button class="btn-icon btn-secondary" @click="showEditTypeDialog(type)">编辑</button>
              <span class="action-divider"></span>
              <button class="btn-icon btn-danger" @click="handleDeleteType(type)">删除</button>
            </td>
          </tr>
          <tr v-if="dictTypes.length === 0">
            <td colspan="6" class="empty-row">
              {{ dictTypes.length === 0 ? '暂无字典类型' : '无匹配的字典类型' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <PaginationBar
      :total="typeTotal"
      :page="typePage"
      :page-size="typePageSize"
      @update:page="handleTypePageChange"
      @update:page-size="handleTypePageSizeChange"
    />

    <!-- 字典类型对话框 -->
    <div v-if="typeDialogVisible" class="dialog-mask" @click.self="closeTypeDialog">
      <div class="dialog">
        <h3>{{ isEditType ? '编辑字典类型' : '新增字典类型' }}</h3>
        <form @submit.prevent="handleSubmitType">
          <div class="form-item">
            <label>字典名称</label>
            <input v-model="typeForm.dictName" type="text" required placeholder="如：订单状态" />
          </div>
          <div class="form-item">
            <label>字典类型</label>
            <input v-model="typeForm.dictType" type="text" required placeholder="如：order_status" :disabled="isEditType" />
            <small v-if="isEditType" class="form-hint">字典编码创建后不可修改</small>
          </div>
          <div class="form-item">
            <label>备注</label>
            <textarea v-model="typeForm.remark" placeholder="备注说明"></textarea>
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="typeForm.status">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </div>
          <div class="dialog-footer">
            <button type="button" class="btn" @click="closeTypeDialog">取消</button>
            <button type="submit" class="btn btn-primary">确定</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 字典数据对话框 -->
    <div v-if="dataDialogVisible" class="dialog-mask" @click.self="closeDataDialog">
      <div class="dialog dialog-wide">
        <div class="dialog-head">
          <h3>字典数据 · {{ selectedType?.dictName }}</h3>
          <button class="btn btn-primary btn-sm" @click="showAddDataDialog">+ 新增数据</button>
        </div>

        <SearchFilterBar
          :fields="dataSearchFields"
          :model="dataFilters"
          @search="handleDataSearch"
          @reset="handleDataReset"
        />

        <table class="data-table compact">
          <thead>
            <tr>
              <th>标签</th>
              <th>键值</th>
              <th class="col-sort">排序</th>
              <th class="col-default">默认</th>
              <th class="col-status">状态</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="data in dictDataList" :key="data.id">
              <td>{{ data.dictLabel }}</td>
              <td><code class="dict-code">{{ data.dictValue }}</code></td>
              <td class="col-sort">{{ data.sortOrder }}</td>
              <td class="col-default">
                <span v-if="data.isDefault === 1" class="tag tag-primary">默认</span>
                <span v-else class="tag">-</span>
              </td>
              <td class="col-status">
                <span class="tag" :class="data.status === 1 ? 'tag-success' : 'tag-danger'">
                  {{ data.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td class="col-actions">
                <button class="btn-icon" @click="showEditDataDialog(data)">编辑</button>
                <span class="action-divider"></span>
                <button class="btn-icon btn-danger" @click="handleDeleteData(data)">删除</button>
              </td>
            </tr>
            <tr v-if="dictDataList.length === 0">
              <td colspan="6" class="empty-row">
                {{ dictDataList.length === 0 ? '暂无字典数据' : '无匹配的字典数据' }}
              </td>
            </tr>
          </tbody>
        </table>

        <PaginationBar
          :total="dataTotal"
          :page="dataPage"
          :page-size="dataPageSize"
          @update:page="handleDataPageChange"
          @update:page-size="handleDataPageSizeChange"
        />

        <div class="dialog-footer">
          <button type="button" class="btn" @click="closeDataDialog">关闭</button>
        </div>
      </div>
    </div>

    <!-- 字典数据表单对话框 -->
    <div v-if="dataFormVisible" class="dialog-mask" @click.self="closeDataForm">
      <div class="dialog">
        <h3>{{ isEditData ? '编辑字典数据' : '新增字典数据' }}</h3>
        <form @submit.prevent="handleSubmitData">
          <div class="form-item">
            <label>字典标签</label>
            <input v-model="dataForm.dictLabel" type="text" required placeholder="显示文本，如：待支付" />
          </div>
          <div class="form-item">
            <label>字典键值</label>
            <input v-model="dataForm.dictValue" type="text" required placeholder="存储值，如：0" />
          </div>
          <div class="form-row">
            <div class="form-item">
              <label>样式类名</label>
              <input v-model="dataForm.cssClass" type="text" placeholder="如：warning, success" />
            </div>
            <div class="form-item">
              <label>排序</label>
              <input v-model.number="dataForm.sortOrder" type="number" placeholder="数字越小越靠前" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-item">
              <label>是否默认</label>
              <select v-model="dataForm.isDefault">
                <option :value="1">是</option>
                <option :value="0">否</option>
              </select>
            </div>
            <div class="form-item">
              <label>状态</label>
              <select v-model="dataForm.status">
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </div>
          </div>
          <div class="form-item">
            <label>备注</label>
            <textarea v-model="dataForm.remark" placeholder="备注说明"></textarea>
          </div>
          <div class="dialog-footer">
            <button type="button" class="btn" @click="closeDataForm">取消</button>
            <button type="submit" class="btn btn-primary">确定</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDictTypeList, addDictType, updateDictType, deleteDictType } from '@/api'
import { getDictDataByType, addDictData, updateDictData, deleteDictData } from '@/api'
import SearchFilterBar from '@/components/SearchFilterBar.vue'
import PaginationBar from '@/components/PaginationBar.vue'

const dictTypes = ref([])
const selectedType = ref(null)
const dictDataList = ref([])

// 字典类型搜索条件（各条件独立控件，可组合使用）
const typeFilters = reactive({ dictName: '', dictType: '', status: '' })
const typeSearchFields = [
  { key: 'dictName', label: '字典名称', placeholder: '按名称搜索' },
  { key: 'dictType', label: '字典编码', placeholder: '按编码搜索' },
  {
    key: 'status', label: '状态', type: 'select',
    options: [
      { label: '全部', value: '' },
      { label: '启用', value: 1 },
      { label: '禁用', value: 0 }
    ]
  }
]

// 字典类型分页状态
const typePage = ref(1)
const typePageSize = ref(10)
const typeTotal = ref(0)

function resetTypeFilters() {
  typeFilters.dictName = ''
  typeFilters.dictType = ''
  typeFilters.status = ''
}

// 字典数据搜索条件
const dataFilters = reactive({ dictLabel: '', dictValue: '', status: '' })
const dataSearchFields = [
  { key: 'dictLabel', label: '标签', placeholder: '按标签搜索' },
  { key: 'dictValue', label: '键值', placeholder: '按键值搜索' },
  {
    key: 'status', label: '状态', type: 'select',
    options: [
      { label: '全部', value: '' },
      { label: '启用', value: 1 },
      { label: '禁用', value: 0 }
    ]
  }
]

// 字典数据分页状态
const dataPage = ref(1)
const dataPageSize = ref(10)
const dataTotal = ref(0)

function resetDataFilters() {
  dataFilters.dictLabel = ''
  dataFilters.dictValue = ''
  dataFilters.status = ''
}

// 字典类型对话框
const typeDialogVisible = ref(false)
const isEditType = ref(false)
const typeForm = ref({
  id: null,
  dictName: '',
  dictType: '',
  remark: '',
  status: 1
})

// 字典数据对话框
const dataDialogVisible = ref(false)
const dataFormVisible = ref(false)
const isEditData = ref(false)
const dataForm = ref({
  id: null,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  cssClass: '',
  sortOrder: 0,
  isDefault: 0,
  status: 1,
  remark: ''
})

const loadDictTypes = async () => {
  const params = {
    page: typePage.value,
    pageSize: typePageSize.value,
    dictName: typeFilters.dictName.trim(),
    dictType: typeFilters.dictType.trim(),
    status: typeFilters.status
  }
  try {
    const result = await getDictTypeList(params)
    dictTypes.value = result.list || []
    typeTotal.value = result.total || 0
    // 删除后当前页无数据时自动回退一页
    if (dictTypes.value.length === 0 && typeTotal.value > 0 && typePage.value > 1) {
      typePage.value = typePage.value - 1
      await loadDictTypes()
    }
  } catch (e) {
    console.error('加载字典类型失败:', e)
    dictTypes.value = []
    typeTotal.value = 0
  }
}

const loadDictData = async () => {
  if (!selectedType.value) return
  const params = {
    page: dataPage.value,
    pageSize: dataPageSize.value,
    dictLabel: dataFilters.dictLabel.trim(),
    dictValue: dataFilters.dictValue.trim(),
    status: dataFilters.status
  }
  try {
    const result = await getDictDataByType(selectedType.value.dictType, params)
    dictDataList.value = result.list || []
    dataTotal.value = result.total || 0
    // 删除后当前页无数据时自动回退一页
    if (dictDataList.value.length === 0 && dataTotal.value > 0 && dataPage.value > 1) {
      dataPage.value = dataPage.value - 1
      await loadDictData()
    }
  } catch (e) {
    console.error('加载字典数据失败:', e)
    dictDataList.value = []
    dataTotal.value = 0
  }
}

// ---- 分页交互：条件变化回到第一页 ----
function handleTypeSearch() {
  typePage.value = 1
  loadDictTypes()
}

function handleTypeReset() {
  resetTypeFilters()
  typePage.value = 1
  loadDictTypes()
}

function handleTypePageChange(target) {
  typePage.value = target
  loadDictTypes()
}

function handleTypePageSizeChange(size) {
  typePageSize.value = size
  typePage.value = 1
  loadDictTypes()
}

function handleDataSearch() {
  dataPage.value = 1
  loadDictData()
}

function handleDataReset() {
  resetDataFilters()
  dataPage.value = 1
  loadDictData()
}

function handleDataPageChange(target) {
  dataPage.value = target
  loadDictData()
}

function handleDataPageSizeChange(size) {
  dataPageSize.value = size
  dataPage.value = 1
  loadDictData()
}

// 打开某字典类型的数据管理弹窗
const openDataDialog = (type) => {
  selectedType.value = type
  resetDataFilters()
  dataPage.value = 1
  loadDictData()
  dataDialogVisible.value = true
}

const closeDataDialog = () => {
  dataDialogVisible.value = false
  selectedType.value = null
  dictDataList.value = []
}

// ---- 字典型操作 ----
const showAddTypeDialog = () => {
  isEditType.value = false
  typeForm.value = { id: null, dictName: '', dictType: '', remark: '', status: 1 }
  typeDialogVisible.value = true
}

const showEditTypeDialog = (type) => {
  isEditType.value = true
  typeForm.value = { ...type }
  typeDialogVisible.value = true
}

const closeTypeDialog = () => {
  typeDialogVisible.value = false
}

const handleSubmitType = async () => {
  try {
    if (isEditType.value) {
      await updateDictType(typeForm.value)
      alert('修改成功')
    } else {
      await addDictType(typeForm.value)
      alert('新增成功')
      // 新增记录排在列表末尾，跳到最后一页便于查看
      typePage.value = Math.max(1, Math.ceil((typeTotal.value + 1) / typePageSize.value))
    }
    closeTypeDialog()
    loadDictTypes()
  } catch (e) {
    alert(e.message)
  }
}

const handleDeleteType = async (type) => {
  if (!confirm(`确定要删除字典类型"${type.dictName}"吗？\n这将同时删除该类型下的所有字典数据！`)) return
  try {
    await deleteDictType(type.id)
    alert('删除成功')
    if (selectedType.value?.id === type.id) {
      closeDataDialog()
    }
    loadDictTypes()
  } catch (e) {
    alert(e.message)
  }
}

// ---- 字典数据操作 ----
const showAddDataDialog = () => {
  isEditData.value = false
  dataForm.value = {
    id: null,
    dictType: selectedType.value.dictType,
    dictLabel: '',
    dictValue: '',
    cssClass: '',
    sortOrder: 0,
    isDefault: 0,
    status: 1,
    remark: ''
  }
  dataFormVisible.value = true
}

const showEditDataDialog = (data) => {
  isEditData.value = true
  dataForm.value = { ...data }
  dataFormVisible.value = true
}

const closeDataForm = () => {
  dataFormVisible.value = false
}

const handleSubmitData = async () => {
  try {
    if (isEditData.value) {
      await updateDictData(dataForm.value)
      alert('修改成功')
    } else {
      await addDictData(dataForm.value)
      alert('新增成功')
      // 新增记录排在末尾（按排序值 + id），跳到最后一页便于查看
      dataPage.value = Math.max(1, Math.ceil((dataTotal.value + 1) / dataPageSize.value))
    }
    closeDataForm()
    loadDictData()
  } catch (e) {
    alert(e.message)
  }
}

const handleDeleteData = async (data) => {
  if (!confirm(`确定要删除字典数据"${data.dictLabel}"吗？`)) return
  try {
    await deleteDictData(data.id)
    alert('删除成功')
    loadDictData()
  } catch (e) {
    alert(e.message)
  }
}

onMounted(() => {
  loadDictTypes()
})
</script>

<style scoped>
.dict-manage {
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
  border: 1px solid var(--border);
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
  font-size: 14px;
}

.data-table th {
  background: var(--surface-2);
  font-weight: 600;
  font-size: 13px;
  color: var(--text-muted);
  white-space: nowrap;
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.data-table tbody tr:hover {
  background: var(--surface-2);
}

.data-table.compact th,
.data-table.compact td {
  padding: 9px 10px;
  font-size: 13px;
}

.col-id { width: 64px; color: var(--text-muted); }
.col-status { width: 96px; }
.col-sort { width: 80px; color: var(--text-muted); }
.col-default { width: 80px; }
.col-actions { width: 220px; white-space: nowrap; }

.dict-name { font-weight: 500; }

.dict-code {
  background: var(--surface-2);
  padding: 2px 6px;
  border-radius: 3px;
  font-family: monospace;
  font-size: 12px;
  color: var(--text-muted);
}

.remark-cell {
  color: var(--text-muted);
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

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

.tag-primary {
  background: var(--primary-soft);
  color: var(--primary);
}

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
  padding: 4px 12px;
  font-size: 12px;
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
  width: 860px;
  min-width: 0;
}

.dialog-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.dialog h3 {
  margin: 0 0 20px 0;
  font-size: 17px;
}

.dialog-head h3 {
  margin: 0;
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-item {
  margin-bottom: 16px;
  flex: 1;
}

.form-item label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  font-size: 13px;
}

.form-item input,
.form-item select,
.form-item textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: 4px;
  font-size: 14px;
  background: var(--card-bg);
  color: var(--text);
}

.form-item textarea {
  min-height: 80px;
  resize: vertical;
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
</style>
