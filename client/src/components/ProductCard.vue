<template>
  <div class="product-card" :class="{ 'sold-out': allSoldOut }">
    <div class="product-img">{{ productEmoji }}</div>
    <div class="product-info">
      <div class="product-name">
        {{ product.name }}
        <span v-if="product.containsAlcohol" class="alcohol-icon">🍷</span>
      </div>
      <div v-if="product.description" class="product-desc">{{ product.description }}</div>
      <div v-if="tags.length" class="product-tags">
        <span v-for="tag in tags" :key="tag" class="tag" :class="tagClass(tag)">{{ tag }}</span>
      </div>

      <!-- 单规格 -->
      <div v-if="skus.length <= 1" class="product-bottom">
        <div class="product-price">
          <span class="yen">¥</span>{{ skus[0]?.price?.toFixed(2) || '0.00' }}
        </div>
        <button
          class="add-btn"
          :disabled="skus[0]?.soldOut === 1 || skus[0]?.stock <= 0"
          @click="handleAdd(skus[0])"
        >+</button>
      </div>

      <!-- 多规格 -->
      <div v-else class="sku-selector">
        <button
          v-for="sku in skus" :key="sku.id"
          class="sku-btn"
          :class="{ 'sold-out': sku.soldOut === 1 || sku.stock <= 0 }"
          @click="handleAdd(sku)"
        >
          <span>{{ sku.skuName }}</span>
          <span class="sku-price">¥{{ sku.price.toFixed(2) }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useCartStore } from '@/stores/cart'

const props = defineProps({
  product: { type: Object, required: true },
  skus: { type: Array, default: () => [] }
})

const cart = useCartStore()

const tags = computed(() => props.product.tags ? props.product.tags.split(',').map(t => t.trim()) : [])
const allSoldOut = computed(() => props.skus.length > 0 && props.skus.every(s => s.soldOut === 1 || s.stock <= 0))
const productEmoji = computed(() => {
  const t = props.product.productType
  return t === 1 ? '🍟' : t === 2 ? '🎁' : '🍺'
})

function tagClass(tag) {
  if (tag === '热门') return 'tag-hot'
  if (tag === '新品') return 'tag-new'
  return 'tag-rec'
}

function handleAdd(sku) {
  if (!sku || sku.soldOut === 1 || sku.stock <= 0) return
  cart.addItem(sku.id, props.product.name, sku.skuName, sku.price, props.product.productType)
}
</script>

<style scoped>
.product-card {
  display: flex;
  background: var(--card-bg);
  border-radius: var(--radius);
  padding: 14px;
  margin-bottom: 10px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  transition: transform .15s;
}
.product-card:active { transform: scale(.98); }
.product-card.sold-out { opacity: .5; pointer-events: none; }

.product-img {
  width: 90px; height: 90px;
  border-radius: var(--radius-sm);
  background: var(--surface-2);
  flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 36px;
  margin-right: 14px;
}

.product-info { flex: 1; min-width: 0; }
.product-name { font-size: 15px; font-weight: 600; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.alcohol-icon { font-size: 11px; }
.product-desc { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.product-tags { display: flex; gap: 4px; margin-bottom: 8px; flex-wrap: wrap; }
.tag { font-size: 10px; padding: 2px 8px; border-radius: 4px; font-weight: 500; }
.tag-hot { background: rgba(239,68,68,.1); color: var(--danger); }
.tag-new { background: rgba(22,163,74,.1); color: var(--success); }
.tag-rec { background: rgba(245,158,11,.12); color: var(--warning); }

.product-bottom { display: flex; align-items: center; justify-content: space-between; }
.product-price { font-size: 17px; font-weight: 700; color: var(--text); }
.product-price .yen { font-size: 12px; color: var(--text-muted); font-weight: 500; }

.sku-selector { display: flex; gap: 6px; flex-wrap: wrap; }
.sku-btn {
  padding: 6px 12px; border-radius: 999px;
  border: 1px solid var(--border); background: transparent;
  color: var(--text); font-size: 12px; cursor: pointer;
  transition: border-color .2s, background .2s;
  display: flex; flex-direction: column; align-items: center; gap: 2px;
}
.sku-btn .sku-price { color: var(--text-muted); font-weight: 600; }
.sku-btn.sold-out { opacity: .4; pointer-events: none; text-decoration: line-through; }
.sku-btn:active { border-color: var(--primary); background: var(--primary-soft); }

.add-btn {
  width: 32px; height: 32px; border-radius: 50%;
  border: none; background: var(--primary); color: #fff;
  font-size: 20px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: transform .15s, background .15s;
}
.add-btn:hover { background: var(--primary-dark); }
.add-btn:active { transform: scale(.9); }
.add-btn:disabled { background: var(--surface-2); color: var(--text-muted); cursor: not-allowed; }
</style>
