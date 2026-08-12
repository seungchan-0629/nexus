<template>
  <div v-if="open" class="fixed inset-0 bg-gray-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
    <div class="bg-white rounded-xl border border-gray-200 shadow-xl w-full max-w-md">
      <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
        <h2 class="text-lg font-bold text-gray-800">결제 수단 선택</h2>
        <button type="button" @click="$emit('close')" class="text-gray-400 hover:text-gray-600 cursor-pointer">✕</button>
      </div>
      <div class="p-8">
        <div class="text-center mb-8">
          <p class="text-sm text-gray-500 mb-2 font-medium">총 결제 금액</p>
          <p class="text-4xl font-bold text-blue-600 tracking-tight">{{ formattedTotal }}</p>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <button type="button" @click="$emit('select', 'card')"
            class="flex flex-col items-center justify-center p-6 border border-gray-200 rounded-xl hover:border-blue-400 hover:bg-blue-50 transition-all group shadow-sm hover:shadow-md cursor-pointer">
            <CreditCard class="w-10 h-10 text-gray-400 group-hover:text-blue-500 mb-3 transition-colors" />
            <span class="font-semibold text-gray-700 group-hover:text-blue-600">신용카드</span>
          </button>
          <button type="button" @click="$emit('select', 'cash')"
            class="flex flex-col items-center justify-center p-6 border border-gray-200 rounded-xl hover:border-emerald-400 hover:bg-emerald-50 transition-all group shadow-sm hover:shadow-md cursor-pointer">
            <Banknote class="w-10 h-10 text-gray-400 group-hover:text-emerald-500 mb-3 transition-colors" />
            <span class="font-semibold text-gray-700 group-hover:text-emerald-600">현금</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { CreditCard, Banknote } from 'lucide-vue-next'

defineProps({
  open: { type: Boolean, default: false },
  formattedTotal: { type: String, required: true },
})

defineEmits(['close', 'select'])
</script>
