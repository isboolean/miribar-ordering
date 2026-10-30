/** 格式化工具 */
function formatPrice(n) {
  const v = Number(n) || 0
  return '¥' + v.toFixed(2)
}

/** 订单状态映射（与后端 t_order.status 一致：0-待接单 1-制作中 2-已完成 3-已拒单 4-已取消 5-已结账） */
const ORDER_STATUS = {
  0: { text: '待接单', cls: 'st-pending' },
  1: { text: '制作中', cls: 'st-making' },
  2: { text: '已完成', cls: 'st-done' },
  3: { text: '已拒单', cls: 'st-reject' },
  4: { text: '已取消', cls: 'st-cancel' },
  5: { text: '已结账', cls: 'st-done' }
}

function orderStatusInfo(status) {
  return ORDER_STATUS[status] || { text: '未知', cls: '' }
}

function formatTime(ts) {
  if (!ts) return ''
  // 兼容 "2026-10-30 12:00:00" 与时间戳
  const d = typeof ts === 'number' ? new Date(ts) : new Date(String(ts).replace(/-/g, '/'))
  if (isNaN(d.getTime())) return String(ts)
  const p = (x) => String(x).padStart(2, '0')
  return `${p(d.getHours())}:${p(d.getMinutes())}`
}

module.exports = { formatPrice, orderStatusInfo, formatTime, ORDER_STATUS }
