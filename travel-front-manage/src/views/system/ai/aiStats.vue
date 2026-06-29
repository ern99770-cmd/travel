<template>
  <div class="ai-stats-page">
    <div class="stats-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-label">AI 对话总次数</div>
        <div class="stat-value primary">{{ stats.chatTotal || 0 }}</div>
        <div class="stat-sub">今日 {{ stats.chatToday || 0 }}</div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-label">生成行程总数</div>
        <div class="stat-value success">{{ stats.planTotal || 0 }}</div>
        <div class="stat-sub">今日 {{ stats.planToday || 0 }}</div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-label">行程生成记录</div>
        <div class="stat-value warning">{{ stats.planLogTotal || 0 }}</div>
        <div class="stat-sub">日志统计</div>
      </el-card>
    </div>

    <div class="chart-row">
      <el-card shadow="hover" class="chart-card">
        <div slot="header">近 7 日 AI 使用趋势</div>
        <div id="aiTrendChart" class="chart-box"></div>
      </el-card>
      <el-card shadow="hover" class="chart-card">
        <div slot="header">热门目的地 TOP10</div>
        <div id="aiDestChart" class="chart-box"></div>
      </el-card>
    </div>

    <el-card shadow="hover" class="recent-card">
      <div slot="header">最近生成的行程</div>
      <el-table :data="stats.recentPlans || []" size="small" stripe>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="destination" label="目的地" width="120" />
        <el-table-column prop="days" label="天数" width="80" />
        <el-table-column prop="budget" label="预算" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getAiStats } from '@/api/api'

export default {
  name: 'AiStats',
  data() {
    return {
      stats: {},
      trendChart: null,
      destChart: null
    }
  },
  mounted() {
    this.loadStats()
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    if (this.trendChart) this.trendChart.dispose()
    if (this.destChart) this.destChart.dispose()
  },
  methods: {
    loadStats() {
      getAiStats().then(res => {
        if (res.code == 1000) {
          this.stats = res.data || {}
          this.$nextTick(() => {
            this.renderTrendChart()
            this.renderDestChart()
          })
        } else {
          this.$message.error(res.message || '加载统计失败')
        }
      })
    },
    renderTrendChart() {
      const el = document.getElementById('aiTrendChart')
      if (!el) return
      if (!this.trendChart) {
        this.trendChart = echarts.init(el)
      }
      const trend = this.stats.dailyTrend || []
      this.trendChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['AI对话', '生成行程'] },
        grid: { left: 40, right: 20, top: 40, bottom: 30 },
        xAxis: { type: 'category', data: trend.map(i => i.date) },
        yAxis: { type: 'value', minInterval: 1 },
        series: [
          {
            name: 'AI对话',
            type: 'line',
            smooth: true,
            data: trend.map(i => i.chatCount)
          },
          {
            name: '生成行程',
            type: 'line',
            smooth: true,
            data: trend.map(i => i.planCount)
          }
        ]
      })
    },
    renderDestChart() {
      const el = document.getElementById('aiDestChart')
      if (!el) return
      if (!this.destChart) {
        this.destChart = echarts.init(el)
      }
      const rank = this.stats.destinationRank || []
      this.destChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 80, right: 20, top: 20, bottom: 30 },
        xAxis: { type: 'value', minInterval: 1 },
        yAxis: {
          type: 'category',
          data: rank.map(i => i.destination).reverse()
        },
        series: [{
          type: 'bar',
          data: rank.map(i => i.count).reverse(),
          itemStyle: { color: '#409EFF' }
        }]
      })
    },
    resizeCharts() {
      if (this.trendChart) this.trendChart.resize()
      if (this.destChart) this.destChart.resize()
    }
  }
}
</script>

<style scoped>
.ai-stats-page {
  padding: 16px;
}
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  text-align: center;
}
.stat-label {
  color: #909399;
  font-size: 14px;
}
.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin: 8px 0;
}
.stat-value.primary { color: #409EFF; }
.stat-value.success { color: #67C23A; }
.stat-value.warning { color: #E6A23C; }
.stat-sub {
  color: #909399;
  font-size: 12px;
}
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.chart-box {
  height: 320px;
}
.recent-card {
  margin-bottom: 16px;
}
@media (max-width: 1200px) {
  .stats-cards,
  .chart-row {
    grid-template-columns: 1fr;
  }
}
</style>
