import api from '@/plugins/axiosinterceptor'

// 생성
export const createCategory = (name) =>
  api.post('/category/reg', { categoryName: name })

// 조회
export const readCategoryList = () =>
  api.get('/category/list')

// 삭제
export const deleteCategory = (idx) =>
  api.delete(`/category/delete`, { data: { idx: idx }})
