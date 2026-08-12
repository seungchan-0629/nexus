<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getTodaySales } from '@/api/statistics'
import { useAnimatedNumber } from '@/composables/useAnimatedNumber'

const todaySales = ref(0)
const displaySales = useAnimatedNumber(todaySales)
let polling = null

const fetchData = async () => {
  try {
    const { data } = await getTodaySales()
    todaySales.value = data.result.todaySales
  } catch (e) {
    console.error('매출 조회 실패', e)
  }
}

onMounted(() => {
  fetchData()
  polling = setInterval(fetchData, 2000)
})

onUnmounted(() => {
  if (polling) clearInterval(polling)
})

const formatted = (val) => val.toLocaleString('ko-KR')
</script>

<template>
  <div class="col-span-full bg-white rounded-2xl border border-gray-200 shadow-sm p-6">
    <p class="text-[11px] font-semibold text-gray-400 uppercase tracking-[0.14em]">오늘 실시간 총 매출</p>
    <div class="flex items-end gap-2 mt-3">
      <p class="text-[36px] leading-none font-extrabold text-gray-900 tracking-tight">₩ {{ formatted(displaySales) }}</p>
    </div>
    <p class="text-xs text-gray-400 mt-2">2초 간격 실시간 갱신</p>
  </div>
</template>
