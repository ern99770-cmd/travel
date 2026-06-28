<template>
  <div class="travel-share">
    <headers></headers>
    <div class="share-container">
      <div class="share-publish-card">
        <div class="publish-header">
          <img v-if="userInfo && userInfo.avatar" :src="$store.state.HOST + userInfo.avatar" class="user-avatar">
          <img v-else :src="require('@/assets/image/image 2.png')" class="user-avatar">
          <el-input 
            size="medium" 
            v-model="publishContent" 
            placeholder="分享你的旅游故事..."
            @click="openPublishDialog">
          </el-input>
        </div>
        <div class="publish-actions">
          <div class="action-item" @click="openPublishDialog">
            <i class="el-icon-camera"></i>
            <span>发图片</span>
          </div>
          <div class="action-item" @click="openPublishDialog">
            <i class="el-icon-location"></i>
            <span>去打卡</span>
          </div>
          <div class="action-item" @click="openPublishDialog">
            <i class="el-icon-edit"></i>
            <span>写心得</span>
          </div>
        </div>
      </div>

      <div class="share-filter">
        <div 
          v-for="(item, index) in filterList" 
          :key="index" 
          @click="changeFilter(item.value)"
          :class="currentFilter === item.value ? 'filter-item active' : 'filter-item'">
          {{ item.label }}
        </div>
      </div>

      <div class="share-list">
        <div v-if="loading" class="loading-wrapper">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-for="(item, index) in tableData" :key="index" class="share-card">
          <div class="share-header">
            <img :src="$store.state.HOST + item.avatar" class="share-avatar">
            <div class="share-user-info">
              <span class="share-user-name">{{ item.userName }}</span>
              <span class="share-time">{{ formatTime(item.createTime) }}</span>
            </div>
            <div v-if="item.location" class="share-location">
              <i class="el-icon-location"></i>
              <span>{{ item.location }}</span>
            </div>
            <div class="share-type" :class="'type-' + item.type">
              {{ getTypeLabel(item.type) }}
            </div>
          </div>
          
          <div v-if="item.title" class="share-title">{{ item.title }}</div>
          
          <div class="share-content">{{ item.content }}</div>
          
          <div v-if="item.images" class="share-images">
            <div class="image-grid">
              <img 
                v-for="(img, imgIndex) in getImagesArray(item.images)" 
                :key="imgIndex" 
                :src="img" 
                class="share-image"
                @click="previewImage(img)">
            </div>
          </div>
          
          <div class="share-footer">
            <div class="share-action" @click="toggleLike(item)">
              <i :class="item.isLiked ? 'el-icon-star-on liked' : 'el-icon-star-off'"></i>
              <span>{{ item.likes }}</span>
            </div>
            <div class="share-action" @click="toggleComments(item)">
              <i class="el-icon-message"></i>
              <span>{{ item.commentCount || 0 }} 评论</span>
            </div>
            <div class="share-action">
              <i class="el-icon-share"></i>
              <span>分享</span>
            </div>
          </div>

          <div v-if="item.showComments" class="share-comments">
            <div v-if="item.commentsLoading" class="comments-loading">
              <el-skeleton :rows="2" animated />
            </div>
            <div v-else-if="item.comments && item.comments.length > 0" class="comments-list">
              <div v-for="(comment, commentIndex) in item.comments" :key="commentIndex" class="comment-item">
                <img :src="$store.state.HOST + comment.avatar" class="comment-avatar">
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-user-name">{{ comment.userName }}</span>
                    <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                  </div>
                  <p class="comment-text">{{ comment.content }}</p>
                </div>
              </div>
            </div>
            <div v-else class="no-comments">
              <p>暂无评论，快来抢沙发吧~</p>
            </div>
            
            <div class="comment-input-area">
              <el-input 
                size="small" 
                v-model="item.commentText" 
                placeholder="写下你的评论..."
                @keyup.enter.native="submitComment(item)">
              </el-input>
              <el-button type="primary" size="small" @click="submitComment(item)">发送</el-button>
            </div>
          </div>
        </div>

        <div v-if="!loading && tableData.length === 0" class="no-data">
          <i class="el-icon-picture-outline"></i>
          <p>暂无旅游分享，快来发布第一个吧！</p>
        </div>
      </div>

      <div class="pagination-wrapper">
        <el-pagination
          background
          :page-size="search.pageSize"
          layout="total, prev, pager, next"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
    </div>
    <bottoms></bottoms>

    <el-dialog 
      title="发布旅游分享" 
      :visible.sync="publishDialogVisible" 
      width="600px"
      top="5%"
      :close-on-click-modal="false">
      <div class="publish-form">
        <el-form :model="publishForm" label-width="80px">
          <el-form-item label="标题">
            <el-input v-model="publishForm.title" placeholder="给你的分享起个标题"></el-input>
          </el-form-item>
          <el-form-item label="内容">
            <el-input 
              type="textarea" 
              v-model="publishForm.content" 
              :rows="4" 
              placeholder="分享你的旅游故事、心得或攻略..."></el-input>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="publishForm.type" placeholder="请选择类型">
              <el-option :label="getTypeLabel(0)" :value="0"></el-option>
              <el-option :label="getTypeLabel(1)" :value="1"></el-option>
              <el-option :label="getTypeLabel(2)" :value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="地点">
            <el-input v-model="publishForm.location" placeholder="你在哪里分享的？"></el-input>
          </el-form-item>
          <el-form-item label="图片">
            <div class="upload-images">
              <div v-for="(img, index) in publishForm.images" :key="index" class="upload-image-item">
                <img :src="img" class="preview-image">
                <i class="el-icon-close" @click="removeImage(index)"></i>
              </div>
              <div class="upload-add" @click="triggerUpload">
                <i class="el-icon-plus"></i>
              </div>
            </div>
            <input type="file" ref="uploadInput" multiple accept="image/*" @change="handleUpload" style="display:none">
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPublish">发布</el-button>
      </span>
    </el-dialog>

    <el-image-viewer 
      v-if="previewVisible" 
      :url-list="previewImages" 
      :initial-index="previewIndex"
      @close="previewVisible = false">
    </el-image-viewer>
  </div>
</template>

<script>
  import {getSysTravelSharePage, saveSysTravelShare, likeShare, getCommentsByTargetId, saveSysComments} from '../../api/api'
  import headers from '@/components/header'
  import bottoms from '@/components/bottom'
  import request from '@/utils/request'
  export default {
    data() {
      return{
        loading: true,
        userInfo: null,
        currentFilter: null,
        filterList: [
          { label: '全部', value: null },
          { label: '普通分享', value: 0 },
          { label: '心得体会', value: 1 },
          { label: '旅游攻略', value: 2 }
        ],
        search: {
          title: "",
          pageSize: 10,
          pageNumber: 1,
          type: null
        },
        total: 0,
        tableData: [],
        publishContent: "",
        publishDialogVisible: false,
        publishForm: {
          title: "",
          content: "",
          type: 0,
          location: "",
          images: []
        },
        previewVisible: false,
        previewImages: [],
        previewIndex: 0
      }
    },
    components: {
      headers,
      bottoms
    },
    methods: {
      changeFilter(type) {
        this.currentFilter = type
        this.search.type = type
        this.search.pageNumber = 1
        this.getSysTravelSharePage()
      },
      getSysTravelSharePage() {
        this.loading = true
        getSysTravelSharePage(this.search).then(res => {
          if (res.code == 1000) {
            this.tableData = res.data.records.map(item => ({
              ...item,
              showComments: false,
              comments: [],
              commentsLoading: false,
              commentText: ''
            }))
            this.total = res.data.total
          }
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      },
      handleCurrentChange(val) {
        this.search.pageNumber = val
        this.getSysTravelSharePage()
      },
      formatTime(time) {
        if (!time) return ''
        const now = new Date()
        const t = new Date(time)
        const diff = now - t
        const days = Math.floor(diff / (1000 * 60 * 60 * 24))
        const hours = Math.floor(diff / (1000 * 60 * 60))
        const minutes = Math.floor(diff / (1000 * 60))
        
        if (days > 0) return `${days}天前`
        if (hours > 0) return `${hours}小时前`
        if (minutes > 0) return `${minutes}分钟前`
        return '刚刚'
      },
      getTypeLabel(type) {
        const types = {
          0: '普通分享',
          1: '心得体会',
          2: '旅游攻略'
        }
        return types[type] || '普通分享'
      },
      getImagesArray(images) {
        if (!images) return []
        return images.split(',')
      },
      openPublishDialog() {
        const userInfoStr = window.localStorage.getItem("user_info")
        if (!userInfoStr) {
          this.$message.warning("请先登录")
          this.$router.push("/login")
          return
        }
        this.publishDialogVisible = true
      },
      triggerUpload() {
        this.$refs.uploadInput.click()
      },
      handleUpload(e) {
        const files = e.target.files
        if (!files || files.length === 0) return
        
        for (let i = 0; i < files.length; i++) {
          const file = files[i]
          const formData = new FormData()
          formData.append('file', file)
          
          request.post('/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          }).then(res => {
            if (res.code == 1000) {
              this.publishForm.images.push(this.$store.state.HOST + res.data)
            }
          })
        }
        e.target.value = ''
      },
      removeImage(index) {
        this.publishForm.images.splice(index, 1)
      },
      submitPublish() {
        if (!this.publishForm.content && this.publishForm.images.length === 0) {
          this.$message.warning("请输入分享内容或上传图片")
          return
        }
        
        const data = {
          ...this.publishForm,
          images: this.publishForm.images.join(',')
        }
        
        saveSysTravelShare(data).then(res => {
          if (res.code == 1000) {
            this.$message.success("发布成功")
            this.publishDialogVisible = false
            this.publishForm = {
              title: "",
              content: "",
              type: 0,
              location: "",
              images: []
            }
            this.publishContent = ""
            this.getSysTravelSharePage()
          } else {
            this.$message.error(res.message || "发布失败")
          }
        })
      },
      toggleLike(item) {
        const userInfoStr = window.localStorage.getItem("user_info")
        if (!userInfoStr) {
          this.$message.warning("请先登录")
          this.$router.push("/login")
          return
        }
        
        likeShare({ shareId: item.id }).then(res => {
          if (res.code == 1000) {
            item.isLiked = !item.isLiked
            item.likes += item.isLiked ? 1 : -1
          } else {
            this.$message.error(res.message)
          }
        })
      },
      toggleComments(item) {
        item.showComments = !item.showComments
        if (item.showComments && (!item.comments || item.comments.length === 0)) {
          this.getComments(item)
        }
      },
      getComments(item) {
        item.commentsLoading = true
        getCommentsByTargetId({
          targetId: item.id,
          targetType: 2,
          pageNumber: 1,
          pageSize: 20
        }).then(res => {
          if (res.code == 1000) {
            item.comments = res.data.records
            item.commentCount = res.data.total
          }
          item.commentsLoading = false
        }).catch(() => {
          item.commentsLoading = false
        })
      },
      submitComment(item) {
        const userInfoStr = window.localStorage.getItem("user_info")
        if (!userInfoStr) {
          this.$message.warning("请先登录")
          this.$router.push("/login")
          return
        }
        
        if (!item.commentText.trim()) {
          this.$message.warning("请输入评论内容")
          return
        }
        
        saveSysComments({
          content: item.commentText,
          targetId: item.id,
          targetType: 2
        }).then(res => {
          if (res.code == 1000) {
            this.$message.success("评论成功")
            item.commentText = ''
            this.getComments(item)
          } else {
            this.$message.error(res.message || "评论失败")
          }
        })
      },
      previewImage(img) {
        this.previewImages = this.getImagesArray(img)
        this.previewVisible = true
      },
      getUserInfo() {
        const userInfoStr = window.localStorage.getItem("user_info")
        if (userInfoStr) {
          this.userInfo = JSON.parse(userInfoStr)
        }
      }
    },
    mounted() {
      this.getUserInfo()
      this.getSysTravelSharePage()
    }
 }
</script>

<style scoped>
.travel-share {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.share-container {
  max-width: 800px;
  margin: 80px auto 40px;
  padding: 0 20px;
}

.share-publish-card {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}

.publish-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.publish-header .el-input {
  flex: 1;
}

.publish-actions {
  display: flex;
  justify-content: space-around;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  cursor: pointer;
  border-radius: 20px;
  transition: all 0.3s;
  color: #606266;
}

.action-item:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

.action-item i {
  font-size: 18px;
}

.share-filter {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.filter-item {
  padding: 8px 20px;
  background: #fff;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s;
}

.filter-item.active {
  background-color: #409eff;
  color: #fff;
}

.share-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.share-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  overflow: hidden;
}

.share-header {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.share-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
}

.share-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.share-user-name {
  font-weight: 600;
  color: #303133;
}

.share-time {
  font-size: 12px;
  color: #909399;
}

.share-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.share-type {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.type-0 {
  background-color: #ecf5ff;
  color: #409eff;
}

.type-1 {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.type-2 {
  background-color: #f0f9eb;
  color: #67c23a;
}

.share-title {
  padding: 0 20px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.share-content {
  padding: 0 20px;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

.share-images {
  padding: 0 20px;
  margin-bottom: 16px;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.share-image {
  width: calc(33.333% - 6px);
  height: 180px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.share-image:hover {
  transform: scale(1.02);
}

.share-footer {
  display: flex;
  justify-content: space-around;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}

.share-action {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  cursor: pointer;
  border-radius: 20px;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s;
}

.share-action:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

.share-action .liked {
  color: #f56c6c;
}

.share-comments {
  padding: 16px 20px;
  background-color: #fafafa;
  border-top: 1px solid #f0f0f0;
}

.comments-loading {
  margin-bottom: 12px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.comment-user-name {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.comment-time {
  font-size: 11px;
  color: #909399;
}

.comment-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin: 0;
}

.no-comments {
  text-align: center;
  padding: 20px 0;
  color: #909399;
  font-size: 14px;
  margin-bottom: 16px;
}

.comment-input-area {
  display: flex;
  gap: 12px;
}

.comment-input-area .el-input {
  flex: 1;
}

.loading-wrapper {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
}

.no-data {
  background: #fff;
  padding: 60px 20px;
  border-radius: 12px;
  text-align: center;
  color: #909399;
}

.no-data i {
  font-size: 48px;
  margin-bottom: 16px;
}

.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.publish-form {
  max-height: 500px;
  overflow-y: auto;
}

.upload-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.upload-image-item {
  position: relative;
  width: 120px;
  height: 120px;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.upload-image-item .el-icon-close {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #f56c6c;
  color: #fff;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.upload-add {
  width: 120px;
  height: 120px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
}

.upload-add:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-add i {
  font-size: 28px;
}
</style>