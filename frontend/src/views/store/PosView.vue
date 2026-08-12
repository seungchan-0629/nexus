<template>
  <div class="h-full flex flex-col overflow-hidden select-none">
    <PosHeaderBar
      :active-tab="activeTab"
      :is-closed="salesData.isClosed"
      :current-time="currentTime"
      :store-name="auth.user?.storeName"
      @select-tab="activeTab = $event" />

    <div v-if="activeTab === 'order'" class="flex-1 flex overflow-hidden">
      <PosMenuBoard
        :loading-menus="loadingMenus"
        :menus="filteredMenus"
        :categories="categories"
        :search-query="searchQuery"
        :selected-category="selectedCategory"
        @update:search-query="searchQuery = $event"
        @update:selected-category="selectedCategory = $event"
        @add-to-cart="addToCart" />
      <PosCartPanel
        :cart="cart"
        :total-price="totalPrice"
        :total-quantity="totalQuantity"
        :is-closed="salesData.isClosed"
        @clear-cart="clearCart"
        @remove-item="removeFromCart"
        @increase-qty="increaseQty"
        @decrease-qty="decreaseQty"
        @checkout="openPaymentModal" />
    </div>

    <PosSettlementPanel
      v-else-if="activeTab === 'settlement'"
      :sales="salesData"
      :payment-history="paymentHistory"
      @toggle-store-status="toggleStoreStatus" />

    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />

    <PosPaymentMethodModal
      :open="showPaymentModal"
      :formatted-total="formatPrice(totalPrice)"
      @close="showPaymentModal = false"
      @select="processPayment" />

    <PosCloseStoreModal
      :open="showCloseModal"
      :loading="isClosingStore"
      @close="showCloseModal = false"
      @confirm="confirmClose" />

    <ConfirmModal
      :open="confirmState.open"
      :title="confirmState.title"
      :message="confirmState.message"
      :confirm-text="confirmState.confirmText"
      :type="confirmState.type"
      @close="closeConfirm"
      @confirm="handleConfirm" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMenuCategoryList, getMenuListPaged, getPosPayToday, postPosPay, postPosClose } from '@/api/pos'
import { formatPrice } from '@/utils/formatPrice.js'
import PosHeaderBar from '@/components/pos/PosHeaderBar.vue'
import PosMenuBoard from '@/components/pos/PosMenuBoard.vue'
import PosCartPanel from '@/components/pos/PosCartPanel.vue'
import PosSettlementPanel from '@/components/pos/PosSettlementPanel.vue'
import Toast from '@/components/common/Toast.vue'
import { useToast } from '@/composables/useToast'
import PosPaymentMethodModal from '@/components/pos/PosPaymentMethodModal.vue'
import PosCloseStoreModal from '@/components/pos/PosCloseStoreModal.vue'
import ConfirmModal from '@/components/common/ConfirmModal.vue'

const auth = useAuthStore()

const activeTab = ref('order')
const selectedCategory = ref('전체')
const searchQuery = ref('')
const showPaymentModal = ref(false)
const showCloseModal = ref(false)
const isClosingStore = ref(false)
const currentTime = ref('')
const { toast, showToast } = useToast()
const confirmState = ref({ open: false, title: '', message: '', type: 'info', confirmText: '확인', action: null })
function openConfirm({ title, message, type, confirmText, action }) {
  confirmState.value = { open: true, title, message: message || '', type: type || 'info', confirmText: confirmText || '확인', action }
}
function closeConfirm() {
  confirmState.value = { open: false, title: '', message: '', type: 'info', confirmText: '확인', action: null }
}
async function handleConfirm() {
  const action = confirmState.value.action
  closeConfirm()
  if (action) await action()
}
const loadingMenus = ref(true)

let timer = null

const categories = ref(['전체'])
const menus = ref([])
const cart = ref([])

const salesData = ref({
  total: 0,
  card: 0,
  cash: 0,
  count: 0,
  isClosed: false,
})

const paymentHistory = ref([])
const CLOSE_STATUS_STORAGE_PREFIX = 'nexus_pos_closed_'

function closeStatusStorageKey() {
  const scope = auth.user?.email || auth.user?.storeId || 'default'
  return `${CLOSE_STATUS_STORAGE_PREFIX}${scope}`
}

function loadClosedStatus() {
  try {
    return localStorage.getItem(closeStatusStorageKey()) === 'true'
  } catch {
    return false
  }
}

function saveClosedStatus(isClosed) {
  try {
    localStorage.setItem(closeStatusStorageKey(), String(isClosed))
  } catch {
    // localStorage 접근 불가 환경에서는 메모리 상태만 사용
  }
}

function formatPaidAtTime(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleTimeString('ko-KR', { hour12: false })
}

function itemsSummaryFromApi(items) {
  if (!items?.length) return '—'
  if (items.length === 1) return `${items[0].menuName} ×${items[0].quantity}`
  const units = items.reduce((s, i) => s + i.quantity, 0)
  return `${items[0].menuName} 외 ${items.length - 1}종 (${units}개)`
}

function mapPayToHistoryRow(p) {
  const methodKr = p.method === 'CARD' ? '신용카드' : '현금'
  return {
    id: p.posPayIdx,
    time: formatPaidAtTime(p.paidAt),
    method: methodKr,
    items: itemsSummaryFromApi(p.items),
    amount: Number(p.payAmount),
  }
}

function computeSalesFromPays(pays) {
  let total = 0
  let card = 0
  let cash = 0
  for (const p of pays) {
    const amt = Number(p.payAmount)
    total += amt
    if (p.method === 'CARD') card += amt
    else if (p.method === 'CASH') cash += amt
  }
  return { total, card, cash, count: pays.length }
}

async function loadTodaySettlement() {
  try {
    const { data } = await getPosPayToday()
    const pays = data?.result ?? []
    paymentHistory.value = pays.map(mapPayToHistoryRow)
    const agg = computeSalesFromPays(pays)
    salesData.value.total = agg.total
    salesData.value.card = agg.card
    salesData.value.cash = agg.cash
    salesData.value.count = agg.count
  } catch (e) {
    console.error(e)
    paymentHistory.value = []
    salesData.value.total = 0
    salesData.value.card = 0
    salesData.value.cash = 0
    salesData.value.count = 0
  }
}

const filteredMenus = computed(() => {
  let result = menus.value
  if (selectedCategory.value !== '전체') {
    result = result.filter((m) => m.menuCategory === selectedCategory.value)
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim()
    result = result.filter((m) => m.menuName.includes(q))
  }
  return result
})

const totalPrice = computed(() => cart.value.reduce((s, i) => s + i.price * i.quantity, 0))
const totalQuantity = computed(() => cart.value.reduce((s, i) => s + i.quantity, 0))


function extractApiErrorMessage(error, fallback = '영업 마감 처리에 실패했습니다. 잠시 후 다시 시도해 주세요.') {
  const message = error?.response?.data?.message
  if (typeof message === 'string' && message.trim()) {
    return message
  }
  return fallback
}

function updateTime() {
  const now = new Date()
  currentTime.value = `${now.toLocaleDateString('ko-KR')} ${now.toLocaleTimeString('ko-KR', { hour12: false })}`
}

async function loadMenusAndCategories() {
  loadingMenus.value = true
  try {
    const [catRes, menuAcc] = await Promise.all([
      getMenuCategoryList().catch(() => ({ data: { result: [] } })),
      (async () => {
        const collected = []
        let page = 0
        let totalPages = 1
        while (page < totalPages) {
          const res = await getMenuListPaged(page, 80)
          const pack = res.data?.result
          if (!pack?.menuList) break
          collected.push(...pack.menuList)
          totalPages = pack.totalPage ?? 1
          page += 1
        }
        return collected
      })(),
    ])
    const catNames = (catRes.data?.result || []).map((c) => c.menuCategoryName).filter(Boolean)
    categories.value = ['전체', ...new Set(catNames)]
    menus.value = menuAcc
  } catch (e) {
    console.error(e)
    showToast('메뉴를 불러오지 못했습니다. 로그인·API 주소를 확인해 주세요.')
    menus.value = []
  } finally {
    loadingMenus.value = false
  }
}

function addToCart(menu) {
  if (salesData.value.isClosed) {
    showToast('영업이 마감되어 주문을 추가할 수 없습니다.', 'error')
    return
  }
  const existing = cart.value.find((i) => i.menuIdx === menu.idx)
  if (existing) existing.quantity += 1
  else {
    cart.value.push({
      menuIdx: menu.idx,
      name: menu.menuName,
      price: menu.price,
      quantity: 1,
    })
  }
}

function increaseQty(menuIdx) {
  const line = cart.value.find((i) => i.menuIdx === menuIdx)
  if (line) line.quantity += 1
}

function decreaseQty(menuIdx) {
  const line = cart.value.find((i) => i.menuIdx === menuIdx)
  if (!line) return
  if (line.quantity > 1) line.quantity -= 1
  else removeFromCart(menuIdx)
}

function removeFromCart(menuIdx) {
  cart.value = cart.value.filter((i) => i.menuIdx !== menuIdx)
}

function clearCart() {
  if (cart.value.length === 0) return
  openConfirm({
    title: '장바구니를 모두 비우시겠습니까?',
    type: 'warning',
    confirmText: '비우기',
    action: () => { cart.value = [] },
  })
}

function openPaymentModal() {
  if (cart.value.length === 0 || salesData.value.isClosed) return
  showPaymentModal.value = true
}

async function processPayment(method) {
  if (cart.value.length === 0) return

  const methodKr = method === 'card' ? '신용카드' : '현금'

  try {
    const reqBody = {
      method: method === 'card' ? 'CARD' : 'CASH',
      items: cart.value.map(({ menuIdx, quantity }) => ({ menuIdx, quantity })),
    }
    await postPosPay(reqBody)

    showPaymentModal.value = false
    cart.value = []
    await loadTodaySettlement()
    showToast(`${methodKr} 결제가 완료되었습니다.`)
  } catch (e) {
    console.error(e)
    showToast('결제 처리에 실패했습니다. 잠시 후 다시 시도해 주세요.')
  }
}

function toggleStoreStatus() {
  if (!salesData.value.isClosed) {
    showCloseModal.value = true
  } else {
    openConfirm({
      title: '새로운 영업을 시작하시겠습니까?',
      message: '기존 매출 및 결제 내역이 모두 초기화됩니다.',
      type: 'warning',
      confirmText: '영업 시작',
      action: () => {
        Object.assign(salesData.value, { isClosed: false, total: 0, card: 0, cash: 0, count: 0 })
        saveClosedStatus(false)
        paymentHistory.value = []
        showToast('새로운 영업이 시작되었습니다.')
      },
    })
  }
}

async function confirmClose() {
  if (isClosingStore.value) return
  try {
    isClosingStore.value = true
    const { data } = await postPosClose()
    const closeMessage = data?.result?.message
    salesData.value.isClosed = true
    saveClosedStatus(true)
    showCloseModal.value = false
    await loadTodaySettlement()
    showToast(closeMessage || '영업이 마감되었습니다. AI 자동 발주서가 생성되었습니다.')
  } catch (e) {
    console.error(e)
    showCloseModal.value = false
    showToast(extractApiErrorMessage(e))
  } finally {
    isClosingStore.value = false
  }
}

watch(
  () => activeTab.value,
  (tab) => {
    if (tab === 'settlement') loadTodaySettlement()
  },
)

onMounted(() => {
  salesData.value.isClosed = loadClosedStatus()
  updateTime()
  timer = setInterval(updateTime, 1000)
  loadMenusAndCategories()
  loadTodaySettlement()
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>
