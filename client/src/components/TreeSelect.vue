<template>
  <div ref="root" class="ts" :style="{ width: width || null }">
    <button type="button" class="ts-trigger" :class="{ open }" @click="open = !open">
      <span class="ts-label" :class="{ placeholder: !selectedLabel }">{{ selectedLabel || placeholder }}</span>
      <span class="ts-caret">▾</span>
    </button>

    <div v-if="open" class="ts-panel">
      <div
        v-if="allLabel"
        class="ts-row ts-all"
        :class="{ active: modelValue === allValue }"
        @click="pick(allValue, allLabel)"
      >
        <span class="ts-arrow is-empty"></span>{{ allLabel }}
      </div>
      <div
        v-for="row in visibleRows"
        :key="row.id"
        class="ts-row"
        :class="{ active: row.id === modelValue }"
        :style="{ paddingLeft: `${10 + row.depth * 16}px` }"
        @click="pick(row.id, row.label)"
      >
        <span
          v-if="row.hasChildren"
          class="ts-arrow"
          :class="{ expanded: expandedIds.has(row.id) }"
          @click.stop="toggle(row.id)"
        >›</span>
        <span v-else class="ts-arrow is-empty"></span>
        <span v-if="iconOf" class="ts-icon">{{ iconOf(row.raw) }}</span>
        <span class="ts-text">{{ row.label }}</span>
      </div>
      <div v-if="visibleRows.length === 0 && !allLabel" class="ts-empty">暂无数据</div>
    </div>
  </div>
</template>

<script setup>
// 树形下拉：默认只显示各层级第一层，点击箭头逐层展开，点击行选中并收起面板
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  modelValue: { type: [String, Number], default: '' },
  nodes: { type: Array, default: () => [] },
  // 顶部「全部」项；传 null 表示不显示
  allLabel: { type: String, default: null },
  allValue: { type: [String, Number], default: '' },
  placeholder: { type: String, default: '请选择' },
  width: { type: String, default: null },
  // 可选：节点图标函数，接收原始节点对象
  iconOf: { type: Function, default: null },
  // 节点名称取值字段
  labelKey: { type: String, default: 'orgName' }
})

const emit = defineEmits(['update:modelValue'])

const root = ref(null)
const open = ref(false)
const expandedIds = ref(new Set())

const hasChildren = (n) => Array.isArray(n.children) && n.children.length > 0

const toggle = (id) => {
  const next = new Set(expandedIds.value)
  next.has(id) ? next.delete(id) : next.add(id)
  expandedIds.value = next
}

// 只递归已展开节点的 children
const visibleRows = computed(() => {
  const rows = []
  const walk = (nodes, depth) => {
    for (const n of nodes) {
      rows.push({ id: n.id, label: n[props.labelKey], raw: n, depth, hasChildren: hasChildren(n) })
      if (hasChildren(n) && expandedIds.value.has(n.id)) walk(n.children, depth + 1)
    }
  }
  walk(props.nodes, 0)
  return rows
})

const selectedLabel = computed(() => {
  if (props.modelValue === props.allValue) return props.allLabel
  const find = (nodes) => {
    for (const n of nodes) {
      if (n.id === props.modelValue) return n[props.labelKey]
      if (hasChildren(n)) {
        const hit = find(n.children)
        if (hit) return hit
      }
    }
    return null
  }
  return find(props.nodes)
})

const pick = (value, label) => {
  emit('update:modelValue', value)
  open.value = false
}

const onDocClick = (e) => {
  if (root.value && !root.value.contains(e.target)) open.value = false
}
onMounted(() => document.addEventListener('mousedown', onDocClick))
onBeforeUnmount(() => document.removeEventListener('mousedown', onDocClick))
</script>

<style scoped>
.ts {
  position: relative;
  display: inline-block;
}

.ts-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  width: 100%;
  padding: 7px 10px;
  border: 1px solid var(--border);
  border-radius: 4px;
  font-size: 13px;
  background: var(--card-bg);
  color: var(--text);
  cursor: pointer;
  transition: border-color .15s, box-shadow .15s;
}

.ts-trigger.open,
.ts-trigger:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-soft);
}

.ts-label {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.ts-label.placeholder {
  color: var(--text-muted);
}

.ts-caret {
  color: var(--text-muted);
  font-size: 11px;
  flex-shrink: 0;
  transition: transform .15s;
}

.ts-trigger.open .ts-caret {
  transform: rotate(180deg);
}

.ts-panel {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  min-width: 100%;
  max-height: 280px;
  overflow-y: auto;
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 6px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, .12);
  z-index: 60;
  padding: 4px;
}

.ts-row {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 30px;
  padding-right: 10px;
  font-size: 13px;
  color: var(--text);
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
  transition: background .12s;
}

.ts-row:hover {
  background: var(--surface-2);
}

.ts-row.active {
  color: var(--primary);
  background: var(--primary-soft);
  font-weight: 600;
}

.ts-all {
  font-weight: 600;
  border-bottom: 1px dashed var(--border);
  border-radius: 4px 4px 0 0;
  margin-bottom: 2px;
}

.ts-arrow {
  width: 14px;
  height: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1;
  border-radius: 3px;
  transition: transform .15s, background .12s;
  flex-shrink: 0;
  user-select: none;
}

.ts-arrow:hover {
  background: var(--surface-2);
  color: var(--text);
}

.ts-arrow.expanded {
  transform: rotate(90deg);
}

.ts-arrow.is-empty {
  cursor: default;
}

.ts-icon {
  flex-shrink: 0;
}

.ts-text {
  overflow: hidden;
  text-overflow: ellipsis;
}

.ts-empty {
  padding: 12px;
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
}
</style>
