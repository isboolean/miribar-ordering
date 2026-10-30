import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/customer'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/customer',
    name: 'Menu',
    component: () => import('../views/customer/MenuView.vue')
  },
  {
    path: '/customer/orders',
    name: 'OrderStatus',
    component: () => import('../views/customer/OrderStatusView.vue')
  },
  {
    path: '/kitchen',
    name: 'Kitchen',
    component: () => import('../views/kitchen/KitchenView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/admin/AdminView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  // 角色管理（独立页面，用于测试）
  {
    path: '/admin/role',
    name: 'RoleManage',
    component: () => import('../views/admin/role/RoleView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  // 菜单管理（独立页面，用于测试）
  {
    path: '/admin/menu',
    name: 'MenuManage',
    component: () => import('../views/admin/menu/MenuView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  // 组织管理（独立页面）
  {
    path: '/admin/org',
    name: 'OrgManage',
    component: () => import('../views/admin/org/OrgView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  // 字典管理（独立页面）
  {
    path: '/admin/dict',
    name: 'DictManage',
    component: () => import('../views/admin/dict/DictView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('miribar_token')
  if (to.meta.requiresAuth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin) {
    try {
      const staff = JSON.parse(localStorage.getItem('miribar_staff'))
      if (!staff || ![2, 3].includes(staff.role)) {
        return { path: '/login', query: { redirect: to.fullPath } }
      }
    } catch {
      return { path: '/login' }
    }
  }
})

export default router
