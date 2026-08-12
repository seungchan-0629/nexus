<script setup>
import { computed } from 'vue'
import SalesKpiCard from '@/components/dashboard/store/SalesKpiCard.vue'
import PendingOrderKpiCard from '@/components/dashboard/store/PendingOrderKpiCard.vue'
import InventoryRiskKpiCard from '@/components/dashboard/store/InventoryRiskKpiCard.vue'
import SettlementKpiCard from '@/components/dashboard/store/SettlementKpiCard.vue'
import DailySalesChart from '@/components/dashboard/store/DailySalesChart.vue'
import MyDeliveryList from '@/components/dashboard/store/MyDeliveryList.vue'
import PendingOrderList from '@/components/dashboard/store/PendingOrderList.vue'
import InventoryWarningList from '@/components/dashboard/store/InventoryWarningList.vue'

const today = computed(() => new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' }))
</script>

<template>
  <div class="flex-1 overflow-auto p-5 space-y-4">
    <!-- Header -->
    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="text-[22px] font-bold text-gray-900 tracking-tight">매장 현황</h1>
      </div>
      <span class="text-xs text-gray-400 border border-gray-200 px-3 py-1.5 rounded-md bg-white">{{ today }}</span>
    </div>

    <!-- KPI 카드 4개 -->
    <div class="grid grid-cols-4 gap-4">
      <SalesKpiCard />
      <PendingOrderKpiCard />
      <InventoryRiskKpiCard />
      <SettlementKpiCard />
    </div>

    <!-- 중간 행: 일별 매출 라인차트 + 배송 현황 -->
    <div class="grid grid-cols-3 gap-4">
      <DailySalesChart />
      <MyDeliveryList />
    </div>

    <!-- 하단 행: 미확정 발주 목록 + 재고 위험 품목 -->
    <div class="grid grid-cols-2 gap-4">
      <PendingOrderList />
      <InventoryWarningList />
    </div>
  </div>
</template>
