<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Settings, CheckCheck } from 'lucide-vue-next'
import ordersApi from '@/api/orders'
import Toast from '@/components/common/Toast.vue'
import ConfirmModal from '@/components/common/ConfirmModal.vue'
import { useToast } from '@/composables/useToast'

const { toast, showToast } = useToast()

const confirmState = ref({ open: false, type: 'info', title: '', confirmText: '확인', action: null })
function openConfirm({ type, title, confirmText, action }) {
  confirmState.value = { open: true, type, title, confirmText, action }
}
function closeConfirm() {
  confirmState.value = { open: false, type: 'info', title: '', confirmText: '확인', action: null }
}
async function handleConfirm() {
  const action = confirmState.value.action
  closeConfirm()
  if (action) await action()
}
import HqAutoOrderTable from '@/components/orders/hq/HqAutoOrderTable.vue'
import HqConfirmedOrderTable from '@/components/orders/hq/HqConfirmedOrderTable.vue'
import HqOrderHistoryTable from '@/components/orders/hq/HqOrderHistoryTable.vue'
import HqAbnormalOrderTable from '@/components/orders/hq/HqAbnormalOrderTable.vue'
import HqOrderDetailModal from '@/components/orders/hq/HqOrderDetailModal.vue'
import HqDangerSettingsModal from '@/components/orders/hq/HqDangerSettingsModal.vue'

const route = useRoute()
const router = useRouter()

const tabs = [
  { id: 'auto',      label: '자동 발주 제안' },
  { id: 'confirmed', label: '확정 발주' },
  { id: 'history',   label: '발주 이력' },
  { id: 'abnormal',  label: '이상 발주' },
]
const activeTab = ref('auto')

const autoOrders = ref([])
const autoPage = ref(0)
const autoTotalPages = ref(1)
const autoSearchParams = ref({})

async function fetchAutoOrders(params = autoSearchParams.value, page = autoPage.value) {
  try {
    const res = await ordersApi.getAutoOrders({ ...params, page, size: 10 })
    const data = res.data.result
    autoOrders.value = data.content.map(o => ({
      id: o.idx,
      store: o.storeName,
      date: o.createdAt?.replace('T', ' ').slice(0, 16) ?? '-',
      price: o.price,
      status: '제안중',
    }))
    autoPage.value = data.number
    autoTotalPages.value = data.totalPages
  } catch (e) {
    console.error('자동 발주 목록 조회 실패', e)
  }
}

function onAutoSearch(params) {
  autoSearchParams.value = params
  fetchAutoOrders(params, 0)
}

function onAutoPageChange(page) {
  fetchAutoOrders(autoSearchParams.value, page)
}

const orderHistory = ref([])
const historyPage = ref(0)
const historyTotalPages = ref(1)
const historySearchParams = ref({})

async function fetchOrderHistory(params = historySearchParams.value, page = historyPage.value) {
  try {
    const res = await ordersApi.getOrderHistory({ ...params, page, size: 10 })
    const data = res.data.result
    orderHistory.value = data.content.map(o => ({
      id: o.idx,
      type: o.ordersType === 'AUTO' ? '자동' : '수동',
      store: o.storeName,
      date: o.createdAt?.replace('T', ' ').slice(0, 16) ?? '-',
      price: o.price,
      status: o.ordersStatus === 'APPROVE' ? '승인' : o.ordersStatus === 'REJECT' ? '거절' : '취소',
    }))
    historyPage.value = data.number
    historyTotalPages.value = data.totalPages
  } catch (e) {
    console.error('발주 이력 조회 실패', e)
  }
}

function onHistorySearch(params) {
  historySearchParams.value = params
  fetchOrderHistory(params, 0)
}

function onHistoryPageChange(page) {
  fetchOrderHistory(historySearchParams.value, page)
}

const abnormalOrders = ref([])
const abnormalPage = ref(0)
const abnormalTotalPages = ref(1)
const abnormalSearchParams = ref({})

async function fetchAbnormalOrders(params = abnormalSearchParams.value, page = abnormalPage.value) {
  try {
    const res = await ordersApi.getDangerOrders({ ...params, page, size: 10 })
    const data = res.data.result
    abnormalOrders.value = data.content.map(o => ({
      id: o.idx,
      store: o.storeName,
      date: o.createdAt?.replace('T', ' ').slice(0, 16) ?? '-',
      price: o.price,
      status: o.ordersStatus,
      qty: o.totalQty,
      avgQty: o.avgQty,
      ratio: o.ratio,
    }))
    abnormalPage.value = data.number
    abnormalTotalPages.value = data.totalPages
  } catch (e) {
    console.error('이상 발주 목록 조회 실패', e)
  }
}

function onAbnormalSearch(params) {
  abnormalSearchParams.value = params
  fetchAbnormalOrders(params, 0)
}

function onAbnormalPageChange(page) {
  fetchAbnormalOrders(abnormalSearchParams.value, page)
}

const confirmedOrders = ref([])
const confirmedPage = ref(0)
const confirmedTotalPages = ref(1)
const confirmedTotalElements = ref(0)
const confirmedSearchParams = ref({})

async function fetchConfirmedOrders(params = confirmedSearchParams.value, page = confirmedPage.value) {
  try {
    const res = await ordersApi.getConfirmedOrders({ ...params, page, size: 10 })
    const data = res.data.result
    confirmedOrders.value = data.content.map(o => ({
      id: o.idx,
      store: o.storeName,
      date: o.createdAt?.replace('T', ' ').slice(0, 16) ?? '-',
      price: o.price,
      status: '확정',
    }))
    confirmedPage.value = data.number
    confirmedTotalPages.value = data.totalPages
    confirmedTotalElements.value = data.totalElements
  } catch (e) {
    console.error('확정 발주 목록 조회 실패', e)
  }
}

function onConfirmedSearch(params) {
  confirmedSearchParams.value = params
  fetchConfirmedOrders(params, 0)
}

function onConfirmedPageChange(page) {
  fetchConfirmedOrders(confirmedSearchParams.value, page)
}

function approveAllConfirmed() {
  if (confirmedTotalElements.value === 0) {
    showToast('승인할 확정 발주가 없습니다.', 'error')
    return
  }
  openConfirm({
    type: 'warning',
    title: `확정 발주 ${confirmedTotalElements.value}건을 전체 승인하시겠습니까?`,
    confirmText: '전체 승인',
    action: async () => {
      try {
        await ordersApi.approveAllConfirmed()
        showToast('전체 승인이 완료되었습니다.')
        await fetchConfirmedOrders()
      } catch (e) {
        console.error('전체 승인 실패', e)
        const code = e.response?.data?.code
        if (code === 4201) {
          showToast('본사 재고가 부족하여 발주를 승인할 수 없습니다.', 'error')
        } else {
          showToast('전체 승인에 실패했습니다.', 'error')
        }
      }
    },
  })
}

// Tab routing
function applyOrderRouteQuery() {
  const q = route.query
  if (q.tab && ['auto', 'confirmed', 'history', 'abnormal'].includes(q.tab)) {
    activeTab.value = q.tab
  }
}

onMounted(() => {
  applyOrderRouteQuery()
  fetchTabData(activeTab.value)
})

function fetchTabData(id) {
  if (id === 'auto') fetchAutoOrders()
  if (id === 'confirmed') fetchConfirmedOrders()
  if (id === 'history') fetchOrderHistory()
  if (id === 'abnormal') fetchAbnormalOrders()
}

function resetSearchParams() {
  autoSearchParams.value = {}
  autoPage.value = 0
  confirmedSearchParams.value = {}
  confirmedPage.value = 0
  historySearchParams.value = {}
  historyPage.value = 0
  abnormalSearchParams.value = {}
  abnormalPage.value = 0
}

function setOrderViewTab(id) {
  activeTab.value = id
  router.replace({ path: '/order', query: { tab: id } })
  resetSearchParams()
  fetchTabData(id)
}

// Detail modal
const selectedOrder = ref(null)

async function openDetail(ordersIdx) {
  try {
    const res = await ordersApi.getOrderDetail(ordersIdx)
    const o = res.data.result
    selectedOrder.value = {
      id: o.idx,
      type: o.ordersType === 'AUTO' ? '자동' : '수동',
      store: o.storeName,
      date: o.createdAt?.replace('T', ' ').slice(0, 16) ?? '-',
      status: o.ordersStatus === 'WAITING' ? '제안중' : o.ordersStatus === 'APPROVE' ? '승인' : o.ordersStatus === 'CONFIRMED' ? '확정' : o.ordersStatus === 'CANCELLED' ? '취소' : '거절',
      reason: o.reason,
      items: o.ordersItemList.map(i => ({
        product: i.productName,
        qty: i.count,
        unitPrice: i.unitPrice,
      })),
    }
  } catch (e) {
    console.error('발주 상세 조회 실패', e)
  }
}

// Settings modal
const showSettings = ref(false)
const dangerSettings = ref({ ratio: 200, period: 3 })

async function openDangerSettings() {
  try {
    const res = await ordersApi.getDangerSettings()
    const s = res.data.result
    dangerSettings.value = { ratio: s.ratio, period: s.period }
  } catch (e) {
    console.error('이상 발주 기준 조회 실패', e)
  }
  showSettings.value = true
}

async function saveDangerSettings({ threshold, months }) {
  try {
    await ordersApi.updateDangerSettings({ ratio: threshold, period: months })
    dangerSettings.value = { ratio: threshold, period: months }
    showSettings.value = false
  } catch (e) {
    console.error('이상 발주 기준 저장 실패', e)
  }
}

// Abnormal order actions
function approveAbnormal(o) {
  openConfirm({
    type: 'info',
    title: `${o.store} 발주를 승인하시겠습니까?`,
    confirmText: '승인',
    action: async () => {
      try {
        await ordersApi.approveDangerOrder(o.id)
        showToast(`${o.store} 발주 승인 처리되었습니다.`)
        await fetchAbnormalOrders()
      } catch (e) {
        console.error('이상 발주 승인 실패', e)
        showToast('승인 처리에 실패했습니다.', 'error')
      }
    },
  })
}
function rejectAbnormal(o) {
  openConfirm({
    type: 'danger',
    title: `${o.store} 발주를 반려하시겠습니까?`,
    confirmText: '반려',
    action: async () => {
      try {
        await ordersApi.rejectDangerOrder(o.id)
        showToast(`${o.store} 발주 반려 처리되었습니다.`)
        await fetchAbnormalOrders()
      } catch (e) {
        console.error('이상 발주 반려 실패', e)
        showToast('반려 처리에 실패했습니다.', 'error')
      }
    },
  })
}

</script>

<template>
  <div class="p-5 space-y-4">
    <!-- Header -->
    <div class="flex justify-between items-start gap-4">
      <div class="min-w-0 flex-1">
        <h1 class="text-xl font-bold text-gray-900 tracking-tight">발주 관리</h1>
      </div>
      <button @click="openDangerSettings"
        class="px-4 py-2 rounded border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 flex items-center gap-2 cursor-pointer">
        <Settings class="w-4 h-4" /> 이상 발주 기준 설정
      </button>
    </div>

    <!-- Tabs -->
    <div class="flex border-b border-gray-200">
      <button v-for="tab in tabs" :key="tab.id"
        @click="setOrderViewTab(tab.id)"
        class="px-5 py-2.5 text-sm font-semibold border-b-2 -mb-px transition-colors cursor-pointer"
        :class="activeTab === tab.id
          ? 'border-[#F37321] text-[#F37321]'
          : 'border-transparent text-gray-500 hover:text-gray-700'">
        {{ tab.label }}
      </button>
    </div>

    <!-- Tab Contents -->
    <HqAutoOrderTable v-if="activeTab === 'auto'" :orders="autoOrders"
      :current-page="autoPage" :total-pages="autoTotalPages"
      @open-detail="openDetail" @search="onAutoSearch" @page-change="onAutoPageChange" />
    <div v-if="activeTab === 'confirmed'" class="space-y-4">
      <div class="flex justify-end">
        <button @click="approveAllConfirmed"
          class="bg-[#F37321] text-white px-4 py-2 text-sm font-semibold rounded hover:bg-[#e0661d] transition-colors flex items-center gap-2 cursor-pointer">
          <CheckCheck class="w-4 h-4" /> 전체 승인
        </button>
      </div>
      <HqConfirmedOrderTable :orders="confirmedOrders"
        :current-page="confirmedPage" :total-pages="confirmedTotalPages"
        @open-detail="openDetail" @search="onConfirmedSearch" @page-change="onConfirmedPageChange" />
    </div>
    <HqOrderHistoryTable v-if="activeTab === 'history'" :orders="orderHistory"
      :current-page="historyPage" :total-pages="historyTotalPages"
      @open-detail="openDetail" @search="onHistorySearch" @page-change="onHistoryPageChange" />
    <HqAbnormalOrderTable v-if="activeTab === 'abnormal'" :orders="abnormalOrders"
      :current-page="abnormalPage" :total-pages="abnormalTotalPages"
      @open-detail="openDetail" @approve="approveAbnormal" @reject="rejectAbnormal"
      @search="onAbnormalSearch" @page-change="onAbnormalPageChange" />

    <!-- Modals -->
    <HqOrderDetailModal :order="selectedOrder" @close="selectedOrder = null" />
    <HqDangerSettingsModal :visible="showSettings" :init-threshold="dangerSettings.ratio" :init-months="dangerSettings.period" @close="showSettings = false" @save="saveDangerSettings" />
    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
    <ConfirmModal
      :open="confirmState.open"
      :title="confirmState.title"
      :confirm-text="confirmState.confirmText"
      :type="confirmState.type"
      @close="closeConfirm"
      @confirm="handleConfirm" />
  </div>
</template>
