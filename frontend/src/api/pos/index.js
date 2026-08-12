import api from '@/plugins/axiosinterceptor'

export const getPosInventoryList = (page = 0, size = 10) =>
  api.get('/pos/inventory/list', { params: { page, size } })

export const changePosInventoryCount = (posStoreInventoryIdx, count) =>
  api.patch(`/pos/inventory/${posStoreInventoryIdx}`, { count })

/** POS 화면용 메뉴 목록 (공통 Menu API, BaseResponse.result = MenuPageRes) */
export const getMenuListPaged = (page = 0, size = 50) =>
  api.get('/menu/list', { params: { page, size } })

/** POS 화면용 메뉴 카테고리 (BaseResponse.result = MenuCategoryRes[]) */
export const getMenuCategoryList = () => api.get('/menu/category/list')

/** POS 결제 저장 */
export const postPosPay = (body) => api.post('/pos/pay', body)

/** 당일 결제 내역 (BaseResponse.result = TodayPayRes[]) */
export const getPosPayToday = () => api.get('/pos/pay/today')

/** POS 영업 마감 → AI 자동 발주서 생성 */
export const postPosClose = () => api.post('/pos/close')
