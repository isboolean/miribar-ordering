<template>
  <Teleport to="body">
    <Transition name="slide">
      <div v-if="visible" class="cart-popup" @click.self="$emit('close')">
        <div class="popup-inner">
          <div class="popup-header">
            <h3>🛒 购物车</h3>
            <button class="close-btn" @click="$emit('close')">✕</button>
          </div>
          <div v-if="cart.items.length === 0" class="empty-state">
            <div class="icon">🛒</div>
            <div>购物车为空</div>
          </div>
          <div v-else class="popup-body">
            <div v-for="item in cart.items" :key="item.skuId" class="cart-item">
              <div class="item-info">
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-sku">{{ item.skuName }}</div>
                <div class="item-price">¥{{ (item.unitPrice * item.quantity).toFixed(2) }}</div>
              </div>
              <div class="qty-control">
                <button class="qty-btn" @click="cart.removeItem(item.skuId)">-</button>
                <span class="qty-num">{{ item.quantity }}</span>
                <button class="qty-btn" @click="cart.increaseQty(item.skuId)">+</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- Cart bar -->
  <div class="cart-bar">
    <div class="cart-icon" @click="$emit('toggle')">
      🛒
      <span v-if="cart.totalCount > 0" class="cart-badge">{{ cart.totalCount }}</span>
    </div>
    <div class="cart-total">
      <div class="amount">¥{{ cart.totalAmount.toFixed(2) }}</div>
      <div class="count">{{ cart.totalCount > 0 ? `${cart.totalCount}件商品` : '购物车为空' }}</div>
    </div>
    <button class="submit-btn" :disabled="cart.totalCount === 0" @click="$emit('submit')">去下单</button>
  </div>
</template>

<script setup>
import { useCartStore } from '@/stores/cart'

defineProps({ visible: { type: Boolean, default: false } })
defineEmits(['toggle', 'close', 'submit'])

const cart = useCartStore()
</script>

<style scoped>
.cart-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: var(--card-bg); border-top: 1px solid var(--border);
  padding: 12px 20px; display: flex; align-items: center; z-index: 200;
}
.cart-icon {
  position: relative; width: 50px; height: 50px; border-radius: 50%;
  background: var(--primary); display: flex; align-items: center; justify-content: center;
  font-size: 24px; cursor: pointer;
}
.cart-badge {
  position: absolute; top: -4px; right: -4px;
  background: var(--danger); color: #fff; font-size: 11px;
  min-width: 20px; height: 20px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; font-weight: 600;
}
.cart-total { flex: 1; margin-left: 16px; }
.cart-total .amount { font-size: 20px; font-weight: 700; color: var(--accent); }
.cart-total .count { font-size: 12px; color: var(--text-muted); }
.submit-btn {
  padding: 12px 28px; border-radius: 24px; border: none;
  background: var(--primary); color: #fff; font-size: 16px; font-weight: 600;
}
.submit-btn:disabled { background: #555; cursor: not-allowed; }

/* Popup */
.cart-popup {
  position: fixed; inset: 0; z-index: 300;
  background: rgba(0,0,0,.4);
}
.popup-inner {
  position: absolute; bottom: 74px; left: 0; right: 0;
  background: var(--card-bg); border-radius: 16px 16px 0 0;
  max-height: 50vh; display: flex; flex-direction: column;
}
.popup-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; border-bottom: 1px solid var(--border);
}
.popup-header h3 { font-size: 16px; }
.close-btn {
  background: none; border: none; color: var(--text-muted);
  font-size: 18px; cursor: pointer; padding: 4px;
}
.popup-body { overflow-y: auto; padding: 0 20px 16px; }
.cart-item {
  display: flex; align-items: center; padding: 12px 0;
  border-bottom: 1px solid var(--border);
}
.cart-item:last-child { border-bottom: none; }
.item-info { flex: 1; }
.item-name { font-size: 14px; font-weight: 500; }
.item-sku { font-size: 12px; color: var(--text-muted); }
.item-price { font-size: 14px; color: var(--accent); font-weight: 600; margin-top: 2px; }
.qty-control { display: flex; align-items: center; gap: 10px; }
.qty-btn {
  width: 28px; height: 28px; border-radius: 50%;
  border: 1px solid var(--border); background: transparent;
  color: var(--text); font-size: 16px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}
.qty-btn:active { background: var(--primary); }
.qty-num { font-size: 15px; font-weight: 600; min-width: 20px; text-align: center; }

/* Transition */
.slide-enter-active, .slide-leave-active { transition: all .3s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(20px); }
</style>
