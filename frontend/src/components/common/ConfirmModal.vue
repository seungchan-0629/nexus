<template>
  <div v-if="open" class="fixed inset-0 bg-gray-900/40 backdrop-blur-sm z-[60] flex items-center justify-center p-4" @click.self="$emit('close')">
    <div class="bg-white rounded-xl border border-gray-200 shadow-xl w-full max-w-sm text-center p-8">
      <div
        class="w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-5"
        :class="iconBgClass">
        <component :is="iconComponent" class="w-8 h-8" :class="iconColorClass" />
      </div>
      <h2 class="text-xl font-bold text-gray-900 mb-2">{{ title }}</h2>
      <p v-if="message" class="text-sm text-gray-500 mb-8 leading-relaxed whitespace-pre-line">{{ message }}</p>
      <div class="flex gap-3">
        <button
          type="button"
          class="flex-1 py-3 bg-white border border-gray-200 text-gray-600 rounded-xl text-sm font-bold hover:bg-gray-50 transition-colors cursor-pointer"
          @click="$emit('close')">
          {{ cancelText }}
        </button>
        <button
          type="button"
          class="flex-1 py-3 text-white rounded-xl text-sm font-bold shadow-sm transition-colors cursor-pointer"
          :class="confirmBtnClass"
          @click="$emit('confirm')">
          {{ confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { AlertCircle, AlertTriangle, Info } from 'lucide-vue-next'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, required: true },
  message: { type: String, default: '' },
  confirmText: { type: String, default: '확인' },
  cancelText: { type: String, default: '취소' },
  type: { type: String, default: 'info', validator: v => ['danger', 'warning', 'info'].includes(v) },
})

defineEmits(['close', 'confirm'])

const typeConfig = {
  danger:  { icon: AlertCircle,   iconBg: 'bg-red-50',    iconColor: 'text-red-500',    btn: 'bg-red-500 hover:bg-red-600' },
  warning: { icon: AlertTriangle, iconBg: 'bg-orange-50',  iconColor: 'text-orange-500', btn: 'bg-orange-500 hover:bg-orange-600' },
  info:    { icon: Info,          iconBg: 'bg-blue-50',    iconColor: 'text-blue-500',   btn: 'bg-blue-500 hover:bg-blue-600' },
}

const config = computed(() => typeConfig[props.type])
const iconComponent = computed(() => config.value.icon)
const iconBgClass = computed(() => config.value.iconBg)
const iconColorClass = computed(() => config.value.iconColor)
const confirmBtnClass = computed(() => config.value.btn)
</script>
