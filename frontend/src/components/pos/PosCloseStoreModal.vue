<template>
  <div v-if="open" class="pos-modal-backdrop">
    <div class="pos-close-dialog">
      <div class="pos-close-icon-wrap">
        <AlertCircle class="w-8 h-8 text-red-500" />
      </div>
      <h2 class="pos-close-title">영업을 마감하시겠습니까?</h2>
      <p class="pos-close-body">
        마감 처리 후에는 새로운 결제를<br />진행할 수 없습니다.<br />
        <span class="pos-close-highlight">판매 데이터가 본사로 자동 전송됩니다.</span>
      </p>
      <div class="pos-close-actions">
        <button type="button" class="pos-close-btn-cancel" :disabled="loading" @click="$emit('close')">
          취소
        </button>
        <button type="button" class="pos-close-btn-confirm" :disabled="loading" @click="$emit('confirm')">
          {{ loading ? '마감 처리중...' : '마감 승인' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { AlertCircle } from 'lucide-vue-next'

defineProps({
  open: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
})

defineEmits(['close', 'confirm'])
</script>
