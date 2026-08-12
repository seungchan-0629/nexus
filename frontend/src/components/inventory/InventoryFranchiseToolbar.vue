<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  storeSearch: {
    type: String,
    required: true,
  },
  selectedStoreIdx: {
    type: String,
    required: true,
  },
  visibleStores: {
    type: Array,
    required: true,
  },
  listLoading: {
    type: Boolean,
    required: true,
  },
})

const emit = defineEmits([
  'update:storeSearch',
  'update:selectedStoreIdx',
  'submit',
])

const isDropdownOpen = ref(false)

const shouldShowDropdown = computed(() => {
  return isDropdownOpen.value
})

const onSearchFocus = () => {
  isDropdownOpen.value = true
}

const onSearchBlur = () => {
  // Allow option click via mousedown before blur closes.
  setTimeout(() => {
    isDropdownOpen.value = false
  }, 120)
}

const onSearchInput = (event) => {
  emit('update:storeSearch', event.target.value)
  emit('update:selectedStoreIdx', '')
  isDropdownOpen.value = true
}

const selectStore = (store) => {
  emit('update:storeSearch', store.storeName)
  emit('update:selectedStoreIdx', String(store.idx))
  isDropdownOpen.value = false
}
</script>

<template>
  <div class="flex gap-3 items-center flex-wrap">
    <div class="relative min-w-[14rem] flex-1 max-w-xs">
      <input
        :value="props.storeSearch"
        type="search"
        placeholder="매장명 검색"
        autocomplete="off"
        class="w-full px-3 py-2 rounded-lg border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none bg-white"
        @focus="onSearchFocus"
        @blur="onSearchBlur"
        @input="onSearchInput"
      />
      <div
        v-if="shouldShowDropdown"
        class="absolute z-20 mt-1 w-full bg-white border border-gray-200 rounded-lg shadow-sm max-h-56 overflow-y-auto"
      >
        <button
          v-for="s in props.visibleStores"
          :key="s.idx"
          type="button"
          class="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 cursor-pointer"
          @mousedown.prevent="selectStore(s)"
        >
          {{ s.storeName }}
        </button>
        <p v-if="props.visibleStores.length === 0" class="px-3 py-2 text-sm text-gray-400">
          검색 결과가 없습니다.
        </p>
      </div>
    </div>

    <button
      type="button"
      class="px-3 py-2 rounded-lg border border-[#F37321] text-sm font-semibold text-[#F37321] hover:bg-[#F37321]/5 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
      :disabled="!props.selectedStoreIdx || props.listLoading"
      @click="emit('submit')"
    >
      {{ props.listLoading ? '조회 중...' : '재고 조회' }}
    </button>
  </div>
</template>
