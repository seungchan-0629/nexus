import api from '@/plugins/axiosinterceptor'

// 제품 목록 조회
export const getProductList = (page = 0, size = 10, categoryName = null) => {
  const params = { page, size }
  if (categoryName) params.categoryName = categoryName
  return api.get('/product/list', { params })
}

// 신규 제품 등록
export const addNewProduct = (dto) =>
  api.post('/product/reg', dto)

// 기존 제품 수정
export const updateProduct = (dto) =>
  api.put(`/product/update`, dto)

// 제품 삭제
export const deleteProduct = (dto) =>
  api.delete(`/product/delete`, { data: dto })

// 제품 검색
export const searchProduct = (productName) =>
  api.get('/product/search', { params: { productName } })

// 가맹점별 제품 목록 조회
export const getStoreProductList = (page = 0, size = 10) =>
  api.get(`/product/store`, { params: { page, size } })

// 가맹점별 제품 검색
export const searchStoreProduct = (productName) =>
  api.get('/product/store/search', { params: { productName } })
