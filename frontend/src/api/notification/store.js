import api from '@/plugins/axiosinterceptor'

// 가맹점 알림 목록 조회 (타입별 필터 + 페이징)
const getNotifications = (type, page = 0, size = 20, isRead) => {
  const params = { page, size }
  if (type) params.type = type
  if (isRead !== undefined) params.isRead = isRead
  return api.get('/store/notification/list', { params })
}

// 미읽음 알림 개수 조회
const getUnreadCount = () => api.get('/store/notification/unread/count')

// 단건 읽음 처리
const markAsRead = (notificationIdx) =>
  api.put(`/store/notification/${notificationIdx}/read`)

// 전체 읽음 처리
const markAllAsRead = () => api.put('/store/notification/read/all')

export default {
  getNotifications,
  getUnreadCount,
  markAsRead,
  markAllAsRead,
}
