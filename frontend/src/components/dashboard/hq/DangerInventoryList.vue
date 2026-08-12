<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDangerInventory } from '@/api/dashboard'

const router = useRouter()
const warnings = ref([])
const scrollContainer = ref(null)
let page = 0
let hasNext = true
let loading = false

const fetchData = async () => {
  if (loading || !hasNext) return
  loading = true
  try {
    const { data } = await getDangerInventory(page, 10)
    const result = data.result
    warnings.value.push(...result.items)
    hasNext = result.hasNext
    page++
  } catch (e) {
    console.error('위험 재고 조회 실패', e)
  } finally {
    loading = false
  }
}

const onScroll = () => {
  const el = scrollContainer.value
  if (!el) return
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 10) {
    fetchData()
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5">
    <div class="mb-3">
      <h2 class="font-bold text-gray-900">재고 위험</h2>
    </div>
    <div v-if="warnings.length === 0" class="text-xs text-gray-400 text-center py-4">
      데이터가 없습니다
    </div>
    <div v-else class="space-y-2 max-h-48 overflow-y-auto" ref="scrollContainer" @scroll="onScroll">
      <div v-for="w in warnings" :key="w.inventoryIdx"
        class="flex items-center justify-between py-1.5 cursor-pointer hover:bg-gray-50 rounded px-1 transition-colors"
        role="button" tabindex="0"
        @click="router.push('/inventory/head-office')"
        @keydown.enter="router.push('/inventory/head-office')"
        @keydown.space.prevent="router.push('/inventory/head-office')">
        <div class="flex items-center gap-2">
          <span class="w-1.5 h-1.5 rounded-full" :class="w.status === 'CRITICAL' ? 'bg-red-500' : 'bg-yellow-500'"></span>
          <p class="text-sm text-gray-800 truncate max-w-28">{{ w.productName }}</p>
        </div>
        <span class="text-xs px-2 py-0.5 rounded-full font-medium"
          :class="w.status === 'CRITICAL' ? 'bg-red-100 text-red-600' : 'bg-yellow-100 text-yellow-600'">
          {{ w.count }}/{{ w.minStock }}
        </span>
      </div>
    </div>
  </div>
</template>
