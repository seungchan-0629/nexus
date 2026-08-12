<script setup>
import { reactive, ref, watch } from 'vue'
import { Plus } from 'lucide-vue-next'
import { formatPrice } from '../orderUtils'
import { getProductList } from '@/api/product'
import Toast from '@/components/common/Toast.vue'
import { useToast } from '@/composables/useToast'

const { toast, showToast } = useToast()

const props = defineProps({
  visible: { type: Boolean, required: true },
})

const emit = defineEmits(['close', 'submit'])

const products = ref([])
const form = reactive({ items: [] })

watch(() => props.visible, async (val) => {
  if (!val) return
  form.items = []
  try {
    const productRes = await getProductList(0, 1000)
    products.value = productRes.data.result.productList ?? []
  } catch (e) {
    console.error('상품 목록 조회 실패', e)
  }
})

function addItem() {
  form.items.push({ product: null, productKeyword: '', showDropdown: false, qty: 1, unitPrice: 0 })
}

function filterProducts(item) {
  const keyword = item.productKeyword?.trim() || ''
  const selectedIds = new Set(form.items.filter(i => i.product).map(i => i.product))
  return products.value.filter(p => !selectedIds.has(p.idx) && (keyword === '' || p.productName.includes(keyword)))
}

function selectProduct(item, product) {
  item.product = product.idx
  item.productKeyword = product.productName
  item.unitPrice = product.unitPrice
  item.showDropdown = false
}

function clearProduct(item) {
  item.product = null
  item.productKeyword = ''
  item.unitPrice = 0
}

function submit() {
  if (form.items.length === 0) {
    showToast('품목을 입력해주세요.', 'error')
    return
  }
  const validItems = form.items.filter(i => i.product)
  if (validItems.length === 0) {
    showToast('품목을 선택해주세요.', 'error')
    return
  }
  emit('submit', {
    ordersItemList: validItems.map(i => ({ productIdx: i.product, count: i.qty })),
  })
  form.items = []
}
</script>

<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="absolute inset-0 bg-black/40" @click="$emit('close')"></div>
    <div class="relative bg-white rounded-xl w-full max-w-2xl border border-gray-200 shadow-xl">
      <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
        <h3 class="font-bold text-gray-900">수동 발주 생성</h3>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 cursor-pointer">✕</button>
      </div>
      <div class="p-6 space-y-4">
        <div class="space-y-2">
          <div class="flex justify-between items-center">
            <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">발주 품목</label>
            <button @click="addItem" class="text-xs text-blue-500 font-semibold hover:underline flex items-center gap-1 cursor-pointer">
              <Plus class="w-3 h-3" /> 품목 추가
            </button>
          </div>
          <div v-for="(item, idx) in form.items" :key="idx" class="flex gap-2 items-center">
            <div class="flex-1 relative">
              <div v-if="item.product" class="flex items-center justify-between px-3 py-2 rounded border border-blue-400 bg-blue-50 text-sm">
                <span class="font-medium text-gray-900">{{ item.productKeyword }}</span>
                <button @click="clearProduct(item)" class="text-gray-400 hover:text-gray-600 cursor-pointer">✕</button>
              </div>
              <input v-else v-model="item.productKeyword" @input="item.showDropdown = true" @focus="item.showDropdown = true" @blur="item.showDropdown = false" placeholder="상품명 검색"
                class="w-full px-3 py-2 rounded border border-gray-200 text-sm focus:border-blue-400 focus:ring-2 focus:ring-blue-100 outline-none" />
              <ul v-if="!item.product && item.showDropdown"
                class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded shadow-lg max-h-60 overflow-y-auto">
                <li v-for="p in filterProducts(item)" :key="p.idx" @mousedown.prevent @click="selectProduct(item, p)"
                  class="px-3 py-2 text-sm hover:bg-blue-50 cursor-pointer">
                  {{ p.productName }} <span class="text-gray-400 text-xs">{{ p.productUnit }}</span>
                </li>
              </ul>
            </div>
            <div class="flex items-center gap-1.5">
              <input v-model.number="item.qty" type="number" min="1" placeholder="수량"
                class="w-20 px-3 py-2 rounded border border-gray-200 text-sm focus:border-blue-400 focus:ring-2 focus:ring-blue-100 outline-none" />
              <span class="text-xs text-gray-400 font-medium w-auto shrink-0">{{ item.product ? products.find(p => p.idx === item.product)?.productUnit : '' }}</span>
            </div>
            <div class="w-28 px-3 py-2 rounded border border-gray-100 bg-gray-50 text-sm text-gray-500 text-right shrink-0">
              {{ item.product ? formatPrice(item.unitPrice) : '-' }}
            </div>
            <button @click="form.items.splice(idx, 1)"
              class="px-3 py-2 text-xs font-semibold rounded border border-red-200 text-red-500 bg-red-50 hover:bg-red-500 hover:text-white hover:cursor-pointer transition-colors shrink-0">
              삭제
            </button>
          </div>
          <div v-if="form.items.length === 0"
            class="text-sm text-gray-400 text-center py-4 bg-gray-50 border border-gray-100 rounded">
            품목 추가 버튼을 눌러 발주 품목을 입력하세요.
          </div>
          <div v-if="form.items.length > 0" class="text-right text-sm font-bold text-blue-600">
            합계: {{ formatPrice(form.items.reduce((s, i) => s + (i.unitPrice || 0) * (i.qty || 0), 0)) }}
          </div>
        </div>
      </div>
      <div class="px-6 py-4 border-t border-gray-100 flex gap-3">
        <button @click="$emit('close')"
          class="flex-1 py-2.5 rounded border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 cursor-pointer">취소</button>
        <button @click="submit"
          class="flex-1 py-2.5 rounded bg-blue-500 text-white text-sm font-bold hover:bg-blue-600 cursor-pointer">발주 생성</button>
      </div>
    </div>
    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </div>
</template>
