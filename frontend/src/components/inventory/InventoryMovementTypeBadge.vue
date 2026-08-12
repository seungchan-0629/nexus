<script setup>
import { computed } from 'vue'
import { badgeClassForDisplayType } from '@/constants/inventoryHistoryDisplay.js'

const props = defineProps({
  type: {
    type: String,
    required: true,
  },
})

const badgeClass = computed(() => badgeClassForDisplayType(props.type))
</script>

<template>
  <span
    class="inline-flex items-center rounded-md px-2 py-1 text-[11px] font-medium leading-none tracking-tight"
    :class="badgeClass"
  >
    {{ type }}
  </span>
</template>
