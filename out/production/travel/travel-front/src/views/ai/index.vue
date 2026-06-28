<template>
    <div>
        <Header />
        <div class="ai-chat">
            <div class="travel-decoration airplane">✈️</div>
            <div class="travel-decoration compass">🧭</div>
            <div class="travel-decoration map">🗺️</div>
            <div class="travel-decoration camera">📷</div>
            <div class="chat-container">
                <div class="action-bar">
                    <button class="clear-button" @click="clearChat">
                        <i class="el-icon-delete"></i>
                        清除对话
                    </button>
                    <button class="travel-request-button" @click="toggleTravelForm">
                        <i class="el-icon-place"></i>
                        旅游推荐
                    </button>
                </div>
                <div class="message-container" id='message-box'>
                    <div v-for="(msg,index) in messageList" :key="index" 
                         :class="['message', msg.role === 'user' ? 'message-user' : 'message-assistant']">
                        <div class="message-content">
                            <div class="avatar">
                                <img :src="msg.role === 'user' ? userImg : aiImg" 
                                     :alt="msg.role === 'user' ? '用户' : 'AI'" 
                                     class="avatar-img"/>
                                <span v-if="msg.role === 'assistant'" class="assistant-name">智游向导</span>
                            </div>
                            <div class="bubble">
                                <div class="text" v-html="processCodeBlocks(msg.content)"></div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="input-area">
                    <div class="chat-input">
                        <textarea 
                            class="message-input"
                            rows="3"
                            placeholder="有关旅游的问题尽管问我吧..."
                            v-model="msgValue"
                            @keydown.enter.prevent="submitMsg"
                        ></textarea>
                        <button class="send-button" @click="submitMsg">发送</button>
                    </div>
                </div>
            </div>
        </div>
        <Bottom />

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
                    <button class="submit-form-button" @click="submitTravelForm">获取个性化推荐</button>
                </div>
            </div>
        </template>
    </div>
</template>

<script>
import { mapState } from 'vuex'
import aiImg from "../../assets/image/Ai.png"
import TTSRecorder from "@/utils/TTSRecorder"
import Header from '@/components/header'
import Bottom from '@/components/bottom'
import '../../assets/css/ai-chat.css'

export default {
    name: 'AiChat',
    components: {
        Header,
        Bottom
    },
    data() {
        return {
            msgValue: '',
            msgDom: null,
            userImg: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png",
            aiImg: aiImg,
            ttsRecorder: null,
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
            }
        }
    },
    computed: {
        messageList() {
            return this.$store.state.msg.list || []
        }
    },
    mounted() {
        this.msgDom = document.getElementById("message-box")
        this.ttsRecorder = new TTSRecorder()
        this.scroll()
        
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
    methods: {
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
        async submitMsg() {
            if (!this.msgValue.trim()) return
            
            this.$store.dispatch('msg/userAddMsg', this.msgValue)
            this.msgValue = ""
            this.ttsRecorder.start(this.$store, this.msgDom)
            this.scroll()
        },
        clearChat() {
            this.$confirm('确认清除所有对话记录吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.$store.commit('msg/CLEAR_MESSAGES')
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
        processCodeBlocks(content) {
            if (!content) return content
            
            // 处理所有代码块，包括没有语言标识的
            content = content.replace(/<pre><code.*?>([\s\S]*?)<\/code><\/pre>/g, (match, code) => {
                return `
                    <div class="code-block">
                        <div class="code-header">
                            <button class="copy-button" onclick="copyCode(this)">
                                <i class="el-icon-document-copy"></i>
                            </button>
                        </div>
                        <pre><code>${code}</code></pre>
                    </div>
                `
            })

            // 处理带有语言标识的代码块
            content = content.replace(/<pre><code class="language-.*?">([\s\S]*?)<\/code><\/pre>/g, (match, code) => {
                return `
                    <div class="code-block">
                        <div class="code-header">
                            <button class="copy-button" onclick="copyCode(this)">
                                <i class="el-icon-document-copy"></i>
                            </button>
                        </div>
                        ${match}
                    </div>
                `
            })

            return content
        },
        submitTravelForm() {
            if (!this.validateForm()) return;
            
            // 1. 发送到后端API
            this.sendTravelRequestToBackend();
            
            // 2. 同时发送到AI对话
            const prompt = this.generateTravelPrompt();
            this.$store.dispatch('msg/userAddMsg', prompt);
            
            // 3. 关闭表单
            this.showTravelForm = false;
            
            // 4. 启动语音合成并滚动
            this.ttsRecorder.start(this.$store, this.msgDom);
            this.scroll();
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
        generateTravelPrompt() {
            const date = this.formatDate(this.travelForm.date);
            const preferences = this.travelForm.preferences.join('、');
            return `请根据以下信息为我推荐旅游方案：
时间：${date}
目的地：${this.travelForm.destination}
游玩天数：${this.travelForm.days}天
预算范围：${this.travelForm.budget}
偏好类型：${preferences}
特殊需求：${this.travelForm.specialNeeds || '无'}

请提供详细的行程安排、景点推荐、美食推荐和注意事项。`;
        },
        formatDate(date) {
            if (!date) return '';
            const d = new Date(date);
            return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`;
        },
        sendTravelRequestToBackend() {
            // 构建要发送到后端的数据
            const formData = {
                departureDate: this.formatDate(this.travelForm.date),
                destination: this.travelForm.destination,
                days: this.travelForm.days,
                budget: this.travelForm.budget,
                preferences: this.travelForm.preferences,
                specialNeeds: this.travelForm.specialNeeds || ''
            };

            // 这里可以添加实际的API调用
            // 例如：this.$http.post('/api/travel/recommendations', formData)
            console.log('发送旅游需求到后端:', formData);
            
            // 提示用户
            this.$message({
                type: 'success',
                message: '旅游需求已提交',
                duration: 2000
            });
        }
    },
    watch: {
        messageList: {
            handler() {
                this.scroll()
            },
            deep: true
        }
    }
}
</script>

<style scoped>
/* 样式已移到外部CSS文件 */
</style>