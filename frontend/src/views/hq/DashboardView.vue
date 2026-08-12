<script setup>
import { computed } from 'vue'
import StoreKpiCard from '@/components/dashboard/hq/StoreKpiCard.vue'
import OrdersKpiCard from '@/components/dashboard/hq/OrdersKpiCard.vue'
import InventoryKpiCard from '@/components/dashboard/hq/InventoryKpiCard.vue'
import DeliveryKpiCard from '@/components/dashboard/hq/DeliveryKpiCard.vue'
import WeeklyOrderChart from '@/components/dashboard/hq/WeeklyOrderChart.vue'
import OngoingDeliveryList from '@/components/dashboard/hq/OngoingDeliveryList.vue'
import DangerOrderChart from '@/components/dashboard/hq/DangerOrderChart.vue'
import DeliveryRatioChart from '@/components/dashboard/hq/DeliveryRatioChart.vue'
import DangerInventoryList from '@/components/dashboard/hq/DangerInventoryList.vue'

const today = computed(() => new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' }))
</script>

<template>
  <div class="flex-1 overflow-auto p-5 space-y-4">
    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="text-[22px] font-bold text-gray-900 tracking-tight">운영 현황</h1>
      </div>
      <span class="text-xs text-gray-400 border border-gray-200 px-3 py-1.5 rounded-md bg-white">{{ today }}</span>
    </div>

    <!-- KPI 카드 4개 -->
    <div class="grid grid-cols-4 gap-4">
      <StoreKpiCard />
      <OrdersKpiCard />
      <InventoryKpiCard />
      <DeliveryKpiCard />
    </div>

    <!-- 중간 행: 라인차트 + 재고위험 -->
    <div class="grid grid-cols-3 gap-4">
      <WeeklyOrderChart />
      <DangerInventoryList />
    </div>

    <!-- 하단 행: 이상발주 바그래프 + 지연배송/배송비율 -->
    <div class="grid grid-cols-3 gap-4">
      <DangerOrderChart />
      <div class="space-y-4">
        <OngoingDeliveryList />
        <DeliveryRatioChart />
      </div>
    </div>

  </div>
</template>
