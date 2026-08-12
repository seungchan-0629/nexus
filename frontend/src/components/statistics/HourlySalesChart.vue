<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Chart } from 'chart.js/auto'
import { getHourlySales } from '@/api/statistics'

const barCanvas = ref(null)
let chartInstance = null
let polling = null

const fetchAndRender = async () => {
  try {
    const { data } = await getHourlySales()
    const hourlyData = data.result.hourlyData

    const labels = Array.from({ length: 24 }, (_, i) => `${i}시`)
    const salesByHour = new Array(24).fill(0)
    hourlyData.forEach((item) => {
      salesByHour[item.hour] = item.sales
    })

    if (chartInstance) {
      chartInstance.data.datasets[0].data = salesByHour
      chartInstance.update()
      return
    }

    chartInstance = new Chart(barCanvas.value, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          {
            label: '매출',
            data: salesByHour,
            backgroundColor: 'rgba(249,115,22,0.7)',
            borderRadius: 4,
            barPercentage: 0.6,
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
            callbacks: {
              label: (ctx) => ` ₩ ${ctx.parsed.y.toLocaleString('ko-KR')}`,
            },
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
            ticks: {
              color: '#9ca3af',
              font: { size: 11 },
              callback: (v) => `₩${(v / 10000).toFixed(0)}만`,
            },
            border: { display: false },
          },
        },
      },
    })
  } catch (e) {
    console.error('시간대별 매출 조회 실패', e)
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
  <div class="col-span-full bg-white rounded-2xl border border-gray-200 shadow-sm p-5 flex flex-col"
    style="min-height:300px">
    <div class="mb-5 shrink-0">
      <h2 class="font-bold text-gray-900">시간대별 매출 추이</h2>
      <p class="text-xs text-gray-400 mt-0.5">오늘 0시~23시 (단위: 원)</p>
    </div>
    <div class="flex-1 min-h-0 relative" style="height:220px">
      <canvas ref="barCanvas" style="display:block; width:100%; height:100%;"></canvas>
    </div>
  </div>
</template>
