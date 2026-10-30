<template>
  <div class="dict-manage">
    <div class="page-header">
      <h2>字典管理</h2>
    </div>

    <div class="layout">
      <!-- 左侧：字典类型列表 -->
      <div class="dict-type-panel">
        <div class="panel-header">
          <span>字典类型</span>
          <button class="btn btn-sm btn-primary" @click="showAddTypeDialog">新增</button>
        </div>
        <div class="type-list">
          <div 
            v-for="type in dictTypes" 
            :key="type.id" 
            class="type-item"
            :class="{ active: selectedType?.id === type.id }"
            @click="selectType(type)"
          >
            <div class="type-info">
              <div class="type-name">{{ type.dictName }}</div>
              <div class="type-code">{{ type.dictType }}</div>
            </div>
            <div class="type-actions">
              <button class="btn-icon" @click.stop="showEditTypeDialog(type)">编辑</button>
              <button class="btn-icon btn-danger" @click.stop="handleDeleteType(type)">删除</button>
            </div>
          </div>
          <div v-if="dictTypes.length === 0" class="empty">暂无字典类型</div>
        </div>
      </div>

      <!-- 右侧：字典数据列表 -->
      <div class="dict-data-panel">
        <div class="panel-header">
          <span>字典数据 {{ selectedType ? `- ${selectedType.dictName}` : '' }}</span>
          <button v-if="selectedType" class="btn btn-sm btn-primary" @click="showAddDataDialog">新增</button>
        </div>
        
        <div v-if="selectedType" class="data-table">
          <table>
            <thead>
              <tr>
                <th>标签</th>
                <th>键值</th>
                <th>排序</th>
                <th>默认</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="data in dictDataList" :key="data.id">
                <td>{{ data.dictLabel }}</td>
                <td><code>{{ data.dictValue }}</code></td>
                <td>{{ data.sortOrder }}</td>
                <td>
                  <span v-if="data.isDefault === 1" class="tag tag-primary">默认</span>
                  <span v-else class="tag">-</span>
                </td>
                <td>
                  <span :class="data.status === 1 ? 'tag tag-success' : 'tag tag-danger'">
                    {{ data.status === 1 ? '启用' : '禁用' }}
                  </span>
                </td>
                <td>
                  <button class="btn-icon" @click="showEditDataDialog(data)">编辑</button>
                  <button class="btn-icon btn-danger" @click="handleDeleteData(data)">删除</button>
                </td>
              </tr>
              <tr v-if="dictDataList.length === 0">
                <td colspan="6" class="empty">暂无字典数据</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <div v-else class="empty-state">
          <div class="icon">📋</div>
          <p>请从左侧选择一个字典类型查看数据</p>
        </div>
      </div>
    </div>

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
          <div class="form-item">
            <label>样式类名</label>
            <input v-model="dataForm.cssClass" type="text" placeholder="如：warning, success, danger" />
          </div>
          <div class="form-item">
            <label>排序</label>
            <input v-model.number="dataForm.sortOrder" type="number" placeholder="数字越小越靠前" />
          </div>
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
          <div class="form-item">
            <label>备注</label>
            <textarea v-model="dataForm.remark" placeholder="备注说明"></textarea>
          </div>
          <div class="dialog-footer">
            <button type="button" class="btn" @click="closeDataDialog">取消</button>
            <button type="submit" class="btn btn-primary">确定</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDictTypeList, addDictType, updateDictType, deleteDictType } from '@/api'
import { getDictDataByType, addDictData, updateDictData, deleteDictData } from '@/api'

const dictTypes = ref([])
const selectedType = ref(null)
const dictDataList = ref([])

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
  dictTypes.value = await getDictTypeList()
}

const loadDictData = async () => {
  if (selectedType.value) {
    dictDataList.value = await getDictDataByType(selectedType.value.dictType)
  }
}

const selectType = (type) => {
  selectedType.value = type
  loadDictData()
}

// 字典类型操作
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
      selectedType.value = null
      dictDataList.value = []
    }
    loadDictTypes()
  } catch (e) {
    alert(e.message)
  }
}

// 字典数据操作
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
  dataDialogVisible.value = true
}

const showEditDataDialog = (data) => {
  isEditData.value = true
  dataForm.value = { ...data }
  dataDialogVisible.value = true
}

const closeDataDialog = () => {
  dataDialogVisible.value = false
}

const handleSubmitData = async () => {
  try {
    if (isEditData.value) {
      await updateDictData(dataForm.value)
      alert('修改成功')
    } else {
      await addDictData(dataForm.value)
      alert('新增成功')
    }
    closeDataDialog()
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

.layout {
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 180px);
}

.dict-type-panel {
  width: 320px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  overflow: hidden;
}

.panel-header {
  padding: 12px 16px;
  background: #f5f5f5;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header span {
  font-weight: 600;
}

.type-list {
  padding: 8px;
  max-height: calc(100vh - 240px);
  overflow-y: auto;
}

.type-item {
  padding: 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all .2s;
  margin-bottom: 4px;
}

.type-item:hover {
  background: #f0f0f0;
}

.type-item.active {
  background: #e6f7ff;
  border-left: 3px solid #1890ff;
}

.type-info {
  margin-bottom: 8px;
}

.type-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.type-code {
  font-size: 12px;
  color: #999;
  font-family: monospace;
}

.type-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity .2s;
}

.type-item:hover .type-actions {
  opacity: 1;
}

.dict-data-panel {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  overflow: hidden;
}

.data-table {
  padding: 16px;
}

.data-table table {
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
  background: #fafafa;
  font-weight: 600;
}

.data-table code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: monospace;
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

.btn-icon {
  background: none;
  border: none;
  color: #1890ff;
  cursor: pointer;
  padding: 4px 8px;
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

.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
  background: #f5f5f5;
}

.tag-primary {
  background: #e6f7ff;
  color: #1890ff;
}

.tag-success {
  background: #f6ffed;
  color: #52c41a;
}

.tag-danger {
  background: #fff2f0;
  color: #f5222d;
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
.form-item select,
.form-item textarea {
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
