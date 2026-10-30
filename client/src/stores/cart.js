import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const orderRemark = ref('')

  const totalCount = computed(() => items.value.reduce((s, i) => s + i.quantity, 0))
  const totalAmount = computed(() => items.value.reduce((s, i) => s + i.unitPrice * i.quantity, 0))

  function addItem(skuId, productName, skuName, unitPrice, itemType) {
    const existing = items.value.find(i => i.skuId === skuId)
    if (existing) {
      existing.quantity++
    } else {
      items.value.push({ skuId, productName, skuName, unitPrice, quantity: 1, remark: '', itemType })
    }
  }

  function removeItem(skuId) {
    const idx = items.value.findIndex(i => i.skuId === skuId)
    if (idx >= 0) {
      if (items.value[idx].quantity > 1) {
        items.value[idx].quantity--
      } else {
        items.value.splice(idx, 1)
      }
    }
  }

  function increaseQty(skuId) {
    const item = items.value.find(i => i.skuId === skuId)
    if (item) item.quantity++
  }

  function clear() {
    items.value = []
    orderRemark.value = ''
  }

  function toOrderItems() {
    return items.value.map(i => ({
      skuId: i.skuId,
      quantity: i.quantity,
      remark: i.remark
    }))
  }

  return { items, orderRemark, totalCount, totalAmount, addItem, removeItem, increaseQty, clear, toOrderItems }
})
