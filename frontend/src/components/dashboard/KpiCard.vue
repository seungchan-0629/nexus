<script setup>
import { RouterLink } from 'vue-router'

defineProps({
  title: String,
  value: [String, Number],
  unit: String,
  sub: String,
  delta: Number,
  badge: String,
  badgeDanger: { type: Boolean, default: false },
  to: String,
})
</script>

<template>
  <RouterLink :to="to"
    class="block rounded-2xl border border-gray-200 bg-white p-5 shadow-[0_2px_8px_rgba(15,23,42,0.03)] transition-all hover:-translate-y-0.5 hover:shadow-[0_8px_20px_rgba(15,23,42,0.08)]">
    <div>
      <p class="text-[11px] font-semibold text-gray-400 uppercase tracking-[0.14em]">{{ title }}</p>
      <div class="flex items-end gap-1 mt-2.5">
        <p class="text-[30px] leading-none font-extrabold text-gray-900 tracking-tight">{{ value }}</p>
        <span class="text-sm text-gray-400 mb-1">{{ unit }}</span>
      </div>
    </div>
    <div class="mt-4 flex items-center justify-between">
      <span class="text-xs text-gray-500">{{ sub }}</span>
      <span v-if="badge" class="text-xs px-2 py-0.5 rounded-full font-medium"
        :class="badgeDanger ? 'bg-red-100 text-red-600' : 'bg-gray-100 text-gray-500'">{{ badge }}</span>
      <span v-else-if="delta != null" class="text-xs font-semibold" :class="delta > 0 ? 'text-emerald-600' : 'text-red-500'">
        {{ delta > 0 ? '+' : '' }}{{ delta }}%
      </span>
    </div>
  </RouterLink>
</template>
