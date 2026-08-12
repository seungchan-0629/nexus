<template>
  <div class="flex-1 overflow-auto p-8 space-y-6">
    <div class="flex justify-between items-end">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">일일 정산</h1>
        <p class="text-sm text-gray-500 mt-1">매출 내역과 결제 이력을 확인하고 영업 마감을 관리합니다.</p>
      </div>
      <button
        type="button"
        @click="$emit('toggle-store-status')"
        :class="['text-sm px-6 py-2.5 rounded-lg font-bold transition-all shadow-sm text-white cursor-pointer',
          sales.isClosed ? 'bg-blue-500 hover:bg-blue-600 ring-4 ring-blue-100' : 'bg-gray-800 hover:bg-gray-700']">
        {{ sales.isClosed ? '새로운 영업 시작하기' : '영업 마감하기' }}
      </button>
    </div>

    <div class="grid grid-cols-3 gap-4">
      <div class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
        <p class="text-sm font-medium text-gray-500">총 매출액</p>
        <div class="flex items-end gap-1 mt-2">
          <h3 class="text-3xl font-bold text-gray-900">{{ sales.total.toLocaleString() }}</h3>
          <span class="text-base font-medium text-gray-500 mb-1">원</span>
        </div>
      </div>
      <div class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
        <p class="text-sm font-medium text-gray-500">총 결제 건수</p>
        <div class="flex items-end gap-1 mt-2">
          <h3 class="text-3xl font-bold text-gray-900">{{ sales.count }}</h3>
          <span class="text-base font-medium text-gray-500 mb-1">건</span>
        </div>
      </div>
      <div class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
        <p class="text-sm font-medium text-gray-500">평균 객단가</p>
        <div class="flex items-end gap-1 mt-2">
          <h3 class="text-3xl font-bold text-gray-900">
            {{ sales.count > 0 ? Math.floor(sales.total / sales.count).toLocaleString() : 0 }}
          </h3>
          <span class="text-base font-medium text-gray-500 mb-1">원</span>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-3 gap-6">
      <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
        <div class="px-5 py-4 border-b border-gray-100 font-semibold text-gray-900 text-sm">결제 수단별 매출</div>
        <table class="w-full text-sm">
          <thead class="bg-gray-50 text-xs text-gray-500 uppercase">
            <tr class="border-b border-gray-200">
              <th class="px-5 py-3 text-left">수단</th>
              <th class="px-5 py-3 text-center">비율</th>
              <th class="px-5 py-3 text-right">금액</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr class="hover:bg-gray-50/50">
              <td class="px-5 py-4 font-medium">신용카드</td>
              <td class="px-5 py-4 text-center text-gray-500">
                {{ sales.total > 0 ? Math.round((sales.card / sales.total) * 100) : 0 }}%
              </td>
              <td class="px-5 py-4 font-bold text-right">{{ formatPrice(sales.card) }}</td>
            </tr>
            <tr class="hover:bg-gray-50/50">
              <td class="px-5 py-4 font-medium">현금</td>
              <td class="px-5 py-4 text-center text-gray-500">
                {{ sales.total > 0 ? Math.round((sales.cash / sales.total) * 100) : 0 }}%
              </td>
              <td class="px-5 py-4 font-bold text-right">{{ formatPrice(sales.cash) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="col-span-2 bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden flex flex-col max-h-[360px]">
        <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center font-semibold text-gray-900 text-sm shrink-0">
          <span>실시간 결제 내역</span>
          <span class="text-xs text-gray-400 font-normal">최신 순</span>
        </div>
        <div class="overflow-auto flex-1">
          <table class="w-full text-sm">
            <thead class="bg-gray-50 text-xs text-gray-500 uppercase sticky top-0">
              <tr class="border-b border-gray-200">
                <th class="px-6 py-3 text-left">결제 시간</th>
                <th class="px-6 py-3 text-left">주문 내역</th>
                <th class="px-6 py-3 text-center">결제 수단</th>
                <th class="px-6 py-3 text-right">결제 금액</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-if="paymentHistory.length === 0">
                <td colspan="4" class="px-6 py-12 text-center text-gray-400">
                  <p class="text-sm">오늘 등록된 결제 내역이 없습니다.</p>
                </td>
              </tr>
              <tr v-for="h in paymentHistory" :key="h.id" class="hover:bg-gray-50/50">
                <td class="px-6 py-4 font-mono text-xs text-gray-500">{{ h.time }}</td>
                <td class="px-6 py-4 text-gray-800 font-medium truncate max-w-[200px]" :title="h.items">{{ h.items }}</td>
                <td class="px-6 py-4 text-center">
                  <span class="px-2.5 py-1 rounded-full text-xs font-bold"
                    :class="h.method === '신용카드' ? 'bg-blue-50 text-blue-600' : 'bg-emerald-50 text-emerald-600'">
                    {{ h.method }}
                  </span>
                </td>
                <td class="px-6 py-4 font-bold text-gray-900 text-right">{{ formatPrice(h.amount) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { formatPrice } from '@/utils/formatPrice.js'

defineProps({
  sales: {
    type: Object,
    required: true,
    /* { total, card, cash, count, isClosed } */
  },
  paymentHistory: { type: Array, default: () => [] },
})

defineEmits(['toggle-store-status'])
</script>
