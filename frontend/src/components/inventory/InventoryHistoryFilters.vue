<script setup>
const filterType = defineModel('filterType', { type: String, default: '' })
const filterStore = defineModel('filterStore', { type: String, default: '' })
const filterFrom = defineModel('filterFrom', { type: String, default: '' })
const filterTo = defineModel('filterTo', { type: String, default: '' })

defineProps({
  stores: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['reset'])

const inputClass =
  'px-3 py-2 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none'
</script>

<template>
  <div class="bg-white border border-gray-200 rounded-lg p-5 flex flex-wrap gap-4 items-end">
    <div class="space-y-1.5">
      <label class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">유형</label>
      <select v-model="filterType" :class="inputClass">
        <option value="">전체</option>
        <option value="입고">입고</option>
        <option value="출고">출고</option>
        <option value="보정">보정</option>
      </select>
    </div>
    <div class="space-y-1.5">
      <label class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">입점 매장</label>
      <select v-model="filterStore" :class="inputClass">
        <option value="">전체</option>
        <option v-for="s in stores" :key="s.idx" :value="s.storeName">{{ s.storeName }}</option>
      </select>
    </div>
    <div class="space-y-1.5">
      <label class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">기간 (시작)</label>
      <input v-model="filterFrom" type="date" :class="inputClass" />
    </div>
    <div class="space-y-1.5">
      <label class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">기간 (종료)</label>
      <input v-model="filterTo" type="date" :class="inputClass" />
    </div>
    <button
      type="button"
      class="px-3 py-2 border border-gray-200 text-sm font-semibold text-gray-500 hover:bg-gray-50"
      @click="emit('reset')"
    >
      초기화
    </button>
  </div>
</template>
