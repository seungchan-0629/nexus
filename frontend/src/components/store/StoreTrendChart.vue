<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Chart } from 'chart.js/auto'
import { getStoreMonthlyTrend } from '@/api/store/index.js'

const barCanvas = ref(null)
let chartInstance = null

// 선택 연도 (기본: 올해) + 드롭다운 옵션 (올해/작년/재작년 = 최근 3년)
const selectedYear = ref(new Date().getFullYear())
const yearOptions = computed(() => {
  const now = new Date().getFullYear()
  return [now, now - 1, now - 2]
})

// 월별 입점/폐점 데이터를 조회해 차트를 그리거나 갱신
async function loadAndRender() {
  let labels = []
  let openedCounts = []
  let closedCounts = []

  // 선택한 연도의 월별 입점/폐점 건수 조회
  try {
    const { data } = await getStoreMonthlyTrend(selectedYear.value)
    const result = data.result
    labels = result.labels
    openedCounts = result.openedCounts
    closedCounts = result.closedCounts
  } catch (e) {
    console.error('월별 입점/폐점 추이 조회 실패', e)
  }

  // 차트가 이미 있으면 데이터만 교체 후 갱신 (재생성 방지)
  if (chartInstance) {
    chartInstance.data.labels = labels
    chartInstance.data.datasets[0].data = openedCounts
    chartInstance.data.datasets[1].data = closedCounts
    chartInstance.update()
    return
  }

  chartInstance = new Chart(barCanvas.value, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: '입점',
          data: openedCounts,
          backgroundColor: '#22c55e', // Green
          borderRadius: 4,
          maxBarThickness: 18,
        },
        {
          label: '폐점',
          data: closedCounts,
          backgroundColor: '#ef4444', // Red
          borderRadius: 4,
          maxBarThickness: 18,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#1f2937',
          titleColor: '#f9fafb',
          bodyColor: '#d1d5db',
          padding: 10,
          cornerRadius: 10,
          callbacks: { label: (ctx) => ` ${ctx.dataset.label}: ${ctx.parsed.y}개` },
        },
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: '#9ca3af', font: { size: 11 } },
          border: { display: false },
        },
        y: {
          min: 0,
          grid: { color: '#f3f4f6' },
          ticks: { color: '#9ca3af', font: { size: 11 }, precision: 0 },
          border: { display: false },
        },
      },
    },
  })
}

onMounted(loadAndRender)

// 부모(StoreView)에서 가맹점 등록/수정 후 차트를 다시 그릴 수 있도록 노출
defineExpose({ reload: loadAndRender })

// 컴포넌트 해제 시 차트 인스턴스 정리 (메모리 누수 방지)
onUnmounted(() => {
  if (chartInstance) {
    chartInstance.destroy()
    chartInstance = null
  }
})
</script>

<template>
  <div class="bg-white rounded-lg border border-gray-200 shadow-sm p-5 flex flex-col" style="min-height:280px">
    <div class="flex items-center justify-between mb-5 shrink-0">
      <div>
        <h2 class="font-bold text-gray-900">월별 입점/폐점 추이</h2>
        <p class="text-xs text-gray-400 mt-0.5">최근 3년 선택 (단위: 개)</p>
      </div>
      <div class="flex items-center gap-3">
        <!-- 연도 선택: 변경 시 해당 연도로 재조회 -->
        <select v-model="selectedYear" @change="loadAndRender"
                class="text-xs border border-gray-200 rounded-md px-2 py-1 text-gray-600 cursor-pointer focus:border-[#F37321] outline-none">
          <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
        </select>
        <div class="flex items-center gap-4 text-xs text-gray-500">
          <span class="flex items-center gap-1.5"><span class="w-3 h-3 bg-[#22c55e] inline-block rounded"></span>입점</span>
          <span class="flex items-center gap-1.5"><span class="w-3 h-3 bg-[#ef4444] inline-block rounded"></span>폐점</span>
        </div>
      </div>
    </div>
    <div class="flex-1 min-h-0 relative" style="height:200px">
      <canvas ref="barCanvas" style="display:block; width:100%; height:100%;"></canvas>
    </div>
  </div>
</template>
