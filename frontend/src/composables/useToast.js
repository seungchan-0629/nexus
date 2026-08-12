import { ref } from 'vue'

export function useToast(duration = 3000) {
  const toast = ref({ show: false, message: '', type: 'success' })

  function showToast(message, type = 'success') {
    toast.value = { show: true, message, type }
    setTimeout(() => { toast.value.show = false }, duration)
  }

  return { toast, showToast }
}
