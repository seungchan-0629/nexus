<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { getMonthlySales } from '@/api/statistics'
import { formatCurrency, calculateChange } from '@/utils/formatCurrency'
import { useAnimatedNumber } from '@/composables/useAnimatedNumber'

const currentYear = new Date().getFullYear()
const currentMonth = new Date().getMonth() + 1

const selectedYear = ref(currentYear)
const selectedMonth = ref(currentMonth)
const isMounted = ref(false)

const currentYearData = ref([])
const previousYearData = ref([])

const yearOptions = [2025, 2026]
const monthOptions = Array.from({ length: 12 }, (_, i) => i + 1)

const fetchData = async () => {
  try {
    const [curr, prev] = await Promise.all([
      getMonthlySales(selectedYear.value),
      getMonthlySales(selectedYear.value - 1),
    ])
    currentYearData.value = curr.data.result.monthlyData
    previousYearData.value = prev.data.result.monthlyData
  } catch (e) {
    console.error('월별 매출 조회 실패', e)
  }
}

const selectedTotal = computed(() => {
  const f = currentYearData.value.find((d) => d.month === selectedMonth.value)
  return f ? f.total : 0
})

const previousTotal = computed(() => {
  const f = previousYearData.value.find((d) => d.month === selectedMonth.value)
  return f ? f.total : null
})

const yoy = computed(() => {
  if (previousTotal.value == null) return null
  return calculateChange(selectedTotal.value, previousTotal.value)
})

// 카운트업
const animatedTotal = useAnimatedNumber(selectedTotal)
const formattedTotal = computed(() => formatCurrency(animatedTotal.value))

// 프로그레스 바
const maxValue = computed(() => Math.max(selectedTotal.value, previousTotal.value || 0))
const currentBarWidth = computed(() => {
  if (!isMounted.value || maxValue.value === 0) return '0%'
  return (selectedTotal.value / maxValue.value) * 100 + '%'
})
const previousBarWidth = computed(() => {
  if (!isMounted.value || maxValue.value === 0 || previousTotal.value == null) return '0%'
  return (previousTotal.value / maxValue.value) * 100 + '%'
})

onMounted(async () => {
  await fetchData()
  setTimeout(() => { isMounted.value = true }, 100)
})
watch(() => selectedYear.value, fetchData)
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 flex flex-col">
    <!-- 헤더 -->
    <div class="flex items-start justify-between mb-4">
      <div>
        <h2 class="font-bold text-gray-900">월별 매출</h2>
        <p class="text-xs text-gray-400 mt-0.5">{{ selectedYear }}년 {{ selectedMonth }}월 총 매출</p>
      </div>
      <div class="flex gap-2">
        <select v-model.number="selectedYear"
          class="border border-gray-200 rounded-md px-3 py-1.5 text-sm bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-orange-300">
          <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
        </select>
        <select v-model.number="selectedMonth"
          class="border border-gray-200 rounded-md px-3 py-1.5 text-sm bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-orange-300">
          <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
        </select>
      </div>
    </div>

    <!-- 큰 숫자 (카운트업) -->
    <p class="text-4xl font-extrabold text-orange-600 tracking-tight">
      {{ formattedTotal }}
    </p>

    <!-- YoY 배지 -->
    <p v-if="yoy" class="text-sm mt-2 font-semibold"
      :class="yoy.isPositive ? 'text-emerald-600' : 'text-red-500'">
      {{ yoy.isPositive ? '▲' : '▼' }} {{ yoy.text }}
      <span class="text-gray-400 font-normal ml-1">vs 작년 동월</span>
    </p>
    <p v-else class="text-sm mt-2 text-gray-400">작년 동월 데이터 없음</p>

    <!-- 프로그레스 바 -->
    <div class="mt-5 space-y-2.5">
      <div>
        <div class="flex justify-between text-xs text-gray-500 mb-1">
          <span>작년 {{ selectedMonth }}월</span>
          <span>{{ previousTotal != null ? formatCurrency(previousTotal) : '-' }}</span>
        </div>
        <div class="bg-gray-100 rounded-full h-2 overflow-hidden">
          <div class="h-full bg-gray-300 rounded-full transition-all duration-1000 ease-out"
            :style="{ width: previousBarWidth }"></div>
        </div>
      </div>
      <div>
        <div class="flex justify-between text-xs mb-1">
          <span class="text-gray-700 font-semibold">올해 {{ selectedMonth }}월</span>
          <span class="text-gray-700 font-semibold">{{ formatCurrency(selectedTotal) }}</span>
        </div>
        <div class="bg-gray-100 rounded-full h-2 overflow-hidden">
          <div class="h-full bg-orange-500 rounded-full transition-all duration-1000 ease-out"
            :style="{ width: currentBarWidth }"></div>
        </div>
      </div>
    </div>
  </div>
</template>
