import { ref, watch, onUnmounted } from 'vue'

export function useAnimatedNumber(source, duration = 600) {
  const display = ref(0)
  let animFrame = null

  watch(source, (to, from) => {
    from = from ?? 0
    if (animFrame) cancelAnimationFrame(animFrame)
    const start = from || 0
    const diff = to - start
    if (diff === 0) return
    const startTime = performance.now()

    const step = (now) => {
      const elapsed = now - startTime
      const progress = Math.min(elapsed / duration, 1)
      const ease = 1 - Math.pow(1 - progress, 3)
      display.value = Math.round(start + diff * ease)
      if (progress < 1) {
        animFrame = requestAnimationFrame(step)
      }
    }
    animFrame = requestAnimationFrame(step)
  }, { immediate: true })

  onUnmounted(() => {
    if (animFrame) cancelAnimationFrame(animFrame)
  })

  return display
}
