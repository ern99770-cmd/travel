import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'

// 配置 marked 使用 highlight.js
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

export default {
    namespaced: true,
    state: {
        list: []
    },
    mutations: {
        ADD_USER_MSG(state, msg) {
            state.list.push({
                role: "user",
                content: msg,
                status: 2
            })
        },
        ADD_AI_MSG(state, { content, status }) {
            let runMsg = state.list.find(i => i.status !== 2)
            if (!runMsg) {
                state.list.push({
                    role: "assistant",
                    content: marked.parse(content), // 初始内容渲染
                    rawContent: content, // 保存原始内容
                    status: status
                })
            } else {
                runMsg.rawContent += content
                runMsg.content = marked.parse(runMsg.rawContent) // 实时渲染
                runMsg.status = status
            }
        },
        CLEAR_MESSAGES(state) {
            state.list = []
        }
    },
    actions: {
        userAddMsg({ commit }, msg) {
            commit('ADD_USER_MSG', msg)
        },
        aiAddMsg({ commit }, { content, status }) {
            commit('ADD_AI_MSG', { content, status })
        }
    }
} 