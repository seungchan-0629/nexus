<script setup>
import { ref, computed, onMounted } from 'vue'
import { Bell } from 'lucide-vue-next'
import { useNotificationStore } from '@/stores/notification'

const store = useNotificationStore()

const tabs = [
  { label: '전체',   value: null },
  { label: '재고',   value: 'stock' },
  { label: '발주',   value: 'order' },
  { label: '배송',   value: 'delivery' },
]

const activeTab = ref(null)

const tabTypeMap = {
  stock:    ['EXPIRY', 'LOW_STOCK'],
  order:    ['ABNORMAL_ORDER'],
  delivery: ['DELIVERY_DELAY', 'DELIVERY_START'],
}

const filtered = computed(() => {
  if (activeTab.value === null) return store.storeNotifications
  const types = tabTypeMap[activeTab.value] ?? []
  return store.storeNotifications.filter(n => types.includes(n.type))
})

function changeTab(tabValue) {
  activeTab.value = tabValue
  // 탭 변경 시 전체 목록 재조회 (백엔드 단일 type 필터 제한으로 프론트 필터링)
  store.storeFetchNotifications(null)
}

function typeStyle(type) {
  return store.typeConfig[type] ?? { label: '알림', color: 'text-gray-600', bg: 'bg-gray-50', border: 'border-gray-200' }
}

function onScroll(e) {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  if (scrollHeight - scrollTop - clientHeight < 50) {
    store.storeLoadMore(20)
  }
}

onMounted(() => {
  store.storeFetchNotifications()
  store.storeFetchUnreadCount()
})
</script>

<template>
  <div class="p-6">
    <!-- Header -->
    <div class="flex items-center justify-between mb-5">
      <h1 class="text-xl font-bold text-gray-900">알림</h1>
      <button
        v-if="store.storeUnreadCount > 0"
        @click="store.storeMarkAllRead()"
        class="text-sm text-blue-600 hover:underline font-medium cursor-pointer"
      >
        전체 읽음 처리
      </button>
    </div>

    <!-- Filter tabs -->
    <div class="flex gap-1 mb-4 border-b border-gray-200">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        @click="changeTab(tab.value)"
        class="px-4 py-2 text-sm font-medium transition-colors border-b-2 -mb-px cursor-pointer"
        :class="activeTab === tab.value
          ? 'border-blue-500 text-blue-600'
          : 'border-transparent text-gray-500 hover:text-gray-700'"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- List -->
    <div v-if="filtered.length > 0" @scroll="onScroll" class="flex flex-col gap-2 max-h-[700px] overflow-y-auto">
      <button
        v-for="n in filtered"
        :key="n.idx"
        @click="store.storeMarkRead(n.idx)"
        class="w-full flex gap-3 p-4 rounded-lg border transition-colors text-left"
        :class="n.isRead
          ? 'bg-white border-gray-100 hover:bg-gray-50'
          : 'bg-blue-50/40 border-blue-100 hover:bg-blue-50/70'"
      >
        <!-- Type badge -->
        <div class="shrink-0 pt-0.5">
          <span
            class="inline-block text-[11px] font-semibold px-2 py-0.5 rounded-full border"
            :class="[typeStyle(n.type).color, typeStyle(n.type).bg, typeStyle(n.type).border]"
          >
            {{ typeStyle(n.type).label }}
          </span>
        </div>

        <!-- Content -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-0.5">
            <span class="text-sm font-semibold text-gray-900">{{ n.title }}</span>
            <span v-if="!n.isRead" class="w-1.5 h-1.5 rounded-full bg-blue-500 shrink-0"></span>
          </div>
          <p class="text-sm text-gray-600 leading-snug">{{ n.content }}</p>
          <p class="text-xs text-gray-400 mt-1">{{ store.relativeTime(n.createdAt) }}</p>
        </div>
      </button>

      <!-- 로딩 인디케이터 -->
      <div v-if="store.storeLoading" class="py-3 text-center text-sm text-gray-400">
        불러오는 중...
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="flex flex-col items-center justify-center py-20 text-gray-400">
      <Bell class="w-10 h-10 mb-3 opacity-30" />
      <p class="text-sm">알림이 없습니다.</p>
    </div>
  </div>
</template>
