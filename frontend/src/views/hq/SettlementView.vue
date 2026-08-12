<template>
  <div class="p-5 space-y-4">

    <div class="flex flex-col gap-3">
      <div class="flex justify-between items-start flex-wrap gap-4">
        <div class="min-w-0">
          <h1 class="text-xl font-bold text-gray-900 tracking-tight">정산 관리</h1>
        </div>
      </div>

      <div class="flex flex-wrap gap-2 items-center">
        <select
          v-model="selectedMonth"
          @change="onMonthChange"
          class="px-4 py-2 rounded-lg border border-gray-200 text-sm bg-white shadow-sm
                 focus:border-[#F37321] focus:ring-1 focus:ring-[#F37321] outline-none"
        >
          <option>2026-04</option>
          <option>2026-03</option>
          <option>2026-02</option>
        </select>

        <div class="relative flex-1 min-w-[12rem] max-w-md">
          <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
            <svg class="w-4 h-4 text-gray-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <input
            v-model="settlementSearch"
            @input="onSearchInput"
            type="search"
            placeholder="매장명 검색…"
            class="w-full pl-10 px-3 py-2 rounded-lg border border-gray-200 text-sm
                 bg-white shadow-sm focus:border-[#F37321] focus:ring-1 focus:ring-[#F37321] outline-none"
          />
        </div>
      </div>
    </div>

    <!-- 매장 청구 합계 카드 -->
    <div class="grid grid-cols-1 gap-4">
      <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
        <div class="p-5">
          <div class="flex items-center gap-2">
            <p class="text-xs font-bold text-gray-400 uppercase tracking-wider">매장 청구 합계</p>
            <p class="text-xs font-bold text-gray-400">(단위: 천원)</p>
          </div>
          <p class="text-2xl font-black text-green-600">₩ {{ Math.floor(totalBillingAmount / 1000).toLocaleString() }}</p>
          <p class="text-xs text-gray-400 mt-2 pt-2 border-t border-gray-100">{{ selectedMonth }}</p>
        </div>
      </div>
    </div>

    <div class="flex border-b border-gray-200">
      <button
        v-for="tab in ['입점 매장 정산']"
        :key="tab"
        @click="activeTab = tab"
        class="px-5 py-2.5 text-sm font-semibold border-b-2 -mb-px transition-colors cursor-pointer"
        :class="activeTab === tab
          ? 'border-[#F37321] text-[#F37321]'
          : 'border-transparent text-gray-500 hover:text-gray-700'"
      >
        {{ tab }}
      </button>
    </div>

    <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <table class="w-full text-sm text-left">
        <thead>
        <tr class="border-b border-gray-200 bg-gray-50">
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">대상</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">정산 기간</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">발주/납품 건수</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">정산금액 (천원)</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">상태</th>
        </tr>
        </thead>

        <tbody class="divide-y divide-gray-100">
        <tr v-for="s in settlementList" :key="s.storeIdx" class="hover:bg-gray-50/50 transition-colors">
          <td class="px-5 py-3.5 font-semibold text-gray-900">{{ s.storeName }}</td>
          <td class="px-5 py-3.5 text-xs text-gray-400 font-mono">{{ s.periodStart }} ~ {{ s.periodEnd }}</td>
          <td class="px-5 py-3.5 text-gray-600">{{ s.orderCount }}건</td>
          <td class="px-5 py-3.5 font-bold text-gray-900">₩ {{ Math.floor((s.totalPrice || 0) / 1000).toLocaleString() }}</td>
          <td class="px-5 py-3.5">
            <span
              class="text-xs font-bold px-2 py-0.5 rounded"
              :class="s.status
                ? 'bg-green-50 text-green-700 border border-green-200'
                : 'bg-amber-50 text-amber-600 border border-amber-200'"
            >
              {{ s.status ? '정산완료' : '대기' }}
            </span>
          </td>
          <td class="px-5 py-3.5 text-center">
          </td>
        </tr>

        <tr v-if="settlementList.length === 0">
          <td colspan="6" class="px-5 py-10 text-center text-sm text-gray-400">
            검색 결과가 없습니다.
          </td>
        </tr>
        </tbody>
      </table>

      <!-- 페이징 -->
      <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 py-4 border-t border-gray-100">
        <button
          @click="changePage(currentPage - 1)"
          :disabled="currentPage === 0"
          class="p-2 border rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M15 19l-7-7 7-7" stroke-width="2"/></svg>
        </button>
        <div class="flex gap-1">
          <button
            v-for="p in totalPages"
            :key="p"
            @click="changePage(p - 1)"
            class="w-9 h-9 text-sm font-semibold rounded-lg transition-colors"
            :class="currentPage === p - 1 ? 'bg-[#F37321] text-white' : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'">
            {{ p }}
          </button>
        </div>
        <button
          @click="changePage(currentPage + 1)"
          :disabled="currentPage === totalPages - 1"
          class="p-2 border rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M9 5l7 7-7 7" stroke-width="2"/></svg>
        </button>
      </div>
    </div>

    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Toast from '@/components/common/Toast.vue'
import { useToast } from '@/composables/useToast'
import { getHeadSettlementSummary, getHeadSettlementList } from '@/api/headincome'

const { toast } = useToast()

const selectedMonth = ref('2026-04')
const activeTab = ref('입점 매장 정산')
const settlementSearch = ref('')

// 상단 카드
const totalBillingAmount = ref(0)

// 리스트 + 페이징
const settlementList = ref([])
const currentPage = ref(0)
const totalPages = ref(1)
const pageSize = 10

// selectedMonth "2026-04" → year: 2026, month: 4 파싱
const parsedDate = () => {
  const [year, month] = selectedMonth.value.split('-').map(Number)
  return { year, month }
}

const fetchSummary = async () => {
  try {
    const { year, month } = parsedDate()
    const res = await getHeadSettlementSummary(year, month, settlementSearch.value)
    totalBillingAmount.value = res.data.result?.totalBillingAmount ?? 0
  } catch (error) {
    console.error('청구 합계 조회 실패:', error)
  }
}

const fetchList = async (page = 0) => {
  try {
    const { year, month } = parsedDate()
    const res = await getHeadSettlementList(year, month, settlementSearch.value, page, pageSize)
    const result = res.data.result

    if (result) {
      settlementList.value = result.content || []
      totalPages.value = result.totalPages || 1
      currentPage.value = result.number || 0
    }
  } catch (error) {
    console.error('정산 리스트 조회 실패:', error)
  }
}

const onMonthChange = () => {
  currentPage.value = 0
  fetchSummary()
  fetchList(0)
}

// 검색어 입력 시 0페이지부터 다시 조회 및 요약 데이터 갱신
let searchTimer = null
const onSearchInput = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 0
    fetchSummary()
    fetchList(0)
  }, 300) // 타이핑 멈추고 300ms 후 조회 (debounce)
}

const changePage = (page) => {
  currentPage.value = page
  fetchList(page)
}

onMounted(() => {
  fetchSummary()
  fetchList(0)
})
</script>
