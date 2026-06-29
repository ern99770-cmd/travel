import { Message, Notification } from 'element-ui'
import router from '@/router'
import { generateAiPlan } from '@/api/api'

const PLAN_TEXTS = [
    '正在分析您的出行需求...',
    '正在匹配景点与路线...',
    '正在生成每日行程安排...',
    '即将完成，请稍候...'
]

let planLoadingTimer = null

function startPlanTextAnimation(commit) {
    clearPlanTextAnimation()
    let index = 0
    commit('SET_PLAN_LOADING_TEXT', PLAN_TEXTS[0])
    planLoadingTimer = setInterval(() => {
        index = Math.min(index + 1, PLAN_TEXTS.length - 1)
        commit('SET_PLAN_LOADING_TEXT', PLAN_TEXTS[index])
    }, 2500)
}

function clearPlanTextAnimation() {
    if (planLoadingTimer) {
        clearInterval(planLoadingTimer)
        planLoadingTimer = null
    }
}

export default {
    namespaced: true,
    state: {
        loading: false,
        loadingText: PLAN_TEXTS[0],
        destination: '',
        minimized: false,
        justCompleted: false,
        lastPlanId: ''
    },
    mutations: {
        SET_LOADING(state, loading) {
            state.loading = loading
        },
        SET_PLAN_LOADING_TEXT(state, text) {
            state.loadingText = text
        },
        SET_DESTINATION(state, destination) {
            state.destination = destination
        },
        SET_MINIMIZED(state, minimized) {
            state.minimized = minimized
        },
        SET_JUST_COMPLETED(state, completed) {
            state.justCompleted = completed
        },
        SET_LAST_PLAN_ID(state, id) {
            state.lastPlanId = id || ''
        },
        RESET(state) {
            state.loading = false
            state.loadingText = PLAN_TEXTS[0]
            state.destination = ''
            state.minimized = false
            state.justCompleted = false
            state.lastPlanId = ''
        }
    },
    actions: {
        generatePlan({ commit, state }, formData) {
            if (state.loading) {
                Message.warning('已有行程正在生成中，请稍候')
                return Promise.reject(new Error('plan generating'))
            }

            commit('SET_LOADING', true)
            commit('SET_JUST_COMPLETED', false)
            commit('SET_MINIMIZED', true)
            commit('SET_DESTINATION', formData.destination || '')
            startPlanTextAnimation(commit)

            Message.info('行程正在后台生成，您可以继续浏览其他页面')

            return generateAiPlan(formData).then(res => {
                if (res.code == 1000) {
                    const planId = res.data && res.data.id
                    commit('SET_LAST_PLAN_ID', planId)
                    commit('SET_JUST_COMPLETED', true)
                    commit('SET_MINIMIZED', true)
                    const dest = formData.destination || '行程'
                    Notification({
                        title: '行程生成完成',
                        message: `「${dest}」已保存，点击查看详情`,
                        type: 'success',
                        duration: 6000,
                        onClick: () => {
                            commit('RESET')
                            router.push(planId ? { path: '/myPlan', query: { id: planId } } : { path: '/myPlan' })
                        }
                    })
                    return res
                }
                Message.error(res.message || '生成行程失败')
                return Promise.reject(new Error(res.message || '生成行程失败'))
            }).catch(error => {
                if (error.message !== 'plan generating') {
                    Message.error('生成行程失败，请稍后重试')
                }
                return Promise.reject(error)
            }).finally(() => {
                clearPlanTextAnimation()
                commit('SET_LOADING', false)
            })
        },
        minimize({ commit }) {
            commit('SET_MINIMIZED', true)
        },
        expand({ commit }) {
            commit('SET_MINIMIZED', false)
        },
        dismiss({ commit }) {
            commit('RESET')
        },
        goToMyPlan({ commit, state }) {
            const planId = state.lastPlanId
            commit('RESET')
            router.push(planId ? { path: '/myPlan', query: { id: planId } } : { path: '/myPlan' })
        }
    }
}
