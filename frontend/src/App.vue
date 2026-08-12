<script setup>
import { onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import SideBar from '@/components/common/SideBar.vue'
import NavBar from '@/components/common/NavBar.vue'
import AiChatbot from '@/components/AiChatbot.vue'

const route = useRoute()
const auth = useAuthStore()

onMounted(async () => {
  if (auth.isLoggedIn) {
    await auth.fetchUserInfo()
  }
})
</script>

<template>
  <!-- 로그인 페이지: 사이드바 없이 전체 화면 -->
  <RouterView v-if="route.name === 'login'" />

  <!-- 메인 레이아웃 -->
  <div v-else class="flex h-screen bg-[#F5F5F5] text-gray-800 overflow-hidden">

    <!-- Sidebar -->
    <SideBar />

    <!-- Main content -->
    <main class="flex-1 flex flex-col overflow-hidden">

      <!-- Header -->
      <NavBar />

      <div :class="route.name === 'storePos' ? 'flex-1 overflow-hidden' : 'flex-1 overflow-auto bg-[#F5F5F5]'">
        <RouterView />
      </div>
    </main>

    <!-- AI 챗봇 (관리자 전용) -->
    <AiChatbot v-if="auth.isAdmin" />
  </div>
</template>
