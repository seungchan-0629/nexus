<template>
  <div class="p-5 space-y-8">
    <div class="flex gap-6 border-b border-gray-200 pb-2">
      <button
        @click="activeTab = 'sales'"
        class="cursor-pointer transition-colors"
        :class="activeTab === 'sales'
          ? 'text-blue-600 font-bold border-b-2 border-blue-600 pb-2'
          : 'text-gray-400 hover:text-gray-600'"
      >
        매출 정산 내역
      </button>

      <button
        @click="activeTab = 'order'"
        class="cursor-pointer transition-colors"
        :class="activeTab === 'order'
          ? 'text-blue-600 font-bold border-b-2 border-blue-600 pb-2'
          : 'text-gray-400 hover:text-gray-600'"
      >
        발주 정산 내역
      </button>

      <button
        @click="activeTab = 'unpaid'"
        class="cursor-pointer transition-colors"
        :class="activeTab === 'unpaid'
          ? 'text-red-600 font-bold border-b-2 border-red-600 pb-2'
          : 'text-gray-400 hover:text-gray-600'"
      >
        미결제 정산 건
      </button>
    </div>

    <div v-if="activeTab === 'sales'" class="space-y-6">
      <div>
        <h1 class="text-xl font-bold text-gray-900 tracking-tight">매출 정산 내역</h1>

        <div class="flex gap-2 mt-3">
          <select v-model="salesYear" @change="fetchSalesData" class="px-3 py-2 rounded-lg border border-gray-200 text-sm bg-white outline-none focus:border-blue-500">
            <option value="2026">2026년</option>
            <option value="2025">2025년</option>
          </select>
          <select v-model="salesMonth" @change="fetchSalesData" class="px-3 py-2 rounded-lg border border-gray-200 text-sm bg-white outline-none focus:border-blue-500">
            <option v-for="m in 12" :key="m" :value="String(m).padStart(2, '0')">{{ m }}월</option>
          </select>
        </div>

        <div class="mt-6">
          <div class="bg-gray-50 p-6 rounded-2xl border border-gray-200 w-full shadow-sm text-left">
            <p class="text-sm font-medium text-gray-500 mb-1">{{ salesYear }}-{{ salesMonth }} 총 매출액</p>
            <p class="text-3xl font-bold text-blue-600">₩ {{ totalSalesAmount.toLocaleString() }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
        <table class="w-full text-sm text-left">
          <thead class="bg-gray-50 border-b border-gray-200 text-gray-500 font-bold">
          <tr>
            <th class="px-6 py-4">결제일</th>
            <th class="px-6 py-4">판매 항목</th>
            <th class="px-6 py-4">수량</th>
            <th class="px-6 py-4">결제 금액</th>
            <th class="px-6 py-4">결제 상태</th>
          </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
          <tr v-for="item in salesData" :key="item.posPayIdx" class="hover:bg-gray-50/50 transition-colors">
            <td class="px-6 py-4 text-gray-600">{{ item.paidDate }}</td>
            <td class="px-6 py-4 font-semibold text-gray-900">{{ item.menuNames }}</td>
            <td class="px-6 py-4 text-gray-600">{{ item.payCount }}건</td>
            <td class="px-6 py-4 font-bold text-gray-900">₩ {{ item.payAmount?.toLocaleString() }}</td>
            <td class="px-6 py-4">
                <span class="px-2.5 py-1 rounded-full text-xs font-bold bg-blue-50 text-blue-600 border border-blue-100">
                  결제완료
                </span>
            </td>
          </tr>
          <tr v-if="salesData.length === 0">
            <td colspan="5" class="px-6 py-10 text-center text-gray-400">해당 기간의 매출 내역이 없습니다.</td>
          </tr>
          </tbody>
        </table>

        <div v-if="salesTotalPages > 1" class="flex justify-center items-center gap-2 py-4 border-t border-gray-100">
          <button
            @click="changeSalesPage(salesCurrentPage - 1)"
            :disabled="salesCurrentPage === 0"
            class="p-2 border rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M15 19l-7-7 7-7" stroke-width="2"/></svg>
          </button>
          <div class="flex gap-1">
            <button
              v-for="p in salesTotalPages"
              :key="p"
              @click="changeSalesPage(p - 1)"
              class="w-9 h-9 text-sm font-semibold rounded-lg transition-colors"
              :class="salesCurrentPage === p - 1 ? 'bg-blue-600 text-white' : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'">
              {{ p }}
            </button>
          </div>
          <button
            @click="changeSalesPage(salesCurrentPage + 1)"
            :disabled="salesCurrentPage === salesTotalPages - 1"
            class="p-2 border rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M9 5l7 7-7 7" stroke-width="2"/></svg>
          </button>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'order'" class="space-y-6">
      <div>
        <h1 class="text-xl font-bold text-gray-900 tracking-tight">발주 정산 내역</h1>

        <div class="flex gap-2 mt-3">
          <select v-model="orderYear" @change="fetchOrderData" class="px-3 py-2 rounded-lg border border-gray-200 text-sm bg-white outline-none focus:border-blue-500">
            <option value="2026">2026년</option>
            <option value="2025">2025년</option>
          </select>
          <select v-model="orderMonth" @change="fetchOrderData" class="px-3 py-2 rounded-lg border border-gray-200 text-sm bg-white outline-none focus:border-blue-500">
            <option v-for="m in 12" :key="m" :value="String(m).padStart(2, '0')">{{ m }}월</option>
          </select>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
          <div class="bg-gray-50 p-6 rounded-2xl border border-gray-200 w-full shadow-sm text-left">
            <p class="text-sm font-medium text-gray-500 mb-1">총 납부액</p>
            <p class="text-2xl font-bold text-green-600">₩ {{ totalOrderAmount.toLocaleString() }}</p>
          </div>
          <div class="bg-gray-50 p-6 rounded-2xl border border-gray-200 w-full shadow-sm text-left">
            <p class="text-sm font-medium text-gray-500 mb-1">이번달 납부 예상 금액</p>
            <p class="text-2xl font-bold text-[#F37321]">₩ {{ expectedPayAmount.toLocaleString() }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
        <table class="w-full text-sm text-left">
          <thead class="bg-gray-50 border-b border-gray-200 text-gray-500 font-bold">
          <tr>
            <th class="px-6 py-4">날짜</th>
            <th class="px-6 py-4">발주 제품</th>
            <th class="px-6 py-4">수량</th>
            <th class="px-6 py-4">정산 금액</th>
            <th class="px-6 py-4">납부 상태</th>
          </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
          <tr v-for="item in orderData" :key="item.ordersIdx" class="hover:bg-gray-50/50 transition-colors">
            <td class="px-6 py-4 text-gray-600">{{ item.createdDate }}</td>
            <td class="px-6 py-4 font-semibold text-gray-900">{{ item.productNames }}</td>
            <td class="px-6 py-4 text-gray-600">{{ item.totalCount }}개</td>
            <td class="px-6 py-4 font-bold text-gray-900">₩ {{ item.price?.toLocaleString() }}</td>
            <td class="px-6 py-4">
              <span class="px-2.5 py-1 rounded-full text-xs font-bold bg-green-50 text-green-600 border border-green-100">
                납부완료
              </span>
            </td>
          </tr>
          <tr v-if="orderData.length === 0">
            <td colspan="5" class="px-6 py-10 text-center text-gray-400">해당 기간의 발주 내역이 없습니다.</td>
          </tr>
          </tbody>
        </table>

        <div v-if="orderTotalPages > 1" class="flex justify-center items-center gap-2 py-4 border-t border-gray-100">
          <button
            @click="changeOrderPage(orderCurrentPage - 1)"
            :disabled="orderCurrentPage === 0"
            class="p-2 border rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M15 19l-7-7 7-7" stroke-width="2"/></svg>
          </button>
          <div class="flex gap-1">
            <button
              v-for="p in orderTotalPages"
              :key="p"
              @click="changeOrderPage(p - 1)"
              class="w-9 h-9 text-sm font-semibold rounded-lg transition-colors"
              :class="orderCurrentPage === p - 1 ? 'bg-blue-600 text-white' : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'">
              {{ p }}
            </button>
          </div>
          <button
            @click="changeOrderPage(orderCurrentPage + 1)"
            :disabled="orderCurrentPage === orderTotalPages - 1"
            class="p-2 border rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M9 5l7 7-7 7" stroke-width="2"/></svg>
          </button>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'unpaid'" class="space-y-6">
      <div>
        <h1 class="text-xl font-bold text-gray-900 tracking-tight">최종 미결제 내역</h1>
        <p class="text-sm text-gray-500 mt-1">자동 결제가 최종 실패하여 수동 결제가 필요한 내역입니다.</p>
      </div>

      <div class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
        <table class="w-full text-sm text-left">
          <thead class="bg-gray-50 border-b border-gray-200 text-gray-500 font-bold">
          <tr>
            <th class="px-6 py-4">정산 월</th>
            <th class="px-6 py-4">미결제 금액</th>
            <th class="px-6 py-4">실패 사유</th>
            <th class="px-6 py-4">동작</th>
          </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
          <tr v-for="item in unpaidList" :key="item.idx" class="hover:bg-gray-50/50 transition-colors">
            <td class="px-6 py-4 text-gray-600">{{ item.payedMonth }}</td>
            <td class="px-6 py-4 font-bold text-gray-900">₩ {{ item.amount?.toLocaleString() }}</td>
            <td class="px-6 py-4 text-red-500 text-xs">{{ item.failReason }}</td>
            <td class="px-6 py-4">
              <button
                @click="onManualPayment(item)"
                :disabled="isPaymentProcessing"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg text-xs font-bold hover:bg-blue-700 disabled:bg-gray-400 transition-colors"
              >
                {{ isPaymentProcessing ? '처리 중...' : '수동 결제' }}
              </button>
            </td>
          </tr>
          <tr v-if="unpaidList.length === 0">
            <td colspan="4" class="px-6 py-10 text-center text-gray-400">최종 미결제 내역이 없습니다.</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getSettlement, getOrderSettlement } from '@/api/store'
import settlementApi from '@/api/settlement'
import PortOne from "@portone/browser-sdk/v2"
import api from '@/plugins/axiosinterceptor'

const activeTab = ref('sales')

// --- 매출 상태 관리 ---
const salesYear = ref('2026')
const salesMonth = ref('04')
const salesData = ref([])
const totalSalesAmount = ref(0)
const salesCurrentPage = ref(0)
const salesTotalPages = ref(1)
const salesPageSize = 10

const fetchSalesData = async () => {
  try {
    const response = await getSettlement(salesYear.value, salesMonth.value, salesCurrentPage.value, salesPageSize)
    const result = response.data.result

    if (result) {
      totalSalesAmount.value = result.monthlyTotalAmount || 0
      if (result.payHistory) {
        salesData.value = result.payHistory.content || []
        salesTotalPages.value = result.payHistory.totalPages || 1
      }
    }
  } catch (error) {
    console.error('매출 데이터를 불러오는데 실패했습니다:', error)
  }
}

const changeSalesPage = (page) => {
  salesCurrentPage.value = page
  fetchSalesData()
}

watch([salesYear, salesMonth], () => {
  salesCurrentPage.value = 0
})

// --- 발주 상태 관리 ---
const orderYear = ref('2026')
const orderMonth = ref('04')
const orderData = ref([])
const totalOrderAmount = ref(0)
const expectedPayAmount = ref(0)
const orderCurrentPage = ref(0)
const orderTotalPages = ref(1)
const orderPageSize = 10

const fetchOrderData = async () => {
  try {
    const response = await getOrderSettlement(orderYear.value, Number(orderMonth.value), orderCurrentPage.value, orderPageSize)
    const result = response.data.result

    if (result) {
      totalOrderAmount.value = result.totalAmount || 0
      expectedPayAmount.value = result.expectedPayAmount || 0
      if (result.orderHistory) {
        orderData.value = result.orderHistory.content || []
        orderTotalPages.value = result.orderHistory.totalPages || 1
      }
    }
  } catch (error) {
    console.error('발주 데이터를 불러오는데 실패했습니다:', error)
  }
}

const changeOrderPage = (page) => {
  orderCurrentPage.value = page
  fetchOrderData()
}

watch([orderYear, orderMonth], () => {
  orderCurrentPage.value = 0
  fetchOrderData()
})

// --- 미결제 상태 관리 ---
const unpaidList = ref([])
const isPaymentProcessing = ref(false)

const fetchUnpaidList = async () => {
  try {
    const response = await settlementApi.getUnpaidList()
    unpaidList.value = response.data || []
  } catch (error) {
    console.error('미결제 내역을 불러오는데 실패했습니다:', error)
  }
}

const onManualPayment = async (item) => {
  if (isPaymentProcessing.value) return
  
  if (!confirm(`${item.payedMonth}분 정산 금액 ${item.amount.toLocaleString()}원을 수동 결제하시겠습니까?`)) {
    return
  }

  isPaymentProcessing.value = true

  try {
    // 1. 서버에 결제 준비 요청 (Settlement 생성)
    const createPay = await api.post('/pay', {
      paymentPrice: item.amount,
      headIncomeidxList: item.headIncomeIdxList
    })

    const settlementIdx = createPay.data.settlementIdx
    const orderName = `${item.payedMonth}분 정산 수동 결제`

    // 2. 포트원 결제 요청
    const paymentId = `manual-${new Date().getTime()}-${Math.floor(Math.random() * 1000)}`
    const payment = await PortOne.requestPayment({
      storeId: "store-e41df7ff-0ba8-4ccc-b19f-7655c291bbcb",
      channelKey: "channel-key-40356538-ccfe-4420-a5a9-d9190bde73cf",
      paymentId: paymentId,
      orderName: orderName,
      totalAmount: item.amount,
      currency: 'KRW',
      payMethod: "CARD",
    })

    if (payment.code != null) {
      // 결제 실패/취소
      alert('결제가 중단되었습니다: ' + (payment.message || '알 수 없는 오류'))
      isPaymentProcessing.value = false
      return
    }

    // 3. 서버 검증 요청
    await settlementApi.verifySettlement({
      paymentId: payment.paymentId,
      settlementIdx: settlementIdx
    })

    alert('결제가 성공적으로 완료되었습니다.')
    await fetchUnpaidList() // 목록 갱신

  } catch (error) {
    console.error('결제 과정 중 오류 발생:', error)
    alert('결제 처리 중 오류가 발생했습니다.')
  } finally {
    isPaymentProcessing.value = false
  }
}

// --- 탭 전환 시 조회 ---
watch(activeTab, (newTab) => {
  if (newTab === 'sales' && salesData.value.length === 0) {
    fetchSalesData()
  }
  if (newTab === 'order' && orderData.value.length === 0) {
    fetchOrderData()
  }
  if (newTab === 'unpaid') {
    fetchUnpaidList()
  }
})

onMounted(() => {
  fetchSalesData()
})
</script>
