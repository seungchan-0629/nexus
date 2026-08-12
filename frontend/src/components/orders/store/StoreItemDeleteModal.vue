<script setup>
defineProps({
  item: { type: Object, default: null },
  visible: { type: Boolean, default: false },
})

const emit = defineEmits(['close', 'confirm'])
</script>

<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-[2px]">
    <div class="bg-white rounded-xl shadow-2xl w-full max-w-sm overflow-hidden animate-modal-up">
      <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
        <h2 class="text-lg font-bold text-gray-900">품목 삭제</h2>
        <button @click="emit('close')" class="text-gray-400 hover:text-gray-600 cursor-pointer">✕</button>
      </div>

      <div class="p-6 space-y-3">
        <p class="text-sm text-gray-500">아래 품목을 발주서에서 삭제하시겠습니까?</p>
        <div class="bg-red-50 border border-red-100 rounded-lg px-4 py-3 flex justify-between items-center">
          <span class="text-sm font-semibold text-gray-900">{{ item?.productName }}</span>
          <span class="text-xs text-gray-500">{{ item?.count }}개</span>
        </div>
      </div>

      <div class="px-6 py-4 bg-gray-50 flex gap-2">
        <button @click="emit('close')" class="flex-1 py-3 bg-white border border-gray-200 text-gray-600 font-bold rounded-lg text-sm cursor-pointer">취소</button>
        <button @click="emit('confirm')" class="flex-1 py-3 bg-red-500 text-white font-bold rounded-lg text-sm cursor-pointer hover:bg-red-600">삭제</button>
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
