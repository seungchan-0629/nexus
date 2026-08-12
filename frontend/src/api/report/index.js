import api from '@/plugins/axiosinterceptor.js'

export const postReportGenerate = (chatRequest) =>
  api.post('/report/generate', chatRequest)

export const getReportList = (page, size) =>
  api.get(`/report/list?page=${page}&size=${size}`)
