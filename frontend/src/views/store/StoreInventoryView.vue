<template>
  <div class="p-5 space-y-4">
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-xl font-bold text-gray-900 tracking-tight">매장 재고 관리</h1>
      </div>
      <div class="text-right">
        <p class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">마지막 POS 동기화</p>
        <p class="text-sm font-semibold text-gray-700 mt-0.5">2026-04-13 13:00</p>
      </div>
    </div>

    <StoreInventoryTable
      :items="inventory"
      :total-stock="totalStock"
      :get-status="getStatus"
      :get-status-class="getStatusClass"
      @open-detail="openDetail"
    />

    <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 pt-2">
      <button
        class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
        :disabled="currentPage === 0"
        @click="fetchInventory(currentPage - 1)"
      >
        <ChevronLeft class="w-4 h-4" />
      </button>
      <button
        v-for="page in totalPages"
        :key="page"
        class="w-8 h-8 rounded text-sm font-semibold cursor-pointer"
        :class="currentPage === page - 1 ? 'bg-[#F37321] text-white' : 'text-gray-500 hover:bg-gray-50'"
        @click="fetchInventory(page - 1)"
      >
        {{ page }}
      </button>
      <button
        class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
        :disabled="currentPage === totalPages - 1"
        @click="fetchInventory(currentPage + 1)"
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
      :editable="true"
      :waste-enabled="true"
      @apply-adjustments="applyLotAdjustments"
      @apply-waste="applyWasteAdjustments"
      @close="closeDetail"
    />
    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { changePosInventoryCount, getPosInventoryList } from '@/api/pos'
import { createPosWasteLog } from '@/api/wastelog'
import InventoryDetailModal from '@/components/inventory/InventoryDetailModal.vue'
import StoreInventoryTable from '@/components/inventory/StoreInventoryTable.vue'
import Toast from '@/components/common/Toast.vue'
import { useToast } from '@/composables/useToast'

const { toast, showToast } = useToast()

const detailTitleId = 'store-inv-detail-title'
const detailItem = ref(null)
const inventory = ref([])
const currentPage = ref(0)
const totalPages = ref(0)
const PAGE_SIZE = 10
const API_BATCH_SIZE = 5

async function runBatchedRequests(items, requestFn, batchSize = API_BATCH_SIZE) {
  for (let i = 0; i < items.length; i += batchSize) {
    const chunk = items.slice(i, i + batchSize)
    await Promise.all(chunk.map(requestFn))
  }
}

function formatDate(value) {
  if (!value) return null
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return null
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function mapInventoryRow(row) {
  return {
    idx: row.idx,
    productIdx: row.productIdx,
    code: `P${row.productIdx}`,
    name: row.productName,
    min: row.minStock ?? 0,
    apiStatus: row.status,
    lots: [
      {
        id: row.idx,
        expiry: formatDate(row.manufacturedDate),
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
    const key = String(mapped.productIdx)
    const existing = grouped.get(key)

    if (!existing) {
      grouped.set(key, mapped)
      continue
    }

    existing.lots.push(...mapped.lots)
    if (statusRank(mapped.apiStatus) < statusRank(existing.apiStatus)) {
      existing.apiStatus = mapped.apiStatus
    }
  }
  return Array.from(grouped.values()).sort((a, b) =>
    a.code.localeCompare(b.code, undefined, { numeric: true }),
  )
}

async function fetchInventory(page = 0) {
  try {
    const { data } = await getPosInventoryList(page, PAGE_SIZE)
    const pageResult = data?.result
    const list = Array.isArray(pageResult?.content) ? pageResult.content : []
    currentPage.value = Number(pageResult?.number ?? 0)
    totalPages.value = Number(pageResult?.totalPages ?? 0)
    inventory.value = aggregateInventoryRows(list)
  } catch (error) {
    console.error('Failed to fetch POS inventory:', error)
    currentPage.value = 0
    totalPages.value = 0
    inventory.value = []
  }
}

function totalStock(item) {
  return (item.lots ?? []).reduce((s, l) => s + l.qty, 0)
}

function fifoLots(item) {
  const lots = [...(item.lots ?? [])]
  return lots.sort((a, b) => {
    if (!a.expiry && !b.expiry) return 0
    if (!a.expiry) return 1
    if (!b.expiry) return -1
    return new Date(a.expiry) - new Date(b.expiry)
  })
}

function hasExpiringSoonLot(item) {
  return (item.lots ?? []).some((l) => isExpiringSoon(l.expiry))
}

const getSubtitle = (item) => item.code

function openDetail(item) {
  detailItem.value = item
}

function closeDetail() {
  detailItem.value = null
}

async function applyLotAdjustments(changes) {
  if (!detailItem.value) return
  if (!Array.isArray(changes) || changes.length === 0) return

  try {
    await runBatchedRequests(changes, (row) => changePosInventoryCount(row.id, row.adjustTo))
    await fetchInventory(currentPage.value)
    closeDetail()
    showToast('lot 재고 보정이 완료되었습니다.')
  } catch (error) {
    console.error('Failed to update POS inventory:', error)
    showToast('lot 재고 보정에 실패했습니다.', 'error')
  }
}

async function applyWasteAdjustments(changes) {
  if (!detailItem.value) return
  if (!Array.isArray(changes) || changes.length === 0) return

  const input = window.prompt('폐기 사유를 입력해 주세요.', '유통기한 임박')
  if (input === null) return
  const wasteReason = input.trim() || '기타'

  try {
    await runBatchedRequests(changes, (row) =>
      createPosWasteLog({
        posStoreInventoryIdx: row.id,
        quantity: Number(row.wasteQty),
        wasteReason,
      }),
    )
    await fetchInventory(currentPage.value)
    closeDetail()
    showToast('폐기 처리가 완료되었습니다.')
  } catch (error) {
    console.error('Failed to create waste log:', error)
    showToast('폐기 처리에 실패했습니다.', 'error')
  }
}

function isExpiringSoon(expiry) {
  if (!expiry) return false
  const diff = (new Date(expiry) - new Date()) / (1000 * 60 * 60 * 24)
  return diff >= 0 && diff <= 7
}

function getStatus(item) {
  if (item.apiStatus === 'CRITICAL') return '위험'
  if (item.apiStatus === 'LOW') return '부족'
  const stock = totalStock(item)
  if (stock < item.min) return '부족'
  if (hasExpiringSoonLot(item)) return '유통기한 임박'
  return '정상'
}

function getStatusClass(item) {
  if (item.apiStatus === 'CRITICAL') return 'bg-red-50 text-red-700 border border-red-200'
  if (item.apiStatus === 'LOW') return 'bg-red-50 text-red-600 border border-red-200'
  const stock = totalStock(item)
  if (stock < item.min) return 'bg-red-50 text-red-600 border border-red-200'
  if (hasExpiringSoonLot(item)) return 'bg-orange-50 text-orange-500 border border-orange-200'
  return 'bg-blue-50 text-blue-600 border border-blue-200'
}

onMounted(() => {
  fetchInventory(0)
})
</script>
