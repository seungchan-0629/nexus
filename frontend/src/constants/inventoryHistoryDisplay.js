/**
 * 입출고 이력 화면에서 쓰는 표시 유형·스타일 맵.
 * API movementType → 한글 라벨은 InventoryHistoryView에서 함께 사용.
 */
export const InventoryHistoryDisplayType = Object.freeze({
  INBOUND: '입고',
  OUTBOUND: '출고',
  ADJUST: '보정',
  OTHER: '기타',
})

/** 백엔드 MovementType 문자열 → 화면 라벨 */
export const ApiMovementTypeToDisplay = Object.freeze({
  INBOUND: InventoryHistoryDisplayType.INBOUND,
  TRANSFER_OUT: InventoryHistoryDisplayType.OUTBOUND,
})

export const QTY_PREFIX_BY_DISPLAY_TYPE = Object.freeze({
  [InventoryHistoryDisplayType.INBOUND]: '+',
  [InventoryHistoryDisplayType.OUTBOUND]: '-',
  [InventoryHistoryDisplayType.ADJUST]: '±',
})

export const QTY_CLASS_BY_DISPLAY_TYPE = Object.freeze({
  [InventoryHistoryDisplayType.INBOUND]: 'text-slate-700',
  [InventoryHistoryDisplayType.OUTBOUND]: 'text-orange-950',
  [InventoryHistoryDisplayType.ADJUST]: 'text-stone-600',
})

export const BADGE_CLASS_BY_DISPLAY_TYPE = Object.freeze({
  [InventoryHistoryDisplayType.INBOUND]: 'bg-slate-100 text-slate-700',
  [InventoryHistoryDisplayType.OUTBOUND]: 'bg-orange-50/90 text-orange-950',
  [InventoryHistoryDisplayType.ADJUST]: 'bg-stone-100 text-stone-700',
})

const QTY_PREFIX_DEFAULT = QTY_PREFIX_BY_DISPLAY_TYPE[InventoryHistoryDisplayType.ADJUST]
const QTY_CLASS_DEFAULT = 'text-stone-600'
const BADGE_CLASS_DEFAULT = 'bg-stone-50 text-stone-600'

export function resolveDisplayTypeLabel(apiMovementType) {
  return ApiMovementTypeToDisplay[apiMovementType] ?? InventoryHistoryDisplayType.OTHER
}

export function qtyPrefixForDisplayType(displayType) {
  return QTY_PREFIX_BY_DISPLAY_TYPE[displayType] ?? QTY_PREFIX_DEFAULT
}

export function qtyClassForDisplayType(displayType) {
  return QTY_CLASS_BY_DISPLAY_TYPE[displayType] ?? QTY_CLASS_DEFAULT
}

export function badgeClassForDisplayType(displayType) {
  return BADGE_CLASS_BY_DISPLAY_TYPE[displayType] ?? BADGE_CLASS_DEFAULT
}

/** 무한 스크롤(점진적 렌더) 배치 크기 */
export const INVENTORY_HISTORY_PAGE_SIZE = 60
