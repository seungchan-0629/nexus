<script setup>
import { ORDER_STATUS_LABEL, ORDER_TYPE_LABEL, storeStatusClass } from '../orderUtils'

defineProps({
  order: { type: Object, default: null },
})

defineEmits(['close'])
</script>

<template>
  <div v-if="order" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="absolute inset-0 bg-black/40" @click="$emit('close')"></div>
    <div class="relative bg-white rounded-xl w-full max-w-2xl border border-gray-200 shadow-xl">
      <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
        <div>
          <h3 class="font-bold text-gray-900">발주 상세</h3>
          <p class="text-xs text-gray-400 font-mono mt-0.5">No. {{ order.idx }}</p>
        </div>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 cursor-pointer">✕</button>
      </div>
      <div class="p-6 space-y-4 text-sm">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <p class="text-xs text-gray-400 mb-1">유형</p>
            <span class="text-xs font-bold px-2 py-0.5 rounded border"
              :class="order.ordersType === 'AUTO' ? 'bg-blue-50 text-blue-600 border-blue-200' : 'bg-purple-50 text-purple-600 border-purple-200'">
              {{ ORDER_TYPE_LABEL[order.ordersType] }}
            </span>
          </div>
          <div>
            <p class="text-xs text-gray-400 mb-1">상태</p>
            <span class="text-xs font-bold px-2 py-0.5 rounded" :class="storeStatusClass(order.ordersStatus)">
              {{ ORDER_STATUS_LABEL[order.ordersStatus] }}
            </span>
          </div>
          <div class="col-span-2">
            <p class="text-xs text-gray-400 mb-1">발주일시</p>
            <p class="text-gray-700 font-mono text-xs">{{ order.createdAt?.replace('T', ' ').slice(0, 16) }}</p>
          </div>
          <div v-if="order.reason" class="col-span-2">
            <p class="text-xs text-gray-400 mb-1">AI 추천 사유</p>
            <p class="text-sm text-blue-600 font-medium">{{ order.reason }}</p>
          </div>
        </div>
        <div>
          <p class="text-xs text-gray-400 mb-2">품목 목록</p>
          <div class="border border-gray-100 rounded-lg overflow-hidden">
            <table class="w-full text-sm table-fixed">
              <colgroup>
                <col class="w-[34%]" />
                <col class="w-[16%]" />
                <col class="w-[22%]" />
                <col class="w-[28%]" />
              </colgroup>
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-4 py-2.5 text-left text-[10px] font-bold text-gray-400 uppercase">품목명</th>
                  <th class="px-4 py-2.5 text-right text-[10px] font-bold text-gray-400 uppercase">수량</th>
                  <th class="px-4 py-2.5 text-right text-[10px] font-bold text-gray-400 uppercase">단가</th>
                  <th class="px-4 py-2.5 text-right text-[10px] font-bold text-gray-400 uppercase">금액</th>
                </tr>
              </thead>
            </table>
            <div class="max-h-[280px] overflow-y-auto [scrollbar-gutter:stable]">
              <table class="w-full text-sm table-fixed">
                <colgroup>
                  <col class="w-[34%]" />
                  <col class="w-[16%]" />
                  <col class="w-[22%]" />
                  <col class="w-[28%]" />
                </colgroup>
                <tbody class="divide-y divide-gray-100">
                  <tr v-for="item in order.ordersItemList" :key="item.idx">
                    <td class="px-4 py-2.5 text-gray-800 font-semibold">{{ item.productName }}</td>
                    <td class="px-4 py-2.5 text-right text-gray-600">{{ item.count.toLocaleString() }}개</td>
                    <td class="px-4 py-2.5 text-right text-xs text-gray-500">₩ {{ item.unitPrice.toLocaleString() }}</td>
                    <td class="px-4 py-2.5 text-right font-bold text-blue-600 whitespace-nowrap">₩ {{ (item.unitPrice * item.count).toLocaleString() }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <table class="w-full text-sm table-fixed">
              <colgroup>
                <col class="w-[34%]" />
                <col class="w-[16%]" />
                <col class="w-[22%]" />
                <col class="w-[28%]" />
              </colgroup>
              <tfoot class="bg-gray-50 border-t border-gray-200">
                <tr>
                  <td class="px-4 py-2.5 text-left text-xs font-bold text-gray-500">합계</td>
                  <td class="px-4 py-2.5"></td>
                  <td class="px-4 py-2.5"></td>
                  <td class="px-4 py-2.5 text-right font-black text-blue-600 whitespace-nowrap">
                    ₩ {{ order.price?.toLocaleString() }}
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>
      </div>
      <div class="px-6 py-4 border-t border-gray-100 flex justify-end">
        <button @click="$emit('close')"
          class="px-4 py-2 text-sm font-semibold text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer">닫기</button>
      </div>
    </div>
  </div>
</template>
