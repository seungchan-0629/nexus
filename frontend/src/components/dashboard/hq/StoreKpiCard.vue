<script setup>
import { ref, onMounted } from 'vue'
import KpiCard from '../KpiCard.vue'
import { getStoreKpi } from '@/api/dashboard'

const value = ref('-')
const sub = ref('이번 달 신규 -개')
const delta = ref(0)

onMounted(async () => {
  try {
    const { data } = await getStoreKpi()
    const result = data.result
    value.value = result.totalStoreCount.toLocaleString()
    sub.value = `이번 달 신규 ${result.newStoreCountThisMonth}개`
    delta.value = result.deltaPercent
  } catch (e) {
    console.error('가맹점 KPI 조회 실패', e)
  }
})
</script>

<template>
  <KpiCard title="입점 매장" :value="value" unit="개" :sub="sub" :delta="delta" to="/store" />
</template>
