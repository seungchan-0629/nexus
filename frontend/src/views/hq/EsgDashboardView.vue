<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Chart } from 'chart.js/auto'
import { getWasteStats, postOverDueDateWaste } from '@/api/wastelog'
import { 
  Trash2, 
  TrendingDown, 
  TrendingUp, 
  AlertCircle, 
  CheckCircle2,
  RefreshCw,
  Clock
} from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const toast = useToast()
const loading = ref(false)
const triggerLoading = ref(false)
const stats = ref({
  wasteSum: 0,
  lastMonthWasteSum: 0,
  wasteGradient: 0,
  useSuccessRate: 0,
  wasteSavingAmount: 0,
  avgTurnover: 0,
  optimalStockRate: 0,
  overstockCount: 0,
  autoOrderAccuracy: 0,
  monthlyWasteTrend: [],
  categoryWasteRatio: [],
  storeWasteStatus: [],
  storeTurnoverTrend: [],
  storeOverstockTrend: []
})

const tabs = [
  { value: 'waste',     label: '폐기 최소화' },
  { value: 'inventory', label: '재고 효율' },
]
const activeTab = ref(route.query.tab || 'waste')

// URL 쿼리 파라미터 변경 감시
watch(() => route.query.tab, (newTab) => {
  if (newTab) activeTab.value = newTab
})

// ── 데이터 페칭 ───────────────────────────────────────────

const fetchStats = async () => {
  loading.value = true
  try {
    const response = await getWasteStats()
    if (response.data.success) {
      stats.value = response.data.result
    }
  } catch (error) {
    console.error('Failed to fetch waste stats:', error)
    toast.error('통계 데이터를 불러오는 데 실패했습니다.')
  } finally {
    loading.value = false
  }
}

const handleTriggerWaste = async () => {
  if (!confirm('유통기한이 지난 모든 재고를 폐기 처리하시겠습니까?')) return
  
  triggerLoading.value = true
  try {
    await postOverDueDateWaste()
    toast.success('유통기한 경과 상품 폐기 처리가 완료되었습니다.')
    await fetchStats()
  } catch (error) {
    console.error('Failed to trigger waste:', error)
    toast.error('폐기 처리 중 오류가 발생했습니다.')
  } finally {
    triggerLoading.value = false
  }
}

// ── 폐기 최소화 ───────────────────────────────────────────

const wasteCards = computed(() => [
  {
    title: '이번달 폐기량',
    value: stats.value.wasteSum?.toLocaleString() || '0',
    unit: '개',
    sub: stats.value.wasteGradient !== null ? `전월 대비 ${Math.abs((stats.value.wasteGradient - 1) * 100).toFixed(1)}% ${stats.value.wasteGradient > 1 ? '증가' : '감소'}` : '전월 데이터 없음',
    valueClass: stats.value.wasteGradient > 1 ? 'text-red-500' : 'text-green-600',
  },
  {
    title: '지난달 대비 감소율',
    value: stats.value.wasteGradient !== null ? Math.abs((stats.value.wasteGradient - 1) * 100).toFixed(1) : '0',
    unit: '%',
    sub: stats.value.lastMonthWasteSum ? `전월 ${stats.value.lastMonthWasteSum}개 → 이번달 ${stats.value.wasteSum}개` : '전월 데이터 없음',
    valueClass: stats.value.wasteGradient > 1 ? 'text-red-500' : 'text-green-600',
  },
  {
    title: '폐기 절감 금액',
    value: stats.value.wasteSavingAmount ? `₩ ${stats.value.wasteSavingAmount.toLocaleString()}` : '₩ 0',
    unit: '',
    sub: '전월 대비 절감 추정액',
    valueClass: 'text-[#F37321]',
  },
  {
    title: '소진 성공률',
    value: (stats.value.useSuccessRate || 0).toFixed(1),
    unit: '%',
    sub: '유통기한 경고 후 소진 성공 비율',
    valueClass: 'text-blue-600'
  },
])

const wasteMonthlyData = computed(() => ({
  labels: stats.value.monthlyWasteTrend?.map(item => {
    const [year, month] = item.month.split('-')
    return `${parseInt(month)}월`
  }) || [],
  data: stats.value.monthlyWasteTrend?.map(item => item.quantity) || [],
}))

const wasteCategoryData = computed(() => ({
  labels: stats.value.categoryWasteRatio?.map(item => item.categoryName) || [],
  data: stats.value.categoryWasteRatio?.map(item => item.quantity) || [],
}))

const expiryRows = computed(() => stats.value.storeWasteStatus?.map(item => ({
  store: item.storeName,
  warned: item.warnedCount,
  success: item.successCount,
  fail: item.failCount,
  rate: Math.round(item.successRate)
})) || [])

// ── 재고 효율 ────────────────────────────────────────────

const inventoryCards = computed(() => [
  {
    title: '평균 재고 회전율',
    value: (stats.value.avgTurnover || 0).toFixed(1),
    unit: '회/월',
    sub: '전체 매장 평균',
  },
  {
    title: '적정 재고 유지율',
    value: (stats.value.optimalStockRate || 0).toFixed(1),
    unit: '%',
    sub: '기준 재고 범위 내 유지 비율',
  },
])

const turnoverData = computed(() => ({
  labels: stats.value.storeTurnoverTrend?.map(item => item.storeName) || [],
  data: stats.value.storeTurnoverTrend?.map(item => item.turnover) || [],
}))

const overStockMonthlyData = computed(() => ({
  labels: stats.value.monthlyWasteTrend?.map(item => {
    const [year, month] = item.month.split('-')
    return `${parseInt(month)}월`
  }) || [],
  datasets: stats.value.storeOverstockTrend?.map(item => ({
    label: item.label,
    data: item.data
  })) || []
}))

const inventoryRows = computed(() => stats.value.storeTurnoverTrend?.map((item, index) => ({
  store: item.storeName,
  stockRate: item.optimalStockRate || 0,
  turnover: item.turnover
})) || [])

// ── 차트 ─────────────────────────────────────────────────

const wasteMonthlyCanvas  = ref(null)
const wasteCategoryCanvas = ref(null)
const turnoverCanvas      = ref(null)

let wasteMonthlyChart, wasteCategoryChart, turnoverChart

function destroyCharts() {
  wasteMonthlyChart?.destroy()
  wasteCategoryChart?.destroy()
  turnoverChart?.destroy()
  wasteMonthlyChart = wasteCategoryChart = turnoverChart = undefined
}

const colors = ['#f97316', '#60a5fa', '#a78bfa', '#4ade80', '#fbbf24', '#94a3b8', '#fb7185', '#2dd4bf']

function renderWasteCharts() {
  wasteMonthlyChart?.destroy()
  wasteCategoryChart?.destroy()

  if (wasteMonthlyCanvas.value) {
    wasteMonthlyChart = new Chart(wasteMonthlyCanvas.value, {
      type: 'line',
      data: {
        labels: wasteMonthlyData.value.labels,
        datasets: [{
          label: '폐기량 (개)',
          data: wasteMonthlyData.value.data,
          borderColor: '#f97316',
          backgroundColor: 'rgba(249,115,22,0.1)',
          fill: true,
          tension: 0.35,
          pointRadius: 4,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: { beginAtZero: false, grid: { color: '#f3f4f6' } },
          x: { grid: { display: false } },
        },
      },
    })
  }

  if (wasteCategoryCanvas.value) {
    wasteCategoryChart = new Chart(wasteCategoryCanvas.value, {
      type: 'doughnut',
      data: {
        labels: wasteCategoryData.value.labels,
        datasets: [{
          data: wasteCategoryData.value.data,
          backgroundColor: wasteCategoryData.value.labels.map((_, i) => colors[i % colors.length]),
          borderWidth: 2,
          borderColor: '#fff',
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom' },
          tooltip: {
            callbacks: {
              label: (ctx) => {
                const total = ctx.dataset.data.reduce((a, b) => a + b, 0)
                const pct = total ? ((ctx.parsed / total) * 100).toFixed(1) : '0'
                return ` ${ctx.label}: ${ctx.parsed}개 (${pct}%)`
              },
            },
          },
        },
      },
    })
  }
}

function renderInventoryCharts() {
  turnoverChart?.destroy()

  if (turnoverCanvas.value) {
    turnoverChart = new Chart(turnoverCanvas.value, {
      type: 'bar',
      data: {
        labels: turnoverData.value.labels,
        datasets: [{
          label: '재고 회전율 (회/월)',
          data: turnoverData.value.data,
          backgroundColor: turnoverData.value.data.map(v => v >= 4.5 ? '#4ade80' : v >= 4.0 ? '#f97316' : '#fb7185'),
          borderRadius: 6,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: { beginAtZero: true, grid: { color: '#f3f4f6' } },
          x: { grid: { display: false } },
        },
      },
    })
  }
}

function renderAllCharts() {
  renderWasteCharts()
  renderInventoryCharts()
}

watch(stats, () => {
  nextTick(renderAllCharts)
}, { deep: true })

onMounted(() => {
  fetchStats()
  nextTick(renderAllCharts)
})
onBeforeUnmount(() => destroyCharts())
</script>

<template>
  <div class="flex-1 overflow-auto p-5 space-y-8 bg-gray-50/30">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-[22px] font-bold text-gray-900 tracking-tight">ESG 대시보드</h1>
        <p class="text-xs text-gray-500 mt-1">친환경 경영을 위한 폐기량 관리 및 재고 효율 지표를 모니터링합니다.</p>
      </div>
      <div class="flex gap-2">
        <button 
          @click="fetchStats"
          :disabled="loading"
          class="flex items-center gap-2 px-3 py-1.5 bg-white border border-gray-200 rounded-lg text-xs font-medium text-gray-600 hover:bg-gray-50 transition-colors disabled:opacity-50 shadow-sm"
        >
          <RefreshCw class="w-3.5 h-3.5" :class="{ 'animate-spin': loading }" />
          새로고침
        </button>
        <button 
          @click="handleTriggerWaste"
          :disabled="triggerLoading"
          class="flex items-center gap-2 px-3 py-1.5 bg-[#F37321] text-white rounded-lg text-xs font-semibold hover:bg-[#e66a1a] transition-all shadow-sm disabled:opacity-50"
        >
          <Clock class="w-3.5 h-3.5" v-if="!triggerLoading" />
          <RefreshCw class="w-3.5 h-3.5 animate-spin" v-else />
          유통기한 경과 처리
        </button>
      </div>
    </div>

    <!-- 섹션 1: 폐기 최소화 -->
    <section class="space-y-4">
      <div class="flex items-center gap-2 pb-1 border-b border-gray-200">
        <Trash2 class="w-5 h-5 text-[#F37321]" />
        <h2 class="text-lg font-bold text-gray-900">폐기 최소화</h2>
      </div>

      <!-- 요약 카드 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div
          v-for="card in wasteCards"
          :key="card.title"
          class="rounded-2xl border border-gray-200 bg-white p-5 shadow-[0_2px_8px_rgba(15,23,42,0.03)]"
        >
          <p class="text-[11px] font-semibold text-gray-400 uppercase tracking-[0.12em]">{{ card.title }}</p>
          <div class="flex items-end gap-1 mt-2.5">
            <p class="text-[26px] leading-none font-extrabold tracking-tight" :class="card.valueClass ?? 'text-gray-900'">
              {{ card.value }}
            </p>
            <span v-if="card.unit" class="text-sm text-gray-400 mb-0.5">{{ card.unit }}</span>
          </div>
          <p class="text-xs text-gray-500 mt-3">{{ card.sub }}</p>
        </div>
      </div>

      <!-- 차트 영역 -->
      <div class="grid grid-cols-1 xl:grid-cols-2 gap-4">
        <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 flex flex-col" style="min-height: 300px">
          <div class="mb-4 shrink-0">
            <h2 class="font-bold text-gray-900">월별 폐기량 추이</h2>
            <p class="text-xs text-gray-400 mt-0.5">최근 6개월 폐기 수량 (개)</p>
          </div>
          <div class="flex-1 min-h-0 relative" style="height: 220px">
            <canvas ref="wasteMonthlyCanvas" class="block w-full h-full"></canvas>
          </div>
        </div>

        <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 flex flex-col" style="min-height: 300px">
          <div class="mb-4 shrink-0">
            <h2 class="font-bold text-gray-900">품목별 폐기 비중</h2>
            <p class="text-xs text-gray-400 mt-0.5">이번달 카테고리별 폐기 비중</p>
          </div>
          <div class="flex-1 min-h-0 relative flex items-center justify-center" style="height: 220px">
            <canvas ref="wasteCategoryCanvas" class="max-w-full" style="max-height: 200px"></canvas>
          </div>
        </div>
      </div>

      <!-- 유통기한 경고 후 소진 현황 -->
      <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5">
        <div class="mb-4">
          <h2 class="font-bold text-gray-900">유통기한 경고 후 소진 현황</h2>
          <p class="text-xs text-gray-400 mt-0.5">경고 발생 후 소진 성공 / 실패 입점 매장 현황</p>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-gray-100 bg-gray-50/70">
                <th class="text-left px-4 py-3 font-semibold text-gray-600">매장명</th>
                <th class="text-center px-4 py-3 font-semibold text-gray-600">경고 횟수</th>
                <th class="text-center px-4 py-3 font-semibold text-gray-600">소진 성공</th>
                <th class="text-center px-4 py-3 font-semibold text-gray-600">소진 실패</th>
                <th class="text-center px-4 py-3 font-semibold text-gray-600">성공률</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="row in expiryRows" :key="row.store" class="hover:bg-gray-50/50 transition-colors">
                <td class="px-4 py-3 font-medium text-gray-800">{{ row.store }}</td>
                <td class="px-4 py-3 text-center text-gray-600">{{ row.warned }}</td>
                <td class="px-4 py-3 text-center text-green-600 font-semibold">{{ row.success }}</td>
                <td class="px-4 py-3 text-center text-red-500 font-semibold">{{ row.fail }}</td>
                <td class="px-4 py-3 text-center">
                  <span
                    class="inline-block px-2 py-0.5 rounded-full text-xs font-bold"
                    :class="row.rate >= 80 ? 'bg-green-100 text-green-700' : row.rate >= 60 ? 'bg-amber-100 text-amber-700' : 'bg-red-100 text-red-600'"
                  >
                    {{ row.rate }}%
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <!-- 섹션 2: 재고 효율 -->
    <section class="space-y-4">
      <div class="flex items-center gap-2 pb-1 border-b border-gray-200">
        <RefreshCw class="w-5 h-5 text-blue-600" />
        <h2 class="text-lg font-bold text-gray-900">재고 효율</h2>
      </div>

      <!-- 요약 카드 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-4">
        <div
          v-for="card in inventoryCards"
          :key="card.title"
          class="rounded-2xl border border-gray-200 bg-white p-5 shadow-[0_2px_8px_rgba(15,23,42,0.03)]"
        >
          <p class="text-[11px] font-semibold text-gray-400 uppercase tracking-[0.12em]">{{ card.title }}</p>
          <div class="flex items-end gap-1 mt-2.5">
            <p class="text-[26px] leading-none font-extrabold tracking-tight" :class="card.valueClass ?? 'text-gray-900'">
              {{ card.value }}
            </p>
            <span v-if="card.unit" class="text-sm text-gray-400 mb-0.5">{{ card.unit }}</span>
          </div>
          <p class="text-xs text-gray-500 mt-3">{{ card.sub }}</p>
        </div>
      </div>

      <!-- 차트 영역 -->
      <div class="grid grid-cols-1 gap-4">
        <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 flex flex-col" style="min-height: 300px">
          <div class="mb-4 shrink-0">
            <h2 class="font-bold text-gray-900">매장별 재고 회전율</h2>
            <p class="text-xs text-gray-400 mt-0.5">이번달 기준 (회/월)</p>
          </div>
          <div class="flex-1 min-h-0 relative" style="height: 220px">
            <canvas ref="turnoverCanvas" class="block w-full h-full"></canvas>
          </div>
        </div>
      </div>

      <!-- 매장별 적정 재고 유지 현황 -->
      <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5">
        <div class="mb-4">
          <h2 class="font-bold text-gray-900">매장별 적정 재고 유지 현황</h2>
          <p class="text-xs text-gray-400 mt-0.5">이번달 기준 적정 재고 유지율 및 재고 회전율</p>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-gray-100 bg-gray-50/70">
                <th class="text-left px-4 py-3 font-semibold text-gray-600">매장명</th>
                <th class="text-center px-4 py-3 font-semibold text-gray-600">적정 재고 유지율</th>
                <th class="text-center px-4 py-3 font-semibold text-gray-600">재고 회전율</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="row in inventoryRows" :key="row.store" class="hover:bg-gray-50/50 transition-colors">
                <td class="px-4 py-3 font-medium text-gray-800">{{ row.store }}</td>
                <td class="px-4 py-3 text-center">
                  <span
                    class="inline-block px-2 py-0.5 rounded-full text-xs font-bold"
                    :class="row.stockRate >= 80 ? 'bg-green-100 text-green-700' : row.stockRate >= 60 ? 'bg-amber-100 text-amber-700' : 'bg-red-100 text-red-600'"
                  >
                    {{ row.stockRate }}%
                  </span>
                </td>
                <td class="px-4 py-3 text-center text-gray-600">{{ row.turnover }}회</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>
  </div>
</template>
