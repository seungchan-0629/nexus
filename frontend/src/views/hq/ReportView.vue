<script setup>
import { reactive, ref, watch, onMounted, computed } from 'vue'
import { FileText, Download, ChevronRight, ChevronLeft } from 'lucide-vue-next'
import Toast from '@/components/common/Toast.vue'
import { useToast } from '@/composables/useToast'
import { useNews } from '@/composables/useNews'
import { getHqNews } from '@/api/news'
import NewsListItem from '@/components/news/NewsListItem.vue'
import NewsDetailModal from '@/components/news/NewsDetailModal.vue'
import { getReportList } from '@/api/report/index.js'

const { toast, showToast } = useToast()
const { selectedNews, detailContents, detailLoading, openDetail } = useNews()
const reports = ref([])  // 보고서 리스트
const newsSummaries = ref([])
const tabs = [
  { label: '보고서', value: 'report' },
  { label: '뉴스 요약', value: 'news' },
]
const activeTab = ref('report')
const newsLoading = ref(false)
const isReportLoading = ref(false)
// 보고서 페이징
const pagination = reactive({
  totalPage: 0,
  totalCount: 0,
  currentPage: 0,
  currentSize: 10
})

//  뉴스 조회
async function fetchHqNews() {
  newsLoading.value = true
  try {
    const today = new Date().toISOString().slice(0, 10)
    const { data } = await getHqNews(today)
    newsSummaries.value = data.result ?? []
  } catch (e) {
    console.error('[ReportView] 뉴스 조회 실패:', e)
  } finally {
    newsLoading.value = false
  }
}

// 보고서 조회
async function fetchReports(page = 0){
  isReportLoading.value = true;

  try {
    const res = await getReportList(page, pagination.currentSize);
    reports.value.splice(0, reports.value.length, ...res.data.result.reportList)

    pagination.totalPage = res.data.result.totalPage
    pagination.totalCount = res.data.result.totalCount
    pagination.currentPage = res.data.result.currentPage

  } catch (error) {
    showToast(`[ReportView] 보고서 조회 실패: ${error.message || error}`)
  } finally {
    isReportLoading.value = false;
  }
}

// 레포트 다운로드
function handleDownload(report) {
  const filePath = report;
  if (!filePath) return showToast('파일이 없습니다.', 'error');

  const s3BaseUrl = 'https://nexus-reports-archive.s3.ap-northeast-2.amazonaws.com/';
  window.open(s3BaseUrl + filePath, '_blank'); // 새 탭에서 열기
  showToast(`${report.reportTitle} 미리보기 준비 중입니다.`)
}

watch(activeTab, (val) => {
  if (val === 'news' && newsSummaries.value.length === 0) fetchHqNews()
  if (val === 'report' && reports.value.length === 0) fetchReports(0);
})

// 페이지 변경
const changePage = (page) => {
  // 0보다 작거나 마지막 페이지(totalPage - 1)보다 크면 무시[cite: 1]
  if (page < 0 || page >= pagination.totalPage) return
  fetchReports(page)
}

// 버튼 클릭
const visiblePages = computed(() => {
  const range = 10; // 한 번에 보여줄 페이지 개수
  const currentGroup = Math.floor(pagination.currentPage / range);
  const start = currentGroup * range;
  const end = Math.min(start + range, pagination.totalPage);
  const pages = [];
  for (let i = start; i < end; i++) {
    pages.push(i);
  }
  return pages;
});

onMounted(() => {
  fetchReports(0);
})

</script>

<template>
  <div class="p-6">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-xl font-bold text-gray-900">AI 보고서</h1>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex gap-1 mb-4 border-b border-gray-200">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        @click="activeTab = tab.value"
        class="px-4 py-2 text-sm font-medium transition-colors border-b-2 -mb-px cursor-pointer"
        :class="activeTab === tab.value
          ? 'border-[#F37321] text-[#F37321]'
          : 'border-transparent text-gray-500 hover:text-gray-700'"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- AI 보고서 탭 -->
    <div v-if="activeTab === 'report'" class="bg-white rounded-lg border border-gray-200 overflow-hidden">
      <table class="w-full text-sm">
        <thead>
        <tr class="border-b border-gray-100 bg-gray-50/70">
          <th class="text-left px-5 py-3 font-semibold text-gray-600 w-20">번호</th>
          <th class="text-left px-5 py-3 font-semibold text-gray-600">보고서 제목</th>
          <th class="text-left px-5 py-3 font-semibold text-gray-600 w-40">생성일시</th>
          <th class="text-center px-5 py-3 font-semibold text-gray-600 w-28">다운로드</th>
        </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
        <tr v-if="reports.length === 0">
          <td colspan="4" class="px-5 py-16 text-center text-gray-400 text-sm">
            생성된 보고서가 없습니다.
          </td>
        </tr>
        <tr
          v-for="report in reports"
          :key="report.idx"
          class="hover:bg-gray-50/50 transition-colors"
        >
          <td class="px-5 py-3.5 text-gray-400">{{ report.idx }}</td>
          <td class="px-5 py-3.5">
            <div class="flex items-center gap-2">
              <FileText class="w-4 h-4 text-[#F37321] shrink-0" />
              <span class="font-medium text-gray-800">{{ report.reportTitle }}</span>
            </div>
          </td>
          <td class="px-5 py-3.5 text-gray-500">{{ report.createdAt }}</td>
          <td class="px-5 py-3.5 text-center">
            <button
              @click="handleDownload(report.reportFilePath)"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-[#F37321] border border-[#F37321] rounded hover:bg-orange-50 transition-colors cursor-pointer"
            >
              <Download class="w-3.5 h-3.5" />
              PDF
            </button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- 뉴스 요약 탭 -->
    <div v-if="activeTab === 'news'" class="flex flex-col gap-3">
      <p class="text-xs text-gray-400">매일 오전 8시 AI가 더벤티 브랜드 및 업계 관련 뉴스를 수집·요약합니다.</p>

      <div v-if="newsLoading" class="bg-white rounded-lg border border-gray-200 px-5 py-16 text-center text-gray-400 text-sm">
        불러오는 중...
      </div>

      <div v-else-if="newsSummaries.length === 0" class="bg-white rounded-lg border border-gray-200 px-5 py-16 text-center text-gray-400 text-sm">
        수집된 뉴스가 없습니다.
      </div>

      <!-- 뉴스 목록 -->
      <NewsListItem
        v-for="news in newsSummaries"
        :key="news.idx"
        :news="news"
        variant="hq"
        @select="openDetail"
      />
    </div>

    <NewsDetailModal
      v-if="selectedNews"
      :news="selectedNews"
      :contents="detailContents"
      :loading="detailLoading"
      variant="hq"
      @close="selectedNews = null"
    />
    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </div>
  <div v-if="pagination.totalPage > 1" class="flex justify-center items-center gap-2 py-4 border-t border-gray-100">
    <button
      class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
      :disabled="pagination.currentPage === 0"
      @click="changePage(pagination.currentPage - 1)">
      <ChevronLeft class="w-4 h-4" />
    </button>

    <button v-for="pageIdx in visiblePages" :key="pageIdx"
            class="w-8 h-8 rounded text-sm font-semibold cursor-pointer transition-colors"
            :class="pagination.currentPage === pageIdx
            ? 'bg-[#F37321] text-white'
            : 'text-gray-500 hover:bg-gray-50'"
            @click="changePage(pageIdx)">
      {{ pageIdx + 1 }}
    </button>

    <button
      class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
      :disabled="pagination.currentPage >= pagination.totalPage - 1"
      @click="changePage(pagination.currentPage + 1)">
      <ChevronRight class="w-4 h-4" />
    </button>
  </div>
</template>

