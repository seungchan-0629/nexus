<template>
  <div class="p-5 space-y-4">
    <div class="flex justify-between items-center">
      <h1 class="text-xl font-bold text-gray-900 tracking-tight">매장 제품 관리</h1>
    </div>

    <div class="flex gap-3 items-center flex-wrap mb-4">
      <div class="relative">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="제품명 검색..."
          @input="handleSearch"
          class="pl-10 pr-4 py-2.5 rounded-lg border border-gray-200 text-sm w-64 bg-white shadow-sm
                 focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none block transition-colors cursor-pointer"
        />
      </div>
      <div class="flex gap-1.5 flex-wrap">
        <button
          @click="changeCategory('전체')"
          class="px-4 py-2 text-sm font-semibold border rounded-lg transition-colors shadow-sm cursor-pointer"
          :class="selectedCategory === '전체'
            ? 'bg-blue-500 text-white border-blue-500'
            : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'"
        >
          전체
        </button>
        <button
          v-for="cat in categories"
          :key="cat.idx"
          @click="changeCategory(cat.categoryName)"
          class="px-4 py-2 text-sm font-semibold border rounded-lg transition-colors shadow-sm cursor-pointer"
          :class="selectedCategory === cat.categoryName
            ? 'bg-blue-500 text-white border-blue-500'
            : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'"
        >
          {{ cat.categoryName }}
        </button>
      </div>
    </div>

    <div class="bg-white border border-gray-200 rounded-lg overflow-hidden pb-4">
      <table class="w-full text-sm text-left">
        <thead>
        <tr class="border-b border-gray-200 bg-gray-50">
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">제품명</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">카테고리</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">단위</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">단가</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">위험 유통기한</th>
        </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
        <tr v-for="p in filteredProducts" :key="p.idx" class="hover:bg-gray-50/50 transition-colors">
          <td class="px-5 py-3.5 font-semibold text-gray-900">{{ p.productName }}</td>
          <td class="px-5 py-3.5">
              <span class="text-xs font-semibold px-2 py-0.5 bg-gray-100 text-gray-600 border border-gray-200 rounded">
                {{ p.categoryName }}
              </span>
          </td>
          <td class="px-5 py-3.5 text-gray-600">{{ p.productUnit }}</td>
          <td class="px-5 py-3.5 font-medium text-gray-900">₩ {{ p.unitPrice?.toLocaleString() }}</td>
          <td class="px-5 py-3.5 text-xs font-mono" :class="p.dangerDays ? 'text-blue-600 font-semibold' : 'text-gray-400'">
            {{ p.dangerDays ? `D-${p.dangerDays}` : '-' }}
          </td>
        </tr>
        <tr v-if="filteredProducts.length === 0">
          <td colspan="5" class="px-5 py-12 text-center text-gray-400 text-sm">
            데이터가 없거나 검색 결과가 없습니다.
          </td>
        </tr>
        </tbody>
      </table>

      <div v-if="totalPages > 1 && !searchQuery" class="flex justify-center items-center gap-2 pt-6">
        <button
          class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
          :disabled="currentPage === 0"
          @click="goToPage(currentPage - 1)"
        >
          <ChevronLeft class="w-4 h-4" />
        </button>
        <button
          v-for="page in totalPages"
          :key="page"
          class="w-8 h-8 rounded text-sm font-semibold cursor-pointer"
          :class="currentPage === page - 1 ? 'bg-blue-500 text-white' : 'text-gray-500 hover:bg-gray-50'"
          @click="goToPage(page - 1)"
        >
          {{ page }}
        </button>
        <button
          class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
          :disabled="currentPage === totalPages - 1"
          @click="goToPage(currentPage + 1)"
        >
          <ChevronRight class="w-4 h-4" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
// 스크립트 로직은 이전과 동일하므로 유지됩니다.
import { ref, computed, onMounted } from 'vue'
import { Search, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { getStoreProductList, searchStoreProduct } from '@/api/product'
import { readCategoryList } from '@/api/category'

const categories = ref([])
const products = ref({ productList: [] })
const selectedCategory = ref('전체')
const searchQuery = ref('')
const currentPage = ref(0)
const pageSize = ref(10)
const totalPages = ref(1)

const fetchCategories = async () => {
  try {
    const response = await readCategoryList()
    categories.value = response.data.result || []
  } catch (error) {
    console.error('카테고리 로드 실패:', error)
  }
}

const fetchStoreProducts = async () => {
  try {
    const response = await getStoreProductList(currentPage.value, pageSize.value)
    const result = response.data.result
    if (result && result.content) {
      products.value = { productList: result.content }
      totalPages.value = result.totalPages
    } else {
      products.value = { productList: result || [] }
      totalPages.value = 1
    }
  } catch (error) {
    console.error('매장 제품 목록 로드 실패:', error)
  }
}

onMounted(() => {
  fetchCategories()
  fetchStoreProducts()
})

const goToPage = (page) => {
  if (page < 0 || page >= totalPages.value) return
  currentPage.value = page
  fetchStoreProducts()
}

const changeCategory = (name) => {
  selectedCategory.value = name
}

const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    currentPage.value = 0
    await fetchStoreProducts()
    return
  }
  try {
    const response = await searchStoreProduct(searchQuery.value)
    const searchResult = response.data.result || []
    products.value = { productList: searchResult }
    totalPages.value = 1
    currentPage.value = 0
  } catch (error) {
    console.error('매장 제품 검색 실패:', error)
  }
}

const filteredProducts = computed(() => {
  const list = products.value.productList || []
  if (selectedCategory.value === '전체') return list
  return list.filter(p => p.categoryName === selectedCategory.value)
})
</script>
