import api from '@/plugins/axiosinterceptor'

const getHeadInventoryList = (page = 0, size = 10) =>
  api.get('/head/inventory/list', { params: { page, size } })

export default {
  getHeadInventoryList,
}
