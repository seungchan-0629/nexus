<script setup>
import {ref, reactive, onMounted} from 'vue'
import axios from "axios";
import PortOne from "@portone/browser-sdk/v2"

const unpaidList = ref([])
const totalPrice = ref(0)
const selected = ref([])
const settlementIdx = ref(null)
const isPaymentProcessing = ref(false)

// 결제 상태
const paymentStatus = ref({
  status: "",
  message: ""
});

const getUnpaidList = async () => {
  try {
    const res = await axios.get('http://localhost:8080/unpaid/list', { withCredentials: true })
    if (res.data) {
      unpaidList.value = res.data.unpaidList || []
      settlementIdx.value = res.data.settlementIdx || null
    }
  } catch (error) {
    console.error("데이터 로드 실패:", error)
  }
}

const toggleSelected = (item) => {
  const index = selected.value.indexOf(item)
  if (index > -1) {
    selected.value.splice(index, 1)
  } else {
    selected.value.push(item)
  }

  totalPrice.value = selected.value.reduce((acc, cur) => acc + Number(cur.price || 0), 0)
}

const selectAll = () => {
  if (selected.value.length === unpaidList.value.length) {
    selected.value = []
  } else {
    selected.value = [...unpaidList.value]
  }
  totalPrice.value = selected.value.reduce((acc, cur) => acc + Number(cur.price || 0), 0)
}

const onPayment = async () => {
  if (selected.value.length === 0) return
  if (isPaymentProcessing.value) return

  isPaymentProcessing.value = true
  paymentStatus.value = {status: "", message: ""}

  const firstItem = selected.value[0]
  const headIncomeidxList = selected.value.map(item => item.idx)
  const orderName = selected.value.length === 1
      ? `주문 #${firstItem.idx}`
      : `주문 #${firstItem.idx} 외 ${selected.value.length - 1}건`

  const createPay = await axios.post('http://localhost:8080/pay', {
        paymentPrice: totalPrice.value,
        headIncomeidxList: headIncomeidxList
      }, {
        withCredentials: true
      }
  )

  settlementIdx.value = createPay.data.settlementIdx
  paymentStatus.value = {status: "IDLE" , message : '결제 진행 중...'}

  const paymentId = `order-${new Date().getTime()}-${Math.floor(Math.random() * 1000)}`;
  const payment = await PortOne.requestPayment({
    storeId: "store-e41df7ff-0ba8-4ccc-b19f-7655c291bbcb",
    channelKey: "channel-key-40356538-ccfe-4420-a5a9-d9190bde73cf",
    paymentId: paymentId,
    orderName: orderName,
    totalAmount: totalPrice.value,
    currency: 'KRW',
    payMethod: "CARD",
    customData: {headIncomeidxList}
  }).then((res) => {
    return res;
  }).catch((error) => {
      paymentStatus.value = {status: "FAILED", message: '결제 시도가 실패하였습니다. 잠시 후 다시 시도해주세요.'}
    });
    if (!payment) {
      isPaymentProcessing.value = false
      return
    }

    await axios.post('http://localhost:8080/verify', {
      paymentId: payment.paymentId,
      settlementIdx: settlementIdx.value || firstItem.settlementIdx
    }, {
      withCredentials: true
    })
    isPaymentProcessing.value = false
  }

onMounted(async () => {
  await getUnpaidList()
})
</script>

<template>
  <div class="p-6 max-w-6xl mx-auto">
    <div class="mb-6">
      <h1 class="text-3xl font-bold text-gray-900">결제 테스트</h1>
      <p class="text-sm text-gray-500 mt-2">미결제 항목을 선택하여 결제를 진행하세요.</p>
    </div>

    <div class="bg-white rounded-lg shadow border border-gray-200 p-6">
      <h2 class="text-xl font-semibold text-gray-900 mb-4">미결제 리스트</h2>
      <div class="mb-4">
        <button
          @click="selectAll"
          class="px-4 py-2 bg-gray-600 text-white rounded-lg font-semibold hover:bg-gray-700"
        >
          {{ selected.length === unpaidList.length ? '전체 해제' : '전체 선택' }}
        </button>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm text-left">
          <thead class="bg-gray-50 text-gray-700">
            <tr>
              <th class="px-4 py-3">
                <input
                  type="checkbox"
                  :checked="selected.length === unpaidList.length && unpaidList.length > 0"
                  @change="selectAll"
                  class="w-4 h-4 text-blue-600 border-gray-300 rounded"
                />
              </th>
              <th class="px-4 py-3">idx</th>
              <th class="px-4 py-3">price</th>
              <th class="px-4 py-3">settlement_idx</th>
              <th class="px-4 py-3">paid</th>
              <th class="px-4 py-3">orders_idx</th>
              <th class="px-4 py-3">store_idx</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in unpaidList" :key="item.idx" class="border-b border-gray-100 hover:bg-gray-50">
              <td class="px-4 py-3">
                <input
                  type="checkbox"
                  :checked="selected.includes(item)"
                  @change="toggleSelected(item)"
                  class="w-4 h-4 text-blue-600 border-gray-300 rounded"
                />
              </td>
              <td class="px-4 py-3 font-mono text-xs">{{ item.idx }}</td>
              <td class="px-4 py-3">{{ item.price != null ? Number(item.price).toLocaleString() : '0' }}원</td>
              <td class="px-4 py-3">{{ item.settlementIdx }}</td>
              <td class="px-4 py-3">
                <span :class="item.paid ? 'text-green-600' : 'text-red-600'">
                  {{ item.paid ? '완료' : '미완료' }}
                </span>
              </td>
              <td class="px-4 py-3">{{ item.ordersIdx }}</td>
              <td class="px-4 py-3">{{ item.storeIdx }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="mt-6 flex items-center justify-between">
        <div class="text-sm text-gray-700">
          선택된 항목: <span class="font-semibold">{{ selected.length }}건</span>,
          총 금액: <span class="font-semibold text-blue-600">{{ totalPrice.toLocaleString() }}원</span>
        </div>
        <button
          @click="onPayment"
          :disabled="isPaymentProcessing || selected.length === 0"
          class="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          {{ isPaymentProcessing ? '결제 처리 중...' : '결제하기' }}
        </button>
      </div>

      <div v-if="paymentStatus.message" class="mt-4 p-4 rounded-lg"
        :class="{
          'bg-blue-50 border-blue-200 text-blue-700': paymentStatus.status === 'IDLE',
          'bg-green-50 border-green-200 text-green-700': paymentStatus.status === 'SUCCESS',
          'bg-red-50 border-red-200 text-red-700': paymentStatus.status === 'FAILED',
        }"
      >
        {{ paymentStatus.message }}
      </div>
    </div>
  </div>
</template>

<style scoped>


</style>