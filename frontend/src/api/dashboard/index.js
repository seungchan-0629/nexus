import api from '@/plugins/axiosinterceptor'

export const getStoreKpi = () => api.get('/dashboard/store/kpi')

export const getOrdersKpi = () => api.get('/dashboard/orders/kpi')

export const getDeliveryKpi = () => api.get('/dashboard/delivery/kpi')

export const getInventoryKpi = () => api.get('/dashboard/inventory/kpi')

export const getDangerStats = () => api.get('/dashboard/orders/danger/stats')

export const getWeeklyOrderStats = () => api.get('/dashboard/orders/weekly/stats')

export const getDeliveryRatio = () => api.get('/dashboard/delivery/ratio')

export const getDangerInventory = (page = 0, size = 4) =>
  api.get('/dashboard/inventory/danger', { params: { page, size } })

export const getDelayDeliveryList = (page = 0, size = 4) =>
  api.get('/dashboard/delivery/delay', { params: { page, size } })
