<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Plus } from 'lucide-vue-next'
import StoreManualOrderModal from '@/components/orders/store/StoreManualOrderModal.vue'
import StoreOrderHistoryTable from '@/components/orders/store/StoreOrderHistoryTable.vue'
import StoreOrderDetailModal from '@/components/orders/store/StoreOrderDetailModal.vue'
import StorePendingOrderList from '@/components/orders/store/StorePendingOrderList.vue'
import StoreOrderConfirmModal from '@/components/orders/store/StoreOrderConfirmModal.vue'
import StoreOrderRejectModal from '@/components/orders/store/StoreOrderRejectModal.vue'
import StoreItemDeleteModal from '@/components/orders/store/StoreItemDeleteModal.vue'
import ordersApi from '@/api/orders'
import Toast from '@/components/common/Toast.vue'
import ConfirmModal from '@/components/common/ConfirmModal.vue'
import { useToast } from '@/composables/useToast'

const { toast, showToast } = useToast()

const confirmState = ref({ open: false, title: '', confirmText: '확인', action: null })
function openConfirm({ title, confirmText, action }) {
  confirmState.value = { open: true, title, confirmText, action }
}
function closeConfirm() {
  confirmState.value = { open: false, title: '', confirmText: '확인', action: null }
}
async function handleConfirm() {
  const action = confirmState.value.action
  closeConfirm()
  if (action) await action()
}

const activeTab = ref('pending')
const selectedOrder = ref(null)
const isConfirmModalOpen = ref(false)
const isRejectModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const deleteTarget = ref({ order: null, item: null })

const pendingOrders = ref([])
const orderHistory = ref([])
const historyPage = ref(0)
const historyTotalPages = ref(0)
const historyTotalElements = ref(0)

async function fetchPendingOrders() {
  try {
    const res = await ordersApi.getStorePendingOrders()
    pendingOrders.value = res.data.result || []
  } catch (e) {
    console.error('제안 발주서 조회 실패', e)
  }
}

async function fetchOrderHistory(page = 0) {
  try {
    const res = await ordersApi.getStoreOrderListPaged(page, 10)
    const data = res.data.result
    orderHistory.value = data.content
    historyPage.value = data.number
    historyTotalPages.value = data.totalPages
    historyTotalElements.value = data.totalElements
  } catch (e) {
    console.error('발주 이력 조회 실패', e)
  }
}

onMounted(() => {
  fetchPendingOrders()
})

watch(activeTab, (tab) => {
  if (tab === 'history') {
    fetchOrderHistory()
  }
})

const selectedHistory = ref(null)

function openHistoryDetail(h) {
  selectedHistory.value = h
}

const tabs = computed(() => [
  { id: 'pending', label: '제안 발주서', count: pendingOrders.value.length },
  { id: 'history', label: '발주 이력', count: historyTotalElements.value },
])

function openConfirmModal(order) {
  selectedOrder.value = order
  isConfirmModalOpen.value = true
}

async function confirmOrder() {
  const order = selectedOrder.value
  try {
    await ordersApi.confirmStoreOrder(order.idx)
    const idx = pendingOrders.value.indexOf(order)
    if (idx > -1) pendingOrders.value.splice(idx, 1)
    isConfirmModalOpen.value = false
    showToast('발주서가 확정되었습니다.')
    fetchOrderHistory()
  } catch (e) {
    console.error('발주서 확정 실패', e)
    showToast('발주서 확정에 실패했습니다.', 'error')
  }
}

function openRejectModal(order) {
  selectedOrder.value = order
  isRejectModalOpen.value = true
}

async function rejectOrder() {
  const order = selectedOrder.value
  try {
    await ordersApi.rejectStoreOrder(order.idx)
    const idx = pendingOrders.value.indexOf(order)
    if (idx > -1) pendingOrders.value.splice(idx, 1)
    isRejectModalOpen.value = false
    showToast('발주서가 거절되었습니다.')
  } catch (e) {
    console.error('발주서 거절 실패', e)
    showToast('발주서 거절에 실패했습니다.', 'error')
  }
}

function deleteItem(order, item) {
  deleteTarget.value = { order, item }
  isDeleteModalOpen.value = true
}

async function confirmDeleteItem() {
  const { item } = deleteTarget.value
  try {
    await ordersApi.deleteStoreItem(item.idx)
    isDeleteModalOpen.value = false
    fetchPendingOrders()
  } catch (e) {
    console.error('품목 삭제 실패', e)
    showToast('품목 삭제에 실패했습니다.', 'error')
  }
}

function cancelOrder(order) {
  openConfirm({
    title: '발주를 취소하시겠습니까?',
    confirmText: '취소',
    action: async () => {
      try {
        await ordersApi.cancelOrder(order.idx)
        order.ordersStatus = 'CANCELLED'
        showToast('발주가 취소되었습니다.')
      } catch (e) {
        console.error('발주 취소 실패', e)
        showToast('발주 취소에 실패했습니다.', 'error')
      }
    },
  })
}

const showManualForm = ref(false)

async function submitManualOrder(data) {
  try {
    await ordersApi.createStoreManualOrder(data)
    showToast('발주가 생성되었습니다.')
    showManualForm.value = false
    activeTab.value = 'pending'
  } catch (e) {
    console.error('수동 발주 생성 실패', e)
    showToast('발주 생성에 실패했습니다.', 'error')
  }
}
</script>

<template>
  <div class="p-5 space-y-4">
    <div class="flex justify-between items-start gap-4">
      <h1 class="text-xl font-bold text-gray-900 tracking-tight">발주서 확인</h1>
      <button @click="showManualForm = true"
        class="bg-blue-500 text-white px-4 py-2 text-sm font-semibold rounded-lg hover:bg-blue-600 transition-colors flex items-center gap-2 cursor-pointer">
        <Plus class="w-4 h-4" /> 수동 발주 생성
      </button>
    </div>

    <div class="flex border-b border-gray-200">
      <button v-for="tab in tabs" :key="tab.id" @click="activeTab = tab.id"
        class="px-5 py-2.5 text-sm font-semibold border-b-2 -mb-px transition-colors cursor-pointer"
        :class="activeTab === tab.id ? 'border-blue-500 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'">
        {{ tab.label }}
        <span v-if="tab.count > 0" class="ml-1.5 text-xs font-bold px-1.5 py-0.5 rounded"
          :class="activeTab === tab.id ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500'">
          {{ tab.count }}
        </span>
      </button>
    </div>

    <StorePendingOrderList v-show="activeTab === 'pending'" :orders="pendingOrders"
      @confirm="openConfirmModal" @reject="openRejectModal" @refresh="fetchPendingOrders"
      @delete-item="deleteItem" />

    <StoreOrderHistoryTable v-if="activeTab === 'history'" :orders="orderHistory"
      :current-page="historyPage" :total-pages="historyTotalPages"
      @open-detail="openHistoryDetail" @cancel="cancelOrder" @page-change="fetchOrderHistory" />

    <StoreOrderDetailModal :order="selectedHistory" @close="selectedHistory = null" />

    <StoreOrderConfirmModal :order="selectedOrder" :visible="isConfirmModalOpen"
      @close="isConfirmModalOpen = false" @confirm="confirmOrder" />

    <StoreOrderRejectModal :order="selectedOrder" :visible="isRejectModalOpen"
      @close="isRejectModalOpen = false" @reject="rejectOrder" />

    <StoreManualOrderModal :visible="showManualForm" @close="showManualForm = false" @submit="submitManualOrder" />

    <StoreItemDeleteModal :item="deleteTarget.item" :visible="isDeleteModalOpen"
      @close="isDeleteModalOpen = false" @confirm="confirmDeleteItem" />
    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
    <ConfirmModal
      :open="confirmState.open"
      :title="confirmState.title"
      :confirm-text="confirmState.confirmText"
      type="danger"
      @close="closeConfirm"
      @confirm="handleConfirm" />
  </div>
</template>
