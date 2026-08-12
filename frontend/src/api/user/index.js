import api from '@/plugins/axiosinterceptor'

export const getUserList = (page = 0, size = 10) => {
  return api.get('/user/list', {
    params: { page, size }
  });
}
