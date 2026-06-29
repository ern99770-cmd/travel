import { Notification } from 'element-ui'
import eventBus from '@/utils/eventBus'

/**
 * 展示积分获得提示，并通知各页面刷新积分余额
 */
export function showPointsEarned(points, title) {
  const value = Number(points)
  if (!value || value <= 0) return
  Notification({
    title: title || '积分奖励',
    message: `+${value} 积分`,
    type: 'success',
    duration: 3000,
    position: 'top-right',
    customClass: 'points-earned-notification'
  })
  eventBus.$emit('points-earned', value)
}

/**
 * 从接口响应中提取 pointsEarned
 */
export function extractPointsEarned(res) {
  if (!res || res.code !== 1000 || !res.data) return 0
  const earned = res.data.pointsEarned
  if (earned === undefined || earned === null) return 0
  const value = Number(earned)
  return value > 0 ? value : 0
}

/**
 * 预约类响应：提取订单对象
 */
export function extractOrderFromRes(res) {
  if (!res || !res.data) return res && res.data
  return res.data.order || res.data
}
