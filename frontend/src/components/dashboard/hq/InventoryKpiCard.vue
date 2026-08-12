<script setup>
import { ref, onMounted } from 'vue'
import KpiCard from '../KpiCard.vue'
import { getInventoryKpi } from '@/api/dashboard'

const value = ref('-')
const sub = ref('위험 -건 · 긴급 -건')
const badge = ref(null)
const badgeDanger = ref(false)

onMounted(async () => {
  try {
    const { data } = await getInventoryKpi()
    const result = data.result

    value.value = result.totalDangerCount.toLocaleString()
    sub.value = `위험 ${result.lowCount}건 · 긴급 ${result.criticalCount}건`

    if (result.criticalCount > 0) {
      badge.value = `긴급 ${result.criticalCount}건`
      badgeDanger.value = true
    }
  } catch (e) {
    console.error('재고 위험 KPI 조회 실패', e)
  }
})
</script>

<template>
  <KpiCard title="재고 위험" :value="value" unit="건" :sub="sub" :badge="badge" :badge-danger="badgeDanger" to="/inventory/head-office" />
</template>
