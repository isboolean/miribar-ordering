<template>
  <div class="sf-bar">
    <div v-for="f in fields" :key="f.key" class="sf-item">
      <span v-if="f.label" class="sf-label">{{ f.label }}</span>
      <input
        v-if="!f.type || f.type === 'text'"
        class="sf-input"
        :class="f.inputClass"
        :style="f.width ? { width: f.width } : null"
        type="text"
        v-model="model[f.key]"
        :placeholder="f.placeholder || ('请输入' + f.label)"
        @keyup.enter="emit('search')"
      />
      <TreeSelect
        v-else-if="f.type === 'tree-select'"
        :model-value="model[f.key]"
        @update:model-value="(val) => { model[f.key] = val; emit('search') }"
        :nodes="f.nodes || []"
        :all-label="f.allLabel"
        :all-value="f.allValue"
        :placeholder="f.placeholder || ('请选择' + f.label)"
        :width="f.width"
        :icon-of="f.iconOf"
      />
      <select
        v-else-if="f.type === 'select'"
        class="sf-input sf-select"
        :style="f.width ? { width: f.width } : null"
        v-model="model[f.key]"
        @change="emit('search')"
      >
        <option v-for="opt in f.options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
      </select>
      <template v-else-if="f.type === 'number-range'">
        <input
          class="sf-input sf-range"
          type="number"
          v-model="model[f.minKey]"
          :placeholder="f.minPlaceholder || '最小值'"
          @keyup.enter="emit('search')"
        />
        <span class="sf-range-sep">-</span>
        <input
          class="sf-input sf-range"
          type="number"
          v-model="model[f.maxKey]"
          :placeholder="f.maxPlaceholder || '最大值'"
          @keyup.enter="emit('search')"
        />
      </template>
    </div>
    <div class="sf-actions">
      <button type="button" class="sf-btn sf-btn-primary" @click="emit('search')">查询</button>
      <button type="button" class="sf-btn sf-btn-outline" @click="emit('reset')">重置</button>
    </div>
  </div>
</template>

<script setup>
import TreeSelect from './TreeSelect.vue'

// 通用搜索栏：fields 配置驱动，条件各自独立控件，支持 text / select / tree-select / number-range
// model 为父组件持有的响应式对象；filtering 由父组件基于 model 自行计算（实时组合过滤）
const props = defineProps({
  fields: { type: Array, required: true },
  model: { type: Object, required: true }
})
const emit = defineEmits(['search', 'reset'])
</script>

<style scoped>
.sf-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 14px;
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 12px 14px;
  margin-bottom: 16px;
}

.sf-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.sf-label {
  font-size: 13px;
  color: var(--text-muted);
  white-space: nowrap;
}

.sf-input {
  padding: 7px 10px;
  border: 1px solid var(--border);
  border-radius: 4px;
  font-size: 13px;
  background: var(--card-bg);
  color: var(--text);
  width: 170px;
  transition: border-color .15s, box-shadow .15s;
}

.sf-input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-soft);
}

.sf-select { width: 130px; }
.sf-range { width: 90px; }
.sf-range-sep { color: var(--text-muted); }

.sf-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.sf-btn {
  padding: 7px 16px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid var(--border);
  background: var(--card-bg);
  color: var(--text);
  transition: all .15s;
}

.sf-btn-primary {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
}

.sf-btn-primary:hover { opacity: .9; }

.sf-btn-outline:hover {
  border-color: var(--primary);
  color: var(--primary);
}
</style>
