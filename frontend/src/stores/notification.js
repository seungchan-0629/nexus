import { defineStore } from 'pinia'
import { ref } from 'vue'
import notificationApi from '@/api/notification'
import storeNotificationApi from '@/api/notification/store'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const hasNext = ref(false)
  const currentPage = ref(0)
  const currentType = ref(null)
  let eventSource = null

  const loading = ref(false)

  const currentIsRead = ref(undefined)

  // 알림 목록 조회 (첫 페이지)
  async function fetchNotifications(type = null, size = 10, isRead = undefined) {
    currentType.value = type
    currentIsRead.value = isRead
    currentPage.value = 0
    const res = await notificationApi.getNotifications(type, 0, size, isRead)
    notifications.value = res.data.result.content
    hasNext.value = !res.data.result.last
  }

  // 알림 목록 추가 로드 (다음 페이지)
  async function loadMore(size = 10) {
    if (!hasNext.value || loading.value) return
    loading.value = true
    currentPage.value++
    const res = await notificationApi.getNotifications(currentType.value, currentPage.value, size, currentIsRead.value)
    notifications.value.push(...res.data.result.content)
    hasNext.value = !res.data.result.last
    loading.value = false
  }

  // 미읽음 개수 조회
  async function fetchUnreadCount() {
    const res = await notificationApi.getUnreadCount()
    unreadCount.value = res.data.result.count
  }

  // 단건 읽음 처리
  async function markRead(idx) {
    await notificationApi.markAsRead(idx)
    const n = notifications.value.find(n => n.idx === idx)
    if (n) n.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }

  // 전체 읽음 처리
  async function markAllRead() {
    await notificationApi.markAllAsRead()
    notifications.value.forEach(n => { n.isRead = true })
    unreadCount.value = 0
  }

  // SSE 구독 시작
  function subscribe() {
    if (eventSource) return
    const baseUrl = import.meta.env.VITE_API_URL || '/api'
    eventSource = new EventSource(`${baseUrl}/notification/subscribe`)

    eventSource.addEventListener('notification', (event) => {
      try {
        const notification = JSON.parse(event.data)
        notifications.value.unshift(notification)
        unreadCount.value++
      } catch (e) {
        console.error('알림 데이터 파싱 실패', e)
      }
    })

    eventSource.onerror = () => {
      // 연결 끊기면 브라우저가 자동 재연결 시도
    }
  }

  // SSE 구독 해제
  function unsubscribe() {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
  }

  // 상대 시간 포맷
  function relativeTime(iso) {
    const diff = Date.now() - new Date(iso).getTime()
    const m = Math.floor(diff / 60000)
    if (m < 1) return '방금 전'
    if (m < 60) return `${m}분 전`
    const h = Math.floor(m / 60)
    if (h < 24) return `${h}시간 전`
    const d = Math.floor(h / 24)
    return `${d}일 전`
  }

  // === 가맹점 알림 ===
  const storeNotifications = ref([])
  const storeUnreadCount = ref(0)
  const storeHasNext = ref(false)
  const storeCurrentPage = ref(0)
  const storeCurrentType = ref(null)
  const storeCurrentIsRead = ref(undefined)
  const storeLoading = ref(false)
  let storeEventSource = null

  async function storeFetchNotifications(type = null, size = 10, isRead = undefined) {
    storeCurrentType.value = type
    storeCurrentIsRead.value = isRead
    storeCurrentPage.value = 0
    const res = await storeNotificationApi.getNotifications(type, 0, size, isRead)
    storeNotifications.value = res.data.result.content
    storeHasNext.value = !res.data.result.last
  }

  async function storeLoadMore(size = 10) {
    if (!storeHasNext.value || storeLoading.value) return
    storeLoading.value = true
    storeCurrentPage.value++
    const res = await storeNotificationApi.getNotifications(storeCurrentType.value, storeCurrentPage.value, size, storeCurrentIsRead.value)
    storeNotifications.value.push(...res.data.result.content)
    storeHasNext.value = !res.data.result.last
    storeLoading.value = false
  }

  async function storeFetchUnreadCount() {
    const res = await storeNotificationApi.getUnreadCount()
    storeUnreadCount.value = res.data.result.count
  }

  async function storeMarkRead(idx) {
    await storeNotificationApi.markAsRead(idx)
    const n = storeNotifications.value.find(n => n.idx === idx)
    if (n) n.isRead = true
    storeUnreadCount.value = Math.max(0, storeUnreadCount.value - 1)
  }

  async function storeMarkAllRead() {
    await storeNotificationApi.markAllAsRead()
    storeNotifications.value.forEach(n => { n.isRead = true })
    storeUnreadCount.value = 0
  }

  function storeSubscribe() {
    if (storeEventSource) return
    const baseUrl = import.meta.env.VITE_API_URL || '/api'
    storeEventSource = new EventSource(`${baseUrl}/store/notification/subscribe`)

    storeEventSource.addEventListener('notification', (event) => {
      try {
        const notification = JSON.parse(event.data)
        storeNotifications.value.unshift(notification)
        storeUnreadCount.value++
      } catch (e) {
        console.error('가맹점 알림 데이터 파싱 실패', e)
      }
    })

    storeEventSource.onerror = () => {
      // 연결 끊기면 브라우저가 자동 재연결 시도
    }
  }

  function storeUnsubscribe() {
    if (storeEventSource) {
      storeEventSource.close()
      storeEventSource = null
    }
  }

  // 알림 타입별 스타일 (백엔드 NotificationType enum 기준)
  const typeConfig = {
    EXPIRY:         { label: '유통기한',    color: 'text-amber-600',  bg: 'bg-amber-50',   border: 'border-amber-200'  },
    LOW_STOCK:      { label: '재고 부족',   color: 'text-amber-600',  bg: 'bg-amber-50',   border: 'border-amber-200'  },
    ABNORMAL_ORDER: { label: '비정상 감지', color: 'text-red-600',    bg: 'bg-red-50',     border: 'border-red-200'    },
    DELIVERY_DELAY: { label: '배송 지연',   color: 'text-blue-600',   bg: 'bg-blue-50',    border: 'border-blue-200'   },
    DELIVERY_START: { label: '배송 시작',   color: 'text-green-600',  bg: 'bg-green-50',   border: 'border-green-200'  },
    DELIVERED:      { label: '배송 완료',   color: 'text-green-600',  bg: 'bg-green-50',   border: 'border-green-200'  },
  }

  return {
    // 본사
    notifications,
    unreadCount,
    hasNext,
    loading,
    currentPage,
    currentType,
    currentIsRead,
    fetchNotifications,
    loadMore,
    fetchUnreadCount,
    markRead,
    markAllRead,
    subscribe,
    unsubscribe,
    // 가맹점
    storeNotifications,
    storeUnreadCount,
    storeHasNext,
    storeLoading,
    storeCurrentPage,
    storeCurrentType,
    storeCurrentIsRead,
    storeFetchNotifications,
    storeLoadMore,
    storeFetchUnreadCount,
    storeMarkRead,
    storeMarkAllRead,
    storeSubscribe,
    storeUnsubscribe,
    // 공통
    relativeTime,
    typeConfig,
  }
})
