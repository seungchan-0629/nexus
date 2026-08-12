<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Chart } from 'chart.js/auto'
import { getCategoryRatio } from '@/api/statistics'

const doughnutCanvas = ref(null)
let chartInstance = null
let polling = null

const COLORS = ['#f97316', '#fb923c', '#fdba74', '#fed7aa', '#ffedd5', '#fff7ed', '#9ca3af']

const fetchAndRender = async () => {
  try {
    const { data } = await getCategoryRatio()
    const ratios = data.result.ratios

    const labels = ratios.map((r) => r.name)
    const amounts = ratios.map((r) => r.amount)

    if (chartInstance) {
      chartInstance.data.labels = labels
      chartInstance.data.datasets[0].data = amounts
      chartInstance.update()
      return
    }

    chartInstance = new Chart(doughnutCanvas.value, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [
          {
            data: amounts,
            backgroundColor: COLORS.slice(0, labels.length),
            borderWidth: 0,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '60%',
        plugins: {
          legend: {
            position: 'right',
            labels: { color: '#6b7280', font: { size: 12 }, padding: 12 },
          },
          tooltip: {
            backgroundColor: '#1f2937',
            titleColor: '#f9fafb',
            bodyColor: '#d1d5db',
            padding: 10,
            cornerRadius: 10,
            callbacks: {
              label: (ctx) => ` ₩ ${ctx.parsed.toLocaleString('ko-KR')}`,
            },
          },
        },
      },
    })
  } catch (e) {
    console.error('카테고리별 매출 조회 실패', e)
  }
}

onMounted(() => {
  fetchAndRender()
  polling = setInterval(fetchAndRender, 2000)
})

onUnmounted(() => {
  if (polling) clearInterval(polling)
  if (chartInstance) {
    chartInstance.destroy()
    chartInstance = null
  }
})
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 flex flex-col" style="min-height:280px">
    <div class="mb-4 shrink-0">
      <h2 class="font-bold text-gray-900">카테고리별 매출</h2>
      <p class="text-xs text-gray-400 mt-0.5">오늘 메뉴 카테고리 기준</p>
    </div>
    <div class="flex-1 min-h-0 relative" style="height:200px">
      <canvas ref="doughnutCanvas" style="display:block; width:100%; height:100%;"></canvas>
    </div>
  </div>
</template>
