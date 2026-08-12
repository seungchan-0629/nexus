import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/plugins/axiosinterceptor'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('nexus_user') || 'null'))

  const isLoggedIn = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  // 백엔드는 Role.STORE를 주지만 프론트 라우터 메타는 STORE_OWNER를 기대합니다.
  const isStoreOwner = computed(() => user.value?.role === 'STORE_OWNER' || user.value?.role === 'STORE')

  const USER_PROFILES = {
    'admin@theventi.co.kr': {
      name: '이재혁',
      dept: 'SCM 통제센터',
      avatar: 'HQ',
    },
    'store01@theventi.co.kr': {
      name: '김동현',
      storeName: '한화빌딩점',
      storeId: 'S001',
      avatar: 'KD',
    },
  }

  function decodeJwtPayload(token) {
    const parts = token.split('.')
    if (parts.length < 2) return null

    try {
      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
      const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
      return JSON.parse(atob(padded))
    } catch {
      return null
    }
  }

  function getCookie(name) {
    const token = `${name}=`
    const matched = document.cookie
      .split(';')
      .map((part) => part.trim())
      .find((part) => part.startsWith(token))

    return matched ? decodeURIComponent(matched.slice(token.length)) : null
  }

  function buildUserFromToken(payload) {
    const email = payload?.email
    const role = payload?.role
    if (!email || !role) return null

    const mappedRole = role === 'STORE' ? 'STORE_OWNER' : role
    const profile = USER_PROFILES[email] ?? {}
    return {
      id: email,
      email,
      role: mappedRole,
      name: profile.name ?? email,
      avatar: profile.avatar ?? 'US',
      ...(profile.dept ? { dept: profile.dept } : {}),
      ...(profile.storeName ? { storeName: profile.storeName } : {}),
      ...(profile.storeId ? { storeId: profile.storeId } : {}),
    }
  }

  async function login(email, password) {
    try {
      await api.post('/login', {
        email: email,
        password: password,
      })

      const ctoken = getCookie('CTOKEN')
      const payload = ctoken ? decodeJwtPayload(ctoken) : null
      const nextUser = buildUserFromToken(payload)
      if (!nextUser) return false

      user.value = nextUser
      localStorage.setItem('nexus_user', JSON.stringify(nextUser))

      // 로그인 성공 후 상세 정보(storeIdx 등) 조회
      await fetchUserInfo()

      return true
    } catch {
      return false
    }
  }

  async function fetchUserInfo() {
    if (!user.value) return
    try {
      const res = await api.get('/user/mypage')
      console.log('MyPage Response:', res.data)
      if (res.data) {
        user.value = {
          ...user.value,
          storeId: res.data.storeIdx ? 'S' + String(res.data.storeIdx).padStart(3, '0') : user.value.storeId,
          rawStoreIdx: res.data.storeIdx ?? null,
          name: res.data.userName || user.value.name,
          tel: res.data.tel || user.value.tel,
        }
        localStorage.setItem('nexus_user', JSON.stringify(user.value))
        console.log('Updated User Object:', user.value)
      }
    } catch (error) {
      console.error('Failed to fetch user info:', error)
    }
  }

  async function logout() {
    try {
      await api.post('/logout')
    } catch (error) {
      console.error('Logout failed:', error)
    } finally {
      user.value = null
      localStorage.removeItem('nexus_user')
      window.location.href = '/'
    }
  }
  return { user, isLoggedIn, isAdmin, isStoreOwner, login, logout, fetchUserInfo }
})
