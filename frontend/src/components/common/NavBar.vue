<script setup>
import { ref, computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { ChevronDown, User, LogOut } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import NotificationBell from '@/components/common/NotificationBell.vue'

const router = useRouter()
const auth   = useAuthStore()

const isUserMenuOpen = ref(false)

const roleLabelShort = computed(() => {
  if (auth.isAdmin)      return '본사 관리자'
  if (auth.isStoreOwner) return auth.user?.storeName
  return ''
})

const avatarColorClass = computed(() => {
  if (auth.isAdmin)      return 'text-[#F37321]'
  if (auth.isStoreOwner) return 'text-blue-500'
  return 'text-gray-500'
})

function handleLogout() {
  auth.logout()
  isUserMenuOpen.value = false
  router.push('/login')
}
</script>

<template>
  <header class="h-14 bg-white border-b border-gray-200 flex items-center justify-end px-7 shrink-0 z-30">
    <div class="flex items-center gap-2">
      <!-- 알림 벨 -->
      <NotificationBell />

      <div class="relative">
        <div v-if="isUserMenuOpen" @click="isUserMenuOpen = false" class="fixed inset-0 z-40"></div>

        <button @click="isUserMenuOpen = !isUserMenuOpen"
          class="flex items-center gap-2.5 hover:bg-gray-50 px-3 py-2 rounded transition-colors relative z-50">
          <div class="w-8 h-8 rounded flex items-center justify-center text-xs font-bold border border-gray-200 bg-gray-50"
            :class="avatarColorClass">
            {{ auth.user?.avatar }}
          </div>
          <div class="flex flex-col text-left">
            <span class="text-sm font-bold text-gray-900 leading-tight">{{ auth.user?.name }}</span>
            <span class="text-[11px] text-gray-400 leading-tight">{{ roleLabelShort }}</span>
          </div>
          <ChevronDown class="w-3.5 h-3.5 text-gray-400 transition-transform duration-200"
            :class="{ 'rotate-180': isUserMenuOpen }" />
        </button>

        <transition enter-active-class="transition ease-out duration-100" enter-from-class="opacity-0 translate-y-1"
          enter-to-class="opacity-100 translate-y-0" leave-active-class="transition ease-in duration-75"
          leave-from-class="opacity-100 translate-y-0" leave-to-class="opacity-0 translate-y-1">
          <div v-if="isUserMenuOpen"
            class="absolute right-0 mt-1 w-44 bg-white border border-gray-200 rounded shadow-md z-50 py-1 overflow-hidden">
            <RouterLink to="/profile" @click="isUserMenuOpen = false"
              class="flex items-center gap-2.5 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition-colors">
              <User class="w-4 h-4 text-gray-400" />
              내 정보 관리
            </RouterLink>
            <div class="h-px bg-gray-100 mx-1"></div>
            <button @click="handleLogout"
              class="w-full flex items-center gap-2.5 px-4 py-2.5 text-sm text-red-500 hover:bg-red-50 transition-colors text-left">
              <LogOut class="w-4 h-4" />
              로그아웃
            </button>
          </div>
        </transition>
      </div>
    </div>
  </header>
</template>
