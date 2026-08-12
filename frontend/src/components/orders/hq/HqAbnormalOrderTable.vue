<script setup>
import { ref, computed } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { formatPrice } from '../orderUtils'

const props = defineProps({
  orders: { type: Array, required: true },
  currentPage: { type: Number, default: 0 },
  totalPages: { type: Number, default: 1 },
})

const emit = defineEmits(['open-detail', 'approve', 'reject', 'search', 'page-change'])

const dateFrom = ref('')
const dateTo = ref('')
const search = ref('')

const visiblePages = computed(() => {
  const range = 10
  const group = Math.floor(props.currentPage / range)
  const start = group * range
  const end = Math.min(start + range, props.totalPages)
  const pages = []
  for (let i = start; i < end; i++) pages.push(i)
  return pages
})

function emitSearch() {
  emit('search', {
    startDate: dateFrom.value || null,
    endDate: dateTo.value || null,
    keyword: search.value.trim() || null,
  })
}

function resetFilters() {
  dateFrom.value = ''
  dateTo.value = ''
  search.value = ''
  emitSearch()
}
</script>

<template>
  <div class="space-y-3">
    <div class="bg-white border border-gray-200 rounded-lg px-5 py-4 flex flex-wrap gap-5 items-end">
      <label class="flex flex-col gap-2">
        <span class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">기간 시작</span>
        <input v-model="dateFrom" type="date" class="px-3 py-2 rounded border border-gray-200 text-sm outline-none focus:border-[#F37321]" />
      </label>
      <label class="flex flex-col gap-2">
        <span class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">기간 종료</span>
        <input v-model="dateTo" type="date" class="px-3 py-2 rounded border border-gray-200 text-sm outline-none focus:border-[#F37321]" />
      </label>
      <div class="flex flex-col gap-2 flex-1 min-w-40">
        <span class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">검색</span>
        <div class="flex gap-2 items-center">
          <input v-model="search" type="search" placeholder="발주번호·가맹점"
            class="flex-1 px-3 py-2 rounded border border-gray-200 text-sm outline-none focus:border-[#F37321]"
            @keyup.enter="emitSearch" />
          <button type="button"
            class="text-xs font-semibold text-white bg-[#F37321] px-4 py-2 rounded hover:bg-[#e0661d] shrink-0 cursor-pointer"
            @click="emitSearch">
            검색
          </button>
          <button type="button"
            class="text-xs font-semibold text-gray-500 border border-gray-200 px-4 py-2 rounded hover:bg-gray-50 shrink-0 cursor-pointer"
            @click="resetFilters">
            초기화
          </button>
        </div>
      </div>
    </div>
    <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <table class="w-full text-sm text-left">
        <thead>
          <tr class="border-b border-gray-200 bg-gray-50">
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">발주번호</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">입점 매장</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">발주수량</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">평균수량</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">초과율</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">총 금액</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">발주일시</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">상태</th>
            <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">처리</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="o in orders" :key="o.id"
            class="hover:bg-gray-50/50 transition-colors cursor-pointer"
            @click="$emit('open-detail', o.id)"
          >
            <td class="px-5 py-3.5 font-mono text-xs text-gray-400">{{ o.id }}</td>
            <td class="px-5 py-3.5 font-semibold text-gray-900">{{ o.store }}</td>
            <td class="px-5 py-3.5 font-bold text-red-600 text-center">{{ (o.qty ?? 0).toLocaleString() }}</td>
            <td class="px-5 py-3.5 text-gray-500 text-center">{{ (o.avgQty ?? 0).toLocaleString() }}</td>
            <td class="px-5 py-3.5 text-center">
              <span class="text-xs font-black px-2 py-0.5 rounded bg-red-50 text-red-600 border border-red-200">
                +{{ o.ratio }}%
              </span>
            </td>
            <td class="px-5 py-3.5 font-semibold text-gray-700">{{ formatPrice(o.price) }}</td>
            <td class="px-5 py-3.5 text-xs text-gray-400 font-mono text-center">{{ o.date }}</td>
            <td class="px-5 py-3.5 text-center">
              <span class="text-xs font-bold px-2 py-0.5 rounded"
                :class="o.status === 'APPROVE' || o.status === 'REJECT'
                  ? 'bg-gray-100 text-gray-400 border border-gray-200'
                  : 'bg-red-50 text-red-600 border border-red-200'">
                {{ o.status === 'APPROVE' || o.status === 'REJECT' ? '처리완료' : 'DANGER' }}
              </span>
            </td>
            <td class="px-5 py-3.5 h-[52px]">
              <div v-if="o.status !== 'APPROVE' && o.status !== 'REJECT'" class="flex justify-center gap-1.5">
                <button @click.stop="$emit('approve', o)"
                  class="px-2.5 py-1 bg-[#F37321] text-white text-xs font-semibold hover:bg-[#e0661d] rounded cursor-pointer">승인</button>
                <button @click.stop="$emit('reject', o)"
                  class="px-2.5 py-1 border border-gray-200 text-gray-600 text-xs font-medium hover:bg-gray-50 rounded cursor-pointer">반려</button>
              </div>
              <span v-else class="text-xs text-gray-400 block text-center">—</span>
            </td>
          </tr>
          <tr v-if="orders.length === 0">
            <td colspan="9" class="px-5 py-10 text-center text-sm text-gray-400">이상 발주가 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 pt-2">
      <button
        class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
        :disabled="currentPage === 0"
        @click="$emit('page-change', currentPage - 1)">
        <ChevronLeft class="w-4 h-4" />
      </button>
      <button v-for="page in visiblePages" :key="page"
        class="w-8 h-8 rounded text-sm font-semibold cursor-pointer"
        :class="currentPage === page
          ? 'bg-[#F37321] text-white'
          : 'text-gray-500 hover:bg-gray-50'"
        @click="$emit('page-change', page)">
        {{ page + 1 }}
      </button>
      <button
        class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
        :disabled="currentPage === totalPages - 1"
        @click="$emit('page-change', currentPage + 1)">
        <ChevronRight class="w-4 h-4" />
      </button>
    </div>
  </div>
</template>
