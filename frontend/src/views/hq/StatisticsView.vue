<script setup>
import { computed } from 'vue'
import TodaySalesCard from '@/components/statistics/TodaySalesCard.vue'
import StoreRankingList from '@/components/statistics/StoreRankingList.vue'
import MenuRankingList from '@/components/statistics/MenuRankingList.vue'
import HourlySalesChart from '@/components/statistics/HourlySalesChart.vue'
import CategoryRatioChart from '@/components/statistics/CategoryRatioChart.vue'
import PaymentRatioChart from '@/components/statistics/PaymentRatioChart.vue'

const today = computed(() =>
  new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' }),
)
</script>

<template>
  <div class="flex-1 overflow-auto p-5 space-y-4">
    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="text-[22px] font-bold text-gray-900 tracking-tight">실시간 통계</h1>
      </div>
      <span class="text-xs text-gray-400 border border-gray-200 px-3 py-1.5 rounded-md bg-white">{{ today }}</span>
    </div>

    <!-- 총 매출 KPI -->
    <div class="grid grid-cols-1">
      <TodaySalesCard />
    </div>

    <!-- 랭킹 -->
    <div class="grid grid-cols-2 gap-4">
      <StoreRankingList />
      <MenuRankingList />
    </div>

    <!-- 시간대별 매출 차트 -->
    <div class="grid grid-cols-1">
      <HourlySalesChart />
    </div>

    <!-- 비율 차트 -->
    <div class="grid grid-cols-2 gap-4">
      <CategoryRatioChart />
      <PaymentRatioChart />
    </div>
  </div>
</template>
