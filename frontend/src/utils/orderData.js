const categories = [
  '한우·정육', '수산물', '채소·신선', '양념·소스', '유제품', '음료·기타', '소모품',
]

const productsByCategory = {
  '한우·정육': ['한우 등심 1kg', '한우 안심 1kg', '한우 채끝 1kg', '한우 갈비 1kg'],
  '수산물': ['연어 필렛 1kg', '참치 블록 1kg', '새우 500g', '광어 1kg'],
  '채소·신선': ['양파 1kg', '마늘 500g', '대파 1kg', '버섯 모둠 500g'],
  '양념·소스': ['간장 1.8L', '고추장 500g', '된장 500g', '올리브오일 1L'],
  '유제품': ['버터 500g', '생크림 1L', '치즈 슬라이스 200g', '우유 1L'],
  '음료·기타': ['콜라 500ml(24)', '생수 500ml(20)', '녹차 티백 50개', '에스프레소 원두 1kg'],
  '소모품': ['위생장갑 100매', '랩 500m', '키친타월 4롤', '포장용기 50개'],
}

const categoryWeights = [
  ['한우·정육', 28], ['수산물', 22], ['채소·신선', 16],
  ['양념·소스', 12], ['유제품', 10], ['음료·기타', 7], ['소모품', 5],
]
const weightTotal = categoryWeights.reduce((s, [, w]) => s + w, 0)

const storeWeights = [24, 22, 20, 18, 16]
const storeWeightTotal = storeWeights.reduce((s, w) => s + w, 0)
const stores = [
  '한우 오마카세', '이탈리안 키친', '일식 스시바', '차이나 가든', '프렌치 비스트로',
]

function mulberry32(seed) {
  return function () {
    let t = (seed += 0x6d2b79f5)
    t = Math.imul(t ^ (t >>> 15), t | 1)
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61)
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

function pickCategory(rand) {
  let r = rand() * weightTotal
  for (const [c, w] of categoryWeights) {
    if (r < w) return c
    r -= w
  }
  return categoryWeights[categoryWeights.length - 1][0]
}

function pickStore(rand) {
  let r = rand() * storeWeightTotal
  for (let i = 0; i < stores.length; i++) {
    if (r < storeWeights[i]) return stores[i]
    r -= storeWeights[i]
  }
  return stores[stores.length - 1]
}

function qtyForCategory(cat, rand) {
  if (cat === '소모품') return 50 + Math.floor(rand() * 200)
  if (cat === '음료·기타') return 10 + Math.floor(rand() * 50)
  if (cat === '한우·정육') return 5 + Math.floor(rand() * 30)
  if (cat === '수산물') return 5 + Math.floor(rand() * 25)
  if (cat === '채소·신선') return 5 + Math.floor(rand() * 40)
  if (cat === '양념·소스') return 3 + Math.floor(rand() * 20)
  return 2 + Math.floor(rand() * 12)
}

function unitPriceFor(cat, rand) {
  if (cat === '한우·정육') return 45000 + Math.floor(rand() * 35000)
  if (cat === '수산물') return 18000 + Math.floor(rand() * 22000)
  if (cat === '채소·신선') return 2000 + Math.floor(rand() * 3000)
  if (cat === '양념·소스') return 4000 + Math.floor(rand() * 6000)
  if (cat === '유제품') return 3500 + Math.floor(rand() * 4500)
  if (cat === '음료·기타') return 800 + Math.floor(rand() * 1200)
  return 500 + Math.floor(rand() * 1500)
}

function buildRawOrders() {
  const rand = mulberry32(20260422)
  const list = []
  const anchor = new Date('2026-04-21T12:00:00')
  for (let day = 0; day < 200; day++) {
    const d = new Date(anchor)
    d.setDate(anchor.getDate() - day)
    const dateStr = d.toISOString().slice(0, 10)
    const dow = d.getDay()
    const isPeak = dow === 5 || dow === 6 || dow === 0
    const isHolidayLike = day % 21 === 0
    let linesPerDay = 10 + Math.floor(rand() * 14)
    if (isPeak) linesPerDay += 8 + Math.floor(rand() * 16)
    if (isHolidayLike) linesPerDay += 12 + Math.floor(rand() * 20)
    linesPerDay = Math.min(linesPerDay, 52)
    for (let i = 0; i < linesPerDay; i++) {
      const cat = pickCategory(rand)
      const names = productsByCategory[cat]
      const product = names[Math.floor(rand() * names.length)]
      const store = pickStore(rand)
      let qty = qtyForCategory(cat, rand)
      if (isPeak && (cat === '한우·정육' || cat === '수산물')) qty += Math.floor(rand() * 10)
      const unitPrice = unitPriceFor(cat, rand)
      const amount = qty * unitPrice
      let abnormalProb = 0.048
      if (cat === '한우·정육') abnormalProb += 0.055
      if (cat === '수산물' && qty > 15) abnormalProb += 0.028
      if (qty > 100) abnormalProb += 0.035
      if (isHolidayLike && rand() < 0.15) abnormalProb += 0.04
      const abnormal = rand() < abnormalProb ? 1 : 0
      list.push({ date: dateStr, category: cat, product, store, amount, qty, abnormal })
    }
  }
  return list
}

export const rawOrders = buildRawOrders()
export { categories, stores }
