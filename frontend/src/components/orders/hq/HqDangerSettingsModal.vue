<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: { type: Boolean, required: true },
  initThreshold: { type: Number, default: 200 },
  initMonths: { type: Number, default: 3 },
})

defineEmits(['close', 'save'])

const threshold = ref(props.initThreshold)
const months = ref(props.initMonths)

watch(() => props.initThreshold, (v) => { threshold.value = v })
watch(() => props.initMonths, (v) => { months.value = v })
watch(threshold, (v) => { if (v < 100) threshold.value = 100 })
</script>

<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="absolute inset-0 bg-black/40" @click="$emit('close')"></div>
    <div class="relative bg-white rounded-lg w-full max-w-sm border border-gray-200 shadow-xl">
      <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
        <h3 class="font-bold text-gray-900">이상 발주 기준 설정</h3>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 cursor-pointer">✕</button>
      </div>
      <div class="p-6 space-y-5">
        <div class="space-y-3">
          <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">이상 발주 감지 기준</label>
          <div class="space-y-3">
            <div>
              <p class="text-xs text-gray-500 mb-1.5 font-medium">초과 비율 기준</p>
              <div class="flex items-center gap-3">
                <input v-model.number="threshold" type="number" min="100" max="1000" step="10"
                  class="w-28 px-3 py-2 rounded border border-gray-200 text-sm font-bold focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none" />
                <span class="text-sm text-gray-600">% 초과 시 이상 감지</span>
              </div>
            </div>
            <div>
              <p class="text-xs text-gray-500 mb-1.5 font-medium">분석 기준 기간</p>
              <div class="flex items-center gap-3">
                <input v-model.number="months" type="number" min="1" max="24"
                  class="w-28 px-3 py-2 rounded border border-gray-200 text-sm font-bold focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none" />
                <span class="text-sm text-gray-600">개월 과거 데이터 기준</span>
              </div>
            </div>
          </div>
          <p class="text-xs text-gray-400 bg-gray-50 px-3 py-2 rounded border border-gray-100">
            현재 설정: 최근 <strong class="text-gray-700">{{ months }}개월</strong> 평균 대비
            <strong class="text-red-600">{{ threshold }}%</strong> 이상 초과 발주 감지
          </p>
        </div>
        <div class="flex gap-3 pt-1">
          <button @click="$emit('close')"
            class="flex-1 py-2.5 rounded border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 cursor-pointer">취소</button>
          <button @click="$emit('save', { threshold, months })"
            class="flex-1 py-2.5 rounded bg-[#F37321] text-white text-sm font-bold hover:bg-[#e0661d] cursor-pointer">저장</button>
        </div>
      </div>
    </div>
  </div>
</template>