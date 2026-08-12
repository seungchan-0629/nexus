<script setup>
import { ref, onMounted } from 'vue'
import KpiCard from '../KpiCard.vue'
import { getSalesKpi } from '@/api/store-dashboard'

const value = ref('-')
const sub = ref('전일 대비')
const delta = ref(0)

onMounted(async () => {
  try {
    const { data } = await getSalesKpi()
    const result = data.result
    value.value = result.todaySales.toLocaleString()
    delta.value = result.deltaPercent
  } catch (e) {
    console.error('금일 매출 KPI 조회 실패', e)
  }
})
</script>

<template>
  <KpiCard title="금일 매출" :value="value" unit="원" :sub="sub" :delta="delta" to="/store-settlement" />
</template>
