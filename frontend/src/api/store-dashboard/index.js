import api from '@/plugins/axiosinterceptor'

export const getSalesKpi = () => api.get('/store/dashboard/sales/kpi')

export const getPendingOrderKpi = () => api.get('/store/dashboard/orders/pending/kpi')

export const getInventoryRiskKpi = () => api.get('/store/dashboard/inventory/risk/kpi')

export const getSettlementKpi = () => api.get('/store/dashboard/settlement/kpi')

export const getDailySalesChart = () => api.get('/store/dashboard/sales/daily')

export const getMyDeliveryList = () => api.get('/store/dashboard/delivery/list')

export const getPendingOrderList = () => api.get('/store/dashboard/orders/pending/list')

export const getInventoryWarningList = () => api.get('/store/dashboard/inventory/risk/list')
