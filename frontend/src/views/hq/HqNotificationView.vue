<script setup>
import { ref, onMounted } from 'vue'
import { Bell } from 'lucide-vue-next'
import { useNotificationStore } from '@/stores/notification'

const store = useNotificationStore()

// 페이지 진입 시 전체 알림 목록 조회
onMounted(() => {
  store.fetchNotifications(null, 20)
})

// 알림 타입별 필터 탭 목록
const tabs = [
  { label: '전체',        value: null },
  { label: '재고 부족',   value: 'LOW_STOCK' },
  { label: '유통기한',    value: 'EXPIRY' },
  { label: '비정상 감지', value: 'ABNORMAL_ORDER' },
  { label: '배송 지연',   value: 'DELIVERY_DELAY' },
]

// 현재 선택된 탭
const activeTab = ref(null)

// 탭 전환 시 해당 타입의 알림 목록 조회
async function switchTab(type) {
  activeTab.value = type
  await store.fetchNotifications(type, 20)
}

// 전체 읽음 처리
async function handleMarkAllRead() {
  await store.markAllRead()
}

// 알림 클릭 시 읽음 처리
async function handleClick(n) {
  if (!n.isRead) await store.markRead(n.idx)
}

// 알림 타입별 스타일 반환 (fallback 포함)
function typeConfig(type) {
  return store.typeConfig[type] ?? { label: type, color: 'text-gray-600', bg: 'bg-gray-50', border: 'border-gray-200' }
}

// 무한스크롤: 스크롤 하단 도달 시 다음 페이지 로드
function onScroll(e) {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  if (scrollHeight - scrollTop - clientHeight < 50) {
    store.loadMore(20)
  }
}
</script>

<template>
  <div class="p-6">
    <!-- Header -->
    <div class="flex items-center justify-between mb-5">
      <h1 class="text-xl font-bold text-gray-900">알림</h1>
      <button
        v-if="store.unreadCount > 0"
        @click="handleMarkAllRead"
        class="text-sm text-[#F37321] hover:underline font-medium cursor-pointer"
      >
        전체 읽음 처리
      </button>
    </div>

    <!-- Filter tabs -->
    <div class="flex gap-1 mb-4 border-b border-gray-200">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        @click="switchTab(tab.value)"
        class="px-4 py-2 text-sm font-medium transition-colors border-b-2 -mb-px cursor-pointer"
        :class="activeTab === tab.value
          ? 'border-[#F37321] text-[#F37321]'
          : 'border-transparent text-gray-500 hover:text-gray-700'"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- List -->
    <div v-if="store.notifications.length > 0" @scroll="onScroll" class="flex flex-col gap-2 max-h-[700px] overflow-y-auto">
      <button
        v-for="n in store.notifications"
        :key="n.idx"
        @click="handleClick(n)"
        class="w-full flex gap-3 p-4 rounded-lg border transition-colors text-left"
        :class="n.isRead
          ? 'bg-white border-gray-100 hover:bg-gray-50'
          : 'bg-orange-50/40 border-orange-100 hover:bg-orange-50/70'"
      >
        <!-- Type badge -->
        <div class="shrink-0 pt-0.5">
          <span
            class="inline-block text-[11px] font-semibold px-2 py-0.5 rounded-full border"
            :class="[typeConfig(n.type).color, typeConfig(n.type).bg, typeConfig(n.type).border]"
          >
            {{ typeConfig(n.type).label }}
          </span>
        </div>

        <!-- Content -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-0.5">
            <span class="text-sm font-semibold text-gray-900">{{ n.title }}</span>
            <span v-if="!n.isRead" class="w-1.5 h-1.5 rounded-full bg-[#F37321] shrink-0"></span>
          </div>
          <p class="text-sm text-gray-600 leading-snug">{{ n.content }}</p>
          <p class="text-xs text-gray-400 mt-1">{{ store.relativeTime(n.createdAt) }}</p>
        </div>
      </button>

      <!-- 로딩 인디케이터 -->
      <div v-if="store.loading" class="py-3 text-center text-sm text-gray-400">
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
