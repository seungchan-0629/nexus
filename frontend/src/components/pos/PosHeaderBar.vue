<template>
  <div class="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between shrink-0">
    <div class="flex items-center gap-4">
      <div class="flex border border-gray-200">
        <button
          type="button"
          @click="$emit('select-tab', 'order')"
          :class="['px-4 py-2 text-sm font-semibold transition-all flex items-center gap-2 cursor-pointer',
            activeTab === 'order' ? 'bg-gray-900 text-white' : 'bg-white text-gray-500 hover:bg-gray-50']">
          <ShoppingCart class="w-4 h-4" /> 주문 및 결제
        </button>
        <button
          type="button"
          @click="$emit('select-tab', 'settlement')"
          :class="['px-4 py-2 text-sm font-semibold transition-all flex items-center gap-2 border-l border-gray-200 cursor-pointer',
            activeTab === 'settlement' ? 'bg-gray-900 text-white' : 'bg-white text-gray-500 hover:bg-gray-50']">
          <TrendingUp class="w-4 h-4" /> 일일 마감 (정산)
        </button>
      </div>
      <span class="text-xs px-2.5 py-1 font-bold rounded border"
        :class="isClosed ? 'bg-red-50 text-red-600 border-red-200' : 'bg-green-50 text-green-600 border-green-200'">
        {{ isClosed ? '마감됨 (주문불가)' : '영업중' }}
      </span>
    </div>
    <div class="text-right">
      <p class="text-sm font-bold text-blue-600">{{ currentTime }}</p>
      <p class="text-xs text-gray-400">{{ storeName }}</p>
    </div>
  </div>
</template>

<script setup>
import { ShoppingCart, TrendingUp } from 'lucide-vue-next'

defineProps({
  activeTab: { type: String, required: true },
  isClosed: { type: Boolean, required: true },
  currentTime: { type: String, required: true },
  storeName: { type: String, default: '' },
})

defineEmits(['select-tab'])
</script>
