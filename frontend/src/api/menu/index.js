import api from '@/plugins/axiosinterceptor'

export const getMenuList = (searchReq, page, size) => {
  return api.get('/menu/list', {
    params: {
      keyword: searchReq.keyword,
      categoryIdx: searchReq.categoryIdx,
      page: page,
      size: size
    }
  });
}

export const getProductList = (page = 0, size = 100) =>
  api.get('/product/list', { params: { page, size } })

export const getCategoryList = () => api.get('/menu/category/list')

export const getMenuItemList = (menuIdx) => api.get(`/menu/item/list/${menuIdx}`)

export const getPresignedUrl = (fileName, fileSize) =>
  api.get(`/menu/presignedUrl/${fileName}?fileSize=${fileSize}`)

export const postNewRegister = (menuRegReq) =>
  api.post('/menu/new/register', menuRegReq)

export const putMenuUpdate = (menuIdx,menuRegReq) =>
  api.put(`/menu/update/${menuIdx}`, menuRegReq)

export const putMenuDelete  = (menuIdx) =>
  api.put(`menu/delete/${menuIdx}`)

export const postMenuCategoryRegister = (categoryName) =>
  api.post('/menu/category/register', categoryName)

export const deleteCategory = (categoryIdx) =>
  api.delete(`/menu/category/delete/${categoryIdx}`)
