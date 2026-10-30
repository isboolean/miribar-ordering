<template>
  <div class="pagination-bar">
    <div class="pg-info">
      共 <strong>{{ total }}</strong> 条
      <span class="pg-divider">|</span>
      第 {{ page }} / {{ pages || 1 }} 页
    </div>

    <div class="pg-pages">
      <button class="pg-btn pg-nav" :disabled="page <= 1" @click="go(page - 1)">上一页</button>
      <button
        v-for="(item, index) in pageItems"
        :key="index"
        class="pg-btn"
        :class="{ active: item === page, ellipsis: item === '...' }"
        :disabled="item === '...'"
        @click="item !== '...' && go(item)"
      >
        {{ item }}
      </button>
      <button class="pg-btn pg-nav" :disabled="page >= pages || pages === 0" @click="go(page + 1)">下一页</button>
    </div>

    <div class="pg-size">
      <span class="pg-size-label">每页</span>
      <select class="pg-select" :value="pageSize" @change="changeSize($event)">
        <option v-for="size in pageSizes" :key="size" :value="size">{{ size }}</option>
      </select>
      <span class="pg-size-label">条</span>
    </div>
  </div>
</template>

<script setup>
// 通用分页条：总条数 / 页码切换 / 每页条数切换（10、15、30）
// 分页动作交给父组件处理，父组件拿到页码后重新请求接口
import { computed } from 'vue'

const props = defineProps({
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  pageSizes: { type: Array, default: () => [10, 15, 30] }
})

const emit = defineEmits(['update:page', 'update:pageSize'])

const pages = computed(() => (props.total <= 0 ? 0 : Math.ceil(props.total / props.pageSize)))

// 页码列表：超过 7 页时用省略号收拢中间部分
const pageItems = computed(() => {
  const totalPages = pages.value
  const current = props.page
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, i) => i + 1)
  }
  const items = [1]
  let start = Math.max(2, current - 1)
  let end = Math.min(totalPages - 1, current + 1)
  if (current <= 4) {
    start = 2
    end = 5
  }
  if (current >= totalPages - 3) {
    start = totalPages - 4
    end = totalPages - 1
  }
  if (start > 2) items.push('...')
  for (let i = start; i <= end; i++) items.push(i)
  if (end < totalPages - 1) items.push('...')
  items.push(totalPages)
  return items
})

function go(target) {
  if (target < 1 || target > pages.value || target === props.page) return
  emit('update:page', target)
}

function changeSize(event) {
  const size = Number(event.target.value)
  if (size === props.pageSize) return
  emit('update:pageSize', size)
}
</script>

<style scoped>
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px 16px;
  padding: 12px 14px;
  margin-top: 12px;
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-muted);
}

.pg-info strong {
  color: var(--text);
  font-weight: 600;
}

.pg-divider {
  margin: 0 6px;
  opacity: .5;
}

.pg-pages {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pg-btn {
  min-width: 30px;
  height: 30px;
  padding: 0 8px;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--card-bg);
  color: var(--text);
  font-size: 13px;
  cursor: pointer;
  transition: all .15s;
}

.pg-btn:hover:not(:disabled):not(.active) {
  border-color: var(--primary);
  color: var(--primary);
}

.pg-btn.active {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
  cursor: default;
}

.pg-btn:disabled {
  opacity: .45;
  cursor: not-allowed;
}

.pg-btn.ellipsis {
  border-color: transparent;
  background: transparent;
  cursor: default;
}

.pg-nav {
  padding: 0 10px;
}

.pg-size {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pg-select {
  height: 30px;
  padding: 0 6px;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--card-bg);
  color: var(--text);
  font-size: 13px;
  cursor: pointer;
}

.pg-select:focus {
  outline: none;
  border-color: var(--primary);
}
</style>
