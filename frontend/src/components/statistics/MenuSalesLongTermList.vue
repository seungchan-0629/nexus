<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { getMenuSalesLongTerm } from '@/api/statistics'

const currentYear = new Date().getFullYear()

const selectedYear = ref(currentYear)
const selectedMonth = ref(null)
const searchKeyword = ref('')

const allMenus = ref([])

const yearOptions = [2025, 2026]
const monthOptions = Array.from({ length: 12 }, (_, i) => i + 1)

const fetchData = async () => {
  try {
    const { data } = await getMenuSalesLongTerm(selectedYear.value, selectedMonth.value)
    allMenus.value = data.result.menus
  } catch (e) {
    console.error('메뉴별 판매량 조회 실패', e)
    allMenus.value = []
  }
}

const filteredMenus = computed(() => {
  if (!searchKeyword.value.trim()) return allMenus.value
  const kw = searchKeyword.value.toLowerCase()
  return allMenus.value.filter((m) => m.menuName.toLowerCase().includes(kw))
})

const periodLabel = computed(() => {
  return selectedMonth.value
    ? `${selectedYear.value}년 ${selectedMonth.value}월`
    : `${selectedYear.value}년 전체`
})

const getOriginalRank = (menu) => {
  return allMenus.value.findIndex((m) => m.menuIdx === menu.menuIdx) + 1
}

onMounted(fetchData)
watch([selectedYear, selectedMonth], fetchData)
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 flex flex-col">
    <!-- 헤더 -->
    <div class="flex items-start justify-between mb-3 shrink-0">
      <div>
        <h2 class="font-bold text-gray-900">메뉴 판매량 순위</h2>
        <p class="text-xs text-gray-400 mt-0.5">
          {{ periodLabel }} ·
          <span v-if="searchKeyword">{{ filteredMenus.length }} / {{ allMenus.length }} 메뉴</span>
          <span v-else>총 {{ allMenus.length }} 메뉴</span>
        </p>
      </div>
      <div class="flex gap-2">
        <select v-model.number="selectedYear"
          class="border border-gray-200 rounded-md px-3 py-1.5 text-sm bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-orange-300">
          <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
        </select>
        <select v-model="selectedMonth"
          class="border border-gray-200 rounded-md px-3 py-1.5 text-sm bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-orange-300">
          <option :value="null">연 전체</option>
          <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
        </select>
      </div>
    </div>

    <!-- 검색 -->
    <div class="mb-3 shrink-0">
      <input v-model="searchKeyword" type="text" placeholder="메뉴명 검색..."
        class="w-full border border-gray-200 rounded-md px-3 py-1.5 text-sm bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-orange-300" />
    </div>

    <!-- 리스트 (스크롤) -->
    <ul class="space-y-2 overflow-auto pr-1" style="height: 432px;">
      <li v-for="item in filteredMenus" :key="item.menuIdx"
        class="flex items-center justify-between py-1.5 px-2 rounded hover:bg-gray-50">
        <div class="flex items-center gap-3">
          <span class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold"
            :class="getOriginalRank(item) <= 3 ? 'bg-orange-100 text-orange-600' : 'bg-gray-100 text-gray-500'">
            {{ getOriginalRank(item) }}
          </span>
          <span class="text-sm font-medium text-gray-800">{{ item.menuName }}</span>
        </div>
        <span class="text-sm font-semibold text-gray-700">
          {{ item.totalQuantity.toLocaleString('ko-KR') }} 개
        </span>
      </li>
      <li v-if="filteredMenus.length === 0" class="text-sm text-gray-400 text-center py-4">
        {{ searchKeyword ? '검색 결과 없음' : '데이터 없음' }}
      </li>
    </ul>
  </div>
</template>
