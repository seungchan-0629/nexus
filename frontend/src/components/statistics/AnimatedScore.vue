<script setup>
import { toRef } from 'vue'
import { useAnimatedNumber } from '@/composables/useAnimatedNumber'

const props = defineProps({
  value: { type: Number, required: true },
  suffix: { type: String, default: '' },
  prefix: { type: String, default: '' },
})

const source = toRef(props, 'value')
const display = useAnimatedNumber(source)

const formatted = (val) => val.toLocaleString('ko-KR')
</script>

<template>
  <span>{{ prefix }}{{ formatted(display) }}{{ suffix }}</span>
</template>
