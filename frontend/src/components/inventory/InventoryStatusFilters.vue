<script setup>
import { computed, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    required: true,
  },
  statusFilters: {
    type: Array,
    required: true,
  },
})

const emit = defineEmits(['update:modelValue'])

const validFilterValues = computed(() => {
  return props.statusFilters.map((filter) => filter.value)
})

const fallbackFilterValue = computed(() => {
  return validFilterValues.value[0] ?? 'all'
})

watch(
  () => props.modelValue,
  (value) => {
    if (validFilterValues.value.includes(value)) return
    emit('update:modelValue', fallbackFilterValue.value)
  },
  { immediate: true }
)
</script>

<template>
  <div class="flex gap-3 items-center flex-wrap">
    <div class="flex gap-1.5">
      <button
        v-for="f in props.statusFilters"
        :key="f.value"
        type="button"
        class="px-3 py-1.5 text-sm font-semibold border rounded-lg transition-colors cursor-pointer"
        :class="props.modelValue === f.value
          ? 'bg-[#F37321] text-white border-[#F37321]'
          : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'"
        @click="emit('update:modelValue', f.value)"
      >
        {{ f.label }}
      </button>
    </div>
  </div>
</template>
