<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyDeliveryList } from '@/api/store-dashboard'

const router = useRouter()
const deliveries = ref([])

const STATUS_LABEL = {
  READY: '출고대기',
  START: '출고완료',
  DELIVERYING: '배송중',
  DELIVERED: '배송완료',
  DELAY: '배송지연',
}

const DELIVERY_STATUS_CLS = {
  READY: 'bg-gray-100 text-gray-600 border-gray-200',
  START: 'bg-green-50 text-green-700 border-green-200',
  DELIVERYING: 'bg-blue-50 text-blue-600 border-blue-200',
  DELIVERED: 'bg-green-50 text-green-700 border-green-200',
  DELAY: 'bg-red-50 text-red-600 border-red-200',
}
const deliveryCls = s => DELIVERY_STATUS_CLS[s] ?? 'bg-gray-100 text-gray-500 border-gray-200'

onMounted(async () => {
  try {
    const { data } = await getMyDeliveryList()
    deliveries.value = data.result
  } catch (e) {
    console.error('배송 현황 조회 실패', e)
  }
})
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm flex flex-col">
    <div class="px-5 pt-5 pb-4 border-b border-gray-100">
      <h2 class="font-bold text-gray-900">나의 배송 현황</h2>
    </div>
    <div class="flex-1 divide-y divide-gray-50 overflow-y-auto max-h-[240px]">
      <div v-if="deliveries.length === 0" class="px-5 py-8 text-center text-sm text-gray-400">
        진행중인 배송이 없습니다
      </div>
      <div v-for="d in deliveries" :key="d.deliveryIdx"
        class="px-5 py-4 hover:bg-gray-50 transition-colors cursor-pointer"
        role="button" tabindex="0"
        @click="router.push('/store-delivery')"
        @keydown.enter="router.push('/store-delivery')"
        @keydown.space.prevent="router.push('/store-delivery')">
        <div class="flex items-center justify-between gap-3">
          <div class="flex-1 min-w-0">
            <p class="text-sm font-bold text-gray-900">발주 <span class="font-mono">#{{ d.ordersIdx }}</span></p>
            <p class="text-xs text-gray-500 mt-0.5">도착 예정: {{ d.estimatedArrival }}</p>
          </div>
          <span class="text-xs px-2 py-0.5 rounded-full font-medium shrink-0 border"
            :class="deliveryCls(d.status)">{{ STATUS_LABEL[d.status] || d.status }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
