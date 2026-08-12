<template>
  <transition
    enter-active-class="transition ease-out duration-200"
    enter-from-class="opacity-0 translate-y-2"
    enter-to-class="opacity-100 translate-y-0"
    leave-active-class="transition ease-in duration-150"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0">
    <div
      v-if="show"
      class="fixed bottom-8 right-8 bg-gray-800 text-white px-5 py-3 rounded-xl shadow-xl flex items-center gap-3 z-50">
      <CheckCircle class="w-5 h-5 text-green-400 shrink-0" />
      <span class="text-sm font-medium">{{ message }}</span>
    </div>
  </transition>
</template>

<script setup>
import { CheckCircle } from 'lucide-vue-next'

defineProps({
  show: { type: Boolean, default: false },
  message: { type: String, default: '' },
})
</script>
