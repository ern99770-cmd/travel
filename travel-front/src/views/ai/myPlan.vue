<template>
  <PageLayout>
  <div class="plan-page page-container">
      <div class="plan-header">
        <div>
          <h2>我的 AI 行程</h2>
          <p class="plan-subtitle">聊完即存 · 一键预约 · 分享印记</p>
        </div>
        <el-button type="primary" size="small" @click="$router.push('/ai')">去生成新行程</el-button>
      </div>

      <div v-if="loading" class="loading-box">
        <el-skeleton :rows="4" animated />
      </div>

      <div v-else-if="tableData.length === 0" class="empty-box">
        <p>还没有保存的 AI 行程</p>
        <el-button type="primary" @click="$router.push('/ai')">立即规划</el-button>
      </div>

      <div v-else class="plan-list">
        <div v-for="item in tableData" :key="item.id" class="plan-card">
          <div class="plan-card-main" @click="openDetail(item)">
            <h3>{{ item.title || item.destination }}</h3>
            <p>{{ item.destination }} · {{ item.days }}天 · {{ item.budget }}</p>
            <p class="plan-time">出发：{{ item.departureDate || '未设置' }} | 创建：{{ formatTime(item.createTime) }}</p>
          </div>
          <div class="plan-card-actions">
            <el-button type="text" @click="openDetail(item)">查看详情</el-button>
            <el-button type="text" @click="bookByPlan(item, 'attractions')">订景点</el-button>
            <el-button type="text" @click="bookByPlan(item, 'hotel')">订酒店</el-button>
            <el-button type="text" @click="sharePlan(item)">分享</el-button>
            <el-button type="text" style="color:#f56c6c" @click="deletePlan(item)">删除</el-button>
          </div>
        </div>
      </div>

      <el-pagination
        v-if="total > search.pageSize"
        background
        layout="prev, pager, next"
        :current-page="search.pageNumber"
        :page-size="search.pageSize"
        :total="total"
        @current-change="handleCurrentChange">
      </el-pagination>
    </div>

    <el-dialog
      :title="currentPlan.title || '行程详情'"
      :visible.sync="detailVisible"
      width="72%"
      top="6vh"
      custom-class="plan-detail-dialog">
      <div v-if="currentPlan.id" class="plan-detail-body">
        <div class="detail-meta">
          <el-tag>{{ currentPlan.destination }}</el-tag>
          <el-tag type="success">{{ currentPlan.days }}天</el-tag>
          <el-tag type="warning">{{ currentPlan.budget }}</el-tag>
          <span v-if="currentPlan.departureDate" class="departure-text">出发：{{ currentPlan.departureDate }}</span>
        </div>

        <div class="book-bar">
          <span class="book-bar-label">按此行程去预订</span>
          <el-button type="primary" size="small" icon="el-icon-place" @click="bookByPlan(currentPlan, 'attractions')">
            预订景点
          </el-button>
          <el-button type="success" size="small" icon="el-icon-office-building" @click="bookByPlan(currentPlan, 'hotel')">
            预订酒店
          </el-button>
          <el-button size="small" icon="el-icon-printer" @click="printCurrentPlan">导出/打印</el-button>
        </div>

        <div v-if="recommendList.length" class="recommend-section">
          <h4><i class="el-icon-place"></i> 推荐景点 · 点击卡片立即预约</h4>
          <div class="recommend-grid">
            <div v-for="item in recommendList" :key="'a-' + item.id" class="book-card" @click="goBookAttraction(item.id)">
              <div class="book-card-img">
                <img :src="getCover(item.images)" alt="">
              </div>
              <div class="book-card-body">
                <div class="book-card-title">{{ item.name }}</div>
                <div class="book-card-meta">{{ item.location || currentPlan.destination }}</div>
                <div class="book-card-desc">{{ item.introduce }}</div>
                <div class="book-card-footer">
                  <span class="price" v-if="item.price != null">¥{{ item.price }}</span>
                  <el-button type="primary" size="mini" @click.stop="goBookAttraction(item.id)">立即预约</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="hotelList.length" class="recommend-section">
          <h4><i class="el-icon-office-building"></i> 推荐酒店 · 点击卡片立即预订</h4>
          <div class="recommend-grid">
            <div v-for="item in hotelList" :key="'h-' + item.id" class="book-card" @click="goBookHotel(item.id)">
              <div class="book-card-img">
                <img :src="getCover(item.images)" alt="">
              </div>
              <div class="book-card-body">
                <div class="book-card-title">{{ item.name }}</div>
                <div class="book-card-meta">{{ item.attractions || item.address }}</div>
                <div class="book-card-desc">{{ item.introduce }}</div>
                <div class="book-card-footer">
                  <el-button type="success" size="mini" @click.stop="goBookHotel(item.id)">立即预订</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="plan-content-block">
          <h4>详细行程安排</h4>
          <div class="plan-content" v-html="renderContent(currentPlan.planContent)"></div>
        </div>

        <div class="detail-actions">
          <el-button type="primary" size="small" @click="sharePlan(currentPlan)">分享到旅途印记</el-button>
        </div>
      </div>
    </el-dialog>
  </PageLayout>
</template>

<script>
import { getMyPlanPage, getPlanById, removePlan, sharePlanToTravelShare } from '@/api/api'
import { marked } from 'marked'
import { printTravelPlan } from '@/utils/planPrint'

const DEFAULT_COVER = 'https://via.placeholder.com/400x240?text=暂无图片'

export default {
  name: 'MyPlan',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      search: {
        pageNumber: 1,
        pageSize: 8
      },
      detailVisible: false,
      currentPlan: {},
      recommendList: [],
      hotelList: []
    }
  },
  mounted() {
    this.loadData().then(() => {
      this.openPlanFromQuery()
    })
  },
  methods: {
    loadData() {
      this.loading = true
      return getMyPlanPage(this.search).then(res => {
        if (res.code == 1000) {
          this.tableData = res.data.records || []
          this.total = res.data.total || 0
        } else {
          this.$message.error(res.message || '加载失败')
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    openPlanFromQuery() {
      const planId = this.$route.query.id
      if (!planId) return
      const cached = this.tableData.find(item => item.id === planId)
      if (cached) {
        this.openDetail(cached)
        return
      }
      getPlanById({ id: planId }).then(res => {
        if (res.code == 1000 && res.data) {
          this.applyPlanDetail(res.data)
          this.detailVisible = true
        }
      })
    },
    handleCurrentChange(page) {
      this.search.pageNumber = page
      this.loadData()
    },
    openDetail(item) {
      getPlanById({ id: item.id }).then(res => {
        if (res.code == 1000) {
          this.applyPlanDetail(res.data)
          this.detailVisible = true
        } else {
          this.$message.error(res.message || '加载详情失败')
        }
      })
    },
    applyPlanDetail(plan) {
      this.currentPlan = plan
      this.recommendList = this.parseJsonList(plan.recommendAttractions)
      this.hotelList = this.parseJsonList(plan.recommendHotels)
    },
    deletePlan(item) {
      this.$confirm('确认删除该行程吗？', '提示', { type: 'warning' }).then(() => {
        removePlan({ id: item.id }).then(res => {
          if (res.code == 1000) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        })
      }).catch(() => {})
    },
    sharePlan(item) {
      if (!window.localStorage.getItem('user_token')) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      this.$confirm('将行程分享到「旅途印记」吗？', '分享确认', { type: 'info' }).then(() => {
        sharePlanToTravelShare({ id: item.id }).then(res => {
          if (res.code == 1000) {
            this.$message.success('分享成功')
            this.$router.push('/travelShare')
          } else {
            this.$message.error(res.message || '分享失败')
          }
        })
      }).catch(() => {})
    },
    bookByPlan(plan, type) {
      if (!plan || !plan.destination) {
        this.$message.warning('行程缺少目的地信息')
        return
      }
      const destination = plan.destination.trim()
      if (type === 'attractions') {
        this.$router.push({ path: '/attractions', query: { location: destination, fromPlan: '1' } })
      } else {
        this.$router.push({ path: '/hotel', query: { destination, fromPlan: '1' } })
      }
    },
    goBookAttraction(id) {
      this.$router.push({ path: '/attractionsInfo', query: { id } })
    },
    goBookHotel(id) {
      this.$router.push({ path: '/hotelInfo', query: { id } })
    },
    printCurrentPlan() {
      const ok = printTravelPlan(this.currentPlan, this.recommendList, this.hotelList)
      if (!ok) {
        this.$message.warning('请允许浏览器弹出窗口以打印行程')
      }
    },
    parseJsonList(json) {
      if (!json) return []
      try {
        const data = JSON.parse(json)
        return Array.isArray(data) ? data : []
      } catch (e) {
        return []
      }
    },
    renderContent(content) {
      return marked.parse(content || '')
    },
    getCover(images) {
      if (!images) return DEFAULT_COVER
      const first = String(images).split(',')[0]
      if (!first) return DEFAULT_COVER
      if (first.startsWith('http')) return first
      return this.$store.state.HOST + first
    },
    formatTime(time) {
      if (!time) return ''
      return String(time).replace('T', ' ').substring(0, 16)
    }
  }
}
</script>

<style scoped>
.plan-page {
  max-width: 1100px;
  min-height: calc(100vh - var(--header-height, 80px) - var(--page-top-gap, 16px) - var(--page-bottom-gap, 40px));
  box-sizing: border-box;
}
.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.plan-header h2 {
  margin: 0 0 4px;
}
.plan-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
.plan-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
.plan-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  overflow: hidden;
}
.plan-card-main {
  padding: 18px;
  cursor: pointer;
}
.plan-card-main h3 {
  margin: 0 0 8px;
  color: #2c3e50;
}
.plan-card-main p {
  margin: 0 0 6px;
  color: #666;
  font-size: 14px;
}
.plan-time {
  color: #999 !important;
  font-size: 12px !important;
}
.plan-card-actions {
  border-top: 1px solid #f0f0f0;
  padding: 8px 12px;
  text-align: right;
}
.empty-box, .loading-box {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
}
.detail-meta {
  margin-bottom: 16px;
}
.detail-meta .el-tag {
  margin-right: 8px;
}
.departure-text {
  margin-left: 8px;
  color: #666;
  font-size: 13px;
}
.book-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9eb 100%);
  border-radius: 10px;
  margin-bottom: 20px;
}
.book-bar-label {
  font-weight: 600;
  color: #303133;
  margin-right: 4px;
}
.recommend-section {
  margin-bottom: 24px;
}
.recommend-section h4 {
  margin: 0 0 14px;
  color: #303133;
  font-size: 15px;
}
.recommend-section h4 i {
  color: #409EFF;
  margin-right: 6px;
}
.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 14px;
}
.book-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s;
}
.book-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}
.book-card-img {
  height: 140px;
  overflow: hidden;
  background: #f5f7fa;
}
.book-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.book-card-body {
  padding: 12px;
}
.book-card-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 4px;
  color: #303133;
}
.book-card-meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}
.book-card-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  max-height: 40px;
  overflow: hidden;
  margin-bottom: 10px;
}
.book-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price {
  color: #f56c6c;
  font-weight: 700;
}
.plan-content-block h4 {
  margin: 0 0 12px;
  color: #303133;
}
.plan-content {
  line-height: 1.8;
  color: #333;
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
}
.detail-actions {
  margin-top: 20px;
  text-align: right;
}
</style>
