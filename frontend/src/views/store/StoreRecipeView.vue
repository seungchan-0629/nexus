<script setup>
import {ref, computed, onMounted, reactive, watch} from 'vue'
import {Plus, Search, Image as ImageIcon, Tag, Trash2, ChevronRight, ChevronLeft} from 'lucide-vue-next'
import {getProductList, getCategoryList, getMenuList, getMenuItemList, getPresignedUrl, postNewRegister, putMenuUpdate, putMenuDelete, postMenuCategoryRegister, deleteCategory} from '@/api/menu/index.js'
import axios from 'axios'
import Toast from '@/components/common/Toast.vue'
import ConfirmModal from '@/components/common/ConfirmModal.vue'
import { useToast } from '@/composables/useToast'

const { toast, showToast } = useToast()

const confirmState = ref({ open: false, idx: null, name: '' })
function openDeleteCategoryConfirm(idx, name) {
  confirmState.value = { open: true, idx, name }
}
function closeConfirm() {
  confirmState.value = { open: false, idx: null, name: '' }
}

const showCategoryModal = ref(false)
const newCategoryInput = ref('')
const searchQuery = ref('')
const selectedCategoryIdx = ref(null)
const showIngredientModal = ref(false)
const selectedMenu = ref(null)
const showDeleteConfirm = ref(false)
const deleteTarget = ref(null)
const selectedFile = ref(null);
const products = ref([])
const menus = ref([])
const pagination = reactive({
  totalPage: 0,
  totalCount: 0,
  currentPage: 0,
  currentSize: 10
})
const UNIT_OPTIONS = ['g', 'kg', 'ml', 'L', '개', '봉', '묶음', 'ea', '기타']
const categories = ref([])

//  메뉴 등록 / 수정 모달
const showMenuModal = ref(false)
const editTarget = ref(null)
const formattedPriceInput = ref('')
const getInFrom = () => ({
  menuName: '',
  price: null,
  imgPath: '',
  imgName:'',
  menuCategoryIdx: null,
  menuItemList: [
    {
      productIdx: '',
      quantity: 0,
      menuUnit: '' ,
      customUnit: ''
    }
  ]
})

const menuForm = ref(getInFrom())

// 제품 목록 조회
const productListRes = async () => {
  const res  = await getProductList()
  products.value = res.data.result
}

// 메뉴 목록 조회
const getMenuRes = async (page = 0)=>{
  const searchReq = {
    keyword : searchQuery.value,
    categoryIdx: selectedCategoryIdx.value
  }
  const res = await getMenuList(searchReq, page, pagination.currentSize)
  menus.value.splice(0, menus.value.length, ...res.data.result.menuList)
  pagination.totalPage = res.data.result.totalPage
  pagination.totalCount = res.data.result.totalCount
  pagination.currentPage = res.data.result.currentPage
}

// 카테고리 목록 조회
const categoryRes = async () => {
  const res = await getCategoryList()
  categories.value = res.data.result
}

// 카테고리 선택
const selectCategory = (idx) => {
  selectedCategoryIdx.value = idx;
  getMenuRes(0);
}

// 등록 모달
function openNewMenuModal() {
  editTarget.value = null;
  selectedFile.value = null;
  formattedPriceInput.value = '';
  const initial = getInFrom();
  initial.menuCategoryIdx = null;
  menuForm.value = initial;
  showMenuModal.value = true;
}

// 수정 모달창
async function openEditMenuModal(menu) {
  try {
    // 1. 상세 데이터 조회 (재료 목록 등)
    const res = await getMenuItemList(menu.idx);
    console.log(res.data.result)
    const detail = res.data.result; // 응답 데이터 (제공해주신 구조)

    editTarget.value = menu;

    // 2. getInFrom()의 표준 구조를 가져와서 데이터를 '재조립' 합니다.
    const form = getInFrom();
    form.menuName = detail.menuName;
    form.price = detail.price;

    form.imgPath = detail.imgPath ? detail.imgPath.trim() : "";
    form.imgName = detail.imgPath ? detail.imgPath.trim() : "";

    form.menuCategoryIdx = categories.value.find(
      c => c.menuCategoryName === detail.menuCategory
    )?.categoryIdx ?? null;

    form.menuItemList = detail.menuItemList.map(i => {
      const isStandardUnit = UNIT_OPTIONS.includes(i.menuUnit);
      return {
        productIdx: products.value.find(p => p.productName === i.productName)?.idx ?? '',
        quantity: i.quantity,
        menuUnit: isStandardUnit ? i.menuUnit : '기타',
        customUnit: isStandardUnit ? '' : i.menuUnit
      };
    });

    menuForm.value = form;
    console.log(menuForm.value)

    formattedPriceInput.value = (detail.price || 0).toLocaleString('ko-KR');

    showMenuModal.value = true;
  } catch (error) {
    console.error("수정 데이터 로드 실패:", error);
    showToast("메뉴 상세 정보를 불러오는 중 오류가 발생했습니다.", 'error');
  }
}

// 숫자 이외의 문자(한글, 영문 등)를 모두 제거
const onPriceInput = (e) => {
  const rawValue = e.target.value.replace(/[^0-9]/g, '')
  const numericValue = rawValue ? parseInt(rawValue, 10) : 0
  menuForm.value.price = numericValue // 실제 데이터 저장
  formattedPriceInput.value = numericValue ? numericValue.toLocaleString('ko-KR') : ''
};

// 재료 버튼 클릭시 menuForm에 재료 추가
function addIngredientRow() {
  menuForm.value.menuItemList.push({
    productIdx: '',
    quantity: 0,
    menuUnit: '' ,
    customUnit: ''
  })
}

// 재료 버튼 클릭시 menuForm에 재료 삭제
function removeIngredientRow(idx) {
  menuForm.value.menuItemList.splice(idx, 1)
}

// 이미지 등록
function handleImageChange(e) {
  const file = e.target.files[0]
  if (!file) return;

  const allowedTypes = ['image/jpg', 'image/jpeg', 'image/png'];
  if (!allowedTypes.includes(file.type)) {
    showToast('JPG, PNG 형식의 이미지 파일만 업로드할 수 있습니다.', 'error');
    e.target.value = '';
    return;
  }
  if (file) {
    selectedFile.value = file; // 선택한 파일 보관
    menuForm.value.imgName = file.name; // 화면 표시용
  }
}

// S3 업로드 로직만 담당하는 공통 함수
async function uploadFileToS3() {
  if (!selectedFile.value) return null;

  try {
    const presigned = await getPresignedUrl(selectedFile.value.name);
    const { url, fileName: s3Path } = presigned.data.result;
    await axios.put(url, selectedFile.value, {
      headers: { 'Content-Type': selectedFile.value.type }
    });
    return s3Path;
  } catch {
    showToast("파일 업로드 중 오류가 발생했습니다. 다시 시도해주세요.", 'error')
  }
}

// 등록 및 저장 시 비어있는지 확인
const isFormValid = computed(() => {
  return (
    menuForm.value.menuName?.trim() &&
    menuForm.value.price !== null &&
    menuForm.value.menuCategoryIdx !== null &&
    menuForm.value.menuItemList.length > 0 &&
    menuForm.value.menuItemList.every(item => {
      // 1. 제품과 수량은 기본적으로 필수
      const baseValid = item.productIdx && item.quantity > 0;

      // 2. 단위 검증 로직
      let unitValid = false;
      if (item.menuUnit === '기타') {
        // '기타'일 때는 customUnit에 값이 채워져 있어야 함
        unitValid = item.customUnit?.trim() !== '';
      } else {
        // '기타'가 아닐 때는 menuUnit만 선택되어 있으면 됨 (빈 값 아님)
        unitValid = item.menuUnit !== '';
      }

      return baseValid && unitValid;
    })
  );
})

// 메뉴 등록 및 수정
async function saveMenu() {
  try{
    if (selectedFile.value) {
      const newS3Path = await uploadFileToS3();
      if (newS3Path) {
        menuForm.value.imgPath = newS3Path;
      }
    }

    const dto = {
      menuName: menuForm.value.menuName,
      price: menuForm.value.price,
      imgPath: menuForm.value.imgPath,
      menuCategoryIdx: menuForm.value.menuCategoryIdx,
      menuItemList: menuForm.value.menuItemList.map(i => ({
        productIdx: i.productIdx,
        quantity: i.quantity,
        menuUnit: i.menuUnit === '기타' ? i.customUnit : i.menuUnit
      }))
    }

    const res = editTarget.value
      ? await putMenuUpdate(editTarget.value.idx, dto)
      : await postNewRegister(dto);

    if (res.data.code === 2000) {
      showToast(editTarget.value ? "메뉴가 수정되었습니다." : "신규 메뉴가 등록되었습니다.");

      if (!editTarget.value) menus.value.length = 0; // 등록일 때만 목록 초기화
      await getMenuRes();

      showMenuModal.value = false;
      selectedFile.value = null;
    }

  }catch (error) {
    const serverMessage = error.response?.data?.message || error.message;
    showToast(`등록 실패 : ${serverMessage}`, 'error');
  }
}

// 카테고리 추가
async function addCategoryAction() {
  const name = newCategoryInput.value.trim()
  console.log(name)
  if (!name) return
  try {
    const categoryName = {
      menuCategoryName: name
    }
    const res = await postMenuCategoryRegister(categoryName)
    console.log(res)
    if (res.data.code === 2000) {
      showToast("카테고리가 등록되었습니다.");

      newCategoryInput.value = ''
      await categoryRes()
    }
  } catch (error) {
    const serverMessage = error.response?.data?.message || error.message;
    showToast(`등록 실패 : ${serverMessage}`, 'error');
  }
}

// 카테고리 삭제
async function deleteCategoryAction() {
  const { idx, name } = confirmState.value
  closeConfirm()
  try {
    const res = await deleteCategory(idx)
    if (res.data.code === 2000) {
      showToast("카테고리가 삭제되었습니다.");
      await categoryRes()
    }

  } catch (error) {
    const serverMessage = error.response?.data?.message || error.message;
    showToast(`등록 실패 : ${serverMessage}`, 'error');
  }
}

// 상세 모달창
async function openIngredientModal(menuIdx) {
  const res = await getMenuItemList(menuIdx)
  selectedMenu.value = res.data.result
  showIngredientModal.value = true
}

// 메뉴 삭제
function openDeleteConfirm(menu) {
  deleteTarget.value = menu
  showDeleteConfirm.value = true
}

// 삭제 모달 창에 있는 삭제
async function confirmDelete() {
  if (deleteTarget.value) {
    const res = await putMenuDelete(deleteTarget.value.idx)
    if (res.data.code === 2000) {
      showToast("메뉴가 삭제되었습니다.");
      await getMenuRes();
    }
  }
  showDeleteConfirm.value = false
  deleteTarget.value = null
}

// 제품 선택 시 중복 비허용
const isAlreadySelected = (productIdx, currentItem) => {
  return menuForm.value.menuItemList.some(
    (i) => i !== currentItem && i.productIdx === productIdx
  );
};

// searchQuery가 변할 때마다 자동으로 getMenuRes 실행
watch(searchQuery, () => {
  getMenuRes(0)
})

//  가격 유틸
function formatPrice(price) {
  return '₩ ' + price.toLocaleString('ko-KR')
}

// 페이지 번호
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

// 페이지 번호 클릭시 수정 (0보다 작거나 마지막 페이지(totalPage - 1)보다 크면 무시)
const changePage = (page) => {
  if (page < 0 || page >= pagination.totalPage) return
  getMenuRes(page)
}

onMounted(() => {
  getMenuRes()
  productListRes()
  categoryRes()
})
</script>

<template>
  <div class="p-5 space-y-4">
    <Toast :toast="toast" />
    <ConfirmModal
      :open="confirmState.open"
      :title="`'${confirmState.name}' 카테고리를 삭제하시겠습니까?`"
      confirm-text="삭제"
      type="danger"
      @close="closeConfirm"
      @confirm="deleteCategoryAction" />
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-xl font-bold text-gray-900 tracking-tight">메뉴 관리</h1>
      </div>
    </div>
    <div class="flex items-center gap-3 mb-4 flex-wrap">
      <div class="relative">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input v-model="searchQuery" type="text" placeholder="메뉴명 검색" @keyup.enter="getMenuRes(0)"
               class="pl-10 pr-4 py-2 rounded-lg border border-gray-200 text-sm w-52 bg-white shadow-sm focus:border-[#2563eb] focus:ring-1 focus:ring-[#2563eb] outline-none transition-colors"/>
      </div>
      <div class="flex gap-1.5 flex-wrap">
        <button @click="selectCategory(null)"
                class="px-3 py-1.5 text-sm font-semibold border rounded-lg transition-colors shadow-sm cursor-pointer"
                :class="!selectedCategoryIdx ? 'bg-[#2563eb] text-white border-[#2563eb]' : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'">
          전체
        </button>
        <button v-for="cat in categories" :key="cat.categoryIdx" @click="selectCategory(cat.categoryIdx)" class="px-3 py-1.5 text-sm font-semibold border rounded-lg transition-colors shadow-sm cursor-pointer"
                :class="selectedCategoryIdx === cat.categoryIdx
        ? 'bg-[#2563eb] text-white border-[#2563eb]'
        : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'">
          {{ cat.menuCategoryName }} </button>
      </div>
    </div>
    <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-sm text-left">
          <thead>
          <tr class="border-b border-gray-200 bg-gray-50">
            <th class="px-4 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">메뉴번호</th>
            <th class="px-4 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">카테고리</th>
            <th class="px-4 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">메뉴명</th>
            <th class="px-4 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider">가격</th>
            <th class="px-4 py-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-center">재료 수</th>
          </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
          <tr v-for="menu in menus" :key="menu.idx" @click="openIngredientModal(menu.idx)" class="hover:bg-gray-50/50 transition-colors cursor-pointer group">
            <td class="px-5 py-3.5 font-mono text-xs text-gray-400">{{ menu.idx }}</td>
            <td class="px-5 py-3.5">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-gray-100 text-gray-600 border border-gray-200">
                {{ menu.menuCategory || '기타' }}
              </span>
            </td>
            <td class="px-5 py-3.5 font-bold text-gray-900 group-hover:text-[#2563eb] transition-colors">{{ menu.menuName }}</td>
            <td class="px-5 py-3.5 text-gray-700 font-semibold">{{ formatPrice(menu.price) }}</td>
            <td class="px-5 py-3.5 text-center">
                <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-blue-50 text-blue-600">
                  {{ menu.menuItemCount }} 개
                </span>
            </td>
          </tr>
          <tr v-if="menus.length === 0">
            <td colspan="5" class="px-5 py-12 text-center text-gray-400 text-sm">메뉴가 없습니다.</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
    <!--  신규 메뉴 등록 / 수정 모달  -->
    <div v-if="showMenuModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/40 " @click="showMenuModal = false"></div>
      <div class="relative bg-white rounded-xl w-full max-w-2xl border border-gray-200 shadow-xl overflow-hidden animate-in fade-in slide-in-from-bottom-4 duration-200">
        <div class="px-7 py-5 border-b border-gray-100 flex justify-between items-center bg-gray-50">
          <h3 class="font-bold text-gray-900 text-lg">{{ editTarget ? '메뉴 수정' : '신규 메뉴 등록' }}</h3>
          <button @click="showMenuModal = false" class="text-gray-400 hover:text-gray-600 font-bold text-xl cursor-pointer">✕</button>
        </div>
        <form @submit.prevent="saveMenu" class="p-7 space-y-5">
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-1.5">
              <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">메뉴명</label>
              <input v-model="menuForm.menuName" required type="text" placeholder="예: 아메리카노"
                     class="w-full px-4 py-2 rounded-lg border border-gray-200 text-sm outline-none focus:border-[#2563eb] focus:ring-4 focus:ring-blue-500/10 transition-all" />
            </div>
            <div class="space-y-1.5">
              <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">가격 (원)</label>
              <input :value="formattedPriceInput" @input="onPriceInput" required type="text" placeholder="0"
                     class="w-full px-4 py-2 rounded-lg border border-gray-200 text-sm outline-none focus:border-[#2563eb] focus:ring-4 focus:ring-blue-500/10 transition-all text-right" />
            </div>
          </div>
          <div class="space-y-1.5">
            <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">카테고리</label>
            <div class="flex items-center gap-2 flex-wrap">
              <button v-for="cat in categories" :key="cat.categoryIdx" type="button" @click="menuForm.menuCategoryIdx = cat.categoryIdx"
                      :class="(menuForm.menuCategoryIdx !== null && menuForm.menuCategoryIdx === cat.categoryIdx)
          ? 'bg-[#3b82f6] text-white border-[#3b82f6]'
          : 'bg-white text-gray-500 border-gray-200 hover:border-gray-300 hover:bg-gray-50'"
                      class="px-3 py-1.5 rounded-lg border text-xs font-semibold transition-all cursor-pointer">
                {{ cat.menuCategoryName }}
              </button>
            </div>
          </div>
          <div class="space-y-1.5">
            <label class="text-[11px] font-bold text-gray-400 uppercase tracking-widest">메뉴 이미지</label>
            <label class="cursor-pointer block">
              <div class="w-full px-4 py-2 rounded-lg border border-gray-200 text-sm text-gray-400 bg-gray-50 hover:bg-gray-100 transition-colors flex items-center justify-between">
                <span>{{ menuForm.imgName || '이미지를 업로드하세요' }}</span>
                <ImageIcon class="w-4 h-4 flex-shrink-0" />
              </div>
              <input type="file" class="hidden" @change="handleImageChange" accept=".jpg, .jpeg, .png" />
            </label>
          </div>
          <div class="border-t border-gray-100 pt-5">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm font-bold text-gray-700">재료 등록</span>
              <button type="button" @click="addIngredientRow" class="flex items-center gap-1 text-xs font-bold text-[#2563eb] hover:text-[#1d4ed8] transition-colors cursor-pointer">
                <Plus class="w-3.5 h-3.5" /> 재료 추가
              </button>
            </div>
            <div class="grid grid-cols-[2.5fr_1fr_1.2fr_1.2fr_32px] gap-2 mb-2 px-0.5">
              <span class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">제품명</span>
              <span class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">소요량</span>
              <span class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">단위 선택</span>
              <span class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">직접 입력</span>
              <span></span>
            </div>
            <div class="space-y-3">
              <div v-for="(item, idx) in menuForm.menuItemList" :key="idx" class="grid grid-cols-[2.5fr_1fr_1.2fr_1.2fr_32px] gap-2 items-center">
                <select v-model="item.productIdx" class="w-full px-2 py-2 rounded-lg border border-gray-200 text-sm outline-none focus:border-[#2563eb] transition-all appearance-none bg-white">
                  <option value="">제품 선택</option>
                  <option v-for="p in products" :key="p.idx" :value="p.idx" :disabled="isAlreadySelected(p.idx, item)">
                    {{ p.productName }} {{ isAlreadySelected(p.idx, item) ? '(선택됨)' : '' }}
                  </option>
                </select>
                <input v-model.number="item.quantity" type="number" min="1" placeholder="0"
                       class="w-full px-2 py-2 rounded-lg border border-gray-200 text-sm outline-none focus:border-[#2563eb] text-right" />
                <select v-model="item.menuUnit" @change="item.menuUnit !== '기타' ? item.customUnit = '' : null"
                        class="w-full px-2 py-2 rounded-lg border border-gray-200 text-sm outline-none focus:border-[#2563eb] bg-white">
                  <option value="">선택</option>
                  <option v-for="unit in UNIT_OPTIONS" :key="unit" :value="unit">{{ unit }}</option>
                </select>
                <input v-model="item.customUnit" :disabled="item.menuUnit !== '기타'" type="text" placeholder="직접 입력"
                       :class="item.menuUnit === '기타' ? 'bg-white border-gray-200' : 'bg-gray-50 border-gray-100 text-transparent select-none'"
                       class="w-full px-2 py-2 rounded-lg border text-sm outline-none focus:border-[#2563eb] transition-all" />
                <button type="button" @click="removeIngredientRow(idx)"
                        class="w-8 h-8 flex items-center justify-center rounded-lg bg-red-50 text-red-400 hover:bg-red-100 transition-colors cursor-pointer">✕
                </button>
              </div>
              <p v-if="menuForm.menuItemList.length === 0" class="text-xs text-gray-300 py-2 text-center">재료를 추가해주세요</p>
            </div>
          </div>
          <div class="flex gap-3 pt-2">
            <button type="button" @click="showMenuModal = false"
                    class="flex-1 py-3 rounded-lg border border-gray-200 text-sm font-bold text-gray-500 hover:bg-gray-50 transition-colors cursor-pointer">취소
            </button>
            <button type="submit" :disabled="!isFormValid"
                    :class="[!isFormValid ? 'bg-gray-300 cursor-not-allowed' : 'bg-[#2563eb] hover:bg-[#e0661d] cursor-pointer']"
                    class="flex-1 py-3 rounded-lg text-white text-sm font-bold transition-colors shadow-sm">
              {{ editTarget ? '수정 저장' : '등록하기' }}
            </button>
          </div>
        </form>
      </div>
    </div>
    <!-- 재료 목록 상세 모달  -->
    <div v-if="showIngredientModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/40 " @click="showIngredientModal = false"></div>
      <div class="relative bg-white rounded-xl w-full max-w-lg border border-gray-200 shadow-xl overflow-hidden animate-in fade-in slide-in-from-bottom-4 duration-200">
        <div class="px-7 py-5 border-b border-gray-100 flex justify-between items-center bg-gray-50">
          <div>
            <h3 class="font-bold text-gray-900 text-lg">{{ selectedMenu?.menuName }}</h3>
            <p class="text-xs text-gray-400 mt-0.5">ID: {{ selectedMenu?.idx }}</p>
          </div>
          <button @click="showIngredientModal = false" class="text-gray-400 hover:text-gray-600 font-bold text-xl cursor-pointer">✕</button>
        </div>
        <div class="p-7">
          <div class="flex justify-center mb-8">
            <div class="w-48 h-48 rounded-2xl border border-gray-100 overflow-hidden bg-white shadow-md">
              <img v-if="selectedMenu?.imgPath"
                   :src="'https://nexus-menu-assets.s3.ap-northeast-2.amazonaws.com/' + selectedMenu.imgPath"
                   :alt="'nexus ' + selectedMenu?.menuName" class="w-full h-full object-cover"
                   @error="(e) => e.target.src = 'https://nexus-menu-assets.s3.ap-northeast-2.amazonaws.com/theVenti_logo.png'" />
              <img v-else :src="'https://nexus-menu-assets.s3.ap-northeast-2.amazonaws.com/theVenti_logo.png'"
                   alt="NEXUS" class="w-full h-full object-cover" />
            </div>
          </div>
        </div>
        <div class="p-7">
          <div class="h-[200px] overflow-y-auto scrollbar-hide border-b border-gray-50">
            <table class="w-full text-sm text-left">
              <thead class="sticky top-0 z-10 bg-gray-50/90 backdrop-blur-sm"> <!-- 헤더 고정 -->
              <tr class="border-b border-gray-100">
                <th class="px-3 py-2.5 text-[10px] font-bold text-gray-400 uppercase tracking-wider">재료번호</th>
                <th class="px-3 py-2.5 text-[10px] font-bold text-gray-400 uppercase tracking-wider">제품명</th>
                <th class="px-3 py-2.5 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-right">소요량</th>
                <th class="px-3 py-2.5 text-[10px] font-bold text-gray-400 uppercase tracking-wider">단위</th>
              </tr>
              </thead>
              <tbody class="divide-y divide-gray-50">
              <tr v-for="(item, idx) in selectedMenu?.menuItemList" :key="idx"
                  class="hover:bg-gray-50/80 transition-colors">
                <td class="px-3 py-3 font-mono text-xs text-gray-400">R-{{ String(idx + 1).padStart(3, '0') }}</td>
                <td class="px-3 py-3 font-semibold text-gray-800">{{ item.productName }}</td>
                <td class="px-3 py-3 text-right text-gray-700 font-mono">{{ item.quantity }}</td>
                <td class="px-3 py-3 text-gray-500">{{ item.menuUnit }}</td>
              </tr>
              <tr v-if="!selectedMenu?.menuItemList?.length">
                <td colspan="4" class="px-3 py-8 text-center text-gray-400 text-sm">등록된 재료가 없습니다.</td>
              </tr>
              </tbody>
            </table>
          </div>
          <div class="mt-5 flex justify-end">
            <button @click="showIngredientModal = false" class="px-5 py-2.5 rounded-lg bg-gray-100 text-gray-600 text-sm font-bold hover:bg-gray-200 transition-colors cursor-pointer">닫기
            </button>
          </div>
        </div>
      </div>
    </div>
    <ConfirmModal
      :open="showDeleteConfirm"
      title="메뉴를 삭제하시겠습니까?"
      message="이 작업은 되돌릴 수 없습니다."
      confirm-text="삭제"
      type="danger"
      @close="showDeleteConfirm = false"
      @confirm="confirmDelete" />
    <div v-if="showCategoryModal" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="absolute inset-0 bg-black/40" @click="showCategoryModal = false"></div>
      <div class="relative bg-white rounded-lg w-full max-w-md border border-gray-200 shadow-xl">
        <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
          <h3 class="font-bold text-gray-900">카테고리 관리</h3>
          <button @click="showCategoryModal = false" class="text-gray-400 hover:text-gray-600 cursor-pointer">✕</button>
        </div>
        <div class="p-6 space-y-4">
          <div class="flex gap-2">
            <input v-model="newCategoryInput" type="text" placeholder="새 카테고리명 입력" @keyup.enter="addCategoryAction"
                   class="flex-1 px-3 py-2 rounded border border-gray-200 text-sm focus:border-[#2563eb] focus:ring-2 focus:ring-[#2563eb]/10 outline-none" />
            <button @click="addCategoryAction" class="px-4 py-2 bg-[#2563eb] text-white text-sm font-semibold rounded hover:bg-[#e0661d] cursor-pointer">추가</button>
          </div>
          <div class="border border-gray-200 rounded-lg overflow-hidden">
            <div class="px-4 py-2.5 bg-gray-50 border-b border-gray-100">
              <p class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">카테고리 목록 ({{ categories.length }}개)</p>
            </div>
            <div class="divide-y divide-gray-100 max-h-64 overflow-y-auto">
              <div v-for="cat in categories" :key="cat.categoryIdx"
                   class="flex items-center justify-between px-4 py-2.5 hover:bg-gray-50">
                <span class="text-sm font-medium text-gray-700">{{ cat.menuCategoryName }}</span>
                <button @click="openDeleteCategoryConfirm(cat.categoryIdx, cat.menuCategoryName)" class="text-gray-300 hover:text-red-500 transition-colors cursor-pointer">
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
              <div v-if="categories.length === 0" class="px-4 py-6 text-center text-gray-400 text-sm">등록된 카테고리가 없습니다.</div>
            </div>
          </div>
          <button @click="showCategoryModal = false" class="w-full py-2.5 rounded border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 cursor-pointer">닫기</button>
        </div>
      </div>
    </div>
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
          ? 'bg-[#2563eb] text-white'
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
  </div>
</template>
