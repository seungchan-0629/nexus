<template>
  <Teleport to="body">
    <div class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="absolute inset-0 bg-black/40" @click="$emit('close')"></div>
      <div class="relative bg-white rounded-xl border border-gray-200 shadow-xl w-full max-w-2xl max-h-[85vh] flex flex-col overflow-hidden">

        <!-- 헤더 -->
        <div class="shrink-0 flex items-center justify-between px-6 py-4 border-b border-gray-200">
          <div class="flex items-center gap-2">
            <Newspaper class="w-4 h-4 shrink-0" :class="variantStyles.icon" />
            <span class="font-bold text-gray-900 text-sm">{{ news.summaryTitle }}</span>
          </div>
          <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 cursor-pointer">✕</button>
        </div>

        <!-- 내용 -->
        <div class="flex-1 overflow-y-auto px-6 py-5 flex flex-col gap-4">
          <div class="flex items-center gap-2">
            <span
              class="text-[11px] font-semibold px-2 py-0.5 rounded-full border"
              :class="categoryStyle(news.category).class"
            >
              {{ categoryStyle(news.category).label }}
            </span>
            <span class="text-xs text-gray-400">{{ formatDate(news.summaryDate) }}</span>
          </div>

          <div class="bg-gray-50 rounded-lg px-4 py-3">
            <p class="text-xs font-semibold text-gray-500 mb-2 uppercase tracking-wide">요약</p>
            <p v-if="loading" class="text-sm text-gray-400">불러오는 중...</p>
            <p v-else class="text-sm text-gray-700 leading-relaxed whitespace-pre-line">{{ contents }}</p>
          </div>

          <div v-if="news.url && news.url !== '-'">
            <p class="text-xs font-semibold text-gray-500 mb-2">원문 링크</p>
            <a
              :href="news.url"
              target="_blank"
              rel="noopener noreferrer"
              class="inline-flex items-center gap-1 text-xs text-blue-500 hover:underline"
            >
              <ExternalLink class="w-3 h-3" />
              원문 보기
            </a>
          </div>
        </div>

        <div class="shrink-0 px-6 py-4 border-t border-gray-100 flex justify-end">
          <button
            @click="$emit('close')"
            class="px-4 py-2 text-sm font-semibold text-gray-600 border border-gray-200 rounded hover:bg-gray-50 cursor-pointer"
          >닫기</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import { Newspaper, ExternalLink } from 'lucide-vue-next'
import { categoryStyle, formatDate } from '@/composables/useNews'

const props = defineProps({
  news:     { type: Object,  required: true },
  contents: { type: String,  default: '' },
  loading:  { type: Boolean, default: false },
  variant:  { type: String,  default: 'store' },
})

defineEmits(['close'])

const VARIANT_STYLES = {
  store: { icon: 'text-blue-500' },
  hq:    { icon: 'text-[#F37321]' },
}

const variantStyles = computed(() => VARIANT_STYLES[props.variant] ?? VARIANT_STYLES.store)
</script>
