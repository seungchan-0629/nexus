/**
 * 금액을 한국식 단위(만/억/조)로 자동 변환.
 * 금액이 클수록 큰 단위로, 작으면 그대로 표시.
 *
 * 예시:
 *   5,000              → "₩ 5,000"
 *   1,234,500          → "₩ 123.5만"
 *   211,300,000        → "₩ 2.1억"
 *   21,127,807,506     → "₩ 211.3억"
 *   1,234,500,000,000  → "₩ 1.23조"
 *
 * @param {number} amount - 금액
 * @param {object} opts
 *   - withSymbol: ₩ 기호 표시 여부 (기본 true)
 *   - decimals: 만/억 단위 소수점 자릿수 (기본 1)
 * @returns {string} 포맷된 문자열
 */
export function formatCurrency(amount, opts = {}) {
  const { withSymbol = true, decimals = 1 } = opts
  const symbol = withSymbol ? '₩ ' : ''

  if (amount == null) return `${symbol}0`

  const num = Number(amount)
  if (Number.isNaN(num)) return `${symbol}0`

  const absAmount = Math.abs(num)
  const sign = num < 0 ? '-' : ''

  // 1조 이상
  if (absAmount >= 1_000_000_000_000) {
    return `${sign}${symbol}${(absAmount / 1_000_000_000_000).toFixed(2)}조`
  }
  // 1억 이상
  if (absAmount >= 100_000_000) {
    return `${sign}${symbol}${(absAmount / 100_000_000).toFixed(decimals)}억`
  }
  // 1만 이상
  if (absAmount >= 10_000) {
    return `${sign}${symbol}${(absAmount / 10_000).toFixed(decimals)}만`
  }
  // 1만 미만 — 그대로
  return `${sign}${symbol}${absAmount.toLocaleString('ko-KR')}`
}

/**
 * 차트 축/툴팁용 짧은 포맷 (₩ 기호 없음).
 *
 * @param {number} amount
 * @returns {string}
 */
export function formatCurrencyShort(amount) {
  return formatCurrency(amount, { withSymbol: false, decimals: 1 })
}

/**
 * 툴팁용 (₩ 기호 + 단위 변환).
 * 차트 tooltip 콜백에서 사용.
 *
 * @param {number} amount
 * @returns {string}
 */
export function formatCurrencyTooltip(amount) {
  return formatCurrency(amount, { withSymbol: true, decimals: 1 })
}

/**
 * 전년/전월 대비 변동률 계산 (YoY / MoM).
 * 양수면 증가, 음수면 감소.
 *
 * @param {number} current - 현재 값
 * @param {number} previous - 비교 대상 값
 * @returns {object|null}
 *   - percent: 변동률 (소수점 1자리)
 *   - isPositive: 증가 여부
 *   - text: "+18.2%" 또는 "-5.4%"
 *   - 데이터 부족 시 null
 */
export function calculateChange(current, previous) {
  if (current == null || previous == null || previous === 0) return null

  const percent = ((current - previous) / previous) * 100
  const isPositive = percent >= 0
  const sign = isPositive ? '+' : ''
  return {
    percent: Math.round(percent * 10) / 10,
    isPositive,
    text: `${sign}${percent.toFixed(1)}%`,
  }
}
