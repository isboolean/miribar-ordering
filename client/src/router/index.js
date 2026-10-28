import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/customer'
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
    component: () => import('../views/kitchen/KitchenView.vue')
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/admin/AdminView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
