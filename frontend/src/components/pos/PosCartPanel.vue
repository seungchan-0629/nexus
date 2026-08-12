<template>
  <aside class="pos-cart-aside">
    <div class="pos-cart-header">
      <h2 class="pos-cart-title">
        <ShoppingCart class="w-4 h-4 text-gray-500" /> 현재 주문 내역
      </h2>
      <button type="button" class="pos-cart-btn-ghost" @click="$emit('clear-cart')">
        전체 삭제
      </button>
    </div>

    <div class="pos-cart-scroll">
      <div v-if="cart.length === 0" class="pos-cart-empty">
        <div class="pos-cart-empty-icon">
          <ShoppingBag class="w-6 h-6 text-gray-300" />
        </div>
        <p class="pos-cart-empty-text">상품을 담아주세요</p>
      </div>

      <div
        v-for="({ menuIdx, name, price, quantity }) in cart"
        :key="menuIdx"
        class="pos-cart-line">
        <div class="pos-cart-line-head">
          <span class="pos-cart-line-name">{{ name }}</span>
          <button type="button" class="pos-cart-line-remove" @click="$emit('remove-item', menuIdx)">
            <X class="w-4 h-4" />
          </button>
        </div>
        <div class="pos-cart-line-body">
          <span class="pos-cart-line-price">{{ formatPrice(price * quantity) }}</span>
          <div class="pos-cart-qty-row">
            <button type="button" class="pos-cart-qty-btn" @click="$emit('decrease-qty', menuIdx)">
              <Minus class="w-3 h-3" />
            </button>
            <span class="pos-cart-qty-value">{{ quantity }}</span>
            <button type="button" class="pos-cart-qty-btn" @click="$emit('increase-qty', menuIdx)">
              <Plus class="w-3 h-3" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="pos-cart-footer">
      <div class="pos-cart-summary">
        <span>총 수량</span>
        <span class="pos-cart-summary-strong">{{ totalQuantity }} 개</span>
      </div>
      <div class="pos-cart-total-row">
        <span class="pos-cart-total-label">총 결제금액</span>
        <span class="pos-cart-total-amount">{{ formatPrice(totalPrice) }}</span>
      </div>
      <button
        type="button"
        :class="(cart.length === 0 || isClosed) ? 'pos-cart-checkout pos-cart-checkout--disabled' : 'pos-cart-checkout'"
        :disabled="cart.length === 0 || isClosed"
        @click="$emit('checkout')">
        {{ isClosed ? '마감됨 — 주문 불가' : '결제 진행하기' }}
      </button>
    </div>
  </aside>
</template>

<script setup>
import { ShoppingCart, ShoppingBag, X, Minus, Plus } from 'lucide-vue-next'
import { formatPrice } from '@/utils/formatPrice.js'

defineProps({
  cart: { type: Array, default: () => [] },
  totalPrice: { type: Number, default: 0 },
  totalQuantity: { type: Number, default: 0 },
  isClosed: { type: Boolean, default: false },
})

defineEmits(['clear-cart', 'remove-item', 'increase-qty', 'decrease-qty', 'checkout'])
</script>
