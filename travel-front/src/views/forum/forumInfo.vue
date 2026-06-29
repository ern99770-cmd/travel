<template>
  <PageLayout>
  <div class="forumInfo">
    <div class="forum-detail-container page-container page-container--narrow">
      <!-- 左侧：文章正文 -->
      <div class="forum-detail-main">
        <div class="article-header">
          <h1 class="article-title">{{info.title}}</h1>
          <div class="article-meta">
            <span><i class="el-icon-date"></i> {{ formatDate(new Date()) }}</span>
            <span><i class="el-icon-user"></i> 智游财经编辑部</span>
            <span><i class="el-icon-view"></i> {{ 245 + (id % 10) * 12 }} 次阅读</span>
          </div>
        </div>
        <div class="article-content" v-html="info.content"></div>
      </div>

      <!-- 右侧：侧边栏财经深度 -->
      <div class="forum-detail-sidebar">
        <!-- 目的地经济指标 -->
        <div class="sidebar-widget">
          <div class="widget-title">目的地经济看板</div>
          <div class="dest-economy">
            <div class="economy-stat">
              <span class="stat-label">人均消费水平</span>
              <span class="stat-value">￥1,250/天</span>
            </div>
            <div class="economy-stat">
              <span class="stat-label">旅游热度指数</span>
              <span class="stat-value">92.4 (极高)</span>
            </div>
            <div class="economy-stat">
              <span class="stat-label">消费增长率</span>
              <span class="stat-value" style="color: #f56c6c;">+4.2% ↑</span>
            </div>
          </div>
        </div>

        <!-- 财务建议 -->
        <div class="sidebar-widget">
          <div class="widget-title">智游财务建议</div>
          <div class="financial-tips">
            <div class="tip-item">
              <i class="el-icon-warning-outline"></i>
              <div class="tip-content">目的地近期进入旅游旺季，酒店价格波动较大，建议提前15天锁定。</div>
            </div>
            <div class="tip-item">
              <i class="el-icon-coin"></i>
              <div class="tip-content">该地区支持主流数字支付，汇率转换存在一定手续费，建议刷卡消费。</div>
            </div>
            <div class="tip-item">
              <i class="el-icon-guide"></i>
              <div class="tip-content">当地特色餐饮性价比极高，人均 80-120 元即可享受深度体验。</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  </PageLayout>
</template>

<script>
  import {getSysForumById} from '../../api/api'
  export default {
    data() {
      return{
        id: "",
        info: {},
      }
    },
    components: {},
    methods: {
      getSysForumById() {
        getSysForumById({id: this.id}).then(res => {
          if (res.code == 1000) {
            this.info = res.data
          }
        }).catch(err => {
          console.error('获取资讯详情失败:', err)
        })
      },
      formatDate(date) {
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
      }
    },
    mounted() {
      this.id = this.$route.query.id
      this.getSysForumById()
    }
 }
</script>

<style scoped>
   @import url('../../assets/css/forumInfo.css');
  ::v-deep img {
    max-width: 100% !important;
  }
</style>