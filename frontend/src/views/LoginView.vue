<template>
  <div class="min-h-screen bg-[#F5F5F5] flex items-center justify-center p-6">
    <div class="w-full max-w-4xl grid grid-cols-2 gap-10 items-center">

      <!-- Left: Branding -->
      <div class="space-y-7">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 bg-[#F37321] flex items-center justify-center text-white font-black text-base">H</div>
          <span class="text-2xl font-bold tracking-tight text-gray-900">HANWHA <span class="font-light">Nexus</span></span>
        </div>
        <div>
          <h1 class="text-4xl font-black text-gray-900 leading-tight tracking-tight">
            유통 관리<br/>통합 플랫폼
          </h1>
          <p class="mt-3 text-sm text-gray-500 leading-relaxed">
            본사·가맹점을 연결하는<br/>
            실시간 재고·발주·정산 자동화 시스템
          </p>
        </div>

        <!-- 데모 계정 -->
        <div class="space-y-2">
          <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">데모 계정</p>
          <div v-for="demo in demoAccounts" :key="demo.email"
            @click="fillDemo(demo)"
            class="flex items-center gap-3 p-3.5 bg-white border border-gray-200 cursor-pointer hover:border-[#F37321] transition-colors group">
            <div class="w-8 h-8 flex items-center justify-center text-xs font-bold text-white shrink-0"
              :class="demo.colorClass">{{ demo.avatar }}</div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-semibold text-gray-800">
                {{ demo.name }}
                <span class="ml-1.5 text-[11px] font-bold px-1.5 py-0.5 rounded"
                  :class="demo.badgeClass">{{ demo.roleLabel }}</span>
              </p>
              <p class="text-xs text-gray-400 mt-0.5 font-mono">{{ demo.email }} / password123</p>
            </div>
            <span class="text-xs text-gray-300 group-hover:text-[#F37321] transition-colors font-medium">입력 →</span>
          </div>
        </div>
      </div>

      <!-- Right: Login Form -->
      <div class="bg-white border border-gray-200 rounded-lg p-8">
        <div class="mb-6">
          <h2 class="text-lg font-bold text-gray-900">로그인</h2>
          
        </div>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <div class="space-y-1.5">
            <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">사용자 email</label>
            <input v-model="form.email" type="text" placeholder="ex) A001"
              class="w-full px-3 py-2.5 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none transition-all"
              :class="{ 'border-red-400': error }" />
          </div>

          <div class="space-y-1.5">
            <label class="text-xs font-bold text-gray-500 uppercase tracking-wider">비밀번호</label>
            <input v-model="form.password" type="password" placeholder="••••••••"
              class="w-full px-3 py-2.5 rounded border border-gray-200 text-sm focus:border-[#F37321] focus:ring-2 focus:ring-[#F37321]/10 outline-none transition-all"
              :class="{ 'border-red-400': error }" />
          </div>

          <div v-if="error"
            class="flex items-center gap-2 text-sm text-red-600 bg-red-50 px-4 py-3 rounded-md">
            <AlertCircle class="w-4 h-4 shrink-0" />
            {{ error }}
          </div>

          <button type="submit"
            class="w-full rounded bg-[#F37321] text-white py-3 font-bold hover:bg-[#e0661d] transition-colors mt-2 text-sm">
            로그인
          </button>
        </form>

        <div class="mt-6 pt-5 border-t border-gray-100 text-center text-xs text-gray-400">
          HANWHA Nexus SCM Platform v1.0
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { AlertCircle } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const form = ref({ email: '', password: '' })
const error = ref('')

const demoAccounts = [
  {
    email: 'admin@theventi.co.kr', name: '이재혁', avatar: 'HQ', roleLabel: '운영자 (본사)',
    colorClass: 'bg-[#F37321]',
    badgeClass: 'bg-orange-50 text-orange-600 border border-orange-200',
  },
  {
    email: 'store0001@theventi.co.kr', name: '김동현', avatar: 'KD', roleLabel: '점주 (가맹점)',
    colorClass: 'bg-blue-500',
    badgeClass: 'bg-blue-50 text-blue-600 border border-blue-200',
  },
]

function fillDemo(demo) {
  form.value.email = demo.email
  form.value.password = 'password123'
  error.value = ''
}

async function handleLogin() {
  error.value = ''
  const ok = await auth.login(form.value.email, form.value.password)
  if (!ok) {
    error.value = '아이디 또는 비밀번호가 올바르지 않습니다.'
    return
  }

  if (auth.isAdmin) router.push('/dashboard')
  else if (auth.isStoreOwner) router.push('/store-dashboard')
}
</script>
