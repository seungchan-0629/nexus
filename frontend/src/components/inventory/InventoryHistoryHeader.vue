<script setup>
import { ArrowLeft } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
</script>

<template>
  <div class="flex justify-between items-center">
    <div>
      <h1 class="text-xl font-bold text-gray-900 tracking-tight">입출고 이력</h1>
    </div>
    <RouterLink
      to="/inventory/head-office"
      class="px-4 py-2 rounded border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 flex items-center gap-2"
    >
      <ArrowLeft class="w-4 h-4" /> 재고 현황
    </RouterLink>
  </div>
</template>
