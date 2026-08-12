<script setup>
import { ref, onMounted } from 'vue'
import KpiCard from '../KpiCard.vue'
import { getPendingOrderKpi } from '@/api/store-dashboard'

const value = ref('-')
const sub = ref('자동 발주 승인 대기')

onMounted(async () => {
  try {
    const { data } = await getPendingOrderKpi()
    value.value = data.result.pendingCount.toLocaleString()
  } catch (e) {
    console.error('제안 발주서 KPI 조회 실패', e)
  }
})
</script>

<template>
  <KpiCard title="미확정 발주" :value="value" unit="건" :sub="sub" to="/store-order" />
</template>
