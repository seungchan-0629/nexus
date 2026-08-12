<script setup>
import { ref, onMounted, watch, computed, onUnmounted, nextTick } from 'vue'
import { Chart } from 'chart.js/auto'
import { getYearlySales, getQuarterlySales, getMonthlySales } from '@/api/statistics'
import { formatCurrency, formatCurrencyShort } from '@/utils/formatCurrency'

const currentYear = new Date().getFullYear()

// 'yearly' | 'quarterly' | 'monthly'
const viewMode = ref('monthly')
const selectedYear = ref(currentYear)

const chartData = ref({ labels: [], values: [] })

const canvas = ref(null)
let chartInstance = null

const yearOptions = [2025, 2026]

const VIEW_MODES = [
  { key: 'yearly', label: '연도' },
  { key: 'quarterly', label: '분기' },
  { key: 'monthly', label: '월' },
]

const fetchData = async () => {
  try {
    if (viewMode.value === 'yearly') {
      const { data } = await getYearlySales()
      const sorted = [...data.result.yearlyData].sort((a, b) => a.year - b.year)
      chartData.value = {
        labels: sorted.map((d) => `${d.year}년`),
        values: sorted.map((d) => d.total),
      }
    } else if (viewMode.value === 'quarterly') {
      const { data } = await getQuarterlySales(selectedYear.value)
      const items = data.result.quarterlyData
      const values = new Array(4).fill(0)
      items.forEach((d) => {
        values[d.quarter - 1] = d.total
      })
      chartData.value = {
        labels: ['1분기', '2분기', '3분기', '4분기'],
        values,
      }
    } else {
      // monthly
      const { data } = await getMonthlySales(selectedYear.value)
      const items = data.result.monthlyData
      const values = new Array(12).fill(0)
      items.forEach((d) => {
        values[d.month - 1] = d.total
      })
      chartData.value = {
        labels: Array.from({ length: 12 }, (_, i) => `${i + 1}월`),
        values,
      }
    }
    await nextTick()
    renderChart()
  } catch (e) {
    console.error('매출 추이 조회 실패', e)
  }
}

// 월별은 line, 연/분기는 bar
const chartType = computed(() => (viewMode.value === 'monthly' ? 'line' : 'bar'))

const renderChart = () => {
  if (!canvas.value) return

  if (chartInstance) chartInstance.destroy()

  const isLine = chartType.value === 'line'

  chartInstance = new Chart(canvas.value, {
    type: chartType.value,
    data: {
      labels: chartData.value.labels,
      datasets: [
        {
          label: '매출',
          data: chartData.value.values,
          backgroundColor: isLine ? 'rgba(249,115,22,0.15)' : 'rgba(249,115,22,0.7)',
          borderColor: '#f97316',
          borderWidth: 2,
          fill: isLine,
          tension: 0.3,
          borderRadius: 4,
          barPercentage: 0.55,
          pointBackgroundColor: '#f97316',
          pointRadius: 4,
          pointHoverRadius: 6,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#1f2937',
          titleColor: '#f9fafb',
          bodyColor: '#d1d5db',
          padding: 10,
          cornerRadius: 10,
          callbacks: { label: (ctx) => ` ${formatCurrency(ctx.parsed.y)}` },
        },
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: '#9ca3af', font: { size: 12 } },
          border: { display: false },
        },
        y: {
          min: 0,
          grid: { color: '#f3f4f6' },
          ticks: {
            color: '#9ca3af',
            font: { size: 11 },
            callback: (v) => formatCurrencyShort(v),
          },
          border: { display: false },
        },
      },
    },
  })
}

const periodLabel = computed(() => {
  if (viewMode.value === 'yearly') return '전체 기간'
  return `${selectedYear.value}년`
})

const totalLabel = computed(() => {
  const sum = chartData.value.values.reduce((a, b) => a + b, 0)
  return formatCurrency(sum)
})

onMounted(fetchData)
watch([viewMode, selectedYear], fetchData)
onUnmounted(() => {
  if (chartInstance) chartInstance.destroy()
})
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 flex flex-col">
    <div class="flex items-start justify-between mb-4 gap-4 flex-wrap">
      <div>
        <h2 class="font-bold text-gray-900">매출 추이</h2>
        <p class="text-xs text-gray-400 mt-0.5">
          {{ periodLabel }} · 합계 {{ totalLabel }}
        </p>
      </div>

      <div class="flex gap-2 items-center">
        <!-- 연도 selector (분기/월에만 표시) -->
        <select v-if="viewMode !== 'yearly'" v-model.number="selectedYear"
          class="border border-gray-200 rounded-md px-3 py-1.5 text-sm bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-orange-300">
          <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
        </select>

        <!-- 토글: 연/분기/월 -->
        <div class="inline-flex rounded-md border border-gray-200 bg-white overflow-hidden">
          <button v-for="mode in VIEW_MODES" :key="mode.key"
            @click="viewMode = mode.key"
            class="px-3 py-1.5 text-sm transition-colors"
            :class="viewMode === mode.key
              ? 'bg-orange-500 text-white font-semibold'
              : 'text-gray-600 hover:bg-gray-50'">
            {{ mode.label }}
          </button>
        </div>
      </div>
    </div>

    <div class="relative" style="height: 280px;">
      <canvas ref="canvas" style="display:block; width:100%; height:100%;"></canvas>
    </div>
  </div>
</template>
