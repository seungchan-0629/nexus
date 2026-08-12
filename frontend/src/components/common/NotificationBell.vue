<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { Bell } from 'lucide-vue-next'
import { useNotificationStore } from '@/stores/notification'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const open = ref(false)
const notifStore = useNotificationStore()
const auth = useAuthStore()
const router = useRouter()

const isStore = computed(() => auth.isStoreOwner)
const accentBadgeClass = computed(() => isStore.value ? 'bg-blue-500' : 'bg-[#F37321]')
const unreadRowClass = computed(() => isStore.value ? 'bg-blue-50/30 hover:bg-blue-50/60' : 'bg-orange-50/30 hover:bg-orange-50/60')

// 역할에 따른 unreadCount
const unreadCount = computed(() => isStore.value ? notifStore.storeUnreadCount : notifStore.unreadCount)

// 역할에 따른 알림 목록 (읽지 않은 것만)
const unreadNotifications = computed(() => {
  const list = isStore.value ? notifStore.storeNotifications : notifStore.notifications
  return list.filter(n => !n.isRead)
})

// 마운트 시 알림 조회 + SSE 구독
onMounted(async () => {
  if (isStore.value) {
    await notifStore.storeFetchUnreadCount()
    notifStore.storeSubscribe()
  } else {
    await notifStore.fetchUnreadCount()
    notifStore.subscribe()
  }
})

// 드롭다운 열릴 때 알림 조회
watch(open, async (isOpen) => {
  if (isOpen) {
    if (isStore.value) {
      await notifStore.storeFetchNotifications(null, 20)
    } else {
      await notifStore.fetchNotifications(null, 20)
    }
  }
})

// 언마운트 시 SSE 해제
onBeforeUnmount(() => {
  if (isStore.value) {
    notifStore.storeUnsubscribe()
  } else {
    notifStore.unsubscribe()
  }
})

async function markAll() {
  if (isStore.value) {
    await notifStore.storeMarkAllRead()
  } else {
    await notifStore.markAllRead()
  }
}

function handleNotifClick(n) {
  if (!n.isRead) {
    if (isStore.value) {
      notifStore.storeMarkRead(n.idx)
    } else {
      notifStore.markRead(n.idx)
    }
  }
  open.value = false
  router.push(isStore.value ? '/store-notification' : '/notification')
}

function onScroll(e) {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  if (scrollHeight - scrollTop - clientHeight < 50) {
    if (isStore.value) {
      notifStore.storeLoadMore(20)
    } else {
      notifStore.loadMore(20)
    }
  }
}

function typeColor(type) {
  const map = {
    EXPIRY: 'bg-amber-500',
    LOW_STOCK: 'bg-amber-500',
    ABNORMAL_ORDER: 'bg-red-500',
    DELIVERY_DELAY: 'bg-blue-500',
    DELIVERY_START: 'bg-green-500',
    DELIVERED: 'bg-green-500',
  }
  return map[type] ?? 'bg-gray-300'
}

function typeConfig(type) {
  return notifStore.typeConfig[type] ?? { label: '알림', color: 'text-gray-600', bg: 'bg-gray-50', border: 'border-gray-200' }
}
</script>

<template>
  <div class="relative">
    <!-- Backdrop -->
    <div v-if="open" @click="open = false" class="fixed inset-0 z-40"></div>

    <!-- Bell button -->
    <button @click="open = !open"
      class="relative flex items-center justify-center w-9 h-9 rounded hover:bg-gray-100 transition-colors z-50">
      <Bell class="w-4.5 h-4.5 text-gray-500" />
      <span v-if="unreadCount > 0"
        class="absolute top-1 right-1 min-w-[16px] h-4 flex items-center justify-center text-[10px] font-black text-white rounded-full px-1 leading-none"
        :class="accentBadgeClass">
        {{ unreadCount > 9 ? '9+' : unreadCount }}
      </span>
    </button>

    <!-- Dropdown panel -->
    <transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="opacity-0 translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-1">
      <div v-if="open"
        class="absolute right-0 mt-1 w-[380px] bg-white border border-gray-200 rounded-lg shadow-lg z-50 overflow-hidden">

        <!-- Panel header -->
        <div class="px-4 py-3 border-b border-gray-100 flex items-center justify-between bg-gray-50/60">
          <div class="flex items-center gap-2">
            <span class="text-sm font-bold text-gray-900">알림</span>
            <span v-if="unreadCount > 0"
              class="text-[11px] font-bold px-1.5 py-0.5 text-white rounded"
              :class="accentBadgeClass">
              {{ unreadCount }}
            </span>
          </div>
          <button v-if="unreadCount > 0" @click="markAll"
            class="text-xs text-gray-400 hover:text-gray-700 font-medium transition-colors">
            모두 읽음
          </button>
        </div>

        <!-- Notification list -->
        <div @scroll="onScroll" class="max-h-[420px] overflow-y-auto divide-y divide-gray-50">
          <div v-if="unreadNotifications.length === 0" class="px-4 py-10 text-center text-gray-400 text-sm">
            새 알림이 없습니다.
          </div>

          <div v-for="n in unreadNotifications" :key="n.idx"
            @click="handleNotifClick(n)"
            class="flex gap-3 px-4 py-3.5 cursor-pointer transition-colors"
            :class="unreadRowClass">

            <!-- Type badge dot -->
            <div class="shrink-0 mt-0.5">
              <div class="w-2 h-2 rounded-full mt-1.5"
                :class="typeColor(n.type)">
              </div>
            </div>

            <!-- Content -->
            <div class="flex-1 min-w-0">
              <div class="flex items-start justify-between gap-2">
                <div class="flex items-center gap-1.5 flex-wrap">
                  <span class="text-[11px] font-bold px-1.5 py-0.5 rounded border"
                    :class="[typeConfig(n.type).bg, typeConfig(n.type).color, typeConfig(n.type).border]">
                    {{ typeConfig(n.type).label }}
                  </span>
                  <span class="text-xs font-semibold text-gray-900">{{ n.title }}</span>
                </div>
                <span class="text-[11px] text-gray-400 shrink-0 mt-0.5">{{ notifStore.relativeTime(n.createdAt) }}</span>
              </div>
              <p class="text-xs text-gray-500 mt-1 leading-relaxed line-clamp-2">{{ n.content }}</p>
            </div>

            <!-- Unread indicator -->
            <div class="shrink-0 self-center">
              <div class="w-1.5 h-1.5 rounded-full" :class="accentBadgeClass"></div>
            </div>
          </div>

          <!-- Loading indicator -->
          <div v-if="notifStore.loading || notifStore.storeLoading" class="px-4 py-3 text-center text-xs text-gray-400">
            불러오는 중...
          </div>
        </div>

        <!-- Panel footer -->
        <div class="px-4 py-2.5 border-t border-gray-100 bg-gray-50/60">
          <p class="text-[11px] text-gray-400 text-center">전체 {{ unreadNotifications.length }}건</p>
        </div>
      </div>
    </transition>
  </div>
</template>
