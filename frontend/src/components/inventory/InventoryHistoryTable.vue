<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import InventoryMovementTypeBadge from '@/components/inventory/InventoryMovementTypeBadge.vue'
import {
  INVENTORY_HISTORY_PAGE_SIZE,
  qtyClassForDisplayType,
  qtyPrefixForDisplayType,
} from '@/constants/inventoryHistoryDisplay.js'

const props = defineProps({
  rows: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const PAGE = INVENTORY_HISTORY_PAGE_SIZE

const scrollRootRef = ref(null)
const sentinelRef = ref(null)
const visibleLimit = ref(PAGE)

let observer = null

function resetVisibleWindow() {
  const n = props.rows.length
  visibleLimit.value = n === 0 ? PAGE : Math.min(PAGE, n)
}

watch(() => props.rows, resetVisibleWindow, { deep: false, immediate: true })

const visibleRows = computed(() => props.rows.slice(0, visibleLimit.value))

const hasMore = computed(() => props.rows.length > visibleLimit.value)

const windowHint = computed(() => {
  const total = props.rows.length
  if (props.loading || total === 0 || total <= visibleLimit.value) return null
  return `${visibleRows.value.length} / ${total}건 표시 · 스크롤 시 추가 로드`
})

function disconnectObserver() {
  observer?.disconnect()
  observer = null
}

function connectObserver() {
  disconnectObserver()
  const root = scrollRootRef.value
  const target = sentinelRef.value
  if (!root || !target || !hasMore.value) return

  observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (!entry.isIntersecting) continue
        const total = props.rows.length
        if (visibleLimit.value >= total) return
        visibleLimit.value = Math.min(visibleLimit.value + PAGE, total)
      }
    },
    { root, rootMargin: '200px 0px', threshold: 0 },
  )
  observer.observe(target)
}

function syncObserver() {
  nextTick(() => {
    disconnectObserver()
    if (props.loading || props.rows.length === 0) return
    connectObserver()
  })
}

watch(
  () => [props.rows.length, props.loading, hasMore.value],
  () => syncObserver(),
)

onMounted(() => syncObserver())
onBeforeUnmount(disconnectObserver)
</script>

<template>
  <div class="bg-white border border-gray-200 rounded-lg overflow-hidden flex flex-col">
    <div class="px-5 py-3 border-b border-gray-100 bg-gray-50/60 text-xs text-gray-500 shrink-0">
      <span v-if="loading">불러오는 중…</span>
      <template v-else>
        총 <span class="font-bold text-gray-900">{{ rows.length }}</span>건
        <span v-if="windowHint" class="ml-2 text-gray-400 font-normal">{{ windowHint }}</span>
      </template>
    </div>

    <!--
      긴 목록: DOM에는 visibleRows만 두고, 하단 센티널로 점진적 로드.
      transition-group은 행 수만큼 enter/leave 비용이 커져 이 패턴에 맞지 않음.
    -->
    <div
      ref="scrollRootRef"
      class="max-h-[min(70vh,720px)] overflow-auto overscroll-contain"
    >
      <table class="w-full text-sm text-left">
        <thead class="sticky top-0 z-[1] bg-gray-50 shadow-[0_1px_0_0_rgb(229_231_235)]">
          <tr class="border-b border-gray-200">
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">일시</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">유형</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">품목명</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">가맹점</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">수량</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">처리자</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">비고</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="h in visibleRows" :key="h.id" class="hover:bg-gray-50/80 transition-colors">
            <td class="px-5 py-3.5 text-xs text-gray-500 tabular-nums">{{ h.datetime }}</td>
            <td class="px-5 py-3.5">
              <InventoryMovementTypeBadge :type="h.type" />
            </td>
            <td class="px-5 py-3.5 font-medium text-gray-900">{{ h.product }}</td>
            <td class="px-5 py-3.5 text-gray-600">{{ h.store }}</td>
            <td class="px-5 py-3.5 tabular-nums font-semibold" :class="qtyClassForDisplayType(h.type)">
              {{ qtyPrefixForDisplayType(h.type) }}{{ h.qty }}
            </td>
            <td class="px-5 py-3.5 text-gray-600 text-xs">{{ h.handler }}</td>
            <td class="px-5 py-3.5 text-gray-400 text-xs">{{ h.note }}</td>
          </tr>
          <tr v-if="rows.length === 0 && !loading">
            <td colspan="7" class="px-5 py-12 text-center text-gray-400 text-sm">조회된 이력이 없습니다.</td>
          </tr>
        </tbody>
      </table>

      <div
        v-if="hasMore"
        ref="sentinelRef"
        class="h-3 w-full shrink-0"
        aria-hidden="true"
      />
    </div>
  </div>
</template>
