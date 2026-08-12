<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getInventoryWarningList } from '@/api/store-dashboard'

const router = useRouter()
const warnings = ref([])

const STATUS_LABEL = { CRITICAL: '위험', LOW: '주의' }

onMounted(async () => {
  try {
    const { data } = await getInventoryWarningList()
    warnings.value = data.result
  } catch (e) {
    console.error('재고 위험 품목 목록 조회 실패', e)
  }
})
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
    <div class="px-5 py-4 border-b border-gray-100">
      <h2 class="font-bold text-gray-900">재고 위험 품목</h2>
    </div>
    <div v-if="warnings.length === 0" class="px-5 py-8 text-center text-sm text-gray-400">
      위험 재고 품목이 없습니다
    </div>
    <div v-else>
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-gray-200 bg-gray-50">
            <th class="px-5 py-3 text-left text-[10px] font-bold text-gray-400 uppercase tracking-wider">품목</th>
            <th class="px-5 py-3 text-right text-[10px] font-bold text-gray-400 uppercase tracking-wider">현재 재고</th>
            <th class="px-5 py-3 text-right text-[10px] font-bold text-gray-400 uppercase tracking-wider">최소 재고</th>
            <th class="px-5 py-3 text-right text-[10px] font-bold text-gray-400 uppercase tracking-wider">상태</th>
          </tr>
        </thead>
      </table>
      <div class="max-h-[200px] overflow-y-auto">
        <table class="w-full text-sm">
          <tbody class="divide-y divide-gray-100">
            <tr v-for="w in warnings" :key="w.inventoryIdx"
              class="hover:bg-gray-50/50 cursor-pointer"
              role="button" tabindex="0"
              @click="router.push('/store-inventory')"
              @keydown.enter="router.push('/store-inventory')"
            @keydown.space.prevent="router.push('/store-inventory')">
              <td class="px-5 py-3.5 font-semibold text-gray-800">{{ w.productName }}</td>
              <td class="px-5 py-3.5 text-right font-bold text-red-600">{{ w.currentStock }}{{ w.unit }}</td>
              <td class="px-5 py-3.5 text-right text-gray-400">{{ w.minStock }}{{ w.unit }}</td>
              <td class="px-5 py-3.5 text-right">
                <span class="text-xs font-bold px-2 py-0.5 rounded"
                  :class="w.status === 'CRITICAL' ? 'bg-red-50 text-red-600 border border-red-200' : 'bg-orange-50 text-orange-600 border border-orange-200'">
                  {{ STATUS_LABEL[w.status] || w.status }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
