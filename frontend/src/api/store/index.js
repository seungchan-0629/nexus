import api from '@/plugins/axiosinterceptor'

export const getStoreList = (searchReq, page, size) => {
  return api.get('/store/list', {
    params: {
      keyword: searchReq.keyword,
      status: searchReq.status,
      page: page,
      size: size
    }
  });
}

export const getStoreTotal = () =>
  api.get('/store/totalCount/list')

export const getStoreDetailList = (storeIdx) => api.get(`/store/detail/list/${storeIdx}`)

export const searchStoreList = (keyword = '') =>
  api.get('/store/search', { params: { keyword } })

export const getStoreInventoryByStore = (storeIdx, page = 0, size = 10) =>
  api.get(`/store/inventory/list/${storeIdx}`, { params: { page, size } })

export const getPresignedUrl = (fileName, fileSize ) =>
  api.get(`/store/presignedUrl/${fileName}?fileSize=${fileSize}`)

export const postNewRegister = (storeRegDto)=>
  api.post('/store/new/register',storeRegDto)

export const putStoreUpdate = (storeIdx,updateData) =>
  api.put(`/store/update/${storeIdx}`,updateData)


export const getSettlement = (year, month, page = 0, size = 10) =>
  api.get('/store/income/settlement', { params: { year, month, page, size }})

export const getOrderSettlement = (year, month, page = 0, size = 10) =>
  api.get('/store/order/settlement', { params: { year, month, page, size }})

// 월별 입점/폐점 추이 (연도 선택)
export const getStoreMonthlyTrend = (year) =>
  api.get('/store/stats/monthly', { params: { year } })
