import { useAuthStore } from '@/stores/auth'

/**
 * 权限指令 v-permission
 * 使用示例：<button v-permission="'staff:add'">新增</button>
 */
export const permissionDirective = {
  mounted(el, binding) {
    const { value } = binding
    const authStore = useAuthStore()
    
    if (value && typeof value === 'string') {
      if (!authStore.hasPermission(value)) {
        el.parentNode?.removeChild(el)
      }
    } else if (Array.isArray(value)) {
      // 支持数组形式，满足任一权限即可
      const hasAny = value.some(perm => authStore.hasPermission(perm))
      if (!hasAny) {
        el.parentNode?.removeChild(el)
      }
    }
  }
}

/**
 * 权限检查函数
 * @param {string|string[]} permission 权限编码或权限编码数组
 * @param {boolean} requireAll 数组时是否需要满足所有权限
 * @returns {boolean}
 */
export function checkPermission(permission, requireAll = false) {
  const authStore = useAuthStore()
  
  if (typeof permission === 'string') {
    return authStore.hasPermission(permission)
  } else if (Array.isArray(permission)) {
    if (requireAll) {
      return permission.every(perm => authStore.hasPermission(perm))
    }
    return permission.some(perm => authStore.hasPermission(perm))
  }
  
  return false
}

export default {
  install(app) {
    app.directive('permission', permissionDirective)
  }
}
