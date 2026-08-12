<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Chart } from 'chart.js/auto'
import { getDangerStats } from '@/api/dashboard'

const barCanvas = ref(null)
let chartInstance = null

onMounted(async () => {
  let labels = []
  let confirmed = []
  let approved = []
  let rejected = []

  try {
    const { data } = await getDangerStats()
    const result = data.result
    labels = result.labels
    confirmed = result.confirmed
    approved = result.approved
    rejected = result.rejected
  } catch (e) {
    console.error('이상 발주 통계 조회 실패', e)
  }

  chartInstance = new Chart(barCanvas.value, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: '승인 대기',
          data: confirmed,
          backgroundColor: '#f87171',
          borderRadius: 4,
          borderSkipped: false,
        },
        {
          label: '승인',
          data: approved,
          backgroundColor: '#4ade80',
          borderRadius: 4,
          borderSkipped: false,
        },
        {
          label: '반려',
          data: rejected,
          backgroundColor: '#d1d5db',
          borderRadius: 4,
          borderSkipped: false,
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
          callbacks: { label: (ctx) => ` ${ctx.dataset.label}: ${ctx.parsed.y}건` },
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
          ticks: { color: '#9ca3af', font: { size: 11 }, stepSize: 2, precision: 0 },
          border: { display: false },
        },
      },
    },
  })
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.destroy()
    chartInstance = null
  }
})
</script>

<template>
  <div class="col-span-2 bg-white rounded-2xl border border-gray-200 shadow-sm p-5 flex flex-col"
    style="min-height:280px">
    <div class="flex items-center justify-between mb-5 shrink-0">
      <div>
        <h2 class="font-bold text-gray-900">이상 발주 통계</h2>
        <p class="text-xs text-gray-400 mt-0.5">최근 6개월 이상 발주 현황 (단위: 건)</p>
      </div>
      <div class="flex items-center gap-4 text-xs text-gray-500">
        <span class="flex items-center gap-1.5"><span
            class="w-2.5 h-2.5 rounded-sm bg-red-400 inline-block"></span>승인 대기</span>
        <span class="flex items-center gap-1.5"><span
            class="w-2.5 h-2.5 rounded-sm bg-green-400 inline-block"></span>승인</span>
        <span class="flex items-center gap-1.5"><span
            class="w-2.5 h-2.5 rounded-sm bg-gray-300 inline-block"></span>반려</span>
      </div>
    </div>
    <div class="flex-1 min-h-0 relative" style="height:200px">
      <canvas ref="barCanvas" style="display:block; width:100%; height:100%;"></canvas>
    </div>
  </div>
</template>
