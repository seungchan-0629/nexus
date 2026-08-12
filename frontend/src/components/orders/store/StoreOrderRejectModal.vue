<script setup>
defineProps({
  order: { type: Object, default: null },
  visible: { type: Boolean, default: false },
})

const emit = defineEmits(['close', 'reject'])
</script>

<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-[2px]">
    <div class="bg-white rounded-xl shadow-2xl w-full max-w-md overflow-hidden animate-modal-up">
      <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
        <h2 class="text-lg font-bold text-gray-900">발주서 거절</h2>
        <button @click="emit('close')" class="text-gray-400 hover:text-gray-600 cursor-pointer">✕</button>
      </div>

      <div class="p-6 space-y-4">
        <p class="text-sm text-gray-500">아래 발주서를 거절하시겠습니까?</p>

        <div class="bg-gray-50 rounded-lg p-4">
          <div class="space-y-2 pb-3 border-b border-gray-200">
            <div v-for="item in order?.ordersItemList" :key="item.idx" class="flex justify-between text-xs">
              <span class="text-gray-500">{{ item.productName }} ({{ item.count }}개 × ₩{{ (item.unitPrice ?? 0).toLocaleString() }})</span>
              <span class="font-medium text-gray-900">₩ {{ ((item.count || 0) * (item.unitPrice ?? 0)).toLocaleString() }}</span>
            </div>
          </div>
          <div class="flex justify-between items-center pt-3">
            <span class="text-sm font-bold text-gray-900">합계</span>
            <span class="text-lg font-black text-red-500">₩ {{ (order?.price ?? 0).toLocaleString() }}</span>
          </div>
        </div>
      </div>

      <div class="px-6 py-4 bg-gray-50 flex gap-2">
        <button @click="emit('close')" class="flex-1 py-3 bg-white border border-gray-200 text-gray-600 font-bold rounded-lg text-sm cursor-pointer">취소</button>
        <button @click="emit('reject')" class="flex-1 py-3 bg-red-500 text-white font-bold rounded-lg text-sm cursor-pointer">거절</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-modal-up {
  animation: modalUp 0.3s ease-out;
}
@keyframes modalUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
