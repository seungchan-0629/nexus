export function useInventoryCommon() {
  const parseExpiryDate = (expiry) => {
    if (!expiry) return null
    const date = new Date(expiry)
    return Number.isNaN(date.getTime()) ? null : date
  }

  const totalStock = (item) => {
    if (typeof item?.count === 'number') return item.count
    return (item?.lots ?? []).reduce((sum, lot) => sum + (lot.qty ?? 0), 0)
  }

  const stockGap = (item, minKey = 'safe') => {
    const minStock = Number(item?.[minKey] ?? 0)
    return totalStock(item) - (Number.isFinite(minStock) ? minStock : 0)
  }

  const isLowStock = (item, minKey = 'safe') => {
    return stockGap(item, minKey) < 0
  }

  const fifoLots = (item) => {
    const lots = [...(item?.lots ?? [])]
    return lots.sort((a, b) => {
      const dateA = parseExpiryDate(a.expiry)
      const dateB = parseExpiryDate(b.expiry)
      if (!dateA && !dateB) return 0
      if (!dateA) return 1
      if (!dateB) return -1
      return dateA - dateB
    })
  }

  const isExpiringSoon = (expiry) => {
    const date = parseExpiryDate(expiry)
    if (!date) return false
    const diff = (date - new Date()) / (1000 * 60 * 60 * 24)
    return diff >= 0 && diff <= 7
  }

  const hasExpiringSoonLot = (item) => {
    return (item?.lots ?? []).some((lot) => isExpiringSoon(lot.expiry))
  }

  return {
    parseExpiryDate,
    totalStock,
    stockGap,
    isLowStock,
    fifoLots,
    isExpiringSoon,
    hasExpiringSoonLot,
  }
}
