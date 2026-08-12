<template>
  <div class="flex-1 flex flex-col overflow-hidden">
    <div class="bg-[#F8F9FA] px-4 pt-4 pb-3 border-b border-gray-200 shrink-0 space-y-3">
      <div class="flex items-center bg-white rounded-full px-4 py-2 border border-gray-200 focus-within:border-blue-300 focus-within:ring-2 focus-within:ring-blue-100 transition-all max-w-sm">
        <Search class="w-4 h-4 text-gray-400 mr-2 shrink-0" />
        <input
          type="text"
          :value="searchQuery"
          @input="$emit('update:searchQuery', $event.target.value)"
          placeholder="메뉴명 검색..."
          class="bg-transparent border-none outline-none text-sm w-full text-gray-700" />
        <button v-if="searchQuery" type="button" @click="$emit('update:searchQuery', '')" class="text-gray-400 hover:text-gray-600 ml-1 cursor-pointer">
          <XCircle class="w-4 h-4" />
        </button>
      </div>
      <div class="flex gap-2 overflow-x-auto pb-1">
        <button
          v-for="cat in categories"
          :key="cat"
          type="button"
          @click="$emit('update:selectedCategory', cat)"
          :class="['px-5 py-2 rounded-full text-sm font-semibold whitespace-nowrap transition-all border shrink-0 cursor-pointer',
            selectedCategory === cat
              ? 'bg-gray-800 text-white border-gray-800 shadow-sm'
              : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50']">
          {{ cat }}
        </button>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto p-5">
      <div v-if="loadingMenus" class="h-full flex flex-col items-center justify-center text-gray-400">
        <p class="text-sm font-medium">메뉴를 불러오는 중…</p>
      </div>
      <div v-else-if="menus.length === 0" class="h-full flex flex-col items-center justify-center text-gray-400">
        <Search class="w-10 h-10 mb-3 opacity-30" />
        <p class="text-sm font-medium">검색된 메뉴가 없습니다.</p>
      </div>
      <div v-else class="grid grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 2xl:grid-cols-6 gap-4">
        <div
          v-for="menu in menus"
          :key="menu.idx"
          role="button"
          tabindex="0"
          @keydown.enter.prevent="$emit('add-to-cart', menu)"
          @keydown.space.prevent="$emit('add-to-cart', menu)"
          @click="$emit('add-to-cart', menu)"
          class="bg-white p-3 border border-gray-200 cursor-pointer flex flex-col h-47.5
                 hover:border-blue-400 transition-all active:scale-[0.97] group relative">
          <div class="w-full h-28 rounded-lg overflow-hidden mb-3 bg-gray-50 border border-gray-50 relative">
            <img
              :src="resolveMenuImage(menu.imgPath)"
              :alt="menu.menuName"
              @error="onImgError"
              class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
              loading="lazy" />
            <div class="absolute inset-0 bg-black/5 group-hover:bg-transparent transition-colors"></div>
          </div>
          <div class="flex-1 flex flex-col justify-between">
            <h3 class="font-medium text-gray-800 text-sm leading-tight line-clamp-1">{{ menu.menuName }}</h3>
            <span class="text-gray-900 font-bold text-sm mt-auto pt-1">{{ formatPrice(menu.price) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Search, XCircle } from 'lucide-vue-next'
import defaultMenuImage from '@/assets/store-pos-default-menu.png'
import { formatPrice } from '@/utils/formatPrice.js'

defineProps({
  loadingMenus: { type: Boolean, default: false },
  menus: { type: Array, default: () => [] },
  categories: { type: Array, default: () => ['전체'] },
  searchQuery: { type: String, default: '' },
  selectedCategory: { type: String, default: '전체' },
})

defineEmits(['update:searchQuery', 'update:selectedCategory', 'add-to-cart'])

function resolveMenuImage(imgPath) {
  if (!imgPath) return defaultMenuImage
  if (imgPath.startsWith('http://') || imgPath.startsWith('https://')) return imgPath
  const base = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '')
  const path = imgPath.startsWith('/') ? imgPath : `/${imgPath}`
  return base ? `${base}${path}` : path
}

function onImgError(event) {
  if (event.target.dataset.fallbackApplied === 'true') return
  event.target.dataset.fallbackApplied = 'true'
  event.target.src = defaultMenuImage
}
</script>
