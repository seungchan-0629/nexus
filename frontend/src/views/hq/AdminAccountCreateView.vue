<template>
  <div class="p-5 space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-xl font-bold text-gray-900 tracking-tight">계정 관리</h1>
      </div>
      <button
        @click="openCreateModal"
        class="inline-flex items-center gap-2 px-4 py-2.5 rounded bg-[#F37321] text-white text-sm font-bold hover:bg-[#e0661d] cursor-pointer"
      >
        + 계정 생성
      </button>
    </div>

    <section class="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <div class="px-5 py-3 border-b border-gray-100 flex items-center justify-between">
        <h2 class="text-sm font-bold text-gray-800">계정 리스트</h2>
        <span class="text-xs text-gray-400">총 {{ pagination.totalCount }}명</span>
      </div>

      <table class="w-full text-sm text-left">
        <thead>
          <tr class="bg-gray-50 border-b border-gray-200">
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">No.</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">이름</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">권한</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">이메일</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">관리</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="(account, index) in accounts" :key="account.email" class="hover:bg-gray-50/60">
            <td class="px-5 py-3.5 text-xs font-mono text-gray-500">{{ (pagination.currentPage * pagination.currentSize) + index + 1 }}</td>
            <td class="px-5 py-3.5 font-semibold text-gray-900">{{ account.name }}</td>
            <td class="px-5 py-3.5 text-gray-600">{{ account.role }}</td>
            <td class="px-5 py-3.5 text-gray-600">{{ account.email }}</td>
            
            <td class="px-5 py-3.5">
              <div class="flex items-center justify-center gap-2">
                <button
                  @click="openEditModal(account)"
                  class="px-2.5 py-1.5 rounded border border-[#F37321] text-xs font-semibold text-[#F37321] hover:bg-orange-50 cursor-pointer"
                >
                  수정
                </button>
                <button
                  @click="openDeleteModal(account)"
                  class="px-2.5 py-1.5 rounded border border-red-400 text-xs font-semibold text-red-500 hover:bg-red-50 cursor-pointer"
                >
                  삭제
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="accounts.length === 0">
            <td colspan="6" class="px-5 py-12 text-center text-gray-400 text-sm">등록된 계정이 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- Pagination -->
    <div v-if="pagination.totalPage > 1" class="flex justify-center items-center gap-2 pt-2">
      <button
        class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
        :disabled="pagination.currentPage === 0"
        @click="changePage(pagination.currentPage - 1)">
        <ChevronLeft class="w-4 h-4" />
      </button>

      <button v-for="pageIdx in visiblePages" :key="pageIdx"
              class="w-8 h-8 rounded text-sm font-semibold cursor-pointer transition-colors"
              :class="pagination.currentPage === pageIdx
          ? 'bg-[#F37321] text-white'
          : 'text-gray-500 hover:bg-gray-50'"
              @click="changePage(pageIdx)">
        {{ pageIdx + 1 }}
      </button>

      <button
        class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
        :disabled="pagination.currentPage >= pagination.totalPage - 1"
        @click="changePage(pagination.currentPage + 1)">
        <ChevronRight class="w-4 h-4" />
      </button>
    </div>

    <div v-if="isFormModalOpen" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="absolute inset-0 bg-black/40" @click="closeFormModal"></div>
      <div class="relative w-full max-w-lg bg-white rounded-lg border border-gray-200 shadow-xl">
        <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
          <h3 class="font-bold text-gray-900">{{ formMode === 'create' ? '계정 생성' : '계정 수정' }}</h3>
          <button @click="closeFormModal" class="text-gray-400 hover:text-gray-600 cursor-pointer">✕</button>
        </div>

        <form class="p-6 space-y-4" @submit.prevent="submitForm">
          <!-- 계정 생성(create): 아이디 + 이메일만 입력 -->
          <div v-if="formMode === 'create'" class="grid grid-cols-2 gap-4">
            <div class="space-y-1.5">
              <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">성함</label>
              <input
                v-model.trim="form.name"
                type="text"
                placeholder="예: 김동현"
                class="w-full px-3 py-2 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none"
                required
              />
            </div>
            <div class="space-y-1.5">
              <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">이메일</label>
              <input
                v-model.trim="form.email"
                type="email"
                placeholder="예: user@hanwha-nexus.com"
                class="w-full px-3 py-2 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none"
                required
              />
            </div>
          </div>

          <!-- 계정 수정(edit): 기존 입력 필드 유지 -->
          <div v-else class="grid grid-cols-2 gap-4">
            <div class="space-y-1.5">
              <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">사번 (ID)</label>
              <input
                v-model.trim="form.id"
                type="text"
                :readonly="formMode === 'edit'"
                placeholder="예: A002"
                class="w-full px-3 py-2 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none read-only:bg-gray-100 read-only:text-gray-500"
                required
              />
            </div>
            <div class="space-y-1.5">
              <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">이름</label>
              <input
                v-model.trim="form.name"
                type="text"
                placeholder="예: 홍길동"
                class="w-full px-3 py-2 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none"
                required
              />
            </div>
          </div>

          <div v-if="formMode !== 'create'" class="grid grid-cols-2 gap-4">
            <div class="space-y-1.5">
              <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">부서</label>
              <input
                v-model.trim="form.dept"
                type="text"
                placeholder="예: 운영지원팀"
                class="w-full px-3 py-2 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none"
                required
              />
            </div>
            <div class="space-y-1.5">
              <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">이메일</label>
              <input
                v-model.trim="form.email"
                type="email"
                placeholder="예: user@hanwha-nexus.com"
                class="w-full px-3 py-2 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none"
                required
              />
            </div>
          </div>

          <div class="flex gap-3 pt-2">
            <button
              type="button"
              @click="closeFormModal"
              class="flex-1 py-2.5 rounded border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 cursor-pointer"
            >
              취소
            </button>
            <button
              type="submit"
              class="flex-1 py-2.5 rounded bg-[#F37321] text-white text-sm font-bold hover:bg-[#e0661d] cursor-pointer"
            >
              {{ formMode === 'create' ? '생성' : '수정 저장' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <ConfirmModal
      :open="isDeleteModalOpen"
      :title="`${selectedAccount?.name} 계정을 삭제하시겠습니까?`"
      message="삭제된 계정은 복구할 수 없습니다."
      confirm-text="삭제"
      type="danger"
      @close="closeDeleteModal"
      @confirm="confirmDelete" />
    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { ChevronRight, ChevronLeft } from 'lucide-vue-next'
import { getUserList } from '@/api/user/index.js'
import api from '@/plugins/axiosinterceptor'
import Toast from '@/components/common/Toast.vue'
import ConfirmModal from '@/components/common/ConfirmModal.vue'
import { useToast } from '@/composables/useToast'

const { toast, showToast } = useToast()

const accounts = ref([])
const pagination = reactive({
  totalPage: 0,
  totalCount: 0,
  currentPage: 0,
  currentSize: 20
})

const isFormModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const formMode = ref('create')
const selectedAccount = ref(null)

const form = reactive({
  id: '',
  name: '',
  dept: '',
  email: '',
})

async function fetchAccounts(page = 0) {
  try {
    const response = await getUserList(page, pagination.currentSize)
    
    // 현재 백엔드 사양: 전체 리스트 반환 (List<UserDto.UserListRes>)
    // 클라이언트에서 20명씩 잘라서 표시 (slice)
    const allData = response.data || []
    
    pagination.totalCount = allData.length
    pagination.totalPage = Math.ceil(allData.length / pagination.currentSize)
    pagination.currentPage = page
    
    const start = page * pagination.currentSize
    const end = start + pagination.currentSize
    accounts.value = allData.slice(start, end)
    
  } catch (error) {
    showToast('사용자 목록을 불러오는데 실패했습니다.', 'error')
  }
}

const changePage = (page) => {
  if (page < 0 || page >= pagination.totalPage) return
  fetchAccounts(page)
}

const visiblePages = computed(() => {
  const range = 10
  const currentGroup = Math.floor(pagination.currentPage / range)
  const start = currentGroup * range
  const end = Math.min(start + range, pagination.totalPage)
  const pages = []
  for (let i = start; i < end; i++) {
    pages.push(i)
  }
  return pages
})

onMounted(() => {
  fetchAccounts()
})

function resetForm() {
  form.id = ''
  form.name = ''
  form.dept = ''
  form.email = ''
}

function openCreateModal() {
  formMode.value = 'create'
  selectedAccount.value = null
  resetForm()
  isFormModalOpen.value = true
}

function openEditModal(account) {
  formMode.value = 'edit'
  selectedAccount.value = account
  form.id = account.id || ''
  form.name = account.name
  form.dept = account.role || ''
  form.email = account.email
  isFormModalOpen.value = true
}

function closeFormModal() {
  isFormModalOpen.value = false
  resetForm()
}

async function submitForm() {
  const normalizedEmail = form.email.toLowerCase()

  if (formMode.value === 'create') {
    // 중복 체크는 전체 데이터를 대상으로 해야 정확하지만, 
    // 현재는 fetchAccounts에서 잘린 데이터만 가지고 있으므로 
    // 실제 운영 환경에서는 서버에서 중복 체크를 수행해야 합니다.
    try {
      const response = await api.post('/store/signup', {
        email: normalizedEmail,
        name: form.name.trim(),
      })

      await fetchAccounts(pagination.currentPage)

      const responseData = response?.data
      let tempPassword = ''
      if (typeof responseData === 'string') {
        const match = responseData.match(/임시\s*비밀번호\s*:\s*(.*)/)
        tempPassword = (match?.[1] ?? responseData).trim()
      }

      if (tempPassword) showToast(`임시 비밀번호: ${tempPassword}`)
      else showToast('계정이 생성되었습니다.')
    } catch (e) {
      showToast('계정 생성에 실패했습니다.', 'error')
      return
    }
  } else {
    if (!selectedAccount.value) return
    showToast('수정 기능은 현재 API 연동이 필요합니다.', 'warning')
  }

  closeFormModal()
}

function openDeleteModal(account) {
  selectedAccount.value = account
  isDeleteModalOpen.value = true
}

function closeDeleteModal() {
  isDeleteModalOpen.value = false
  selectedAccount.value = null
}

function confirmDelete() {
  if (!selectedAccount.value) return
  showToast('삭제 기능은 현재 API 연동이 필요합니다.', 'warning')
  closeDeleteModal()
}
</script>
