import api from '@/plugins/axiosinterceptor'

// [가맹점] POS 재고 lot 기준 폐기 등록
export const createPosWasteLog = (body) => api.post('/wastelog/waste/pos', body)

// [본사] 유통기한 경과 상품 자동 폐기 처리 트리거
export const postOverDueDateWaste = () => api.post('/wastelog/over/due-date')

// [본사] 폐기 통계 및 추이 조회
export const getWasteStats = () => api.get('/wastelog/compare')

// [가맹점] 일반 폐기 처리
export const postStoreWasteDiscard = (body) => api.post('/wastelog/store/discard', body)
