import { ref } from 'vue'
import { getNewsDetail } from '@/api/news'

const categoryMap = {
  LOCAL_EVENT: { label: '지역 행사', class: 'text-purple-600 bg-purple-50 border-purple-200' },
  TRAFFIC:     { label: '교통',     class: 'text-blue-600 bg-blue-50 border-blue-200' },
  WEATHER:     { label: '날씨',     class: 'text-sky-600 bg-sky-50 border-sky-200' },
  RISK:        { label: '위험',     class: 'text-red-600 bg-red-50 border-red-200' },
}

export function categoryStyle(category) {
  return categoryMap[category] ?? { label: category, class: 'text-gray-600 bg-gray-50 border-gray-200' }
}

export function formatDate(dateTime) {
  if (!dateTime) return ''
  return dateTime.slice(0, 10)
}

export function useNews() {
  const selectedNews = ref(null)
  const detailContents = ref('')
  const detailLoading = ref(false)

  async function openDetail(news) {
    selectedNews.value = news
    detailContents.value = ''
    detailLoading.value = true
    try {
      const { data } = await getNewsDetail(news.idx)
      detailContents.value = data.result?.summaryContents ?? ''
    } catch {
      detailContents.value = news.summaryContentsPreview ?? ''
    } finally {
      detailLoading.value = false
    }
  }

  return { selectedNews, detailContents, detailLoading, openDetail }
}
