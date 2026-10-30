<template>
  <Teleport to="body">
    <Transition name="slide">
      <div v-if="visible" class="cart-popup" @click.self="$emit('close')">
        <div class="popup-inner">
          <div class="popup-header">
            <h3>购物车</h3>
            <button class="close-btn" aria-label="关闭购物车" @click="$emit('close')">✕</button>
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
                <button class="qty-btn" aria-label="减少数量" @click="cart.removeItem(item.skuId)">−</button>
                <span class="qty-num">{{ item.quantity }}</span>
                <button class="qty-btn" aria-label="增加数量" @click="cart.increaseQty(item.skuId)">+</button>
              </div>
            </div>
            <div class="remark-block">
              <label class="remark-label" for="order-remark">备注</label>
              <textarea
                id="order-remark"
                class="remark-input"
                v-model="cart.orderRemark"
                placeholder="口味偏好、冰量、上菜时间等（选填）"
                rows="2"
                maxlength="200"
              ></textarea>
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
      <span v-if="cart.totalCount > 0" :key="cart.totalCount" class="cart-badge">{{ cart.totalCount }}</span>
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
  padding: 10px 20px; display: flex; align-items: center; z-index: 200;
  box-shadow: 0 -1px 8px rgba(16,24,40,.06);
}
.cart-icon {
  position: relative; width: 46px; height: 46px; border-radius: 50%;
  background: var(--primary); display: flex; align-items: center; justify-content: center;
  font-size: 22px; cursor: pointer;
}
.cart-badge {
  position: absolute; top: -4px; right: -4px;
  background: var(--danger); color: #fff; font-size: 11px;
  min-width: 20px; height: 20px; border-radius: 999px;
  display: flex; align-items: center; justify-content: center; font-weight: 600;
  animation: badgePop .35s cubic-bezier(.34, 1.56, .64, 1);
}
@keyframes badgePop {
  0% { transform: scale(.4); }
  60% { transform: scale(1.25); }
  100% { transform: scale(1); }
}
.cart-total { flex: 1; margin-left: 16px; }
.cart-total .amount { font-size: 19px; font-weight: 700; color: var(--accent); }
.cart-total .count { font-size: 12px; color: var(--text-muted); }
.submit-btn {
  padding: 12px 28px; border-radius: 999px; border: none;
  background: var(--primary); color: #fff; font-size: 15px; font-weight: 600;
  transition: background .15s;
}
.submit-btn:hover { background: var(--primary-dark); }
.submit-btn:disabled { background: var(--surface-2); color: var(--text-muted); cursor: not-allowed; }

/* Popup */
.cart-popup {
  position: fixed; inset: 0; z-index: 300;
  background: rgba(16,24,40,.4);
}
.popup-inner {
  position: absolute; bottom: 74px; left: 0; right: 0;
  background: var(--card-bg); border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  max-height: 50vh; display: flex; flex-direction: column;
  box-shadow: var(--shadow-md);
}
.popup-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; border-bottom: 1px solid var(--border);
}
.popup-header h3 { font-size: 16px; font-weight: 600; }
.close-btn {
  background: none; border: none; color: var(--text-muted);
  font-size: 18px; cursor: pointer; padding: 12px; margin: -12px;
  line-height: 1; min-width: 44px; min-height: 44px;
  display: flex; align-items: center; justify-content: center;
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
.item-price { font-size: 14px; color: var(--text); font-weight: 600; margin-top: 2px; }
.qty-control { display: flex; align-items: center; gap: 8px; }
.qty-btn {
  position: relative;
  width: 34px; height: 34px; border-radius: 50%;
  border: 1px solid var(--border); background: transparent;
  color: var(--text); font-size: 18px; line-height: 1; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background .15s, border-color .15s;
}
/* 扩大触控热区至 44px，不影响视觉尺寸 */
.qty-btn::after {
  content: ''; position: absolute; inset: -5px; border-radius: 50%;
}
.qty-btn:active { background: var(--primary-soft); border-color: var(--primary); }
.qty-num { font-size: 15px; font-weight: 600; min-width: 24px; text-align: center; }

.remark-block { padding: 12px 0 4px; border-top: 1px solid var(--border); margin-top: 4px; }
.remark-label { display: block; font-size: 12px; color: var(--text-muted); margin-bottom: 6px; }
.remark-input {
  width: 100%; padding: 10px 12px; border-radius: var(--radius-sm);
  border: 1px solid var(--border); background: var(--surface-2);
  color: var(--text); font-size: 13px; resize: none; outline: none;
  font-family: inherit; line-height: 1.5;
  transition: border-color .15s, box-shadow .15s;
}
.remark-input:focus { border-color: var(--primary); background: var(--card-bg); box-shadow: 0 0 0 3px var(--primary-soft); }

/* Transition */
.slide-enter-active, .slide-leave-active { transition: all .3s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(20px); }
</style>
