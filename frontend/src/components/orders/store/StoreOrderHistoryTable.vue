<script setup>
import { computed } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { ORDER_STATUS_LABEL, ORDER_TYPE_LABEL, storeStatusClass } from '../orderUtils'

const props = defineProps({
  orders: { type: Array, required: true },
  currentPage: { type: Number, default: 0 },
  totalPages: { type: Number, default: 0 },
})

const emit = defineEmits(['open-detail', 'cancel', 'page-change'])

const visiblePages = computed(() => {
  const range = 10
  const group = Math.floor(props.currentPage / range)
  const start = group * range
  const end = Math.min(start + range, props.totalPages)
  const pages = []
  for (let i = start; i < end; i++) pages.push(i)
  return pages
})
</script>

<template>
  <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
    <table class="w-full text-sm text-left">
      <thead>
        <tr class="border-b border-gray-200 bg-gray-50">
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">발주번호</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">유형</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">품목</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">총 금액</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">발주일시</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">상태</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider w-[100px] text-center">처리</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-100">
        <tr v-for="h in orders" :key="h.idx"
          class="hover:bg-gray-50/50 transition-colors cursor-pointer"
          @click="emit('open-detail', h)">
          <td class="px-5 py-3.5 font-mono text-xs text-gray-400">{{ h.idx }}</td>
          <td class="px-5 py-3.5 text-center">
            <span class="text-xs font-bold px-2 py-0.5 rounded"
              :class="h.ordersType === 'AUTO' ? 'bg-blue-50 text-blue-600 border border-blue-200' : 'bg-purple-50 text-purple-600 border border-purple-200'">
              {{ ORDER_TYPE_LABEL[h.ordersType] }}
            </span>
          </td>
          <td class="px-5 py-3.5 font-semibold text-gray-900">
            {{ h.ordersItemList[0]?.productName }}
            <span v-if="h.ordersItemList.length > 1" class="text-xs text-gray-400 font-normal"> 외 {{ h.ordersItemList.length - 1 }}건</span>
          </td>
          <td class="px-5 py-3.5 font-semibold text-gray-700">
            ₩ {{ h.price?.toLocaleString() }}
          </td>
          <td class="px-5 py-3.5 text-xs text-gray-400 font-mono text-center">{{ h.createdAt?.replace('T', ' ').slice(0, 16) }}</td>
          <td class="px-5 py-3.5 text-center">
            <span class="text-xs font-bold px-2 py-0.5 rounded" :class="storeStatusClass(h.ordersStatus)">
              {{ ORDER_STATUS_LABEL[h.ordersStatus] }}
            </span>
          </td>
          <td class="px-5 py-2 align-middle">
            <div class="flex items-center justify-center h-[28px]">
              <button v-if="h.ordersStatus === 'CONFIRMED'"
                @click.stop="emit('cancel', h)"
                class="px-3 py-1.5 text-xs font-semibold rounded-lg border border-red-200 text-red-500 bg-red-50 hover:bg-red-500 hover:text-white hover:cursor-pointer transition-colors">
                취소
              </button>
              <span v-else class="text-xs text-gray-400">—</span>
            </div>
          </td>
        </tr>
        <tr v-if="orders.length === 0">
          <td colspan="7" class="px-5 py-10 text-center text-sm text-gray-400">발주 이력이 없습니다.</td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- Pagination -->
  <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 pt-2">
    <button
      class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
      :disabled="currentPage === 0"
      @click="emit('page-change', currentPage - 1)">
      <ChevronLeft class="w-4 h-4" />
    </button>
    <button v-for="page in visiblePages" :key="page"
      class="w-8 h-8 rounded text-sm font-semibold cursor-pointer"
      :class="currentPage === page
        ? 'bg-[#F37321] text-white'
        : 'text-gray-500 hover:bg-gray-50'"
      @click="emit('page-change', page)">
      {{ page + 1 }}
    </button>
    <button
      class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
      :disabled="currentPage === totalPages - 1"
      @click="emit('page-change', currentPage + 1)">
      <ChevronRight class="w-4 h-4" />
    </button>
  </div>
</template>
