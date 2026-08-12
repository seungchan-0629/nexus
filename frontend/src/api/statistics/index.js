import api from '@/plugins/axiosinterceptor'

export const getTodaySales = () => api.get('/statistics/sales/today')

export const getTopStores = () => api.get('/statistics/store/top')

export const getTopMenus = () => api.get('/statistics/menu/top')

export const getHourlySales = () => api.get('/statistics/sales/hourly')

export const getCategoryRatio = () => api.get('/statistics/category/ratio')

export const getPaymentRatio = () => api.get('/statistics/payment/ratio')

// ─── 장기 통계 (월/연/매장/카테고리별) ─────────────────────

export const getYearlySales = () =>
  api.get('/statistics/long-term/yearly')

export const getMonthlySales = (year) =>
  api.get('/statistics/long-term/monthly', { params: { year } })

export const getQuarterlySales = (year) =>
  api.get('/statistics/long-term/quarterly', { params: { year } })

export const getStoreSalesLongTerm = (year, month) =>
  api.get('/statistics/long-term/stores', {
    params: month ? { year, month } : { year },
  })

export const getCategorySalesLongTerm = (year, month) =>
  api.get('/statistics/long-term/categories', {
    params: month ? { year, month } : { year },
  })

export const getMenuSalesLongTerm = (year, month) =>
  api.get('/statistics/long-term/menus', {
    params: month ? { year, month } : { year },
  })
