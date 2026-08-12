<script setup>
import { statusClass, formatPrice } from '../orderUtils'

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
          <p class="text-xs text-gray-400 font-mono mt-0.5">{{ order.id }}</p>
        </div>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 cursor-pointer">✕</button>
      </div>
      <div class="p-6 space-y-4">
        <div class="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p class="text-xs text-gray-400 mb-1">유형</p>
            <span class="text-xs font-bold px-2 py-0.5 rounded border"
              :class="order.type === '자동' ? 'bg-blue-50 text-blue-700 border-blue-200' : 'bg-purple-50 text-purple-700 border-purple-200'">
              {{ order.type }}
            </span>
          </div>
          <div>
            <p class="text-xs text-gray-400 mb-1">상태</p>
            <span class="text-xs font-bold px-2 py-0.5 rounded" :class="statusClass(order.status)">
              {{ order.status }}
            </span>
          </div>
          <div>
            <p class="text-xs text-gray-400 mb-1">대상 매장</p>
            <p class="font-semibold text-gray-900">{{ order.store }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-400 mb-1">발주 일시</p>
            <p class="text-gray-700 font-mono text-xs">{{ order.date }}</p>
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
                  <tr v-for="item in order.items" :key="item.product"
                    :class="item.isAbnormal ? 'bg-red-50/60' : ''">
                    <td class="px-4 py-2.5">
                      <div class="flex items-center gap-1.5">
                        <span :class="item.isAbnormal ? 'text-red-700 font-semibold' : 'text-gray-800'">{{ item.product }}</span>
                        <span v-if="item.isAbnormal" class="text-[10px] font-black px-1.5 py-0.5 rounded bg-red-100 text-red-600 border border-red-200">이상</span>
                      </div>
                    </td>
                    <td class="px-4 py-2.5 text-right font-semibold" :class="item.isAbnormal ? 'text-red-600' : 'text-gray-900'">{{ item.qty.toLocaleString() }}</td>
                    <td class="px-4 py-2.5 text-right text-xs text-gray-500">{{ item.unitPrice ? item.unitPrice.toLocaleString() + '원' : '-' }}</td>
                    <td class="px-4 py-2.5 text-right font-bold text-[#F37321] whitespace-nowrap">{{ item.unitPrice ? formatPrice(item.unitPrice * item.qty) : '-' }}</td>
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
                  <td class="px-4 py-2.5 text-right font-black text-[#F37321] whitespace-nowrap">
                    {{ formatPrice((order.items ?? []).reduce((s, i) => s + (i.unitPrice ? i.unitPrice * i.qty : 0), 0)) }}
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>
        <div v-if="order.avgQty" class="grid grid-cols-2 gap-4 text-sm p-3 bg-rose-50 rounded-lg border border-rose-100">
          <div>
            <p class="text-xs text-rose-400 mb-1">평균 발주수량</p>
            <p class="font-semibold text-gray-800">{{ order.avgQty.toLocaleString() }}</p>
          </div>
          <div>
            <p class="text-xs text-rose-400 mb-1">초과율</p>
            <p class="font-bold text-red-600">+{{ order.ratio }}%</p>
          </div>
        </div>
        <div v-if="order.note" class="text-sm">
          <p class="text-xs text-gray-400 mb-1">비고</p>
          <p class="text-gray-600 bg-gray-50 px-3 py-2 rounded border border-gray-100">{{ order.note }}</p>
        </div>
      </div>
      <div class="px-6 py-4 border-t border-gray-100 flex justify-end">
        <button @click="$emit('close')"
          class="px-4 py-2 text-sm font-semibold text-gray-600 border border-gray-200 rounded hover:bg-gray-50 cursor-pointer">닫기</button>
      </div>
    </div>
  </div>
</template>