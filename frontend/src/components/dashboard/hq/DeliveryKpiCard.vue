<script setup>
import { ref, onMounted } from 'vue'
import KpiCard from '../KpiCard.vue'
import { getDeliveryKpi } from '@/api/dashboard'

const value = ref('-')
const sub = ref('지연 -건')
const badge = ref('지연 0건')
const badgeDanger = ref(false)

onMounted(async () => {
  try {
    const { data } = await getDeliveryKpi()
    const result = data.result

    value.value = result.ongoingCount.toLocaleString()
    sub.value = `진행중 ${result.ongoingCount}건`
    badge.value = `지연 ${result.delayCount}건`
    badgeDanger.value = result.delayCount > 0
  } catch (e) {
    console.error('배송 KPI 조회 실패', e)
  }
})
</script>

<template>
  <KpiCard title="배송 현황" :value="value" unit="건" :sub="sub" :badge="badge" :badge-danger="badgeDanger" to="/delivery" />
</template>
