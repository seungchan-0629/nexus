import api from '@/plugins/axiosinterceptor'

export const getHqNews = (date) =>
  api.get('/news', { params: { date } })

export const getMyStoreNews = (date) =>
  api.get('/news/my', { params: { date } })

export const getNewsDetail = (idx) =>
  api.get(`/news/${idx}`)
