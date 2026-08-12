<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDelayDeliveryList } from '@/api/dashboard'

const router = useRouter()
const deliveries = ref([])
const scrollContainer = ref(null)
let page = 0
let hasNext = true
const loading = ref(false)

const fetchData = async () => {
  if (loading.value || !hasNext) return
  loading.value = true
  try {
    const { data } = await getDelayDeliveryList(page, 10)
    const result = data.result
    deliveries.value.push(...result.items)
    hasNext = result.hasNext
    page++
  } catch (e) {
    console.error('지연 배송 조회 실패', e)
  } finally {
    loading.value = false
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
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm flex flex-col max-h-[320px]">
    <div class="px-5 pt-5 pb-4 border-b border-gray-100">
      <h2 class="font-bold text-gray-900">지연 배송</h2>
    </div>
    <div v-if="deliveries.length === 0 && !loading" class="flex-1 flex items-center justify-center py-8">
      <p class="text-xs text-gray-400">데이터가 없습니다</p>
    </div>
    <div v-else class="flex-1 divide-y divide-gray-50 overflow-y-auto" ref="scrollContainer" @scroll="onScroll">
      <div v-for="d in deliveries" :key="d.deliveryIdx"
        class="px-5 py-4 hover:bg-gray-50 transition-colors cursor-pointer"
        role="button" tabindex="0"
        @click="router.push('/delivery')"
        @keydown.enter="router.push('/delivery')"
        @keydown.space.prevent="router.push('/delivery')">
        <div class="flex items-start gap-3">
          <div class="flex-1 min-w-0">
            <p class="text-xs text-gray-400">발주 #{{ d.ordersIdx }}</p>
            <p class="text-sm font-bold text-gray-900">{{ d.storeName }}</p>
          </div>
          <span class="text-xs px-2 py-0.5 rounded-full font-medium shrink-0 bg-red-50 text-red-600 border border-red-200">
            지연
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
