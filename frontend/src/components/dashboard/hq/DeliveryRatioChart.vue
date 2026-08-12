<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Chart } from 'chart.js/auto'
import { getDeliveryRatio } from '@/api/dashboard'

const donutCanvas = ref(null)
let chartInstance = null

const donutLegend = ref([
  { label: '배송완료', pct: 0, color: '#f97316' },
  { label: '배송중', pct: 0, color: '#60a5fa' },
  { label: '배송지연', pct: 0, color: '#f87171' },
  { label: '출고대기', pct: 0, color: '#a78bfa' },
  { label: '출발', pct: 0, color: '#34d399' },
])

const deliveryRate = computed(() => donutLegend.value[0].pct)

onMounted(async () => {
  try {
    const { data } = await getDeliveryRatio()
    const r = data.result
    const total = r.ready + r.start + r.delivering + r.delivered + r.delay

    if (total > 0) {
      const pct = (v) => Math.round(v * 1000 / total) / 10
      donutLegend.value[0].pct = pct(r.delivered)
      donutLegend.value[1].pct = pct(r.delivering)
      donutLegend.value[2].pct = pct(r.delay)
      donutLegend.value[3].pct = pct(r.ready)
      donutLegend.value[4].pct = pct(r.start)
    }
  } catch (e) {
    console.error('배송 비율 조회 실패', e)
  }

  chartInstance = new Chart(donutCanvas.value, {
    type: 'doughnut',
    data: {
      labels: donutLegend.value.map(d => d.label),
      datasets: [{
        data: donutLegend.value.map(d => d.pct),
        backgroundColor: donutLegend.value.map(d => d.color),
        borderWidth: 3,
        borderColor: '#ffffff',
        hoverBorderWidth: 3,
        hoverOffset: 6,
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      cutout: '72%',
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#1f2937',
          titleColor: '#f9fafb',
          bodyColor: '#d1d5db',
          padding: 10,
          cornerRadius: 10,
          callbacks: { label: (ctx) => ` ${ctx.label}: ${ctx.parsed}%` },
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
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5">
    <div class="flex items-center justify-between mb-4">
      <h2 class="font-bold text-gray-900">배송 비율</h2>
      <span class="text-xs text-gray-400">최근 1개월</span>
    </div>
    <div class="relative mx-auto" style="width:140px;height:140px">
      <canvas ref="donutCanvas"></canvas>
      <div class="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
        <p class="text-2xl font-bold text-gray-900">{{ deliveryRate }}%</p>
        <p class="text-xs text-gray-400 mt-0.5">배송완료율</p>
      </div>
    </div>
    <div class="mt-4 space-y-2">
      <div v-for="item in donutLegend" :key="item.label" class="flex items-center gap-2">
        <span class="w-2 h-2 rounded-full shrink-0" :style="{ background: item.color }"></span>
        <span class="text-xs text-gray-500 flex-1">{{ item.label }}</span>
        <div class="w-16 h-1 rounded-full bg-gray-100 overflow-hidden">
          <div class="h-full rounded-full" :style="{ width: item.pct + '%', background: item.color }"></div>
        </div>
        <span class="text-xs font-semibold text-gray-700 w-7 text-right">{{ item.pct }}%</span>
      </div>
    </div>
  </div>
</template>
