import Vue from 'vue'
import VueRouter from 'vue-router'
// 导入所有已创建的组件
import AuthView from '../views/LoginView.vue'
import HomeView from '../views/HomeView.vue'
import TicketBuy from '../views/TicketBuy.vue'
import OrderList from '../views/OrderList.vue'
import OrderPay from '../views/OrderPay.vue'
import AdminView from '../views/AdminView.vue'
import ProfileView from '../views/ProfileView.vue'

Vue.use(VueRouter)

const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalPush.call(this, location, onResolve, onReject)
  }
  return originalPush.call(this, location).catch(error => {
    if (error && error.name === 'NavigationDuplicated') {
      return error
    }
    throw error
  })
}

const originalReplace = VueRouter.prototype.replace
VueRouter.prototype.replace = function replace(location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalReplace.call(this, location, onResolve, onReject)
  }
  return originalReplace.call(this, location).catch(error => {
    if (error && error.name === 'NavigationDuplicated') {
      return error
    }
    throw error
  })
}

const routes = [
  // 修复1：默认重定向到首页（而非登录页），更符合用户习惯
  {
    path: '/',
    redirect: '/home'
  },
  // 登录/注册一体化页面
  {
    path: '/auth',
    name: 'AuthView',
    component: AuthView
  },
  // 首页（演唱会列表，需要登录）
  {
    path: '/home',
    name: 'HomeView',
    component: HomeView,
    meta: { requireAuth: true }
  },
  // 购票页面（参数名保持id，无需修改）
  {
    path: '/buy/:id',
    name: 'TicketBuy',
    component: TicketBuy,
    meta: { requireAuth: true }
  },
  // 修复2：订单列表路径改为 /order/list，匹配前端跳转逻辑
  {
    path: '/order/list',
    name: 'OrderList',
    component: OrderList,
    meta: { requireAuth: true }
  },
  // 订单支付页面（支付订单，需要登录）
  {
    path: '/pay/:orderNo',
    name: 'OrderPay',
    component: OrderPay,
    meta: { requireAuth: true }
  },
  // 个人中心
  {
    path: '/profile',
    name: 'ProfileView',
    component: ProfileView,
    meta: { requireAuth: true }
  },
  // 管理后台（需要管理员权限）
  {
    path: '/admin',
    name: 'AdminView',
    component: AdminView,
    meta: { requireAuth: true }
  },
  // 修复3：404页面重定向到首页（而非登录页），避免误跳
  {
    path: '*',
    redirect: '/home'
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫：优化提示时机和逻辑
router.beforeEach((to, from, next) => {
  if (to.meta.requireAuth) {
    const userId = localStorage.getItem('userId')
    if (userId) {
      next()
    } else {
      // 修复4：先提示，再跳登录（确保$message已初始化）
      Vue.nextTick(() => {
        Vue.prototype.$message?.warning('请先登录后再访问！')
      })
      next('/auth')
    }
  } else {
    next()
  }
})

router.afterEach(() => {
  window.history.scrollRestoration = 'manual'
})

export default router