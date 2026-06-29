<template>
  <PageLayout>
  <div class="forum">
    <div class="forum-container page-container page-container--narrow">
      <!-- 顶部搜索和财经概览 -->
      <div class="forum-header">
        <div class="search-section">
          <el-input 
            size="medium" 
            v-model="search.title" 
            placeholder="搜索全球旅游财经资讯..."
            prefix-icon="el-icon-search">
          </el-input>
          <el-button type="primary" @click="searchPage">搜索</el-button>
        </div>
        <div class="economic-overview">
          <div class="overview-item">
            <span class="label">全球旅游信心指数</span>
            <span class="value up">112.5 <i class="el-icon-caret-top"></i></span>
          </div>
          <div class="overview-item">
            <span class="label">航空板块指数</span>
            <span class="value up">3,241.0 <i class="el-icon-caret-top"></i></span>
          </div>
          <div class="overview-item">
            <span class="label">酒店租赁指数</span>
            <span class="value down">1,854.2 <i class="el-icon-caret-bottom"></i></span>
          </div>
        </div>
      </div>

      <div class="forum-main">
        <!-- 左侧：资讯列表 -->
        <div class="forum-content">
          <div class="section-title">
            <i class="el-icon-news"></i> 深度财经资讯
          </div>
          <div class="news-list">
            <div v-for="(item,index) in tableData" :key="index" class="news-item" @click="toInfo(item.id)">
              <div class="news-meta">
                <span class="news-tag" :class="index % 3 === 0 ? 'tag-finance' : 'tag-travel'">
                  {{ index % 3 === 0 ? '财经深度' : '行业快讯' }}
                </span>
                <span class="news-date">{{ formatDate(new Date()) }}</span>
              </div>
              <div class="news-title">{{item.title}}</div>
              <div class="news-footer">
                <span class="views"><i class="el-icon-view"></i> {{ 100 + index * 15 }}阅读</span>
                <span class="more">阅读全文 <i class="el-icon-arrow-right"></i></span>
              </div>
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

        <!-- 右侧：侧边栏财经工具 -->
        <div class="forum-sidebar">
          <!-- 汇率工具 -->
          <div class="sidebar-card">
            <div class="card-title">全球汇率看板</div>
            <div class="exchange-rates">
              <div class="rate-item" v-for="(rate, key) in rates" :key="key">
                <span class="currency">{{ key }}</span>
                <span class="price">{{ rate.price }}</span>
                <span class="change" :class="rate.type">{{ rate.change }}</span>
              </div>
            </div>
            <div class="card-footer">数据更新于: {{ formatTime(new Date()) }}</div>
          </div>

          <!-- 热门旅游标的 -->
          <div class="sidebar-card">
            <div class="card-title">目的地投资潜力榜</div>
            <div class="investment-list">
              <div class="invest-item" v-for="(item, idx) in investmentList" :key="idx">
                <span class="rank">{{ idx + 1 }}</span>
                <span class="name">{{ item.name }}</span>
                <el-progress :percentage="item.score" :color="item.color" :show-text="false"></el-progress>
                <span class="score">{{ item.score }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  </PageLayout>
</template>

<script>
  import {getSysForumPage} from '../../api/api'
  export default {
    data() {
      return{
        search: {
          title: "",
          pageSize: 10,
          pageNumber: 1,
        },
        total: 0,
        tableData: [],
        rates: {
          'USD/CNY': { price: '7.1842', change: '+0.02%', type: 'up' },
          'EUR/CNY': { price: '7.8251', change: '-0.15%', type: 'down' },
          'JPY/CNY': { price: '0.0478', change: '+0.12%', type: 'up' },
          'HKD/CNY': { price: '0.9184', change: '-0.01%', type: 'down' }
        },
        investmentList: [
          { name: '三亚度假地产', score: 92, color: '#f56c6c' },
          { name: '上海商圈酒店', score: 88, color: '#e6a23c' },
          { name: '杭州电竞文旅', score: 85, color: '#409eff' },
          { name: '成都文创综合体', score: 82, color: '#67c23a' }
        ]
      }
    },
    components: {},
    methods: {
      searchPage() {
        this.search.pageNumber = 1
        this.getSysForumPage()
      },
      getSysForumPage() {
        getSysForumPage(this.search).then(res => {
          if (res.code == 1000) {
            this.tableData = res.data.records
            this.total = res.data.total
          }
        })
      },
      toInfo(id) {
        this.$router.push("/forumInfo?id=" + id)
      },
      handleCurrentChange(val) {
        this.search.pageNumber = val
        this.getSysForumPage()
      },
      formatDate(date) {
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
      },
      formatTime(date) {
        return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
      }
    },
    mounted() {
      this.getSysForumPage()
    }
 }
</script>

<style scoped>
   @import url('../../assets/css/forum.css');
</style>