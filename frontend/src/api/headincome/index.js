import api from '@/plugins/axiosinterceptor'

// 본사 정산 관리 - 상단 카드 요약 조회
export const getHeadSettlementSummary = (year, month, storeName = '') =>
  api.get('/head/settlement/summary', { params: { year, month, storeName } })

// 본사 정산 관리 - 가맹점별 정산 리스트 페이징 조회
export const getHeadSettlementList = (year, month, storeName = '', page = 0, size = 10) =>
  api.get('/head/settlement/list', { params: { year, month, storeName, page, size } })
