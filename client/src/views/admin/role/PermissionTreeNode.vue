<template>
  <div class="perm-node">
    <div
      class="perm-row"
      :class="{ 'is-dir': node.menuType === 0 }"
      :style="{ paddingLeft: `${6 + depth * 18}px` }"
    >
      <span
        v-if="hasChildren"
        class="perm-arrow"
        :class="{ expanded }"
        @click.stop="expanded = !expanded"
      >›</span>
      <span v-else class="perm-arrow is-empty"></span>

      <label class="perm-check">
        <input
          type="checkbox"
          :checked="checked"
          @change="onToggle"
        />
      </label>

      <span class="perm-icon" :class="`t-${node.menuType}`">{{ iconText }}</span>
      <span class="perm-name" :title="node.menuName" @click="onToggle">{{ node.menuName }}</span>
      <span v-if="hasChildren" class="perm-count">{{ visibleCount }}</span>
    </div>

    <div v-if="hasChildren && expanded" class="perm-children">
      <PermissionTreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :depth="depth + 1"
        :checked-ids="checkedIds"
        @toggle="(target, value) => emit('toggle', target, value)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  node: { type: Object, required: true },
  depth: { type: Number, default: 0 },
  checkedIds: { type: Array, default: () => [] }
})
const emit = defineEmits(['toggle'])

// 默认收起，点击箭头逐层展开
const expanded = ref(false)
const hasChildren = computed(() => Array.isArray(props.node.children) && props.node.children.length > 0)
const checked = computed(() => props.checkedIds.includes(props.node.id))
const iconText = computed(() => ({ 0: '目', 1: '菜', 2: '按' }[props.node.menuType] ?? '·'))

// 子项数量：目录显示下级数，便于判断是否需要展开
const visibleCount = computed(() => {
  const count = (nodes) => nodes.reduce((sum, n) => sum + 1 + (n.children?.length ? count(n.children) : 0), 0)
  return count(props.node.children || [])
})

function onToggle() {
  emit('toggle', props.node, !checked.value)
}
</script>

<style scoped>
.perm-row {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 30px;
  padding-right: 10px;
  border-radius: 4px;
  transition: background .15s;
}

.perm-row:hover {
  background: var(--surface-2);
}

.perm-arrow {
  width: 14px;
  height: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1;
  cursor: pointer;
  transition: transform .15s;
  flex-shrink: 0;
  user-select: none;
}

.perm-arrow.expanded {
  transform: rotate(90deg);
}

.perm-arrow.is-empty {
  cursor: default;
}

.perm-check {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  cursor: pointer;
}

.perm-check input {
  width: 14px;
  height: 14px;
  accent-color: var(--primary);
  cursor: pointer;
  margin: 0;
}

.perm-icon {
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.perm-icon.t-0 {
  background: var(--primary-soft);
  color: var(--primary);
}

.perm-icon.t-1 {
  background: rgba(22, 163, 74, .1);
  color: var(--success);
}

.perm-icon.t-2 {
  background: var(--surface-2);
  color: var(--text-muted);
}

.perm-name {
  font-size: 13px;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}

.perm-row.is-dir .perm-name {
  font-weight: 600;
}

.perm-count {
  margin-left: auto;
  font-size: 11px;
  color: var(--text-muted);
  background: var(--surface-2);
  border-radius: 8px;
  padding: 0 6px;
  line-height: 16px;
  flex-shrink: 0;
}

.perm-children {
  margin-left: 13px;
  border-left: 1px dashed var(--border);
  padding-left: 2px;
}
</style>
