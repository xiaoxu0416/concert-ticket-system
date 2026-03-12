import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import axios from 'axios'

Vue.config.productionTip = false
Vue.use(ElementUI)

// Axios全局配置
axios.defaults.baseURL = '/api' 
// 修复1：延长超时时间到15秒（适配抢票的异步操作）
axios.defaults.timeout = 15000 
axios.defaults.withCredentials = true 

// 请求拦截器
axios.interceptors.request.use(config => {
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器优化
axios.interceptors.response.use(response => {
  return response
}, error => {
  // 修复2：区分不同错误类型，避免误提示
  if (error.response) {
    // 有响应的错误（后端返回状态码）
    if (error.response.status === 401) {
      if (router.currentRoute.path !== '/auth') {
        Vue.prototype.$message.error('登录已过期，请重新登录！');
        localStorage.clear();
        router.push('/auth');
      }
    } else {
      // 非401的业务错误（如400/500）
      const errorMsg = error.response.data?.msg || '请求失败';
      Vue.prototype.$message.error(errorMsg);
    }
  } else if (error.request) {
    // 无响应的错误（超时/网络问题）
    Vue.prototype.$message.error('网络超时，请检查网络或稍后重试');
  } else {
    // 其他错误
    Vue.prototype.$message.error('请求异常：' + error.message);
  }
  return Promise.reject(error);
})

Vue.prototype.$axios = axios

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')