<script setup>
import { ref, onMounted } from 'vue'
import KpiCard from '../KpiCard.vue'
import { getSettlementKpi } from '@/api/store-dashboard'

const value = ref('-')
const sub = ref('')
const delta = ref(0)

onMounted(async () => {
  try {
    const { data } = await getSettlementKpi()
    const result = data.result
    value.value = result.currentAmount.toLocaleString()
    sub.value = result.currentPeriod
    delta.value = result.deltaPercent
  } catch (e) {
    console.error('정산 현황 KPI 조회 실패', e)
  }
})
</script>

<template>
  <KpiCard title="정산 현황" :value="value" unit="원" :sub="sub" :delta="delta" to="/store-settlement" />
</template>
