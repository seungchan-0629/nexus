<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-bold text-gray-900">지역 뉴스</h1>
    </div>

    <p class="text-xs text-gray-400 mb-3">매일 오전 8시 AI가 매장 지역 관련 정보를 수집·요약합니다.</p>

    <div v-if="isLoading" class="bg-white rounded-lg border border-gray-200 px-5 py-16 text-center text-gray-400 text-sm">
      불러오는 중...
    </div>

    <div v-else-if="newsList.length === 0" class="bg-white rounded-lg border border-gray-200 px-5 py-16 text-center text-gray-400 text-sm">
      수집된 뉴스가 없습니다.
    </div>

    <div v-else class="flex flex-col gap-3">
      <NewsListItem
        v-for="news in newsList"
        :key="news.idx"
        :news="news"
        variant="store"
        @select="openDetail"
      />
    </div>

    <NewsDetailModal
      v-if="selectedNews"
      :news="selectedNews"
      :contents="detailContents"
      :loading="detailLoading"
      variant="store"
      @close="selectedNews = null"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyStoreNews } from '@/api/news'
import { useNews } from '@/composables/useNews'
import NewsListItem from '@/components/news/NewsListItem.vue'
import NewsDetailModal from '@/components/news/NewsDetailModal.vue'

const newsList = ref([])
const isLoading = ref(false)
const { selectedNews, detailContents, detailLoading, openDetail } = useNews()

async function fetchNews() {
  isLoading.value = true
  try {
    const today = new Date().toISOString().slice(0, 10)
    const { data } = await getMyStoreNews(today)
    newsList.value = data.result ?? []
  } catch (e) {
    console.error('[StoreNewsView] 뉴스 조회 실패:', e)
  } finally {
    isLoading.value = false
  }
}

onMounted(fetchNews)
</script>
