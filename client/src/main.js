import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import permissionDirective from './directives/permission'
import './assets/style.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(permissionDirective) // 注册权限指令
app.mount('#app')
