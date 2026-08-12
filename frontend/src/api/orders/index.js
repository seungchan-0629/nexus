import api from '@/plugins/axiosinterceptor'

const getAutoOrders = (params = {}) => {
  return api.get('/orders/list/auto', { params })
}

const getOrderDetail = (ordersIdx) => {
  return api.get(`/orders/${ordersIdx}`)
}

const getDangerSettings = () => {
  return api.get('/orders/danger')
}

const updateDangerSettings = (data) => {
  return api.put('/orders/danger', data)
}

const createStoreManualOrder = (data) => {
  return api.post('/orders/store/reg/manual', data)
}

const cancelOrder = (ordersIdx) => {
  return api.put(`/orders/store/${ordersIdx}/cancel`)
}

const getOrderHistory = (params = {}) => {
  return api.get('/orders/list/history', { params })
}

const getConfirmedOrders = (params = {}) => {
  return api.get('/orders/list/confirmed', { params })

}

const getDangerOrders = (params = {}) => {
  return api.get('/orders/list/danger', { params })
}

const approveAllConfirmed = () => {
  return api.put('/orders/confirmed/approve')
}

const approveDangerOrder = (ordersIdx) => {
  return api.put(`/orders/${ordersIdx}/approve`)
}

const rejectDangerOrder = (ordersIdx) => {
  return api.put(`/orders/${ordersIdx}/reject`)
}

const getStoreOrderListPaged = (page = 0, size = 10) => {
  return api.get('/orders/store/list/paged', { params: { page, size } })
}

const getStorePendingOrders = () => {
  return api.get('/orders/store/find')
}

const confirmStoreOrder = (ordersIdx) => {
  return api.put(`/orders/store/${ordersIdx}/confirm`)
}

const addStoreOrderItem = (ordersIdx, data) => {
  return api.post(`/orders/store/${ordersIdx}/items`, data)
}

const rejectStoreOrder = (ordersIdx) => {
  return api.put(`/orders/store/${ordersIdx}/reject`)
}

const updateStoreItemCount = (ordersItemIdx, data) => {
  return api.put(`/orders/store/${ordersItemIdx}/items`, data)
}

const deleteStoreItem = (ordersItemIdx) => {
  return api.delete(`/orders/store/${ordersItemIdx}/items`)
}

export default {
  getAutoOrders,
  getOrderDetail,
  getOrderHistory,
  getDangerSettings,
  updateDangerSettings,
  createStoreManualOrder,
  cancelOrder,
  getConfirmedOrders,
  getDangerOrders,
  approveAllConfirmed,
  approveDangerOrder,
  rejectDangerOrder,
  getStoreOrderListPaged,
  getStorePendingOrders,
  confirmStoreOrder,
  addStoreOrderItem,
  rejectStoreOrder,
  updateStoreItemCount,
  deleteStoreItem,
}
