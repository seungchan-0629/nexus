import api from '@/plugins/axiosinterceptor'

// 본사 전체 배송 현황 조회 (페이징 및 모든 필터 포함)
export const getAllDeliveries = (params) =>
  api.get('/delivery/head', { params })

// 본사 배송 지연 사유 등록 및 수정
export const updateDelayReason = (dto) =>
  api.patch('/delivery/head/delayreason', dto)

// 가맹점 배송 현황 조회 (페이징 및 모든 필터 포함)
export const getMyStoreDeliveries = (params) =>
  api.get('/delivery/store', { params })
