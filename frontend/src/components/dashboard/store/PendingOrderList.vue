<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPendingOrderList } from '@/api/store-dashboard'

const router = useRouter()
const orders = ref([])

onMounted(async () => {
  try {
    const { data } = await getPendingOrderList()
    orders.value = data.result
  } catch (e) {
    console.error('제안 발주서 목록 조회 실패', e)
  }
})
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
    <div class="px-5 py-4 border-b border-gray-100">
      <h2 class="font-bold text-gray-900">미확정 발주</h2>
    </div>
    <div v-if="orders.length === 0" class="px-5 py-8 text-center text-sm text-gray-400">
      제안된 발주서가 없습니다
    </div>
    <div v-else>
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-gray-200 bg-gray-50">
            <th class="px-5 py-3 text-left text-[10px] font-bold text-gray-400 uppercase tracking-wider">품목</th>
            <th class="px-5 py-3 text-right text-[10px] font-bold text-gray-400 uppercase tracking-wider">제안 수량</th>
            <th class="px-5 py-3 text-right text-[10px] font-bold text-gray-400 uppercase tracking-wider">금액</th>
            <th class="px-5 py-3 text-right text-[10px] font-bold text-gray-400 uppercase tracking-wider">상태</th>
          </tr>
        </thead>
      </table>
      <div class="max-h-[200px] overflow-y-auto">
        <table class="w-full text-sm">
          <tbody class="divide-y divide-gray-100">
            <tr v-for="o in orders" :key="o.ordersIdx"
              class="hover:bg-gray-50/50 cursor-pointer"
              role="button" tabindex="0"
              @click="router.push('/store-order')"
              @keydown.enter="router.push('/store-order')"
            @keydown.space.prevent="router.push('/store-order')">
              <td class="px-5 py-3.5 font-semibold text-gray-800">{{ o.productName }}</td>
              <td class="px-5 py-3.5 text-right text-gray-600">{{ o.quantity.toLocaleString() }}개</td>
              <td class="px-5 py-3.5 text-right font-semibold text-gray-700">₩ {{ o.price.toLocaleString() }}</td>
              <td class="px-5 py-3.5 text-right">
                <span class="text-xs font-bold px-2 py-0.5 rounded bg-blue-50 text-blue-600 border border-blue-200">제안중</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
