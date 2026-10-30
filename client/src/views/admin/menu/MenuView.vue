<template>
  <div class="menu-manage">
    <div class="page-header">
      <h2>菜单管理</h2>
      <button class="btn btn-primary" @click="showAddDialog(null)">新增顶级菜单</button>
    </div>

    <div class="tree-container">
      <div v-for="menu in menuTree" :key="menu.id" class="tree-node">
        <div class="node-content">
          <span class="node-icon">{{ getMenuTypeIcon(menu.menuType) }}</span>
          <span class="node-name">{{ menu.menuName }}</span>
          <span v-if="menu.permissionCode" class="node-permission">[{{ menu.permissionCode }}]</span>
          <span class="node-actions">
            <button class="btn btn-sm" @click="showAddDialog(menu)">添加子菜单</button>
            <button class="btn btn-sm" @click="showEditDialog(menu)">编辑</button>
            <button class="btn btn-sm btn-danger" @click="handleDelete(menu)">删除</button>
          </span>
        </div>
        <div v-if="menu.children && menu.children.length > 0" class="tree-children">
          <MenuTreeNode 
            v-for="child in menu.children" 
            :key="child.id" 
            :menu="child"
            :depth="1"
            @add="showAddDialog"
            @edit="showEditDialog"
            @delete="handleDelete"
          />
        </div>
      </div>
      <div v-if="menuTree.length === 0" class="empty">暂无菜单数据</div>
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
            <input v-model="form.component" type="text" placeholder="如：staff/ StaffList" />
          </div>
          <div v-if="form.menuType === 2" class="form-item">
            <label>权限编码</label>
            <input v-model="form.permissionCode" type="text" placeholder="如：staff:add" />
          </div>
          <div class="form-item">
            <label>图标</label>
            <input v-model="form.icon" type="text" placeholder="图标名称" />
          </div>
          <div class="form-item">
            <label>排序</label>
            <input v-model.number="form.sortOrder" type="number" placeholder="数字越小越靠前" />
          </div>
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
import { getMenuList, addMenu, updateMenu, deleteMenu } from '@/api'

const menuTree = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const parentMenu = ref(null)
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

const loadData = async () => {
  menuTree.value = await getMenuList()
}

const getMenuTypeIcon = (type) => {
  const icons = { 0: '📁', 1: '📄', 2: '🔘' }
  return icons[type] || '❓'
}

const showAddDialog = (parent) => {
  isEdit.value = false
  parentMenu.value = parent
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
  parentMenu.value = null
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
  if (!confirm(`确定要删除菜单"${menu.menuName}"吗？`)) return
  try {
    await deleteMenu(menu.id)
    alert('删除成功')
    loadData()
  } catch (e) {
    alert(e.message)
  }
}

// 递归菜单树节点组件
const MenuTreeNode = defineComponent({
  name: 'MenuTreeNode',
  props: {
    menu: { type: Object, required: true },
    depth: { type: Number, default: 0 }
  },
  emits: ['add', 'edit', 'delete'],
  setup(props, { emit }) {
    return () => {
      const children = []
      
      // 节点内容
      children.push(
        h('div', { class: 'node-content', style: { paddingLeft: `${props.depth * 24}px` } }, [
          h('span', { class: 'node-icon' }, getMenuTypeIcon(props.menu.menuType)),
          h('span', { class: 'node-name' }, props.menu.menuName),
          props.menu.permissionCode ? h('span', { class: 'node-permission' }, `[${props.menu.permissionCode}] `) : null,
          h('span', { class: 'node-actions' }, [
            h('button', { class: 'btn btn-sm', onClick: () => emit('add', props.menu) }, '添加子菜单'),
            h('button', { class: 'btn btn-sm', onClick: () => emit('edit', props.menu) }, '编辑'),
            h('button', { class: 'btn btn-sm btn-danger', onClick: () => emit('delete', props.menu) }, '删除')
          ])
        ])
      )
      
      // 子节点
      if (props.menu.children && props.menu.children.length > 0) {
        children.push(
          h('div', { class: 'tree-children' }, 
            props.menu.children.map(child => 
              h(MenuTreeNode, {
                key: child.id,
                menu: child,
                depth: props.depth + 1,
                onAdd: (m) => emit('add', m),
                onEdit: (m) => emit('edit', m),
                onDelete: (m) => emit('delete', m)
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
.menu-manage {
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

.tree-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.tree-node {
  margin: 4px 0;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fafafa;
  border-radius: 4px;
}

.node-icon {
  font-size: 16px;
}

.node-name {
  font-weight: 500;
  flex: 1;
}

.node-permission {
  color: #1890ff;
  font-size: 12px;
}

.node-actions {
  display: flex;
  gap: 4px;
}

.tree-children {
  margin-left: 24px;
  border-left: 1px dashed #d9d9d9;
  padding-left: 12px;
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

.empty {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
