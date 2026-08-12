<script setup>
const props = defineProps({
  items: {
    type: Array,
    required: true,
  },
  totalStock: {
    // (item) => number
    type: Function,
    required: true,
  },
  getStatus: {
    // (item) => string
    type: Function,
    required: true,
  },
  getStatusClass: {
    // (item) => string
    type: Function,
    required: true,
  },
})

const emit = defineEmits(['open-detail'])

const openDetail = (item) => emit('open-detail', item)
const getRowKey = (item) => item.productIdx ?? item.idx ?? item.code

const headerCellClass = 'px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider'
const rowCellClass = 'px-5 py-3.5'

const handleRowKeydown = (event, item) => {
  if (event.key !== 'Enter' && event.key !== ' ') return
  event.preventDefault()
  openDetail(item)
}
</script>

<template>
  <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
    <table class="w-full text-sm text-left">
      <thead>
        <tr class="border-b border-gray-200 bg-gray-50">
          <th :class="headerCellClass">상품코드</th>
          <th :class="headerCellClass">품목명</th>
          <th :class="headerCellClass">전산 재고</th>
          <th :class="headerCellClass">상태</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-100">
        <tr
          v-for="item in props.items"
          :key="getRowKey(item)"
          role="button"
          tabindex="0"
          class="hover:bg-blue-50/40 transition-colors cursor-pointer"
          @click="openDetail(item)"
          @keydown="handleRowKeydown($event, item)"
        >
          <td :class="`${rowCellClass} font-mono text-xs text-gray-400`">{{ item.code }}</td>
          <td :class="`${rowCellClass} font-semibold text-gray-900`">{{ item.name }}</td>
          <td :class="[`${rowCellClass} font-bold`, props.totalStock(item) < item.min ? 'text-red-600' : 'text-gray-900']">
            {{ props.totalStock(item).toLocaleString() }}
          </td>
          <td :class="rowCellClass">
            <span class="text-xs font-bold px-2 py-0.5 rounded" :class="props.getStatusClass(item)">
              {{ props.getStatus(item) }}
            </span>
          </td>
        </tr>
        <tr v-if="props.items.length === 0">
          <td colspan="4" class="px-5 py-12 text-center text-gray-400 text-sm">조회된 재고가 없습니다.</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
