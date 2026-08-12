<script setup>
import { ref, nextTick } from 'vue'
import { Bot, X, Send, RefreshCw } from 'lucide-vue-next'
import { postReportGenerate } from '@/api/report/index.js'
import { useRouter } from 'vue-router'

const router = useRouter()
const isOpen = ref(false)
const isLoading = ref(false)
const input = ref('')
const messageContainer = ref(null)
const textareaRef = ref(null)

function autoResize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = el.scrollHeight + 'px'
}

const messages = ref([
  {
    id: 1,
    role: 'ai',
    text: `반갑습니다!
    Nexus AI 어시스턴트입니다. 🤖

    매장의 실시간 데이터를 바탕으로
    똑똑한 분석을 도와드릴게요.

    궁금한 점은 채팅으로 편하게 물어보시고, 상세 분석이 필요하면 "보고서 만들어줘"라고 요청해 보세요!

    예) "이번 달 매출 요약 보고서 써줘"`,
  },
])

let nextId = 2

// 추천 질문(quick replies): 현재 AI가 확실히 답할 수 있는 데이터(매출/메뉴/매장) 위주로 구성
// 사용자가 "무엇을 물어볼 수 있는지" 바로 알도록 챗봇 시작 화면에 노출
const suggestions = [
  '이번 달 매출 알려줘',
  '지난달 인기 메뉴 TOP 5',
  '이번 달 매출 TOP 매장',
  '이번 달 매출 보고서 만들어줘',
]

// 세션ID 생성: crypto.randomUUID는 보안 컨텍스트(https/localhost)에서만 동작 → 평문 http 대비 fallback
function makeSessionId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) return crypto.randomUUID()
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

// 대화 세션 식별자: 컴포넌트 생성 시 1회 발급 → 챗봇을 열고 닫아도 유지됨
const sessionId = ref(makeSessionId())

// 최초 인사 메시지 스냅샷 (새 대화 시작 시 복원용)
const initialMessages = JSON.parse(JSON.stringify(messages.value))

// "새 대화" 시작: 세션ID 재발급 + 메시지/상태 초기화 (이전 대화 기억 끊김)
function startNewChat() {
  sessionId.value = makeSessionId()
  messages.value = JSON.parse(JSON.stringify(initialMessages))
  nextId = 2
}

// 스크롤 하단 이동 함수
async function scrollToBottom() {
  await nextTick()
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}

// 메세지 보내기 (추천 질문 클릭 시 preset 문자열을 직접 전달; 버튼/엔터 이벤트 객체는 무시)
async function sendMessage(preset) {
  const text = (typeof preset === 'string' ? preset : input.value).trim()
  if (!text || isLoading.value) return

  messages.value.push({ id: nextId++, role: 'user', text })
  input.value = ''
  await nextTick()
  if (textareaRef.value) textareaRef.value.style.height = 'auto'
  isLoading.value = true
  await scrollToBottom()

  try {
    const chatRequest  = { message: text, sessionId: sessionId.value }
    const res = await postReportGenerate(chatRequest)

    messages.value.push({
      id: nextId++,
      role: 'ai',
      text: res.data.result
    })

    if (res.data.result.includes("보고서 생성이 완료되었습니다")) {
      // 사용자에게 의사를 물어봅니다.
      const goToReportPage = confirm(
        "보고서 생성이 완료되었습니다!\n보고서 목록 페이지로 이동하여 확인하시겠습니까?"
      )

      if (goToReportPage) {
        // 현재 경로 확인
        if (window.location.pathname === '/report') {
          // 이미 보고서 페이지라면 새로고침해서 리스트 갱신
          location.reload()
        } else {
          // 다른 페이지라면 보고서 페이지로 이동
          router.push({ name: 'report' });
        }
      }

    }

  } catch (error) {
    messages.value.push({
      id: nextId++,
      role: 'ai',
      text: '보고서 생성 요청 중 오류가 발생했습니다. 다시 시도해주세요.'
    })
  } finally {
    isLoading.value = false
    await scrollToBottom()
  }
}

</script>

<template>
  <!-- Floating button -->
  <div class="fixed bottom-6 right-6 z-50 flex flex-col items-end gap-3">

    <!-- Chat panel -->
    <transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0 translate-y-4"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-4"
    >
      <div
        v-if="isOpen"
        class="w-[360px] h-[480px] bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col overflow-hidden"
      >
        <!-- Header -->
        <div class="flex items-center justify-between px-4 py-3 bg-[#F37321] shrink-0">
          <div class="flex items-center gap-2">
            <Bot class="w-5 h-5 text-white" />
            <span class="text-sm font-bold text-white">Nexus AI 어시스턴트</span>
          </div>
          <div class="flex items-center gap-2">
            <button @click="startNewChat" title="새 대화" class="text-white/80 hover:text-white transition-colors">
              <RefreshCw class="w-4 h-4" />
            </button>
            <button @click="isOpen = false" class="text-white/80 hover:text-white transition-colors">
              <X class="w-4 h-4" />
            </button>
          </div>
        </div>

        <!-- Messages -->
        <div ref="messageContainer" class="flex-1 overflow-y-auto px-4 py-3 flex flex-col gap-3">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="flex"
            :class="msg.role === 'user' ? 'justify-end' : 'justify-start'"
          >
            <!-- AI avatar -->
            <div v-if="msg.role === 'ai'" class="flex items-end gap-2">
              <div class="w-7 h-7 rounded-full bg-[#F37321] flex items-center justify-center shrink-0">
                <Bot class="w-4 h-4 text-white" />
              </div>
              <div class="max-w-[240px] px-3 py-2 rounded-2xl rounded-bl-sm bg-gray-100 text-sm text-gray-800 leading-relaxed whitespace-pre-line">
                {{ msg.text }}
              </div>
            </div>

            <!-- User message -->
            <div v-else class="max-w-[240px] px-3 py-2 rounded-2xl rounded-br-sm bg-[#F37321] text-sm text-white leading-relaxed">
              {{ msg.text }}
            </div>
          </div>

          <!-- 추천 질문(quick replies): 대화 시작 화면(인사말만 있을 때)에서만 노출 -->
          <div v-if="messages.length <= 1 && !isLoading" class="flex flex-wrap gap-2 pl-9">
            <button
              v-for="s in suggestions"
              :key="s"
              @click="sendMessage(s)"
              class="text-xs px-3 py-1.5 rounded-full border border-[#F37321]/40 text-[#F37321] bg-orange-50 hover:bg-orange-100 transition-colors cursor-pointer"
            >
              {{ s }}
            </button>
          </div>

          <!-- Loading indicator -->
          <div v-if="isLoading" class="flex items-end gap-2">
            <div class="w-7 h-7 rounded-full bg-[#F37321] flex items-center justify-center shrink-0">
              <Bot class="w-4 h-4 text-white" />
            </div>
            <div class="px-3 py-2 rounded-2xl rounded-bl-sm bg-gray-100">
              <div class="flex gap-1 items-center h-4">
                <span class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce [animation-delay:0ms]"></span>
                <span class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce [animation-delay:150ms]"></span>
                <span class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce [animation-delay:300ms]"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="px-3 py-3 border-t border-gray-100 shrink-0">
          <div class="flex items-end gap-2">
            <textarea
              ref="textareaRef"
              v-model="input"
              @keydown.enter.exact.prevent="sendMessage"
              @input="autoResize"
              placeholder="'보고서 만들어줘'라고 요청해 보세요!"
              rows="1"
              class="flex-1 resize-none text-sm px-3 py-2 rounded-xl border border-gray-200 focus:outline-none focus:border-[#F37321] transition-colors leading-relaxed max-h-28 overflow-y-auto"
              :disabled="isLoading"
            />
            <button
              @click="sendMessage"
              :disabled="!input.trim() || isLoading"
              class="w-9 h-9 rounded-xl flex items-center justify-center transition-colors shrink-0"
              :class="input.trim() && !isLoading ? 'bg-[#F37321] hover:bg-orange-500 text-white' : 'bg-gray-100 text-gray-300'"
            >
              <Send class="w-4 h-4" />
            </button>
          </div>
          <p class="text-[11px] text-gray-400 mt-1.5 px-1">Enter로 전송 · Shift+Enter로 줄바꿈</p>
        </div>
      </div>
    </transition>

    <!-- Floating button -->
    <button
      @click="isOpen = !isOpen"
      class="w-14 h-14 rounded-full bg-[#F37321] hover:bg-orange-500 text-white shadow-lg flex items-center justify-center transition-all hover:scale-105"
    >
      <Bot v-if="!isOpen" class="w-6 h-6" />
      <X v-else class="w-6 h-6" />
    </button>
  </div>
</template>


