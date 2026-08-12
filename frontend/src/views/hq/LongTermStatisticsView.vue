<script setup>
import YearlyTotalCard from '@/components/statistics/YearlyTotalCard.vue'
import MonthlyTotalCard from '@/components/statistics/MonthlyTotalCard.vue'
import SalesTrendChart from '@/components/statistics/SalesTrendChart.vue'
import StoreSalesLongTermList from '@/components/statistics/StoreSalesLongTermList.vue'
import MenuSalesLongTermList from '@/components/statistics/MenuSalesLongTermList.vue'
</script>

<template>
  <div class="flex-1 overflow-auto p-5 space-y-4">
    <!-- 헤더 -->
    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="text-[22px] font-bold text-gray-900 tracking-tight">장기 통계</h1>
        <p class="text-xs text-gray-400 mt-1">연도/월/분기/매장/메뉴별 매출 분석</p>
      </div>
    </div>

    <!-- KPI 카드 (연도/월 — 큰 숫자 + sparkline + YoY) -->
    <div class="grid grid-cols-2 gap-4">
      <YearlyTotalCard />
      <MonthlyTotalCard />
    </div>

    <!-- 매출 추이 (연/분기/월 토글, full width) -->
    <div class="grid grid-cols-1">
      <SalesTrendChart />
    </div>

    <!-- 매장/메뉴 순위 (스크롤 + 검색) -->
    <div class="grid grid-cols-2 gap-4">
      <StoreSalesLongTermList />
      <MenuSalesLongTermList />
    </div>
  </div>
</template>
