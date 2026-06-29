<template>
    <PageLayout full-height :show-footer="false">
        <div class="ai-chat">
            <div class="travel-decoration airplane">✈️</div>
            <div class="travel-decoration compass">🧭</div>
            <div class="travel-decoration map">🗺️</div>
            <div class="travel-decoration camera">📷</div>
            <div class="chat-container">
                <div class="action-bar">
                    <div class="action-bar-left">
                        <img :src="aiImg" alt="AI" class="action-bar-logo">
                        <div class="action-bar-title">
                            <h3>AI 旅游规划师</h3>
                            <p>智能问答 · 行程规划 · 景点推荐</p>
                        </div>
                    </div>
                    <div class="action-bar-right">
                        <button class="clear-button" @click="clearChat">
                            <i class="el-icon-delete"></i>
                            清除对话
                        </button>
                        <button class="travel-request-button" @click="toggleTravelForm">
                            <i class="el-icon-place"></i>
                            智能行程规划
                        </button>
                        <button class="travel-request-button secondary" @click="$router.push('/myPlan')">
                            <i class="el-icon-notebook-2"></i>
                            我的行程
                        </button>
                    </div>
                </div>
                <div class="message-container" id='message-box'>
                    <div v-if="!messageList.length" class="empty-chat">
                        <img :src="aiImg" alt="AI" class="empty-chat-icon">
                        <h4>你好，我是 AI 旅游规划师</h4>
                        <p>你可以直接提问，例如：</p>
                        <div class="quick-questions">
                            <span @click="askQuick('成都有什么必去景点？')">成都有什么必去景点？</span>
                            <span @click="askQuick('帮我规划一个3天西安旅行')">帮我规划一个3天西安旅行</span>
                            <span @click="askQuick('夏季适合去哪些避暑目的地？')">夏季适合去哪些避暑目的地？</span>
                        </div>
                    </div>
                    <div v-for="(msg,index) in messageList" :key="index" 
                         :class="['message', msg.role === 'user' ? 'message-user' : 'message-assistant']">
                        <div class="message-content">
                            <div class="avatar">
                                <img :src="msg.role === 'user' ? userImg : aiImg" 
                                     :alt="msg.role === 'user' ? '用户' : 'AI'" 
                                     class="avatar-img"/>
                                <span v-if="msg.role === 'assistant'" class="assistant-name">AI旅游规划师</span>
                            </div>
                            <div class="bubble">
                                <div v-if="msg.streaming" class="text streaming-text">{{ msg.rawContent || msg.content }}<span class="stream-cursor"></span></div>
                                <div v-else class="text" v-html="msg.content"></div>
                            </div>
                        </div>
                    </div>
                    <div v-if="isWaitingReply" class="message message-assistant">
                        <div class="message-content">
                            <div class="avatar">
                                <img :src="aiImg" alt="AI" class="avatar-img"/>
                                <span class="assistant-name">AI旅游规划师</span>
                            </div>
                            <div class="bubble typing-bubble">
                                <div class="typing-indicator">
                                    <span></span><span></span><span></span>
                                </div>
                                <span class="typing-text">{{ thinkingText }}</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="input-area">
                    <div class="chat-input">
                        <textarea 
                            class="message-input"
                            rows="3"
                            :placeholder="inputDisabled ? 'AI 正在回复中，请稍候...' : '有关旅游的问题尽管问我吧...'"
                            v-model="msgValue"
                            :disabled="inputDisabled"
                            @keydown.enter.prevent="submitMsg"
                        ></textarea>
                        <button 
                            class="send-button" 
                            :disabled="inputDisabled || !msgValue.trim()" 
                            @click="submitMsg">
                            <i v-if="chatLoading" class="el-icon-loading"></i>
                            {{ chatLoading ? '回复中' : '发送' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 将表单放到外部，防止被遮挡 -->
        <template v-if="showTravelForm">
            <div class="travel-form-overlay" @click.self="toggleTravelForm"></div>
            <div class="travel-form">
                <div class="form-header">
                    <div class="form-title">填写您的旅游需求</div>
                    <button class="close-form-button" @click="toggleTravelForm">
                        <i class="el-icon-close"></i>
                    </button>
                </div>
                <div class="form-group">
                    <label>出发时间：</label>
                    <el-date-picker
                        v-model="travelForm.date"
                        type="date"
                        placeholder="选择日期"
                        :picker-options="pickerOptions">
                    </el-date-picker>
                </div>
                <div class="form-group">
                    <label>目的地：</label>
                    <el-input v-model="travelForm.destination" placeholder="请输入目的地"></el-input>
                </div>
                <div class="form-group">
                    <label>游玩天数：</label>
                    <el-input-number v-model="travelForm.days" :min="1" :max="30"></el-input-number>
                </div>
                <div class="form-group">
                    <label>预算范围：</label>
                    <el-select v-model="travelForm.budget" placeholder="请选择预算范围">
                        <el-option label="经济型（2000以下）" value="经济型"></el-option>
                        <el-option label="舒适型（2000-5000）" value="舒适型"></el-option>
                        <el-option label="豪华型（5000以上）" value="豪华型"></el-option>
                    </el-select>
                </div>
                <div class="form-group">
                    <label>偏好类型：</label>
                    <el-checkbox-group v-model="travelForm.preferences">
                        <el-checkbox label="自然风光">自然风光</el-checkbox>
                        <el-checkbox label="人文历史">人文历史</el-checkbox>
                        <el-checkbox label="美食体验">美食体验</el-checkbox>
                        <el-checkbox label="购物娱乐">购物娱乐</el-checkbox>
                    </el-checkbox-group>
                </div>
                <div class="form-group">
                    <label>特殊需求：</label>
                    <el-input
                        type="textarea"
                        v-model="travelForm.specialNeeds"
                        placeholder="请输入特殊需求（如：带小孩、老年人等）">
                    </el-input>
                </div>
                <div class="form-footer">
                    <button class="cancel-button" @click="toggleTravelForm">取消</button>
                    <button class="submit-form-button" :disabled="planGenerating" @click="submitTravelForm">
                        {{ planGenerating ? '生成中...' : '获取个性化推荐' }}
                    </button>
                </div>
            </div>
        </template>
    </PageLayout>
</template>

<script>
import aiImg from "../../assets/image/Ai.png"
import '../../assets/css/ai-chat.css'
import { mapState } from 'vuex'

export default {
    name: 'AiChat',
    components: {
    },
    data() {
        return {
            msgValue: '',
            msgDom: null,
            userImg: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png",
            aiImg: aiImg,
            showTravelForm: false,
            travelForm: {
                date: '',
                destination: '',
                days: 3,
                budget: '',
                preferences: [],
                specialNeeds: ''
            },
            pickerOptions: {
                disabledDate(time) {
                    return time.getTime() < Date.now() - 8.64e7;
                }
            },
            thinkingText: '正在思考',
            thinkingTimer: null
        }
    },
    computed: {
        ...mapState('plan', ['loading']),
        planGenerating() {
            return this.loading
        },
        chatLoading() {
            return this.$store.state.msg.chatLoading
        },
        messageList() {
            return this.$store.state.msg.list || []
        },
        isWaitingReply() {
            if (!this.chatLoading) {
                return false
            }
            const list = this.messageList
            if (!list.length) {
                return true
            }
            const lastMsg = list[list.length - 1]
            return lastMsg.role === 'user'
        },
        inputDisabled() {
            return this.chatLoading
        },
        userInfo() {
            const info = window.localStorage.getItem("user_info");
            return info ? JSON.parse(info) : null;
        }
    },
    mounted() {
        if (this.userInfo && this.userInfo.avatar) {
            this.userImg = this.$store.state.HOST + this.userInfo.avatar;
        }
        this.msgDom = document.getElementById("message-box")
        this.scroll()
        if (this.chatLoading) {
            this.startThinkingAnimation()
        }
        
        window.copyCode = (button) => {
            const codeBlock = button.closest('.code-block').querySelector('code')
            const text = codeBlock.textContent
            
            navigator.clipboard.writeText(text).then(() => {
                this.$message({
                    message: '代码已复制到剪贴板',
                    type: 'success',
                    duration: 2000
                })
                
                button.innerHTML = '<i class="el-icon-check"></i>'
                setTimeout(() => {
                    button.innerHTML = '<i class="el-icon-document-copy"></i>'
                }, 2000)
            }).catch(err => {
                this.$message.error('复制失败，请手动复制')
                console.error('复制失败:', err)
            })
        }
    },
    beforeDestroy() {
        this.clearThinkingTimer()
    },
    methods: {
        clearThinkingTimer() {
            if (this.thinkingTimer) {
                clearInterval(this.thinkingTimer)
                this.thinkingTimer = null
            }
        },
        startThinkingAnimation() {
            this.clearThinkingTimer()
            const texts = ['正在思考', '正在检索旅游信息', '正在组织回答']
            let index = 0
            this.thinkingText = texts[0]
            this.thinkingTimer = setInterval(() => {
                index = (index + 1) % texts.length
                this.thinkingText = texts[index]
            }, 1800)
        },
        toggleTravelForm() {
            this.showTravelForm = !this.showTravelForm;
            if (!this.showTravelForm) {
                // 重置表单数据
                this.resetTravelForm();
            }
        },
        resetTravelForm() {
            this.travelForm = {
                date: '',
                destination: '',
                days: 3,
                budget: '',
                preferences: [],
                specialNeeds: ''
            };
        },
        scroll() {
            this.$nextTick(() => {
                if (this.msgDom) {
                    this.msgDom.scrollTop = this.msgDom.scrollHeight
                }
            })
        },
        submitMsg() {
            if (!this.msgValue.trim() || this.inputDisabled) return

            if (!window.localStorage.getItem("user_token")) {
                this.$message.warning("请先登录后再使用 AI 功能")
                this.$router.push("/login")
                return
            }

            const userMsg = this.msgValue.trim()
            this.startThinkingAnimation()
            this.$store.dispatch('msg/userAddMsg', userMsg)
            this.msgValue = ""
            this.scroll()

            const history = (this.messageList || [])
                .slice(0, -1)
                .slice(-8)
                .map(item => ({
                    role: item.role,
                    content: item.rawContent || this.stripHtmlContent(item.content)
                }))

            this.$store.dispatch('msg/sendAiStream', { question: userMsg, history }).finally(() => {
                this.clearThinkingTimer()
                this.scroll()
            })
        },
        clearChat() {
            this.$confirm('确认清除所有对话记录吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.$store.dispatch('msg/clearMessages')
                this.clearThinkingTimer()
                this.$message({
                    type: 'success',
                    message: '对话已清除'
                })
                this.scroll()
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消清除'
                })
            })
        },
        submitTravelForm() {
            if (!this.validateForm()) return

            if (!window.localStorage.getItem("user_token")) {
                this.$message.warning("请先登录后再生成行程")
                this.$router.push("/login")
                return
            }

            if (this.planGenerating) {
                this.$message.warning('已有行程正在生成中，请稍候')
                return
            }

            const formData = {
                departureDate: this.formatDate(this.travelForm.date),
                destination: this.travelForm.destination,
                days: this.travelForm.days,
                budget: this.travelForm.budget,
                preferences: this.travelForm.preferences.join(','),
                specialNeeds: this.travelForm.specialNeeds || ''
            }

            this.showTravelForm = false
            this.resetTravelForm()
            this.$store.dispatch('plan/generatePlan', formData).catch(() => {})
        },
        stripHtmlContent(content) {
            if (!content) return ''
            return String(content).replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
        },
        validateForm() {
            if (!this.travelForm.date) {
                this.$message.warning('请选择出发时间');
                return false;
            }
            if (!this.travelForm.destination) {
                this.$message.warning('请输入目的地');
                return false;
            }
            if (!this.travelForm.budget) {
                this.$message.warning('请选择预算范围');
                return false;
            }
            if (this.travelForm.preferences.length === 0) {
                this.$message.warning('请至少选择一个偏好类型');
                return false;
            }
            return true;
        },
        formatDate(date) {
            if (!date) return '';
            const str = String(date);
            const matched = str.match(/^(\d{4})-(\d{1,2})-(\d{1,2})/);
            if (matched) {
                return `${matched[1]}年${parseInt(matched[2], 10)}月${parseInt(matched[3], 10)}日`;
            }
            const d = new Date(date);
            if (isNaN(d.getTime())) return '';
            return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`;
        },
        askQuick(question) {
            this.msgValue = question
            this.submitMsg()
        }
    },
    watch: {
        messageList: {
            handler() {
                this.scroll()
            },
            deep: true
        },
        chatLoading(val) {
            if (val && this.isWaitingReply) {
                this.startThinkingAnimation()
            } else if (!val) {
                this.clearThinkingTimer()
            }
        }
    }
}
</script>

<style scoped>
/* 样式已移到外部CSS文件 */
</style>