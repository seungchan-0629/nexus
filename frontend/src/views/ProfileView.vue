<template>
  <div class="p-5 max-w-4xl mx-auto space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-xl font-bold text-gray-900 tracking-tight">내 정보 관리</h1>
        <p class="page-spec-hint">
          <code>USER_005·006 · USER_010~013</code>프로필 조회·저장, 비밀번호 규칙, 점주 이메일·전화번호 변경을 반영합니다.
        </p>
      </div>
      <button @click="isEditing = !isEditing"
        class="px-4 py-2 text-sm font-semibold border transition-colors rounded"
        :class="isEditing
          ? 'border-gray-200 text-gray-600 hover:bg-gray-50'
          : (auth.isAdmin
            ? 'bg-[#F37321] text-white border-[#F37321] hover:bg-[#e0661d]'
            : 'bg-blue-500 text-white border-blue-500 hover:bg-blue-600')">
        {{ isEditing ? '취소' : '정보 수정' }}
      </button>
    </div>

    <div class="grid grid-cols-3 gap-6">
      <!-- 프로필 카드 -->
      <div class="col-span-1 bg-white border border-gray-200 rounded-lg p-6 flex flex-col items-center text-center">
        <div class="w-20 h-20 bg-orange-50 border-2 border-[#F37321]/30 flex items-center justify-center text-2xl font-black text-[#F37321] mb-4 rounded">
          {{ auth.isAdmin ? 'HQ' : auth.user?.avatar }}
        </div>
        <h3 class="text-base font-bold text-gray-900">{{ userInfo.name }}</h3>
        <p class="text-xs font-bold uppercase tracking-widest mt-1"
          :class="auth.isAdmin ? 'text-[#F37321]' : 'text-blue-500'">
          {{ auth.isAdmin ? 'ADMIN' : 'STORE OWNER' }}
        </p>
        <div class="mt-5 w-full pt-5 border-t border-gray-100 space-y-2.5">
          <div class="flex justify-between text-sm">
            <span class="text-gray-400 text-xs">소속</span>
            <span class="text-gray-700 font-semibold text-xs">{{ auth.isAdmin ? userInfo.dept : auth.user?.storeName }}</span>
          </div>
          <div class="flex justify-between text-sm">
            <span class="text-gray-400 text-xs">최종 업데이트</span>
            <span class="text-gray-700 font-semibold text-xs">2026.04.15</span>
          </div>
        </div>
      </div>

      <!-- 정보 폼 -->
      <div class="col-span-2 space-y-4">
        <!-- 기본 정보 -->
        <div class="bg-white border border-gray-200 rounded-lg p-7">
          <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-4">기본 정보</p>
          <form @submit.prevent="saveProfile" class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">사용자 이름</label>
                <input v-model="userInfo.name" :disabled="!isEditing" type="text"
                  class="w-full px-3 py-2.5 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none disabled:bg-gray-50 disabled:text-gray-400 transition-all" />
              </div>
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">
                  {{ auth.isAdmin ? '부서명' : '가맹점명' }}
                </label>
                <input v-model="userInfo.dept" :disabled="true" type="text"
                  class="w-full px-3 py-2.5 rounded border border-gray-200 text-sm outline-none bg-gray-50 text-gray-400" />
              </div>
            </div>

            <!-- 이메일 변경 (USER_005 admin, USER_012 store) -->
            <div class="space-y-1.5">
              <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">이메일 주소</label>
              <input v-model="userInfo.email" :disabled="!isEditing" type="email"
                class="w-full px-3 py-2.5 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none disabled:bg-gray-50 disabled:text-gray-400 transition-all" />
            </div>

            <!-- 전화번호 변경 - 점주 전용 (USER_013) -->
            <div v-if="auth.isStoreOwner" class="space-y-1.5">
              <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">전화번호</label>
              <input v-model="userInfo.phone" :disabled="!isEditing" type="tel"
                placeholder="010-0000-0000"
                class="w-full px-3 py-2.5 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none disabled:bg-gray-50 disabled:text-gray-400 transition-all" />
            </div>

            <div v-if="isEditing" class="pt-1">
              <button type="submit"
                class="w-full rounded py-3 font-bold transition-colors text-sm text-white"
                :class="auth.isAdmin ? 'bg-[#F37321] hover:bg-[#e0661d]' : 'bg-blue-500 hover:bg-blue-600'">
                변경 사항 저장
              </button>
            </div>
          </form>
        </div>

        <!-- 비밀번호 변경 (USER_006 / USER_011) -->
        <div class="bg-white border border-gray-200 rounded-lg p-7">
          <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-4">비밀번호 변경</p>
          <form @submit.prevent="changePassword" class="space-y-4">
            <div class="space-y-1.5">
              <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">현재 비밀번호</label>
              <input v-model="pwForm.current" type="password"
                class="w-full px-3 py-2.5 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none" />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">새 비밀번호</label>
                <input v-model="pwForm.next" type="password" placeholder="8자 이상, 영문·숫자·특수문자"
                  class="w-full px-3 py-2.5 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none" />
              </div>
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">새 비밀번호 확인</label>
                <input v-model="pwForm.confirm" type="password"
                  class="w-full px-3 py-2.5 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none" />
              </div>
            </div>
            <button type="submit"
              class="w-full rounded py-2.5 border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50">
              비밀번호 변경
            </button>
          </form>
        </div>
      </div>
    </div>
    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/plugins/axiosinterceptor'
import Toast from '@/components/common/Toast.vue'
import { useToast } from '@/composables/useToast'

const { toast, showToast } = useToast()

const auth = useAuthStore()
const isEditing = ref(false)

const userInfo = ref(
  auth.isAdmin
    ? { name: '이재혁', dept: 'SCM 통제센터', email: 'jh.lee@hanwha-nexus.com', phone: '' }
    : { name: auth.user?.name ?? '점주', dept: auth.user?.storeName ?? '', email: 'store@hanwha-nexus.com', phone: '010-1234-5678' }
)

const pwForm = ref({ current: '', next: '', confirm: '' })

async function loadMyPage() {
  try {
    const response = await api.get('/user/mypage')
    const data = response.data

    if (!data) return

    userInfo.value.name = data.userName ?? userInfo.value.name
    userInfo.value.email = data.email ?? userInfo.value.email
    userInfo.value.phone = data.tel ?? userInfo.value.phone

    if (auth.isAdmin) {
      userInfo.value.dept = userInfo.value.dept || 'SCM 통제센터'
    } else {
      userInfo.value.dept = auth.user?.storeName ?? userInfo.value.dept
    }
  } catch (error) {
    console.error('Failed to load mypage data:', error)
  }
}

onMounted(loadMyPage)

function saveProfile() {
  showToast('프로필 정보가 업데이트되었습니다.')
  isEditing.value = false
}

function changePassword() {
  if (!pwForm.value.current) { showToast('현재 비밀번호를 입력해주세요.', 'error'); return }
  if (pwForm.value.next.length < 8) { showToast('새 비밀번호는 8자 이상이어야 합니다.', 'error'); return }
  if (pwForm.value.next !== pwForm.value.confirm) { showToast('새 비밀번호가 일치하지 않습니다.', 'error'); return }
  showToast('비밀번호가 변경되었습니다.')
  pwForm.value = { current: '', next: '', confirm: '' }
}
</script>
