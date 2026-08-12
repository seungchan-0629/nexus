/** POS 등에서 공통 사용 */
export function formatPrice(price) {
  return '₩ ' + Number(price ?? 0).toLocaleString()
}
