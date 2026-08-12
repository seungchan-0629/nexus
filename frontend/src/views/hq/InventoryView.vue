<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { RouterLink } from 'vue-router'
import { ChevronLeft, ChevronRight, History } from 'lucide-vue-next'
import { getStoreInventoryByStore, searchStoreList } from '@/api/store'
import { useInventoryCommon } from '@/composables/useInventoryCommon'
import InventoryStatusFilters from '@/components/inventory/InventoryStatusFilters.vue'
import InventoryDetailModal from '@/components/inventory/InventoryDetailModal.vue'
import InventoryFranchiseToolbar from '@/components/inventory/InventoryFranchiseToolbar.vue'
import InventoryFranchiseTable from '@/components/inventory/InventoryFranchiseTable.vue'

const storeSearch = ref('')
const selectedStoreIdx = ref('')
const filterStatus = ref('all')
const detailItem = ref(null)
const detailTitleId = 'inv-store-detail-title'
const storeList = ref([])
const items = ref([])
const listLoading = ref(false)
const listError = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const PAGE_SIZE = 10
let searchDebounceTimer = null
const SEARCH_DEBOUNCE_MS = 250

const { totalStock, fifoLots, isExpiringSoon, hasExpiringSoonLot } = useInventoryCommon()

const statusFilters = [
  { value: 'all', label: '전체' },
  { value: 'danger', label: '재고부족' },
  { value: 'expiring', label: '유통기한 임박' },
  { value: 'normal', label: '정상' },
]

const visibleStores = computed(() => {
  const q = storeSearch.value.toLowerCase()
  if (!q) return storeList.value
  return storeList.value.filter((s) => String(s.storeName ?? '').toLowerCase().includes(q))
})

function formatDate(value) {
  if (!value) return null
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return null
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function mapInventoryRow(row) {
  return {
    idx: row.idx,
    productIdx: row.productIdx,
    code: `P${row.productIdx}`,
    name: row.productName,
    store: row.storeName,
    safe: row.minStock ?? 0,
    avgStock: row.avgStock,
    apiStatus: row.status,
    manufacturedDate: row.manufacturedDate,
    expiryDate: row.expiryDate,
    lots: [
      {
        id: row.idx,
        manufacturedDate: formatDate(row.expiryDate ?? row.manufacturedDate),
        expiry: formatDate(row.expiryDate ?? row.manufacturedDate),
        qty: row.count ?? 0,
      },
    ],
  }
}

function statusRank(status) {
  if (status === 'CRITICAL') return 0
  if (status === 'LOW') return 1
  return 2
}

function aggregateInventoryRows(list) {
  const grouped = new Map()

  for (const row of list) {
    if (!row || row.idx === null || row.idx === undefined) continue
    const mapped = mapInventoryRow(row)
    const key = `${mapped.store}-${mapped.productIdx}`
    const existing = grouped.get(key)

    if (!existing) {
      grouped.set(key, mapped)
      continue
    }

    existing.lots.push(...mapped.lots)
    if (statusRank(mapped.apiStatus) < statusRank(existing.apiStatus)) {
      existing.apiStatus = mapped.apiStatus
    }
    existing.manufacturedDate = (existing.manufacturedDate && mapped.manufacturedDate)
      ? (existing.manufacturedDate < mapped.manufacturedDate ? existing.manufacturedDate : mapped.manufacturedDate)
      : (existing.manufacturedDate ?? mapped.manufacturedDate)
  }

  return Array.from(grouped.values())
}

async function loadStoreList(keyword = '') {
  try {
    resetStoreListError()
    const response = await fetchStoreListByKeyword(keyword)
    const { data } = response
    const list = Array.isArray(data) ? data : data?.result
    storeList.value = Array.isArray(list) ? list : []
  } catch (error) {
    handleStoreListError(error)
  }
}

function fetchStoreListByKeyword(keyword = '') {
  return searchStoreList(keyword)
}

function resetStoreListError() {
  listError.value = ''
}

function handleStoreListError(error) {
  console.error('Failed to fetch store list:', error)
  storeList.value = []
  listError.value = '매장 목록을 불러오지 못했습니다.'
}

async function fetchStoreInventory(page = 0) {
  if (!selectedStoreIdx.value) {
    items.value = []
    currentPage.value = 0
    totalPages.value = 0
    return
  }
  listLoading.value = true
  listError.value = ''
  try {
    const { data } = await getStoreInventoryByStore(selectedStoreIdx.value, page, PAGE_SIZE)
    const pageResult = data?.result
    const list = Array.isArray(pageResult?.content) ? pageResult.content : []
    currentPage.value = Number(pageResult?.number ?? 0)
    totalPages.value = Number(pageResult?.totalPages ?? 0)
    items.value = aggregateInventoryRows(list)
  } catch (error) {
    console.error('Failed to fetch store inventory:', error)
    items.value = []
    currentPage.value = 0
    totalPages.value = 0
    listError.value = '해당 매장 재고를 불러오지 못했습니다.'
  } finally {
    listLoading.value = false
  }
}

function formatAverageStock(value) {
  if (value === null || value === undefined || value === '') return '-'
  const numeric = Number(value)
  if (!Number.isFinite(numeric)) return '-'
  return Math.round(numeric).toLocaleString()
}

function sortRows(list) {
  return [...list].sort((a, b) => `${a.code}\u0000${a.idx ?? ''}`.localeCompare(`${b.code}\u0000${b.idx ?? ''}`, undefined, { numeric: true }))
}

const filteredItems = computed(() => {
  const rows = items.value.filter((item) => {
    const stock = totalStock(item)
    if (filterStatus.value === 'danger' && stock >= item.safe) return false
    if (filterStatus.value === 'normal' && (stock < item.safe || hasExpiringSoonLot(item))) return false
    if (filterStatus.value === 'expiring' && !hasExpiringSoonLot(item)) return false
    return true
  })
  return sortRows(rows)
})

watch(storeSearch, () => {
  // 검색 조건 변경으로 현재 선택 매장이 목록에서 사라지면 선택/결과를 초기화한다.
  if (!selectedStoreIdx.value) return
  const selectedExists = visibleStores.value.some((s) => String(s.idx) === selectedStoreIdx.value)
  if (!selectedExists) {
    selectedStoreIdx.value = ''
    items.value = []
  }
})

watch(storeSearch, (keyword) => {
  // 키워드 입력은 디바운스로 서버 검색 호출을 제한한다.
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
  searchDebounceTimer = setTimeout(() => {
    loadStoreList(keyword.trim())
  }, SEARCH_DEBOUNCE_MS)
})

watch(selectedStoreIdx, (value) => {
  // 매장 선택이 바뀌면 해당 매장 재고를 즉시 조회한다.
  if (!value) return
  fetchStoreInventory(0)
})

function getStatus(item) {
  if (item.apiStatus === 'CRITICAL') return '재고위험'
  if (item.apiStatus === 'LOW') return '재고부족'
  if (totalStock(item) < item.safe) return '재고부족'
  if (hasExpiringSoonLot(item)) return '유통기한 임박'
  return '정상'
}

function getStatusClass(item) {
  if (item.apiStatus === 'CRITICAL') return 'bg-red-50 text-red-700 border border-red-200'
  if (item.apiStatus === 'LOW') return 'bg-red-50 text-red-600 border border-red-200'
  if (totalStock(item) < item.safe) return 'bg-red-50 text-red-600 border border-red-200'
  if (hasExpiringSoonLot(item)) return 'bg-orange-50 text-orange-500 border border-orange-200'
  return 'bg-blue-50 text-blue-600 border border-blue-200'
}

const getSubtitle = (item) => `${item.code} · ${item.store}`

function openDetail(item) {
  detailItem.value = item
}

function closeDetail() {
  detailItem.value = null
}

onMounted(() => {
  loadStoreList('')
})

onBeforeUnmount(() => {
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
})
</script>

<template>
  <div class="p-5 space-y-4">
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-xl font-bold text-gray-900 tracking-tight">입점 매장 재고 현황</h1>
      </div>
      <div class="flex gap-2">
        <RouterLink
          to="/inventory/history"
          class="px-4 py-2 rounded border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 flex items-center gap-2"
        >
          <History class="w-4 h-4" /> 입출고 이력
        </RouterLink>
        <button class="px-4 py-2 border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 cursor-pointer">재고 동기화</button>
      </div>
    </div>

    <InventoryFranchiseToolbar
      v-model:store-search="storeSearch"
      v-model:selected-store-idx="selectedStoreIdx"
      :visible-stores="visibleStores"
      :list-loading="listLoading"
      @submit="fetchStoreInventory"
    />

    <InventoryStatusFilters v-model="filterStatus" :status-filters="statusFilters" />

    <p v-if="listError" class="text-sm text-red-600">{{ listError }}</p>

    <InventoryFranchiseTable
      :items="filteredItems"
      :total-stock="totalStock"
      :format-average-stock="formatAverageStock"
      :get-status="getStatus"
      :get-status-class="getStatusClass"
      @open-detail="openDetail"
    />

    <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 pt-2">
      <button
        class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
        :disabled="currentPage === 0"
        @click="fetchStoreInventory(currentPage - 1)"
      >
        <ChevronLeft class="w-4 h-4" />
      </button>
      <button
        v-for="page in totalPages"
        :key="page"
        class="w-8 h-8 rounded text-sm font-semibold cursor-pointer"
        :class="currentPage === page - 1 ? 'bg-[#F37321] text-white' : 'text-gray-500 hover:bg-gray-50'"
        @click="fetchStoreInventory(page - 1)"
      >
        {{ page }}
      </button>
      <button
        class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
        :disabled="currentPage === totalPages - 1"
        @click="fetchStoreInventory(currentPage + 1)"
      >
        <ChevronRight class="w-4 h-4" />
      </button>
    </div>

    <InventoryDetailModal
      :item="detailItem"
      :detail-title-id="detailTitleId"
      :total-stock="totalStock"
      :fifo-lots="fifoLots"
      :is-expiring-soon="isExpiringSoon"
      :get-subtitle="getSubtitle"
      @close="closeDetail"
    />
  </div>
</template>
