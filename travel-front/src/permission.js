import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken,removeToken } from '@/utils/token'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register','/forget']

router.beforeEach((to, from, next) => {
    NProgress.start()
    if (getToken()) {
        if (whiteList.indexOf(to.path) !== -1) {
            next({ path: '/' })
        } else {
            next()
        }
    } else if (whiteList.indexOf(to.path) !== -1) {
        next()
    } else {
        next({ path: '/login' })
    }
})

router.afterEach(() => {
    NProgress.done()
})