<template>
  <div class="p-5 space-y-4">
    <InventoryHistoryHeader />

    <InventoryHistoryFilters
      v-model:filter-type="filterType"
      v-model:filter-store="filterStore"
      v-model:filter-from="filterFrom"
      v-model:filter-to="filterTo"
      :stores="stores"
      @reset="resetFilter"
    />

    <p v-if="loadError" class="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-4 py-3">
      {{ loadError }}
    </p>

    <InventoryHistoryTable :rows="filtered" :loading="loading" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import InventoryHistoryHeader from '@/components/inventory/InventoryHistoryHeader.vue'
import InventoryHistoryFilters from '@/components/inventory/InventoryHistoryFilters.vue'
import InventoryHistoryTable from '@/components/inventory/InventoryHistoryTable.vue'
import { searchStoreList } from '@/api/store/index.js'
import { getInventoryMovements } from '@/api/inventory/index.js'
import { resolveDisplayTypeLabel } from '@/constants/inventoryHistoryDisplay.js'

const filterType = ref('')
const filterStore = ref('')
const filterFrom = ref('')
const filterTo = ref('')

const stores = ref([])
const history = ref([])
const loading = ref(false)
const loadError = ref('')

function formatLocalDateTime(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return String(iso)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

function mapMovementRow(m, storeNameByIdx) {
  const movementType = m.movementType
  const type = resolveDisplayTypeLabel(movementType)
  let storeLabel = '—'
  if (movementType === 'INBOUND') {
    storeLabel = '본사'
  } else if (m.toLocationType === 'STORE' && m.toRefIdx != null) {
    storeLabel = storeNameByIdx.get(Number(m.toRefIdx)) || `매장 #${m.toRefIdx}`
  }

  return {
    id: m.movementIdx,
    datetime: formatLocalDateTime(m.createdAt),
    type,
    product: m.productName || '',
    store: storeLabel,
    qty: m.quantity ?? 0,
    handler: '—',
    note: m.memo || '',
  }
}

async function loadData() {
  loadError.value = ''
  loading.value = true
  try {
    const [storesRes, movRes] = await Promise.all([searchStoreList(''), getInventoryMovements()])
    const storeRows = storesRes.data?.result ?? []
    stores.value = storeRows
    const storeNameByIdx = new Map(storeRows.map((s) => [Number(s.idx), s.storeName]))
    const list = movRes.data?.result ?? []
    history.value = list.map((m) => mapMovementRow(m, storeNameByIdx))
  } catch (e) {
    loadError.value =
      e?.response?.data?.message || e?.message || '입출고 이력을 불러오지 못했습니다.'
    history.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

function parseHistoryDate(s) {
  return new Date(String(s).replace(' ', 'T'))
}

const filtered = computed(() => {
  const rows = history.value.filter((h) => {
    if (filterType.value && h.type !== filterType.value) return false
    if (filterStore.value && h.store !== filterStore.value) return false
    if (filterFrom.value && h.datetime.slice(0, 10) < filterFrom.value) return false
    if (filterTo.value && h.datetime.slice(0, 10) > filterTo.value) return false
    return true
  })
  return [...rows].sort((a, b) => parseHistoryDate(b.datetime) - parseHistoryDate(a.datetime))
})

function resetFilter() {
  filterType.value = ''
  filterStore.value = ''
  filterFrom.value = ''
  filterTo.value = ''
}
</script>
