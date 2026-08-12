<script setup>
import {ref, reactive, onMounted, computed, nextTick, watch} from 'vue'
import {Plus, Search, FileText, ChevronRight, ChevronLeft, RefreshCw} from 'lucide-vue-next'
import { getStoreList , getStoreDetailList, getPresignedUrl, postNewRegister, putStoreUpdate, getStoreTotal} from '@/api/store/index.js'
import axios from 'axios'
import Toast from '@/components/common/Toast.vue'
import StoreTrendChart from '@/components/store/StoreTrendChart.vue'
import { useToast } from '@/composables/useToast'

const { toast, showToast } = useToast()
const searchQuery = ref('')
const filterStatus = ref('전체')
const statusOptions = ['전체', '입점', '폐점']
const storesList = ref([])
const storeTotalCount = ref()
const pagination = reactive({
  totalPage: 0,
  totalCount: 0,
  currentPage: 0,
  currentSize: 10
})
const showModal = ref(false)
const showDetailModal = ref(false)
const editTarget = ref(null)
const detailTarget = ref(null)
const selectedFile = ref(null);
// 입점/폐점 추이 차트 컴포넌트 참조 (저장 후 차트 갱신용)
const trendChart = ref(null)
// 초기화 하는 함수
const getInitialForm = () => ({
  storeName: '',
  ownerName: '',
  ownerEmail: '',
  postcode: '',
  address: '',
  addressDetail: '',
  totalAddress:'',
  business: '',
  createdAt: '',
  closedAt: '',
  filePath: '',
  fileName:''
});

const form = reactive(getInitialForm())

// 리스트 조회
const storeListRes = async (page = 0)=>{
  const searchReq = {
    keyword: searchQuery.value,
    status: filterStatus.value === '입점' ? 'ACTIVE' : filterStatus.value === '폐점' ? 'CLOSED' : null
  }

  // 토탈 수 조회
  const totalRes = await getStoreTotal()
  storeTotalCount.value = totalRes.data.result
  // 가맹점 리스트 조회

  const res = await getStoreList(searchReq, page, pagination.currentSize)
  storesList.value.splice(0, storesList.value.length, ...res.data.result.storeList)

  pagination.totalPage = res.data.result.totalPage
  pagination.totalCount = res.data.result.totalCount
  pagination.currentPage = res.data.result.currentPage

}

// 페이지 변경
const changePage = (page) => {
  // 0보다 작거나 마지막 페이지(totalPage - 1)보다 크면 무시
  if (page < 0 || page >= pagination.totalPage) return
  storeListRes(page)
}

// searchQuery가 변할 때마다 자동으로 getMenuRes 실행
watch(searchQuery, () => {
  storeListRes(0)
})


// 사업자 번호 포맷팅
const formatBusinessNumber = (e) => {
  const inputValue = e.target.value;

  // 한글이나 영문이 감지되거나, 조합 중일 때
  if (e.isComposing || /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣a-zA-Z]/.test(inputValue)) {
    showToast("숫자만 입력 가능합니다.", "error");

    // 1. 인풋 아예 끄기 (초점 강제 해제 -> 한글 조합 상태 파괴)
    e.target.blur();

    // 2. 아주 찰나의 시간(10ms) 뒤에 입력된 글자를 지우고 다시 켜기
    setTimeout(() => {
      e.target.value = form.business; // 기존에 입력해둔 안전한 숫자로 되돌림
      e.target.focus();               // 다시 인풋 켜기 (초점 주기)
    }, 10);

    return; // 아래 숫자 포맷팅 로직은 타지 않고 여기서 종료
  }

  // 정상적으로 숫자만 들어왔을 때의 하이픈(-) 포맷팅 로직
  let val = inputValue.replace(/[^0-9]/g, '');
  if (val.length > 10) val = val.substring(0, 10);

  let formatted = '';
  if (val.length <= 3) {
    formatted = val;
  } else if (val.length <= 5) {
    formatted = `${val.slice(0, 3)}-${val.slice(3)}`;
  } else {
    formatted = `${val.slice(0, 3)}-${val.slice(3, 5)}-${val.slice(5, 10)}`;
  }

  form.business = formatted;
  e.target.value = formatted;
};

// 가맹점 목록 클릭시 상세 모달창
async function openDetail(idx) {
  const res = await getStoreDetailList(idx)

  detailTarget.value = res.data.result
  showDetailModal.value = true
}

// 수정 등록 팝업 창 열기
async function openModal(idx =! null) {
  emailError.value = '';
  businessError.value = '';
  if (idx) {
    // [수정 모드]
    const res = await getStoreDetailList(idx);
    const detailData = res.data.result;
    Object.assign(form, getInitialForm());
    Object.assign(form, detailData);
    if (detailData.filePath) {
      // S3 경로나 파일 경로에서 마지막 '/' 뒤의 이름만 가져옵니다.
      form.fileName = detailData.filePath.split('/').pop();
    }
    editTarget.value = detailData;
  } else {
    // [등록 모드]
    Object.assign(form, getInitialForm());
    editTarget.value = null;
  }
  showModal.value = true;
}

// 선택 파일 감지
async function handleFileChange(e) {
  const file = e.target.files[0]
  if (!file) return;
  const allowedTypes = ['application/pdf'];
  if (!allowedTypes.includes(file.type)) {
    showToast('사업자 등록증은 PDF 형식의 파일만 업로드할 수 있습니다.', 'error');
    e.target.value = ''; // input 태그 초기화
    return;
  }
  selectedFile.value = file;
  form.fileName = file.name;
}

// S3 업로드 로직만 담당하는 공통 함수
async function uploadFileToS3() {
  // 선택된 파일이 없으면 업로드 과정을 건너뛰고 null 반환
  if (!selectedFile.value) return null;

  try {
    // 백엔드에 Presigned URL 요청
    const presigned = await getPresignedUrl(selectedFile.value.name,selectedFile.value.size );
    const { url, fileName: s3Path } = presigned.data.result;

    // S3에 실제 파일 업로드 (PUT 방식)
    await axios.put(url, selectedFile.value, {
      headers: { 'Content-Type': 'application/pdf' }
    });

    return s3Path; // 업로드된 S3 파일 경로(DB 저장용) 반환
  } catch {
    showToast("파일 업로드 중 오류가 발생했습니다. 다시 시도해주세요.", 'error')
  }
}

// 이메일 유효성 검사
const emailError = ref('');
const validateEmail = () => {
  if (!form.ownerEmail) {
    emailError.value = '';
    return true;
  }
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const isValid = emailPattern.test(form.ownerEmail);

  if (!isValid) {
    emailError.value = '올바른 이메일 형식으로 작성해주세요.';
    // 사용자가 이해할 수 있도록 주석 추가: 이메일 형식이 틀렸을 때 하단 빨간 글씨와 함께 우측 상단 토스트 알림을 띄움
    showToast('올바른 이메일 형식으로 작성해주세요.', 'error');
  } else {
    emailError.value = '';
  }
  return isValid;
};

// 사업자 등록번호 유효성 검사 (숫자 10자리)
const businessError = ref('');
const validateBusiness = () => {
  if (!form.business) { businessError.value = ''; return true; }   // 빈 값은 isFormValid가 처리
  const digits = String(form.business).replace(/\D/g, '');         // 하이픈 제거 후 순수 숫자만 카운트
  const isValid = digits.length === 10;
  businessError.value = isValid ? '' : '사업자 등록번호를 다시 확인해주세요.';
  if (!isValid) showToast('사업자 등록번호를 다시 확인해주세요.', 'error');
  return isValid;
};

// 등록 및 저장
async function saveStore() {
  if (!validateEmail()) {
    showToast('올바른 이메일 형식으로 작성해주세요.', 'error');
    return;
  }
  if (!validateBusiness()) return;   // 사업자번호 미달 시 중단 (validateBusiness가 토스트 표시)

  try {
    let finalFilePath = form.filePath;
    if (selectedFile.value) {
      const newS3Path = await uploadFileToS3(selectedFile.value.name,selectedFile.value.size );

      if (newS3Path) {
        finalFilePath = newS3Path;
      }
    }
    // [ 수정 로직 ]
    if (editTarget.value) {
      const updateData = {
        storeName: form.storeName,
        ownerName: form.ownerName,
        ownerEmail: form.ownerEmail,
        postcode: form.postcode,
        address: form.address,
        addressDetail: form.addressDetail,
        closedAt: (form.closedAt === "운영 중" || !form.closedAt) ? null : form.closedAt + "T00:00:00",
        filePath: finalFilePath
      };

      const res = await putStoreUpdate(editTarget.value.idx, updateData);
      if (res.data.code === 2000) {
        showToast('가맹점 정보가 수정되었습니다.');
        await storeListRes(pagination.currentPage);
        trendChart.value?.reload();   // 수정 후 월별 입점/폐점 그래프도 갱신
        showModal.value = false;
      }
    }
    // [ 등록 로직 ]
    else {
      const storeRegDto = {
        storeName: form.storeName,
        ownerEmail: form.ownerEmail,
        postcode: form.postcode,
        address: form.address,
        addressDetail: form.addressDetail,
        business: form.business,
        filePath: finalFilePath
      };

      const res = await postNewRegister(storeRegDto);
      if (res.data.code === 2000) {
        showToast('가맹점이 등록되었습니다.');
        storesList.value.length = 0;
        await storeListRes();
        trendChart.value?.reload();   // 등록 후 월별 입점/폐점 그래프도 갱신
      }
    }

    showModal.value = false;
    selectedFile.value = null;

  } catch (error) {
    const serverMessage = error.response?.data?.message || error.message;
    showToast(`등록 실패 : ${serverMessage}`, 'error');
  }
}

// 필수 항목들이 비어있는지 확인 후 모두 입력시 버튼 활성화
const isFormValid = computed(() => {
  const fields = [
    form.storeName,
    form.ownerEmail,
    form.address,
    form.business,
    form.postcode,
    form.fileName
  ];

  // 모든 필드에 값이 있고, 사업자 번호가 12자리(하이픈 포함 000-00-00000)인지 확인
  return fields.every(field => field && String(field).trim() !== '') &&
    form.business.length === 12;
});

// S3 미리보기 (뷰어)
function viewPdf() {
  const filePath = detailTarget.value?.filePath;
  if (!filePath) return showToast('파일이 없습니다.', 'error');

  const s3BaseUrl = 'https://nexus-store-archive.s3.ap-northeast-2.amazonaws.com/';
  window.open(s3BaseUrl + filePath, '_blank'); // 새 탭에서 열기
}

// S3 다운로드
async function downloadPdf() {
  const filePath = detailTarget.value?.filePath;
  if (!filePath) return showToast('파일이 없습니다.', 'error');

  const s3BaseUrl = 'https://nexus-store-archive.s3.ap-northeast-2.amazonaws.com/';
  const fileUrl = s3BaseUrl + filePath;

  try {
    // fetch로 파일을 가져옵니다.
    const response = await fetch(fileUrl);
    if (!response.ok) throw new Error('파일을 가져오는데 실패했습니다.');

    // 파일 데이터를 Blob(Binary Large Object)으로 변환합니다.
    const blob = await response.blob();

    // Blob 데이터를 위한 임시 URL을 생성합니다.
    const url = window.URL.createObjectURL(blob);

    // 가상의 링크를 생성하여 클릭을 유도합니다.
    const link = document.createElement('a');
    link.href = url;
    link.download = `${detailTarget.value.storeName}_사업자등록증.pdf`; // 저장될 파일명
    document.body.appendChild(link);
    link.click();

    // 사용이 끝난 임시 URL과 링크를 제거합니다.
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

  } catch (error) {
    // 만약 CORS 에러가 난다면, 가장 단순한 방법인 window.open을 백업으로 사용합니다.
    showToast('브라우저 보안 정책으로 인해 직접 다운로드가 제한되었습니다. 새 탭에서 파일을 엽니다.', 'error');
    window.open(fileUrl, '_blank');
  }
}

// 카카오 주소 API
const sample6_execDaumPostcode = () => {
  new window.daum.Postcode({
    oncomplete: (data) => {
      let addr = '';

      // 사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
      if (data.userSelectedType === 'R') {
        addr = data.roadAddress;
      } else {
        addr = data.jibunAddress;
      }

      form.address = addr;
      form.postcode = data.zonecode;

      // 상세주소 입력창으로 포커스 이동
      nextTick(() => {
        const detailInput = document.getElementById("sample6_detailAddress");
        if (detailInput) detailInput.focus();
      });
    }
  }).open();
}

// 버튼 클릭
const visiblePages = computed(() => {
  const range = 10;
  const currentGroup = Math.floor(pagination.currentPage / range);
  const start = currentGroup * range;
  const end = Math.min(start + range, pagination.totalPage);
  const pages = [];
  for (let i = start; i < end; i++) {
    pages.push(i);
  }
  return pages;
});

// 상태 선택 함수 (버튼 전용)
const selectStatus = (status) => {
  filterStatus.value = status;
  storeListRes(0);
};

// 전체 가맹점 다시 불러오기 (필터 초기화)
const resetFilters = () => {
  searchQuery.value = ''
  filterStatus.value = '전체'
  storeListRes(0)
  trendChart.value?.reload()
}

onMounted(() => {
  storeListRes(0)
})
</script>


<template>
  <div class="p-5 space-y-4">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-xl font-bold text-gray-900 tracking-tight">가맹점 관리</h1>
      </div>
      <button @click="openModal(null)" class="bg-[#F37321] text-white px-4 py-2 text-sm font-semibold rounded-lg hover:bg-[#e0661d] transition-colors flex items-center gap-2 shadow-sm cursor-pointer">
        <Plus class="w-4 h-4" /> 신규 가맹점 등록
      </button>
    </div>
    <!-- Summary -->
    <div class="grid grid-cols-3 gap-4">
      <div class="bg-white border border-gray-200 rounded-lg p-5 shadow-sm">
        <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">전체 입점 가맹점</p>
        <p class="text-3xl font-black text-gray-900 mt-2">{{ storeTotalCount?.totalCount }}<span class="text-sm font-normal text-gray-400 ml-1">개</span></p>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-5 shadow-sm">
        <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">입점</p>
        <p class="text-3xl font-black text-green-600 mt-2">{{ storeTotalCount?.activeCount }}<span class="text-sm font-normal text-gray-400 ml-1">개</span></p>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-5 shadow-sm">
        <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">폐점 가맹점</p>
        <p class="text-3xl font-black text-red-500 mt-2">{{ storeTotalCount?.closedCount }}<span class="text-sm font-normal text-gray-400 ml-1">개</span></p>
      </div>
    </div>
    <!-- 월별 입점/폐점 추이 그래프 -->
    <StoreTrendChart ref="trendChart" />
    <div class="flex items-center justify-between gap-4">
      <div class="flex items-center gap-2 flex-1 max-w-md">
        <div class="relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input v-model="searchQuery" type="text" placeholder="가맹점명 검색" @keyup.enter="storeListRes(0)"
                 class="pl-10 pr-4 py-2 rounded-lg border border-gray-200 text-sm w-52 bg-white shadow-sm focus:border-[#F37321] focus:ring-1 focus:ring-[#F37321] outline-none transition-colors"/>
        </div>
        <button v-for="s in statusOptions" :key="s" @click="selectStatus(s)" class="px-3 py-1.5 text-sm font-semibold border rounded-lg transition-colors shadow-sm cursor-pointer"
          :class="filterStatus === s ? 'bg-[#F37321] text-white border-[#F37321]' : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'">
          {{ s }}
        </button>
      </div>
    </div>
    <!-- Table Section -->
    <div class="bg-white border border-gray-200 rounded-lg overflow-hidden shadow-sm">
      <table class="w-full text-sm text-left">
        <thead>
        <tr class="border-b border-gray-200 bg-gray-50">
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">가맹점 코드</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">가맹점명</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">담당자명</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">이메일</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">가맹점 위치</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">사업자번호</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">운영상태</th>
          <th class="px-5 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">관리</th>
        </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
        <tr v-for="store in storesList" :key="store.idx" @click="openDetail(store.idx)" class="hover:bg-gray-50/50 transition-colors cursor-pointer group"
            :class="{ 'bg-gray-50/40 opacity-70': store.status === '폐점' }">
          <td class="px-5 py-3.5 font-mono text-xs text-gray-400">{{ store.idx }}</td>
          <td class="px-5 py-3.5 font-bold text-gray-900 group-hover:text-[#F37321] transition-colors">
            {{ store.storeName }}
          </td>
          <td class="px-5 py-3.5 text-gray-600">{{ store.ownerName }}</td>
          <td class="px-5 py-3.5 text-gray-500 text-xs truncate max-w-[150px]">{{ store.ownerEmail }}</td>
          <td class="px-5 py-3.5 text-gray-500 text-xs truncate max-w-[250px]" :title="store.address">
            {{ store.address }}
          </td>
          <td class="px-5 py-3.5 font-mono text-xs text-gray-400">{{ store.business }}</td>
          <td class="px-5 py-3.5 text-center">
              <span class="text-[11px] font-bold px-2 py-0.5 rounded-lg"
                    :class="store.status === '입점' ? 'bg-green-100 text-green-700' : 'bg-red-50 text-red-600'">
                {{ store.status }}
              </span>
          </td>
          <td class="px-5 py-3.5">
            <div class="flex justify-center">
              <button @click.stop="openModal(store.idx)" :disabled="store.status === '폐점'" class="px-3 py-1.5 text-xs font-semibold rounded transition-colors shadow-sm"
                :class="store.status === '입점'
                ? 'text-[#F37321] border border-[#F37321] hover:bg-orange-50 cursor-pointer'
                : 'text-gray-400 border border-gray-200 bg-gray-50 cursor-not-allowed'">
                수정
              </button>
            </div>
          </td>
        </tr>
        <tr v-if="storesList.length === 0">
          <td colspan="8" class="px-5 py-24 text-center">
            <div class="flex flex-col items-center justify-center space-y-4">
              <p class="text-gray-400 text-sm">등록된 가맹점이 없거나 검색 결과가 없습니다.</p>
              <button @click="resetFilters"
                      class="flex items-center gap-2 px-5 py-2.5 text-sm font-bold text-[#F37321] border border-[#F37321] rounded-lg hover:bg-orange-50 transition-all shadow-sm cursor-pointer active:scale-95">
                <RefreshCw class="w-4 h-4" />
                전체 가맹점 다시 불러오기
              </button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
    <!-- 입점 가맹점 상세 정보 모달 -->
    <Teleport to="body">
    <div v-if="showDetailModal" class="fixed inset-0 z-[60] flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/40" @click="showDetailModal = false"></div>
      <div class="relative bg-white rounded-xl w-full max-w-lg border border-gray-200 shadow-xl overflow-hidden animate-in fade-in slide-in-from-bottom-4 duration-200">
        <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
          <h3 class="font-bold text-gray-900">입점 가맹점 상세 정보</h3>
          <button @click="showDetailModal = false" class="text-gray-400 hover:text-gray-600 cursor-pointer">✕</button>
        </div>
        <div class="p-6 space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-1.5">
              <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">가맹점 코드</label>
              <div class="w-full px-4 py-2 bg-gray-50 border border-gray-100 rounded-lg text-sm font-mono text-gray-500">
                {{ detailTarget?.idx }}
              </div>
            </div>
            <div class="space-y-1.5">
              <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">가맹점명</label>
              <div class="w-full px-4 py-2 bg-gray-50 border border-gray-100 rounded-lg text-sm font-bold text-gray-900">
                {{ detailTarget?.storeName }}
              </div>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-5">
            <div class="space-y-1.5">
              <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">담당자명</label>
              <div class="w-full px-4 py-2 bg-gray-50 border border-gray-100 rounded-lg text-sm text-gray-700">
                {{ detailTarget?.ownerName }}
              </div>
            </div>
            <div class="space-y-1.5">
              <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">이메일 주소</label>
              <div class="w-full px-4 py-2 bg-gray-50 border border-gray-100 rounded-lg text-sm text-gray-700">
                {{ detailTarget?.ownerEmail }}
              </div>
            </div>
          </div>
          <div class="space-y-1.5">
            <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">가맹점 위치</label>
            <div class="w-full px-4 py-2 bg-gray-50 border border-gray-100 rounded-lg text-sm text-gray-600">
              {{ detailTarget?.totalAddress }}
            </div>
          </div>
          <div class="space-y-1.5">
            <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">사업자번호</label>
          </div>
          <div class="grid grid-cols-2 gap-5 p-4 bg-gray-50 rounded-lg border border-gray-100">
            <div class="space-y-1.5">
              <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest flex items-center gap-1">
                <div class="w-1 h-1 bg-green-400 rounded-full"></div> 개업일
              </label>
              <div class="text-sm font-bold text-gray-700">{{ detailTarget?.createdAt || '-' }}</div>
            </div>
            <div class="space-y-1.5">
              <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest flex items-center gap-1">
                <div class="w-1 h-1 rounded-full" :class="detailTarget?.closedAt === '운영 중' ? 'bg-gray-300' : 'bg-red-400'"></div>
                폐업일
              </label>
              <div class="text-sm font-bold"
                   :class="detailTarget?.closedAt === '운영 중' ? 'text-gray-300' : 'text-gray-900'">
                {{ detailTarget?.closedAt }}
              </div>
            </div>
          </div>
          <div class="space-y-3 pt-2">
            <div v-if="detailTarget.closedAt && detailTarget.closedAt !== '운영 중'"
                 class="flex flex-col items-center p-3 bg-red-50 rounded-lg border border-red-100">
              <span class="text-xs text-red-600 font-bold">* 폐업 처리된 가맹점입니다. 증빙 용도로만 사용하세요.</span>
              <span class="text-[11px] text-red-400 mt-0.5">폐업일: {{ detailTarget.closedAt }}</span>
            </div>
            <button @click="viewPdf" type="button" class="w-full flex items-center justify-center gap-2 py-3 rounded-lg bg-white border border-gray-200 text-gray-700 text-sm font-bold hover:bg-gray-50 transition-colors shadow-sm cursor-pointer">
              <Search class="w-4 h-4 text-gray-400" />
              사업자 PDF 미리보기
            </button>
            <button @click="downloadPdf" type="button" :class="[
            'w-full flex items-center justify-center gap-2 py-3 rounded-lg text-sm font-bold transition-colors shadow-sm cursor-pointer',
            detailTarget.closedAt && detailTarget.closedAt !== '운영 중'
              ? 'bg-white border border-gray-200 text-gray-700 text-sm font-bold hover:bg-gray-50' // 폐업 시: 미리보기와 유사한 연회색 톤
              : 'bg-[#F37321] text-white hover:bg-[#e0661d]' ]">
              <FileText :class="['w-4 h-4', detailTarget.closedAt && detailTarget.closedAt !== '운영 중' ? 'text-gray-400' : 'text-white']" />
              사업자 PDF 다운로드
            </button>
          </div>
        </div>
        <div class="px-6 py-4 border-t border-gray-100 flex justify-end">
          <button @click="showDetailModal = false"
                  class="px-4 py-2 text-sm font-semibold text-gray-600 border border-gray-200 rounded hover:bg-gray-50 cursor-pointer">닫기</button>
        </div>
      </div>
    </div>
    </Teleport>
    <!-- 신규 가맹점 등록 / 수정 모달 -->
    <Teleport to="body">
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 overflow-hidden">
      <div class="absolute inset-0 bg-black/40" @click="showModal = false"></div>
      <div class="relative bg-white rounded-lg w-full max-w-lg border border-gray-200 shadow-xl overflow-hidden animate-in fade-in slide-in-from-bottom-4 duration-200">
        <div class="px-8 py-5 border-b border-gray-100 flex justify-between items-center bg-gray-50">
          <h3 class="font-bold text-gray-900 text-lg">{{ editTarget ? '입점 가맹점 정보 수정' : '신규 가맹점 등록' }}</h3>
          <button @click="showModal = false" class="text-gray-400 hover:text-gray-600 font-bold text-xl cursor-pointer">✕</button>
        </div>
        <form @submit.prevent="saveStore" class="p-8 space-y-5">
          <div class="space-y-1.5">
            <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">가맹점명</label>
            <input v-model="form.storeName" required type="text" placeholder="예: 더벤티 서울점"
                   class="w-full px-4 py-2 rounded-lg border border-gray-200 text-sm focus:border-[#F37321] focus:ring-4 focus:ring-[#F37321]/5 outline-none transition-all" />
          </div>
          <div class="grid gap-5" :class="editTarget ? 'grid-cols-2' : 'grid-cols-1'">
            <div v-if="editTarget" class="space-y-1.5">
              <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">담당자명</label>
              <input v-model="form.ownerName" required type="text" placeholder="성함 입력"
                     class="w-full px-4 py-2 rounded-lg border border-gray-200 text-sm focus:border-[#F37321] focus:ring-4 focus:ring-[#F37321]/5 outline-none transition-all" />
            </div>
            <div class="space-y-1.5">
              <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">이메일</label>
              <input v-model="form.ownerEmail" @blur="validateEmail" type="email" placeholder="example@email.com"
                     class="w-full px-4 py-2 rounded-lg border text-sm outline-none transition-all"
                     :class="emailError ? 'border-red-500 focus:ring-4 focus:ring-red-500/20' : 'border-gray-200 focus:border-[#F37321] focus:ring-4 focus:ring-[#F37321]/5'" />
              <p v-if="emailError" class="text-xs text-red-500 mt-1">{{ emailError }}</p>
            </div>
          </div>
          <div class="space-y-3">
            <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">위치 (주소)</label>
            <div class="flex gap-2">
              <input v-model="form.postcode" type="text" id="sample6_postcode" placeholder="우편번호" readonly class="w-32 px-4 py-2 rounded-lg border border-gray-200 text-sm outline-none bg-gray-50"/>
              <button type="button" @click="sample6_execDaumPostcode" class="px-4 py-2 bg-gray-800 text-white text-sm rounded-lg hover:bg-gray-700 transition-colors cursor-pointer">
                주소 검색
              </button>
            </div>
            <input v-model="form.address" type="text" placeholder="도로명/지번 주소" readonly class="w-full px-4 py-2 rounded-lg border border-gray-200 text-sm focus:border-[#F37321] focus:ring-4 focus:ring-[#F37321]/5 outline-none transition-all"/>
            <input v-model="form.addressDetail" type="text" id="sample6_detailAddress" placeholder="상세 주소를 입력해주세요" class="w-full px-4 py-2 rounded-lg border border-gray-200 text-sm focus:border-[#F37321] focus:ring-4 focus:ring-[#F37321]/5 outline-none transition-all"/>
          </div>
          <!-- 수정 모달에서만 표시 -->
          <div v-if="editTarget" class="grid grid-cols-2 gap-5">
            <div class="space-y-1.5">
              <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">개업일</label>
              <input v-model="form.createdAt" type="date"
                     class="w-full px-4 py-2 rounded-lg border border-gray-200 text-sm focus:border-[#F37321] focus:ring-4 focus:ring-[#F37321]/5 outline-none transition-all" />
            </div>
            <div class="space-y-1.5">
              <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">폐업일</label>
              <input v-model="form.closedAt" type="date"
                     class="w-full px-4 py-2 rounded-lg border border-gray-200 text-sm focus:border-[#F37321] focus:ring-4 focus:ring-[#F37321]/5 outline-none transition-all" />
            </div>
          </div>
          <div class="space-y-1.5">
            <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">사업자번호</label>
            <input :value="form.business" @input="formatBusinessNumber" @blur="validateBusiness" inputmode="numeric" type="text" placeholder="숫자만 입력하세요" maxlength="12"
                   class="w-full px-4 py-2 rounded-lg border text-sm outline-none transition-all"
                   :class="businessError ? 'border-red-500 focus:ring-4 focus:ring-red-500/20' : 'border-gray-200 focus:border-[#F37321] focus:ring-4 focus:ring-[#F37321]/5'"/>
            <p v-if="businessError" class="text-xs text-red-500 mt-1">{{ businessError }}</p>
          </div>
          <div class="space-y-1.5">
            <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">사업자 PDF</label>
            <label class="cursor-pointer block">
              <div class="w-full px-4 py-2 rounded-lg border border-gray-200 text-sm text-gray-400 bg-gray-50 hover:bg-gray-100 transition-colors flex items-center justify-between">
                <span>{{ form.fileName || '파일을 선택해주세요' }}</span>
                <FileText class="w-4 h-4" />
              </div>
              <input type="file" class="hidden" @change="handleFileChange" accept=".pdf" />
            </label>
          </div>
          <div class="flex gap-3 pt-4">
            <button type="button" @click="showModal = false"
                    class="flex-1 py-3 rounded-lg border border-gray-200 text-sm font-bold text-gray-500 hover:bg-gray-50 transition-colors cursor-pointer">취소</button>
            <button type="submit" :disabled="!isFormValid" :class="[!isFormValid ? 'bg-gray-300 cursor-not-allowed' : 'bg-[#F37321] hover:bg-[#e0661d] cursor-pointer']" class="flex-1 py-3 rounded-lg text-white text-sm font-bold transition-colors shadow-sm">
              저장
            </button>
          </div>
        </form>
      </div>
    </div>
    </Teleport>
    <div v-if="pagination.totalPage > 1" class="flex justify-center items-center gap-2 pt-2">
      <button
        class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
        :disabled="pagination.currentPage === 0"
        @click="changePage(pagination.currentPage - 1)">
        <ChevronLeft class="w-4 h-4" />
      </button>

      <button v-for="pageIdx in visiblePages" :key="pageIdx"
              class="w-8 h-8 rounded text-sm font-semibold cursor-pointer transition-colors"
              :class="pagination.currentPage === pageIdx
          ? 'bg-[#F37321] text-white'
          : 'text-gray-500 hover:bg-gray-50'"
              @click="changePage(pageIdx)">
        {{ pageIdx + 1 }}
      </button>

      <button
        class="p-2 rounded border border-gray-200 text-gray-400 hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
        :disabled="pagination.currentPage >= pagination.totalPage - 1"
        @click="changePage(pagination.currentPage + 1)">
        <ChevronRight class="w-4 h-4" />
      </button>
    </div>
    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </div>
</template>
