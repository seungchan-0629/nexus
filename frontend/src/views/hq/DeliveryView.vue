<template>
  <div class="p-5 space-y-4">
    <div>
      <h1 class="text-xl font-bold text-gray-900 tracking-tight">배송 관리</h1>
    </div>

    <div class="flex flex-wrap gap-3 items-center mt-2">
      <div class="relative w-full sm:w-80">
        <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
          <svg class="w-4 h-4 text-gray-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
        <input
          type="text"
          v-model="searchQuery"
          @input="onSearchInput"
          class="bg-white border border-gray-200 text-gray-900 text-sm rounded-lg focus:ring-1 focus:ring-[#F37321] focus:border-[#F37321] outline-none block w-full pl-10 p-2.5 transition-colors shadow-sm"
          placeholder="매장명 검색..."
        >
      </div>

      <div class="flex flex-wrap gap-2">
        <div class="relative w-32">
          <select
            v-model="selectedYear"
            @change="onFilterChange"
            class="bg-white border border-gray-200 text-gray-900 text-sm rounded-lg focus:ring-1 focus:ring-[#F37321] focus:border-[#F37321] outline-none block w-full p-2.5 cursor-pointer shadow-sm">
            <option value="">연도 전체</option>
            <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
          </select>
        </div>
        <div class="relative w-28">
          <select
            v-model="selectedMonth"
            @change="onFilterChange"
            class="bg-white border border-gray-200 text-gray-900 text-sm rounded-lg focus:ring-1 focus:ring-[#F37321] focus:border-[#F37321] outline-none block w-full p-2.5 cursor-pointer shadow-sm">
            <option value="">월 전체</option>
            <option v-for="m in 12" :key="m" :value="m">{{ m }}월</option>
          </select>
        </div>
        <div class="relative w-28">
          <select
            v-model="selectedDay"
            @change="onFilterChange"
            class="bg-white border border-gray-200 text-gray-900 text-sm rounded-lg focus:ring-1 focus:ring-[#F37321] focus:border-[#F37321] outline-none block w-full p-2.5 cursor-pointer shadow-sm">
            <option value="">일 전체</option>
            <option v-for="d in 31" :key="d" :value="d">{{ d }}일</option>
          </select>
        </div>
      </div>
    </div>

    <div class="flex gap-2 flex-wrap">
      <button
        v-for="f in statusFilters"
        :key="f.value"
        @click="onStatusFilter(f.value)"
        class="px-3.5 py-1.5 text-sm font-semibold border rounded-md transition-colors cursor-pointer"
        :class="filterStatus === f.value
          ? 'bg-[#F37321] text-white border-[#F37321]'
          : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50'">
        {{ f.label }}
      </button>
    </div>

    <div v-if="isLoading" class="bg-white border border-gray-200 py-16 text-center rounded-xl shadow-sm">
      <div class="flex flex-col items-center gap-3">
        <svg class="w-6 h-6 text-[#F37321] animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
        </svg>
        <p class="text-gray-400 text-sm">배송 정보를 불러오는 중...</p>
      </div>
    </div>

    <div v-else-if="fetchError" class="bg-red-50 border border-red-200 py-12 text-center rounded-xl">
      <p class="text-red-500 text-sm font-medium">데이터를 불러오는 데 실패했습니다.</p>
      <button
        @click="fetchDeliveries"
        class="mt-3 px-4 py-1.5 text-xs font-semibold bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors cursor-pointer">
        다시 시도
      </button>
    </div>

    <div v-else-if="deliveries.length > 0" class="space-y-3">
      <div
        v-for="d in deliveries"
        :key="d.deliveryIdx"
        @click="d.statusEnum === 'DELAY' ? openModal(d) : null"
        class="bg-white border rounded-xl overflow-hidden shadow-[0_2px_10px_rgba(15,23,42,0.03)] transition-all duration-200"
        :class="[
          d.statusEnum === 'DELAY'
            ? 'border-red-300 cursor-pointer hover:border-red-400 hover:shadow-md'
            : 'border-gray-200'
        ]">

        <div
          class="px-5 py-3 border-b flex justify-between items-center"
          :class="d.statusEnum === 'DELAY' ? 'bg-red-50/60 border-red-200' : 'bg-gray-50/60 border-gray-100'">
          <div class="flex items-center gap-3">
            <span class="text-xs font-mono text-gray-400"># {{ d.orderIdx }}</span>
            <span class="font-bold text-gray-900 text-sm">{{ d.storeName }}</span>
          </div>
          <div class="flex items-center gap-3">
            <span v-if="d.orderPrice != null" class="text-[11px] font-medium text-gray-500 bg-white/80 px-2 py-0.5 rounded-full border border-gray-100 shadow-sm">
              {{ formatPrice(d.orderPrice) }} ({{ d.itemCount }}건)
            </span>
            <span
              class="text-xs font-bold px-2 py-0.5 rounded whitespace-nowrap"
              :class="statusClass(d.statusEnum)">
              {{ d.statusLabel }}
              <span v-if="d.statusEnum === 'DELIVERED' && d.deliveredTime" class="ml-1 opacity-80 font-medium">
                ({{ d.deliveredTime }})
              </span>
            </span>
          </div>
        </div>

        <div class="px-5 py-4 flex items-start overflow-x-auto hide-scrollbar">
          <div v-for="(step, idx) in d.timeline" :key="idx" class="flex items-center">
            <div class="flex flex-col items-center">
              <div
                class="w-3 h-3 rounded-full border-2 border-white shadow-sm"
                :class="step.done
                  ? 'bg-[#F37321]'
                  : step.current
                    ? 'bg-red-700 ring-2 ring-red-700'
                    : 'bg-gray-200'">
              </div>
              <p
                class="text-xs font-medium mt-1.5 text-center w-[150px] truncate"
                :title="step.label"
                :class="step.done
                  ? 'text-gray-800'
                  : step.current
                    ? 'text-red-700'
                    : 'text-gray-400'">
                {{ step.label }}
              </p>
              <p class="text-[10px] text-gray-400 mt-0.5">{{ step.time }}</p>
            </div>
            <div
              v-if="idx < d.timeline.length - 1"
              class="h-px w-20 sm:w-24 mx-1 sm:mx-2 mt-1 shrink-0"
              :class="step.done ? 'bg-[#F37321]' : 'bg-gray-200'">
            </div>
          </div>
        </div>

        <div v-if="d.statusEnum === 'DELAY'" class="px-5 pb-4 pt-1">
          <div
            v-if="d.delayReason"
            class="bg-red-50/80 text-red-700 text-xs p-3 rounded-md border border-red-100 flex items-start gap-2">
            <svg class="w-4 h-4 shrink-0 mt-0.5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
            </svg>
            <div>
              <span class="font-bold">지연 사유:</span>
              <p class="mt-0.5 whitespace-pre-wrap">{{ d.delayReason }}</p>
            </div>
          </div>
          <div
            v-else
            class="text-[11px] text-red-400 bg-red-50/50 p-2 rounded-md border border-red-100 border-dashed text-center">
            지연 사유를 입력해 주세요.
          </div>
        </div>
      </div>
    </div>

    <div v-else class="bg-white border border-gray-200 py-16 text-center rounded-xl shadow-sm">
      <p class="text-gray-400 text-sm">해당 조건에 일치하는 배송 건이 없습니다.</p>
    </div>

    <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-6">
      <!-- 이전 10페이지 그룹 -->
      <button
        @click="goToPage(Math.max(0, currentPage - 10))"
        :disabled="currentPage < 10"
        class="p-2 border rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M11 19l-7-7 7-7m8 14l-7-7 7-7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>

      <!-- 이전 페이지 -->
      <button
        @click="goToPage(currentPage - 1)"
        :disabled="currentPage === 0"
        class="p-2 border rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M15 19l-7-7 7-7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>

      <!-- 페이지 번호 (최대 10개) -->
      <div class="flex gap-1">
        <button
          v-for="p in visiblePages"
          :key="p"
          @click="goToPage(p - 1)"
          class="w-9 h-9 text-sm font-semibold rounded-lg transition-colors"
          :class="currentPage === p - 1
            ? 'bg-[#F37321] text-white'
            : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'">
          {{ p }}
        </button>
      </div>

      <!-- 다음 페이지 -->
      <button
        @click="goToPage(currentPage + 1)"
        :disabled="currentPage === totalPages - 1"
        class="p-2 border rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M9 5l7 7-7 7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>

      <!-- 다음 10페이지 그룹 -->
      <button
        @click="goToPage(Math.min(totalPages - 1, currentPage + 10))"
        :disabled="currentPage >= Math.floor((totalPages - 1) / 10) * 10"
        class="p-2 border rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M13 5l7 7-7 7M5 5l7 7-7 7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
    </div>

    <div
      v-if="isModalOpen"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/40 backdrop-blur-sm">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden" @click.stop>
        <div class="px-6 py-4 border-b border-gray-100 bg-gray-50/50 flex justify-between items-center">
          <h3 class="text-lg font-bold text-gray-900 flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-red-500"></span>
            배송 지연 사유 입력
          </h3>
          <button @click="closeModal" class="text-gray-400 hover:text-gray-600 transition-colors cursor-pointer">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <div class="p-6">
          <div class="mb-5 text-sm text-gray-600 bg-gray-50 p-3.5 rounded-lg border border-gray-100">
            <div class="flex justify-between mb-1">
              <span class="font-medium text-gray-500">발주 번호</span>
              <span class="font-mono text-gray-900"># {{ selectedDelivery?.orderIdx }}</span>
            </div>
            <div class="flex justify-between">
              <span class="font-medium text-gray-500">입점 매장</span>
              <span class="font-bold text-gray-900">{{ selectedDelivery?.storeName }}</span>
            </div>
          </div>

          <div class="space-y-2">
            <label class="block text-xs font-bold text-gray-700">상세 사유</label>
            <textarea
              v-model="delayReasonText"
              class="w-full border border-gray-300 rounded-lg p-3 text-sm focus:outline-none focus:border-red-400 focus:ring-4 focus:ring-red-100 min-h-[120px] resize-none transition-all"
              placeholder="배송 지연 사유를 상세히 입력해 주세요"
            ></textarea>
          </div>
        </div>

        <div class="px-6 py-4 bg-gray-50 flex justify-end gap-2 border-t border-gray-100">
          <button @click="closeModal" class="px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-lg text-sm font-semibold cursor-pointer">취소</button>
          <button
            @click="saveDelayReason"
            :disabled="isSaving || !delayReasonText.trim()"
            class="px-5 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 text-sm font-semibold transition-colors shadow-sm cursor-pointer disabled:opacity-50">
            {{ isSaving ? '저장 중...' : '사유 저장' }}
          </button>
        </div>
      </div>
    </div>
    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAllDeliveries, updateDelayReason } from '@/api/delivery'
import Toast from '@/components/common/Toast.vue'
import { useToast } from '@/composables/useToast'

const { toast, showToast } = useToast()

// 상태
const deliveries   = ref([])
const isLoading    = ref(false)
const fetchError   = ref(false)
const filterStatus = ref('')
const searchQuery  = ref('')
const selectedYear  = ref('')
const selectedMonth = ref('')
const selectedDay   = ref('')

// 페이징 상태 (추가)
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = ref(10)

const isModalOpen      = ref(false)
const selectedDelivery = ref(null)
const delayReasonText  = ref('')
const isSaving         = ref(false)

let searchDebounceTimer = null

const STATUS_LABEL_MAP = {
  READY:       '출고대기',
  START:       '출고중',
  DELIVERYING: '배송중',
  DELIVERED:   '입고완료',
  DELAY:       '지연',
}

const statusFilters = [
  { value: '',           label: '전체'    },
  { value: 'READY',      label: '출고 대기' },
  { value: 'START',      label: '출고 중'  },
  { value: 'DELIVERYING',label: '배송 중'  },
  { value: 'DELIVERED',  label: '입고 완료' },
  { value: 'DELAY',      label: '지연'     },
]

const yearOptions = computed(() => {
  const cur = new Date().getFullYear()
  return [cur, cur - 1, cur - 2]
})

const visiblePages = computed(() => {
  const range = 10
  const startPage = Math.floor(currentPage.value / range) * range + 1
  const endPage = Math.min(startPage + range - 1, totalPages.value)

  const pages = []
  for (let i = startPage; i <= endPage; i++) {
    pages.push(i)
  }
  return pages
})

function formatDatetime(val) {
  if (!val) return null
  return String(val).replace('T', ' ').substring(0, 16)
}

function formatShortTime(val) {
  if (!val) return null
  const date = new Date(val)
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const i = String(date.getMinutes()).padStart(2, '0')
  return `${m}.${d} ${h}:${i}`
}

function formatPrice(value) {
  if (value === null || value === undefined) return '0원'
  return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(value)
}

// DTO 맵핑 로직 (기존과 동일)
function mapDelivery(dto) {
  const statusEnum  = dto.deliveryStatus
  const statusLabel = STATUS_LABEL_MAP[statusEnum] || statusEnum
  const departureFormatted = formatDatetime(dto.departureDate)
  const estimatedFormatted = formatDatetime(dto.estimatedArrivalAt)
  const deliveredFormatted = formatDatetime(dto.deliveredDate)
  const deliveredTimeShort = formatShortTime(dto.deliveredDate)

  const ORDER = ['READY', 'START', 'DELIVERYING', 'DELIVERED']
  const currentIdx = statusEnum === 'DELAY' ? 2 : ORDER.indexOf(statusEnum)

  const timeline = [
    { label: '출고 대기', time: departureFormatted ? `${departureFormatted} 이전` : '-', done: currentIdx > 0, current: currentIdx === 0 },
    { label: '출고 완료', time: departureFormatted || '-', done: currentIdx > 1, current: currentIdx === 1 },
    { label: statusEnum === 'DELAY' ? '배송 지연' : '배송 중', time: statusEnum === 'DELAY' ? '지연 발생' : (estimatedFormatted ? `도착 예정 ${estimatedFormatted}` : '-'), done: currentIdx > 2, current: currentIdx === 2 },
    { label: '입고 완료', time: deliveredFormatted || (estimatedFormatted ? `예정 ${estimatedFormatted}` : '예정'), done: statusEnum === 'DELIVERED', current: false },
  ]

  return {
    deliveryIdx:  dto.deliveryIdx,
    orderIdx:     dto.orderIdx,
    storeName:    dto.storeName   || '-',
    statusEnum,
    statusLabel,
    delayReason:  dto.delayReason || '',
    orderPrice:   dto.orderPrice,
    itemCount:    dto.itemCount,
    deliveredTime: deliveredTimeShort,
    timeline,
  }
}

// API 호출 (페이징 반영)
async function fetchDeliveries() {
  isLoading.value  = true
  fetchError.value = false

  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchQuery.value.trim()) params.storeName = searchQuery.value.trim()
    if (filterStatus.value)       params.status    = filterStatus.value
    if (selectedYear.value)       params.year      = Number(selectedYear.value)
    if (selectedMonth.value)      params.month     = Number(selectedMonth.value)
    if (selectedDay.value)        params.day       = Number(selectedDay.value)

    const res = await getAllDeliveries(params)
    // 백엔드 응답 구조: res.data.result (DeliveryPageRes)
    const result = res.data.result

    if (result.deliveryList && result.deliveryList.length > 0) {
      console.log('[DeliveryView] 첫 번째 배송 데이터 샘플:', result.deliveryList[0])
    }

    deliveries.value = (result.deliveryList || []).map(mapDelivery)
    totalPages.value = result.totalPage
  } catch (err) {
    console.error('[DeliveryView] 배송 목록 조회 실패:', err)
    fetchError.value = true
  } finally {
    isLoading.value = false
  }
}

// 검색 및 필터 변경 시 첫 페이지로 리셋
function onSearchInput() {
  clearTimeout(searchDebounceTimer)
  searchDebounceTimer = setTimeout(() => {
    currentPage.value = 0
    fetchDeliveries()
  }, 400)
}

function onFilterChange() {
  currentPage.value = 0
  fetchDeliveries()
}

function onStatusFilter(value) {
  filterStatus.value = value
  currentPage.value = 0
  fetchDeliveries()
}

// 페이지 이동 함수
function goToPage(page) {
  currentPage.value = page
  fetchDeliveries()
}

function statusClass(statusEnum) {
  const map = {
    READY:       'bg-gray-100 text-gray-600 border border-gray-200',
    START:       'bg-orange-50 text-orange-600 border border-orange-200',
    DELIVERYING: 'bg-blue-50 text-blue-600 border border-blue-200',
    DELIVERED:   'bg-green-50 text-green-700 border border-green-200',
    DELAY:       'bg-red-50 text-red-600 border border-red-200',
  }
  return map[statusEnum] || 'bg-gray-100 text-gray-500 border border-gray-200'
}

function openModal(delivery) {
  selectedDelivery.value = delivery
  delayReasonText.value  = delivery.delayReason || ''
  isModalOpen.value      = true
}

function closeModal() {
  isModalOpen.value      = false
  selectedDelivery.value = null
  delayReasonText.value  = ''
}

// 지연 사유 저장 (API 명세에 맞춰 dto 객체로 전달)
async function saveDelayReason() {
  if (!selectedDelivery.value || !delayReasonText.value.trim()) return

  isSaving.value = true
  try {
    // 컨트롤러가 @RequestBody DeliveryDto.DelayReasonRequest를 받으므로 객체로 전달
    await updateDelayReason({
      deliveryIdx: selectedDelivery.value.deliveryIdx,
      delayReason: delayReasonText.value.trim()
    })

    showToast('지연 사유가 저장되었습니다.', 'success')

    // 로컬 상태 즉시 반영
    const target = deliveries.value.find(d => d.deliveryIdx === selectedDelivery.value.deliveryIdx)
    if (target) target.delayReason = delayReasonText.value.trim()

    closeModal()
  } catch (err) {
    console.error('[DeliveryView] 지연 사유 저장 실패:', err)
    showToast('지연 사유 저장에 실패했습니다.', 'error')
  } finally {
    isSaving.value = false
  }
}

onMounted(() => {
  fetchDeliveries()
})
</script>
