<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  item: {
    type: Object,
    default: null,
  },
  detailTitleId: {
    type: String,
    required: true,
  },
  totalStock: {
    type: Function,
    required: true,
  },
  fifoLots: {
    type: Function,
    required: true,
  },
  isExpiringSoon: {
    type: Function,
    required: true,
  },
  getSubtitle: {
    type: Function,
    required: true,
  },
  editable: {
    type: Boolean,
    default: false,
  },
  wasteEnabled: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['close', 'apply-adjustments', 'apply-waste'])

const sortedLots = computed(() => {
  if (!props.item) return []
  return props.fifoLots(props.item)
})

const lotDate = (row) => row?.manufacturedDate ?? row?.expiry ?? null
const draftLots = ref([])

watch(
  sortedLots,
  (lots) => {
    draftLots.value = lots.map((row, idx) => ({
      id: row.id ?? `${lotDate(row) ?? 'none'}-${idx}`,
      expiry: lotDate(row),
      qty: row.qty ?? 0,
      adjustTo: row.qty ?? 0,
      wasteQty: 0,
    }))
  },
  { immediate: true },
)

const hasDraftLotChanges = computed(() =>
  draftLots.value.some((row) => Number(row.adjustTo) !== Number(row.qty)),
)

const hasWasteQtyChanges = computed(() =>
  draftLots.value.some((row) => Number(row.wasteQty) > 0),
)

const applyAdjustments = () => {
  const changes = draftLots.value
    .map((row) => ({ ...row, adjustTo: Number(row.adjustTo) }))
    .filter((row) => Number.isFinite(row.adjustTo) && row.adjustTo >= 0 && row.adjustTo !== Number(row.qty))

  if (!changes.length) return
  emit('apply-adjustments', changes)
}

const applyWaste = () => {
  const changes = draftLots.value
    .map((row) => {
      const qty = Number(row.qty ?? 0)
      const wasteQty = Number(row.wasteQty ?? 0)
      return { ...row, qty, wasteQty }
    })
    .filter((row) => Number.isFinite(row.wasteQty) && row.wasteQty > 0 && row.wasteQty <= row.qty)

  if (!changes.length) return
  emit('apply-waste', changes)
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="item"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
      role="dialog"
      aria-modal="true"
      :aria-labelledby="detailTitleId"
      @click.self="$emit('close')"
    >
      <div class="relative bg-white rounded-xl shadow-xl max-w-lg w-full max-h-[85vh] overflow-hidden flex flex-col border border-gray-200">
        <div class="flex items-start justify-between gap-3 px-6 py-4 border-b border-gray-200">
          <div class="min-w-0">
            <p :id="detailTitleId" class="font-bold text-gray-900 truncate">{{ item.name }}</p>
            <p class="text-xs font-mono text-gray-500 mt-0.5">{{ getSubtitle(item) }}</p>
            <p class="text-xs text-gray-500 mt-2">
              합계 <span class="font-semibold text-gray-800">{{ totalStock(item).toLocaleString() }}</span>
            </p>
          </div>
          <button
            type="button"
            class="text-gray-400 hover:text-gray-600 cursor-pointer"
            aria-label="닫기"
            @click="$emit('close')"
          >
            ✕
          </button>
        </div>
        <div class="overflow-y-auto flex-1">
          <table class="w-full text-sm text-left">
            <thead>
              <tr class="border-b border-gray-100 bg-white sticky top-0">
                <th class="px-5 py-2.5 text-[10px] font-bold text-gray-400 uppercase tracking-wider">유통기한</th>
                <th class="px-5 py-2.5 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-right">수량</th>
                <th v-if="props.editable" class="px-5 py-2.5 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-right">보정 후</th>
                <th v-if="props.editable && props.wasteEnabled" class="px-5 py-2.5 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-right">폐기 수량</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-for="(row, idx) in draftLots" :key="row.id ?? `${row.expiry ?? 'none'}-${row.qty}-${idx}`">
                <td class="px-5 py-3 text-xs font-mono" :class="isExpiringSoon(lotDate(row)) ? 'text-orange-600 font-semibold' : 'text-gray-700'">
                  {{ lotDate(row) ?? '—' }}
                </td>
                <td class="px-5 py-3 text-right font-semibold text-gray-900">{{ row.qty.toLocaleString() }}</td>
                <td v-if="props.editable" class="px-5 py-3 text-right">
                  <input
                    v-model.number="row.adjustTo"
                    type="number"
                    min="0"
                    step="1"
                    class="w-full max-w-[6rem] ml-auto px-2 py-1.5 rounded-md border border-gray-200 text-sm text-right focus:border-blue-400 focus:ring-2 focus:ring-blue-100 outline-none"
                  />
                </td>
                <td v-if="props.editable && props.wasteEnabled" class="px-5 py-3 text-right">
                  <input
                    v-model.number="row.wasteQty"
                    type="number"
                    min="0"
                    :max="row.qty"
                    step="1"
                    class="w-full max-w-[6rem] ml-auto px-2 py-1.5 rounded-md border border-gray-200 text-sm text-right focus:border-red-400 focus:ring-2 focus:ring-red-100 outline-none"
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="px-6 py-4 border-t border-gray-100 flex justify-end">
          <button
            v-if="props.editable && props.wasteEnabled"
            type="button"
            class="px-4 py-2 text-sm font-semibold text-white bg-red-500 rounded hover:bg-red-600 disabled:bg-gray-200 disabled:text-gray-400 disabled:cursor-not-allowed mr-2"
            :disabled="!hasWasteQtyChanges"
            @click="applyWaste"
          >
            폐기
          </button>
          <button
            v-if="props.editable"
            type="button"
            class="px-4 py-2 text-sm font-semibold text-white bg-blue-500 rounded hover:bg-blue-600 disabled:bg-gray-200 disabled:text-gray-400 disabled:cursor-not-allowed mr-2"
            :disabled="!hasDraftLotChanges"
            @click="applyAdjustments"
          >
            수정
          </button>
          <button
            type="button"
            class="px-4 py-2 text-sm font-semibold text-gray-600 border border-gray-200 rounded hover:bg-gray-50 cursor-pointer"
            @click="$emit('close')"
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
