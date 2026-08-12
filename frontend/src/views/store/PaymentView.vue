<template>
  <div class="payment-container">
    <div class="header">
      <h1 class="title">발주 정산 카드 등록</h1>
      <p class="description">매달 정기 결제를 위한 결제 수단을 관리합니다.</p>
    </div>

    <!-- 1. 등록된 카드 리스트 -->
    <div class="section billing-list-section">
      <h2 class="section-title">등록된 결제 수단</h2>
      <div v-if="loading" class="center-content">
        <div class="spinner"></div>
      </div>
      <div v-else-if="billingList.length > 0" class="billing-list">
        <div v-for="billing in billingList" :key="billing.id" class="billing-card">
          <div class="card-info">
            <div class="card-method">
              <span class="icon">💳</span>
              <span class="method-name">{{ billing.cardCompany || billing.method }}</span>
            </div>
            <div class="card-details">
              <p v-if="billing.cardNumber"><strong>카드번호:</strong> {{ billing.cardNumber }}</p>
              <p><strong>등록일:</strong> {{ formatDate(billing.authenticatedAt) }}</p>
            </div>
          </div>
          <button class="btn btn-delete" @click="handleDelete(billing.storeIdx)">삭제</button>
        </div>
      </div>
      <div v-else class="empty-state">
        <p>등록된 결제 수단이 없습니다. 새로운 카드를 등록해주세요.</p>
      </div>
    </div>

    <!-- 2. 카드 등록 버튼 -->
    <div class="section action-section">
      <button v-if="status === 'READY'" class="btn btn-primary" @click="requestBilling('카드')">
        새로운 카드 등록 및 변경하기
      </button>

      <!-- 진행 상태 -->
      <div v-else-if="status === 'PENDING'" class="center-content">
        <div class="spinner"></div>
        <p>카드 정보를 등록하는 중입니다...</p>
      </div>

      <!-- 성공/실패 알림 -->
      <div v-else-if="status === 'SUCCESS'" class="alert alert-success">
        카드 등록이 완료되었습니다!
        <button class="btn-text" @click="status = 'READY'">확인</button>
      </div>

      <div v-else-if="status === 'FAIL'" class="alert alert-danger">
        <p><strong>등록 실패:</strong> {{ errorData.message }}</p>
        <button class="btn-text" @click="status = 'READY'">다시 시도</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import settlementApi from '@/api/settlement'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const CLIENT_KEY = import.meta.env.VITE_CLIENT_KEY
let tossPayments = null

const status = ref('READY')
const loading = ref(false)
const billingList = ref([])
const errorData = ref({ code: '', message: '' })

onMounted(async () => {
  loadTossSDK()
  await fetchBillingList()

  const urlParams = new URLSearchParams(window.location.search)
  const authKey = urlParams.get('authKey')
  const customerKey = urlParams.get('customerKey')
  const errorCode = urlParams.get('code')
  const errorMessage = urlParams.get('message')

  if (authKey && customerKey) {
    handleIssueBillingKey(authKey, customerKey)
  } else if (errorCode && errorMessage) {
    status.value = 'FAIL'
    errorData.value = { code: errorCode, message: errorMessage }
  }
})

const fetchBillingList = async () => {
  loading.value = true
  try {
    const res = await settlementApi.getBillingList()
    if (res.data.success) {
      billingList.value = res.data.result
    }
  } catch (error) {
    console.error('빌링 리스트 조회 실패:', error)
  } finally {
    loading.value = false
  }
}

const loadTossSDK = () => {
  if (window.TossPayments) {
    initToss()
  } else {
    const script = document.createElement('script')
    script.src = 'https://js.tosspayments.com/v1/payment'
    script.async = true
    script.onload = initToss
    document.head.appendChild(script)
  }
}

const initToss = () => {
  if (window.TossPayments) {
    tossPayments = window.TossPayments(CLIENT_KEY)
  }
}

const requestBilling = (method) => {
  if (!tossPayments) {
    alert('결제 모듈이 아직 준비되지 않았습니다.')
    return
  }

  const currentUrl = window.location.origin + window.location.pathname

  const storeIdx = authStore.user?.rawStoreIdx ||
                   (authStore.user?.storeId ? authStore.user.storeId.replace('S', '') : null)

  if (!storeIdx) {
    alert('가맹점 정보를 확인할 수 없습니다. 다시 로그인해주세요.')
    return
  }

  const requestData = {
    customerKey: 'STORE_' + storeIdx,
    successUrl: currentUrl,
    failUrl: currentUrl,
  }

  tossPayments.requestBillingAuth(method, requestData).catch((error) => {
    if (error.code !== 'USER_CANCEL') {
      status.value = 'FAIL'
      errorData.value = { code: error.code, message: error.message }
    }
  })
}

const handleIssueBillingKey = async (authKey, customerKey) => {
  status.value = 'PENDING'
  try {
    const res = await settlementApi.issueBillingKey({ authKey, customerKey })
    if (res.data.success) {
      status.value = 'SUCCESS'
      await fetchBillingList()
    } else {
      status.value = 'FAIL'
      errorData.value = { code: 'ERROR', message: res.data.message }
    }
  } catch (error) {
    status.value = 'FAIL'
    errorData.value = { code: 'NETWORK_ERROR', message: '잠시 후 다시 시도해주세요.' }
  } finally {
    window.history.replaceState({}, document.title, window.location.pathname)
  }
}

const handleDelete = async (storeIdx) => {
  if (!confirm('정말로 이 결제 수단을 삭제하시겠습니까?')) return

  try {
    const res = await settlementApi.deleteBillingInfo(storeIdx)
    if (res.data.success) {
      alert('삭제되었습니다.')
      await fetchBillingList()
    }
  } catch (error) {
    alert('삭제 실패: ' + error.message)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}
</script>

<style scoped>
.payment-container {
  padding: 40px 20px;
  max-width: 800px;
  margin: 0 auto;
}

.header {
  margin-bottom: 40px;
  text-align: center;
}

.title {
  font-size: 2rem;
  font-weight: 700;
  color: #111827;
}

.description {
  color: #6b7280;
  margin-top: 8px;
}

.section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 16px;
  color: #374151;
}

.billing-list {
  display: grid;
  gap: 16px;
}

.billing-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: border-color 0.2s;
}

.billing-card:hover {
  border-color: #3182f6;
}

.card-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.card-method {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 1.1rem;
}

.card-details p {
  margin: 2px 0;
  font-size: 0.875rem;
  color: #6b7280;
}

.btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: #3182f6;
  color: white;
  width: 100%;
}

.btn-delete {
  background: #fee2e2;
  color: #ef4444;
}

.btn-delete:hover {
  background: #fecaca;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
  border: 2px dashed #e5e7eb;
  border-radius: 8px;
}

.alert {
  padding: 16px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-success { background: #ecfdf5; color: #065f46; }
.alert-danger { background: #fef2f2; color: #991b1b; }

.btn-text {
  background: none;
  border: none;
  text-decoration: underline;
  cursor: pointer;
  font-weight: 600;
}

.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f4f6;
  border-top: 3px solid #3182f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 10px;
}

@keyframes spin { 100% { transform: rotate(360deg); } }
</style>
