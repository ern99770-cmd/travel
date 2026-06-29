import axios from 'axios'
import store from '@/store'
import { Message, MessageBox } from 'element-ui'  //导入element-ui组件库
 
// 创建axios的对象
const instance = axios.create({
    baseURL: process.env.NODE_ENV === 'production' 
        ? "http://localhost:8080"  // 生产环境使用实际URL
        : "/api",  // 开发环境使用代理
    timeout: 5000
})
 
// 请求拦截
// 所有的网络请求都会走这个方法,可以在请求添加自定义内容
instance.interceptors.request.use(
    function (config) {
        if (config.method === 'post') {
            config.data = {
              ...config.data,
              _t: Date.parse(new Date()) / 1000 // 时间戳
            }
        } else if (config.method === 'get') {
            config.params = {
              _t: Date.now(), // 使用更精确的时间戳
              ...config.params
            }
        }
        // 添加防止缓存的请求头
        config.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate'
        config.headers['Pragma'] = 'no-cache'
        config.headers['Expires'] = '0'
        config.headers.x_access_token = window.localStorage.getItem("user_token") // 请求头添加token值
        return config
    },
    function (err) {
        return Promise.reject(err) // 修正为正确的Promise.reject
    }
)
 
// 响应拦截
// 此处可以根据服务器返回的状态码做相应的数据
instance.interceptors.response.use(
    function (response) {
        const res = response
        if (res.status === 1011) {
            MessageBox.alert('系统登陆已过期，请重新登录', '错误', {
                confirmButtonText: '确定',
                type: 'error'
            }).then(() => {
                store.dispatch('logout').then(() => {
                    window.localStorage.removeItem("user_token")
                    window.localStorage.removeItem("user_info")
                    location.reload()
                })
            })
            return Promise.reject('error')
        } else if(res.data.code == 1009) {
            MessageBox.alert('该账号已被锁定', '错误', {
                confirmButtonText: '确定',
                type: 'error'
            }).then(() => {
                store.dispatch('logout').then(() => {
                    window.localStorage.removeItem("user_token")
                    window.localStorage.removeItem("user_info")
                    location.reload()
                })
            })
            return Promise.reject('error')
        } else if (res.status != 200) {
            MessageBox.alert('系统内部错误，请联系管理员维护', '错误', {
                confirmButtonText: '确定',
                type: 'error'
            }).then(() => {
                store.dispatch('logout').then(() => {
                    window.localStorage.removeItem("user_token")
                    window.localStorage.removeItem("user_info")
                    location.reload()
                })
            })
            return Promise.reject('error')
        } else {
            return res.data
        }
    },
    function (err) {
        const status = err.response && err.response.status

        if (status === 1011) {
            MessageBox.alert('系统登陆已过期，请重新登录', '错误', {
                confirmButtonText: '确定',
                type: 'error'
            }).then(() => {
                store.dispatch('logout').then(() => {
                    window.localStorage.removeItem("user_token")
                    window.localStorage.removeItem("user_info")
                    location.reload()
                })
            })
            return Promise.reject(err)
        }

        if (err.code === 'ECONNABORTED' || (err.message && err.message.indexOf('timeout') !== -1)) {
            Message({
                message: '请求超时，请稍后重试',
                type: 'warning',
                duration: 3000
            })
            return Promise.reject(err)
        }

        if (!err.response) {
            Message({
                message: '网络异常，请检查后端服务是否已启动',
                type: 'error',
                duration: 3000
            })
            return Promise.reject(err)
        }

        Message({
            message: '系统内部错误，请联系管理员维护',
            type: 'error',
            duration: 3000
        })
        return Promise.reject(err)
    }
)
 
// 封装get和post请求
export function get(url, params) {
    return instance.get(url, {params})
}
 
export function post(url, data, config) {
    return instance.post(url, data, config)
}
 
export default instance;