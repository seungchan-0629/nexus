<template>
  <div class="p-5 space-y-4 w-full">
    <div class="space-y-4">
      <div>
        <h1 class="text-xl font-bold text-gray-900 tracking-tight">배송 현황</h1>
      </div>

      <!-- 검색 & 날짜 필터 -->
      <div class="flex flex-wrap gap-3 items-center">
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
            class="bg-white border border-gray-200 text-gray-900 text-sm rounded-lg focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none block w-full pl-10 p-2.5 transition-colors shadow-sm"
            placeholder="발주 번호로 검색..."
          >
        </div>

        <div class="flex flex-wrap gap-2">
          <div class="relative w-32">
            <select
              v-model="selectedYear"
              @change="fetchDeliveries"
              class="bg-white border border-gray-200 text-gray-900 text-sm rounded-lg focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none block w-full p-2.5 cursor-pointer shadow-sm">
              <option value="">연도 전체</option>
              <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
            </select>
          </div>
          <div class="relative w-28">
            <select
              v-model="selectedMonth"
              @change="fetchDeliveries"
              class="bg-white border border-gray-200 text-gray-900 text-sm rounded-lg focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none block w-full p-2.5 cursor-pointer shadow-sm">
              <option value="">월 전체</option>
              <option v-for="m in 12" :key="m" :value="m">{{ m }}월</option>
            </select>
          </div>
          <div class="relative w-28">
            <select
              v-model="selectedDay"
              @change="fetchDeliveries"
              class="bg-white border border-gray-200 text-gray-900 text-sm rounded-lg focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none block w-full p-2.5 cursor-pointer shadow-sm">
              <option value="">일 전체</option>
              <option v-for="d in 31" :key="d" :value="d">{{ d }}일</option>
            </select>
          </div>
        </div>
      </div>

      <!-- 상태 필터 버튼 -->
      <div class="flex gap-2 flex-wrap pt-1">
        <button
          v-for="f in statusFilters"
          :key="f.value"
          @click="onStatusFilter(f.value)"
          class="px-3.5 py-1.5 text-sm font-semibold border rounded-lg transition-colors cursor-pointer"
          :class="currentFilter === f.value
            ? 'bg-blue-500 text-white border-blue-500 shadow-sm'
            : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50'">
          {{ f.label }}
        </button>
      </div>
    </div>

    <!-- 로딩 -->
    <div v-if="isLoading" class="bg-white border border-gray-200 py-16 text-center rounded-lg shadow-sm">
      <div class="flex flex-col items-center gap-3">
        <svg class="w-6 h-6 text-blue-500 animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
        </svg>
        <p class="text-gray-400 text-sm">배송 정보를 불러오는 중...</p>
      </div>
    </div>

    <!-- 에러 -->
    <div v-else-if="fetchError" class="bg-red-50 border border-red-200 py-12 text-center rounded-lg">
      <p class="text-red-500 text-sm font-medium">데이터를 불러오는 데 실패했습니다.</p>
      <button
        @click="fetchDeliveries"
        class="mt-3 px-4 py-1.5 text-xs font-semibold bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors cursor-pointer">
        다시 시도
      </button>
    </div>

    <!-- 배송 카드 목록 -->
    <div v-else-if="deliveries.length > 0" class="space-y-3">
      <div
        v-for="d in deliveries"
        :key="d.deliveryIdx"
        @click="d.statusEnum === 'DELAY' ? openModal(d) : null"
        class="bg-white border overflow-hidden rounded-lg transition-all duration-200"
        :class="[
          d.statusEnum === 'DELAY'
            ? 'border-red-300 cursor-pointer hover:border-red-400 hover:shadow-md'
            : 'border-gray-200 shadow-sm'
        ]">

        <!-- 카드 헤더 -->
        <div
          class="px-5 py-3 border-b flex justify-between items-center"
          :class="d.statusEnum === 'DELAY' ? 'bg-red-50/60 border-red-200' : 'bg-gray-50/60 border-gray-100'">
          <div class="flex items-center gap-4">
            <div class="flex flex-col">
              <span class="text-[10px] font-bold text-blue-500 mb-0.5">{{ d.date }}</span>
              <span class="text-xs font-mono text-gray-400">발주 # {{ d.orderIdx }}</span>
            </div>
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
              <span v-if="d.statusEnum === 'DELIVERED' && d.deliveredTime" class="ml-1 opacity-80 font-medium text-[10px]">
                ({{ d.deliveredTime }})
              </span>
            </span>
          </div>
        </div>

        <!-- 타임라인 -->
        <div class="px-5 py-4 flex items-center gap-0 overflow-x-auto hide-scrollbar">
          <div v-for="(step, idx) in d.timeline" :key="idx" class="flex items-center">
            <div class="flex flex-col items-center">
              <div
                class="w-3 h-3 border-2 border-white rounded-full shadow-sm"
                :class="step.done
                  ? 'bg-blue-400'
                  : step.current
                    ? 'bg-blue-600 ring-2 ring-blue-500/20'
                    : 'bg-gray-200'">
              </div>
              <p
                class="text-xs font-medium mt-1.5 text-center w-20 sm:w-24 truncate"
                :class="step.done
                  ? 'text-gray-800'
                  : step.current
                    ? 'text-blue-600'
                    : 'text-gray-400'">
                {{ step.label }}
              </p>
              <p class="text-[10px] text-gray-400 mt-0.5">{{ step.time }}</p>
            </div>
            <div
              v-if="idx < d.timeline.length - 1"
              class="h-px w-8 sm:w-16 mx-1 sm:mx-2 mb-6"
              :class="step.done ? 'bg-blue-300' : 'bg-gray-200'">
            </div>
          </div>
        </div>

        <!-- 지연 안내 바 -->
        <div
          v-if="d.statusEnum === 'DELAY'"
          class="px-5 py-2.5 bg-red-50/30 border-t border-red-100 flex items-center justify-between">
          <span class="text-xs text-red-500 font-medium flex items-center gap-1.5">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            배송 지연 상세 사유를 확인하려면 클릭하세요.
          </span>
        </div>
      </div>
    </div>

    <!-- 빈 결과 -->
    <div v-else class="bg-white border border-gray-200 py-16 text-center rounded-lg shadow-sm">
      <p class="text-sm text-gray-400 font-medium">조건에 맞는 배송 내역이 없습니다.</p>
    </div>

    <!-- 페이지네이션 -->
    <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-6">
      <!-- 이전 10페이지 그룹 -->
      <button
        @click="fetchDeliveries(Math.max(0, currentPage - 10))"
        :disabled="currentPage < 10"
        class="p-2 border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M11 19l-7-7 7-7m8 14l-7-7 7-7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>

      <!-- 이전 페이지 -->
      <button
        @click="fetchDeliveries(currentPage - 1)"
        :disabled="currentPage === 0"
        class="p-2 border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path d="M15 19l-7-7 7-7" stroke-width="2"/>
        </svg>
      </button>

      <!-- 페이지 번호 (최대 10개) -->
      <div class="flex gap-1">
        <button
          v-for="p in visiblePages"
          :key="p"
          @click="fetchDeliveries(p - 1)"
          class="w-9 h-9 text-sm font-semibold rounded-lg transition-colors cursor-pointer"
          :class="currentPage === p - 1
            ? 'bg-blue-500 text-white'
            : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'">
          {{ p }}
        </button>
      </div>

      <!-- 다음 페이지 -->
      <button
        @click="fetchDeliveries(currentPage + 1)"
        :disabled="currentPage === totalPages - 1"
        class="p-2 border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path d="M9 5l7 7-7 7" stroke-width="2"/>
        </svg>
      </button>

      <!-- 다음 10페이지 그룹 -->
      <button
        @click="fetchDeliveries(Math.min(totalPages - 1, currentPage + 10))"
        :disabled="currentPage >= Math.floor((totalPages - 1) / 10) * 10"
        class="p-2 border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M13 5l7 7-7 7M5 5l7 7-7 7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
    </div>

    <!-- 지연 사유 조회 모달 (가맹점은 읽기 전용) -->
    <div
      v-if="isModalOpen"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/40 backdrop-blur-sm"
      @click="closeModal">
      <div class="bg-white rounded-xl border border-gray-200 shadow-xl w-full max-w-sm overflow-hidden" @click.stop>
        <div class="px-6 py-4 border-b border-red-100 bg-red-50 flex justify-between items-center">
          <h3 class="text-base font-bold text-red-700">배송 지연 안내</h3>
          <button @click="closeModal" class="text-red-400 hover:text-red-600 cursor-pointer">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <div class="p-6 space-y-4">
          <div class="flex flex-col gap-1">
            <span class="text-xs font-semibold text-gray-500">발주 번호</span>
            <span class="text-sm font-mono text-gray-900 bg-gray-50 px-2 py-1.5 rounded border border-gray-100 w-fit">
              # {{ selectedDelivery?.orderIdx }}
            </span>
          </div>
          <div class="flex flex-col gap-1">
            <span class="text-xs font-semibold text-gray-500">매장명</span>
            <span class="text-sm text-gray-900 font-medium">{{ selectedDelivery?.storeName }}</span>
          </div>
          <div class="flex flex-col gap-1">
            <span class="text-xs font-semibold text-gray-500">출발일</span>
            <span class="text-sm text-gray-900">{{ selectedDelivery?.date || '-' }}</span>
          </div>
          <div class="flex flex-col gap-2 mt-2">
            <span class="text-xs font-semibold text-red-600">지연 사유</span>
            <div class="bg-red-50/50 border border-red-100 text-gray-800 text-sm p-4 rounded-lg leading-relaxed">
              {{ selectedDelivery?.delayReason || '등록된 상세 사유가 없습니다.' }}
            </div>
          </div>
        </div>

        <div class="px-6 py-4 border-t border-gray-100 flex justify-end">
          <button
            @click="closeModal"
            class="px-4 py-2 bg-blue-500 text-white text-sm font-bold hover:bg-blue-600 rounded cursor-pointer transition-colors shadow-sm">
            확인
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyStoreDeliveries } from '@/api/delivery'

// 상태
const deliveries  = ref([])
const isLoading   = ref(false)
const fetchError  = ref(false)
const currentFilter  = ref('')
const searchQuery    = ref('')
const selectedYear   = ref('')
const selectedMonth  = ref('')
const selectedDay    = ref('')

const isModalOpen      = ref(false)
const selectedDelivery = ref(null)

let searchDebounceTimer = null

// 기존 상태 변수들 아래에 추가
const currentPage  = ref(0)
const totalPages   = ref(0)
const totalCount   = ref(0)
const pageSize     = 10

// DeliveryStatus enum -> 레이블 매핑
const STATUS_LABEL_MAP = {
  READY:       '출고대기',
  START:       '출고중',
  DELIVERYING: '배송중',
  DELIVERED:   '배송완료',
  DELAY:       '지연',
}

// 상태 필터 버튼 목록
const statusFilters = [
  { value: '',            label: '전체'    },
  { value: 'READY',       label: '출고대기' },
  { value: 'START',       label: '출고중'   },
  { value: 'DELIVERYING', label: '배송중'   },
  { value: 'DELIVERED',   label: '배송완료' },
  { value: 'DELAY',       label: '지연'    },
]

// 연도 옵션
const yearOptions = computed(() => {
  const cur = new Date().getFullYear()
  return [cur, cur - 1, cur - 2]
})

// LocalDateTime 포맷
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

function extractDate(val) {
  if (!val) return null
  return String(val).substring(0, 10) // "YYYY-MM-DD"
}

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

// DeliveryDto -> 프론트엔드 포맷 매핑
// DeliveryStatus enum 순서 기반 타임라인 구성
function mapDelivery(dto) {
  const statusEnum  = dto.deliveryStatus
  const statusLabel = STATUS_LABEL_MAP[statusEnum] || statusEnum

  const departureFormatted = formatDatetime(dto.departureDate)
  const estimatedFormatted = formatDatetime(dto.estimatedArrivalAt)
  const deliveredFormatted = formatDatetime(dto.deliveredDate)
  const dateStr            = extractDate(dto.departureDate)
  const deliveredTimeShort = formatShortTime(dto.deliveredDate)

  // READY -> START -> DELIVERYING -> DELIVERED
  // DELAY 는 DELIVERYING 단계에서 발생
  const ORDER = ['READY', 'START', 'DELIVERYING', 'DELIVERED']
  const currentIdx = statusEnum === 'DELAY'
    ? 2  // DELIVERYING 위치에서 지연 표시
    : ORDER.indexOf(statusEnum)

  const timeline = [
    {
      label:   '발주 확정',
      time:    departureFormatted ? `${departureFormatted} 이전` : '-',
      done:    currentIdx > 0,
      current: currentIdx === 0,
    },
    {
      label:   '출고 완료',
      time:    departureFormatted || '-',
      done:    currentIdx > 1,
      current: currentIdx === 1,
    },
    {
      label:   statusEnum === 'DELAY' ? '배송 지연' : '배송 중',
      time:    statusEnum === 'DELAY'
        ? '지연 발생'
        : estimatedFormatted
          ? `예정 ${estimatedFormatted}`
          : '-',
      done:    currentIdx > 2,
      current: currentIdx === 2,
    },
    {
      label:   '배송 완료',
      time:    deliveredFormatted
        ? deliveredFormatted
        : estimatedFormatted
          ? `예정 ${estimatedFormatted}`
          : '예정',
      done:    statusEnum === 'DELIVERED',
      current: false,
    },
  ]

  return {
    deliveryIdx:  dto.deliveryIdx,
    orderIdx:     dto.orderIdx,
    storeName:    dto.storeName    || '-',
    date:         dateStr          || '-',
    statusEnum,
    statusLabel,
    delayReason:  dto.delayReason  || '',
    orderPrice:   dto.orderPrice,
    itemCount:    dto.itemCount,
    deliveredTime: deliveredTimeShort,
    timeline,
  }
}

// API 호출
async function fetchDeliveries(page = 0) {
  isLoading.value  = true
  fetchError.value = false

  try {
    const params = { page, size: pageSize }

    const parsed = parseInt(searchQuery.value.trim(), 10)
    if (!isNaN(parsed) && searchQuery.value.trim() !== '') {
      params.orderIdx = parsed
    }

    if (currentFilter.value)  params.status = currentFilter.value
    if (selectedYear.value)   params.year   = Number(selectedYear.value)
    if (selectedMonth.value)  params.month  = Number(selectedMonth.value)
    if (selectedDay.value)    params.day    = Number(selectedDay.value)

    const { data } = await getMyStoreDeliveries(params)
    deliveries.value = (data?.result?.deliveryList || []).map(mapDelivery)
    currentPage.value = data?.result?.currentPage ?? 0
    totalPages.value  = data?.result?.totalPage   ?? 0
    totalCount.value  = data?.result?.totalCount  ?? 0
  } catch (err) {
    console.error('[StoreDeliveryView] 배송 현황 조회 실패:', err)
    fetchError.value = true
  } finally {
    isLoading.value = false
  }
}

// 검색어 디바운스 (400ms)
function onSearchInput() {
  clearTimeout(searchDebounceTimer)
  searchDebounceTimer = setTimeout(() => fetchDeliveries(0), 400)
}

// 상태 필터 클릭
function onStatusFilter(value) {
  currentFilter.value = value
  fetchDeliveries(0)
}

// 상태별 배지 스타일
function statusClass(statusEnum) {
  const map = {
    READY:       'bg-gray-100 text-gray-600 border border-gray-200',
    START:       'bg-orange-50 text-orange-600 border border-orange-200',
    DELIVERYING: 'bg-blue-50 text-blue-600 border border-blue-200',
    DELIVERED:   'bg-green-50 text-green-600 border border-green-200',
    DELAY:       'bg-red-50 text-red-600 border border-red-200',
  }
  return map[statusEnum] || 'bg-gray-100 text-gray-500 border border-gray-200'
}

// 모달 제어
function openModal(delivery) {
  selectedDelivery.value = delivery
  isModalOpen.value      = true
}

function closeModal() {
  isModalOpen.value = false
  setTimeout(() => { selectedDelivery.value = null }, 200)
}

//초기 로드
onMounted(() => {
  fetchDeliveries()
})
</script>
