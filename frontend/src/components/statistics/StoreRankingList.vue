<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getTopStores } from '@/api/statistics'
import AnimatedScore from '@/components/statistics/AnimatedScore.vue'

const rankings = ref([])
let polling = null

const fetchData = async () => {
  try {
    const { data } = await getTopStores()
    rankings.value = data.result.rankings
  } catch (e) {
    console.error('매장 랭킹 조회 실패', e)
  }
}

onMounted(() => {
  fetchData()
  polling = setInterval(fetchData, 2000)
})

onUnmounted(() => {
  if (polling) clearInterval(polling)
})
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 flex flex-col" style="min-height:280px">
    <h2 class="font-bold text-gray-900 mb-1">매장 매출 TOP 5</h2>
    <p class="text-xs text-gray-400 mb-4">오늘 매출 기준</p>
    <TransitionGroup tag="ul" name="rank" class="space-y-3 flex-1 relative">
      <li v-for="(item, i) in rankings" :key="item.idx" class="flex items-center justify-between rank-item">
        <div class="flex items-center gap-3">
          <span class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold"
            :class="i < 3 ? 'bg-orange-100 text-orange-600' : 'bg-gray-100 text-gray-500'">
            {{ i + 1 }}
          </span>
          <span class="text-sm font-medium text-gray-800">{{ item.name }}</span>
        </div>
        <span class="text-sm font-semibold text-gray-600">
          <AnimatedScore :value="item.score" prefix="₩ " />
        </span>
      </li>
      <li v-if="rankings.length === 0" key="empty" class="text-sm text-gray-400 text-center py-4">데이터 없음</li>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.rank-item {
  transition: all 0.5s ease;
}
.rank-move {
  transition: transform 0.5s ease;
}
.rank-enter-active {
  transition: all 0.3s ease;
}
.rank-leave-active {
  transition: all 0.3s ease;
  position: absolute;
}
.rank-enter-from {
  opacity: 0;
  transform: translateX(30px);
}
.rank-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
</style>
