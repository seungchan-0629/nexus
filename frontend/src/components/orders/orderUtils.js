export function formatPrice(n) {
  return '₩ ' + n.toLocaleString('ko-KR')
}

export function statusClass(status) {
  const map = {
    '제안중':   'bg-blue-50 text-blue-600 border border-blue-200',
    '검토필요': 'bg-rose-50 text-rose-700 border border-rose-200',
    '확정':     'bg-green-50 text-green-700 border border-green-200',
    '승인':     'bg-green-50 text-green-600 border border-green-200',
    '거절':     'bg-red-50 text-red-500 border border-red-200',
    '취소':     'bg-amber-50 text-amber-600 border border-amber-200',
    '배송중':   'bg-orange-50 text-orange-600 border border-orange-200',
    '입고완료': 'bg-emerald-50 text-emerald-700 border border-emerald-200',
  }
  return map[status] || 'bg-gray-100 text-gray-500 border border-gray-200'
}

export const PRODUCT_UNIT = {
  '한우 등심':   'kg',
  '올리브오일':  'L',
  '버터':        'kg',
  '생크림':      'L',
  '간장':        'L',
  '양파':        'kg',
  '생수':        '박스',
}

export const productPrices = {
  '한우 등심':   85000,
  '올리브오일':  12000,
  '버터':        9000,
  '생크림':      7000,
  '간장':        4000,
  '양파':        1500,
  '생수':        8000,
}

export const ORDER_STATUS_LABEL = {
  WAITING: '제안중',
  CONFIRMED: '확정',
  APPROVE: '승인',
  REJECT: '거절',
  CANCELLED: '취소',
}

export const ORDER_TYPE_LABEL = {
  AUTO: '자동',
  MANUAL: '수동',
}

export function storeStatusClass(status) {
  const map = {
    WAITING:   'bg-blue-50 text-blue-600 border border-blue-200',
    CONFIRMED: 'bg-green-50 text-green-700 border border-green-200',
    APPROVE:   'bg-blue-50 text-blue-600 border border-blue-200',
    REJECT:    'bg-red-50 text-red-600 border border-red-200',
    CANCELLED: 'bg-red-50 text-red-500 border border-red-200',
  }
  return map[status] || 'bg-gray-100 text-gray-500 border border-gray-200'
}