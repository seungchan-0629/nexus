import api from '@/plugins/axiosinterceptor'

export const getInventoryMovements = () => api.get('/inventory/movements')

export default {
  getInventoryMovements,
}
