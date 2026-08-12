<template>
  <button
    @click="$emit('select', news)"
    class="w-full bg-white rounded-lg border border-gray-200 p-4 flex items-start justify-between gap-4 transition-colors text-left cursor-pointer"
    :class="variantStyles.hover"
  >
    <div class="flex items-start gap-3 min-w-0">
      <Newspaper class="w-4 h-4 shrink-0 mt-0.5" :class="variantStyles.icon" />
      <div class="min-w-0">
        <div class="flex items-center gap-2 mb-1">
          <span
            class="text-[11px] font-semibold px-2 py-0.5 rounded-full border"
            :class="categoryStyle(news.category).class"
          >
            {{ categoryStyle(news.category).label }}
          </span>
        </div>
        <p class="font-semibold text-gray-900 text-sm">{{ news.summaryTitle }}</p>
        <p class="text-xs text-gray-500 mt-1 line-clamp-1">{{ news.summaryContentsPreview }}</p>
      </div>
    </div>
    <span class="text-xs text-gray-400 shrink-0">{{ formatDate(news.summaryDate) }}</span>
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { Newspaper } from 'lucide-vue-next'
import { categoryStyle, formatDate } from '@/composables/useNews'

const props = defineProps({
  news: { type: Object, required: true },
  variant: { type: String, default: 'store' },
})

defineEmits(['select'])

const VARIANT_STYLES = {
  store: { hover: 'hover:border-blue-400 hover:bg-blue-50/20',       icon: 'text-blue-500' },
  hq:    { hover: 'hover:border-[#F37321] hover:bg-orange-50/20',    icon: 'text-[#F37321]' },
}

const variantStyles = computed(() => VARIANT_STYLES[props.variant] ?? VARIANT_STYLES.store)
</script>
