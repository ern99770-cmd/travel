import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
import { Message } from 'element-ui'
import router from '@/router'
import { aiChatStream } from '@/utils/aiStream'
import { showPointsEarned } from '@/utils/pointsToast'

marked.use({
    renderer: {
        code(code, lang) {
            const language = (lang && hljs.getLanguage(lang)) ? lang : 'plaintext';
            const highlighted = hljs.highlight(code, { language }).value;
            return `
                <div class="code-block">
                    <div class="code-header">
                        <button class="copy-button" onclick="copyCode(this)">
                            <i class="el-icon-document-copy"></i>
                        </button>
                    </div>
                    <pre><code class="hljs language-${language}">${highlighted}</code></pre>
                </div>
            `;
        }
    },
    breaks: true,
    gfm: true
})

let currentAbortController = null

function abortCurrentStream() {
    if (currentAbortController) {
        currentAbortController.abort()
        currentAbortController = null
    }
}

export default {
    namespaced: true,
    state: {
        list: [],
        chatLoading: false
    },
    mutations: {
        SET_CHAT_LOADING(state, loading) {
            state.chatLoading = loading
        },
        ADD_USER_MSG(state, msg) {
            state.list.push({
                role: "user",
                content: msg,
                status: 2
            })
        },
        ADD_AI_MSG(state, { content, status }) {
            let runMsg = state.list.find(i => i.role === 'assistant' && i.status !== 2)
            if (!runMsg) {
                state.list.push({
                    role: "assistant",
                    content: content,
                    rawContent: content,
                    status: status,
                    streaming: status !== 2
                })
            } else {
                runMsg.rawContent += content
                runMsg.status = status
                runMsg.streaming = status !== 2
                if (status === 2) {
                    runMsg.content = marked.parse(runMsg.rawContent)
                } else {
                    runMsg.content = runMsg.rawContent
                }
            }
        },
        CLEAR_MESSAGES(state) {
            state.list = []
            state.chatLoading = false
        }
    },
    actions: {
        userAddMsg({ commit }, msg) {
            commit('ADD_USER_MSG', msg)
        },
        aiAddMsg({ commit }, payload) {
            commit('ADD_AI_MSG', payload)
        },
        sendAiStream({ commit, state }, { question, history }) {
            if (state.chatLoading) {
                return Promise.reject(new Error('AI 正在回复中，请稍候'))
            }

            abortCurrentStream()
            currentAbortController = new AbortController()
            const signal = currentAbortController.signal

            commit('SET_CHAT_LOADING', true)

            return aiChatStream(
                { question, history },
                (payload) => {
                    if (payload && payload.event === 'points') {
                        showPointsEarned(payload.pointsEarned, 'AI 对话奖励')
                        return
                    }
                    commit('ADD_AI_MSG', {
                        content: payload.content || '',
                        status: payload.status
                    })
                },
                (error) => {
                    if (signal.aborted) {
                        return
                    }
                    Message.error(error.message || 'AI 回复失败')
                },
                signal
            ).then(() => {
                if (!signal.aborted && router.currentRoute.path !== '/ai') {
                    Message({
                        message: 'AI 回复已完成，可返回 AI 规划页查看',
                        type: 'success',
                        duration: 3000
                    })
                }
            }).catch((error) => {
                if (error.name === 'AbortError' || signal.aborted) {
                    return
                }
                if (error.code === 1011) {
                    Message.warning('登录已过期，请重新登录')
                    router.push('/login')
                    return
                }
                Message.error(error.message || '发送失败，请重试')
            }).finally(() => {
                if (currentAbortController && currentAbortController.signal === signal) {
                    currentAbortController = null
                }
                commit('SET_CHAT_LOADING', false)
            })
        },
        clearMessages({ commit }) {
            abortCurrentStream()
            commit('CLEAR_MESSAGES')
        }
    }
}
