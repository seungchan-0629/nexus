<script setup>
import { ref, onMounted, computed } from 'vue'
import { getYearlySales } from '@/api/statistics'
import { formatCurrency, calculateChange } from '@/utils/formatCurrency'
import { useAnimatedNumber } from '@/composables/useAnimatedNumber'

const allYears = ref([])
const selectedYear = ref(null)
const isMounted = ref(false)

const fetchData = async () => {
  try {
    const { data } = await getYearlySales()
    allYears.value = data.result.yearlyData
    if (allYears.value.length > 0 && selectedYear.value == null) {
      selectedYear.value = allYears.value[allYears.value.length - 1].year
    }
  } catch (e) {
    console.error('연도별 매출 조회 실패', e)
  }
}

const sortedYears = computed(() =>
  [...allYears.value].sort((a, b) => a.year - b.year),
)

const selectedTotal = computed(() => {
  const f = allYears.value.find((d) => d.year === selectedYear.value)
  return f ? f.total : 0
})

const previousTotal = computed(() => {
  const sorted = sortedYears.value
  const idx = sorted.findIndex((d) => d.year === selectedYear.value)
  if (idx <= 0) return null
  return sorted[idx - 1].total
})

const yoy = computed(() => {
  if (previousTotal.value == null) return null
  return calculateChange(selectedTotal.value, previousTotal.value)
})

const yearOptions = computed(() => sortedYears.value.map((d) => d.year))

// 카운트업 애니메이션
const animatedTotal = useAnimatedNumber(selectedTotal)
const formattedTotal = computed(() => formatCurrency(animatedTotal.value))

// 프로그레스 바 (페이지 진입 시 0% → final 애니메이션 위해 isMounted 가드)
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
  // 다음 frame 후 mount flag → 0% → final 트랜지션 트리거
  setTimeout(() => { isMounted.value = true }, 100)
})
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 flex flex-col">
    <!-- 헤더 -->
    <div class="flex items-start justify-between mb-4">
      <div>
        <h2 class="font-bold text-gray-900">연도별 매출</h2>
        <p class="text-xs text-gray-400 mt-0.5">{{ selectedYear }}년 총 매출</p>
      </div>
      <select v-model.number="selectedYear"
        class="border border-gray-200 rounded-md px-3 py-1.5 text-sm bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-orange-300">
        <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
      </select>
    </div>

    <!-- 큰 숫자 (카운트업) -->
    <p class="text-4xl font-extrabold text-orange-600 tracking-tight">
      {{ formattedTotal }}
    </p>

    <!-- YoY 배지 -->
    <p v-if="yoy" class="text-sm mt-2 font-semibold"
      :class="yoy.isPositive ? 'text-emerald-600' : 'text-red-500'">
      {{ yoy.isPositive ? '▲' : '▼' }} {{ yoy.text }}
      <span class="text-gray-400 font-normal ml-1">vs 전년</span>
    </p>
    <p v-else class="text-sm mt-2 text-gray-400">전년 데이터 없음</p>

    <!-- 프로그레스 바 (0% → final 애니메이션) -->
    <div class="mt-5 space-y-2.5">
      <div>
        <div class="flex justify-between text-xs text-gray-500 mb-1">
          <span>작년</span>
          <span>{{ previousTotal != null ? formatCurrency(previousTotal) : '-' }}</span>
        </div>
        <div class="bg-gray-100 rounded-full h-2 overflow-hidden">
          <div class="h-full bg-gray-300 rounded-full transition-all duration-1000 ease-out"
            :style="{ width: previousBarWidth }"></div>
        </div>
      </div>
      <div>
        <div class="flex justify-between text-xs mb-1">
          <span class="text-gray-700 font-semibold">올해</span>
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
