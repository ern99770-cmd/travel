<template>
  <div class="exchange-page">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div
        class="stat-card stat-card--pending"
        :class="{ active: search.status === 0 }"
        @click="filterByStatus(0)">
        <div class="stat-card__icon"><i class="el-icon-time"></i></div>
        <div class="stat-card__body">
          <div class="stat-card__num">{{ stats.pending || 0 }}</div>
          <div class="stat-card__label">待处理</div>
        </div>
      </div>
      <div
        class="stat-card stat-card--done"
        :class="{ active: search.status === 1 }"
        @click="filterByStatus(1)">
        <div class="stat-card__icon"><i class="el-icon-circle-check"></i></div>
        <div class="stat-card__body">
          <div class="stat-card__num">{{ stats.completed || 0 }}</div>
          <div class="stat-card__label">已完成</div>
        </div>
      </div>
      <div
        class="stat-card stat-card--cancel"
        :class="{ active: search.status === 2 }"
        @click="filterByStatus(2)">
        <div class="stat-card__icon"><i class="el-icon-circle-close"></i></div>
        <div class="stat-card__body">
          <div class="stat-card__num">{{ stats.cancelled || 0 }}</div>
          <div class="stat-card__label">已取消</div>
        </div>
      </div>
      <div
        class="stat-card stat-card--all"
        :class="{ active: search.status === null }"
        @click="filterByStatus(null)">
        <div class="stat-card__icon"><i class="el-icon-s-order"></i></div>
        <div class="stat-card__body">
          <div class="stat-card__num">{{ totalAll }}</div>
          <div class="stat-card__label">全部记录</div>
        </div>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="panel filter-panel">
      <el-form :inline="true" size="small" class="filter-form" @submit.native.prevent>
        <el-form-item label="名称">
          <el-input v-model="search.relatedName" placeholder="商品/服务名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="search.type" clearable placeholder="全部" style="width: 130px">
            <el-option label="实物商品" :value="0" />
            <el-option label="纪念品" :value="1" />
            <el-option label="景点门票" :value="2" />
            <el-option label="酒店" :value="3" />
            <el-option label="会员购买" :value="4" />
            <el-option label="优惠券" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="searchPage">查询</el-button>
          <el-button icon="el-icon-refresh-left" @click="refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <div class="panel table-panel">
      <div class="panel-header">
        <span class="panel-title"><i class="el-icon-s-order"></i> 兑换记录列表</span>
        <span class="panel-sub">共 {{ total }} 条</span>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        size="small"
        class="exchange-table"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 600 }">

        <el-table-column label="兑换商品" min-width="200">
          <template slot-scope="{ row }">
            <div class="item-cell">
              <div class="item-cell__name">{{ row.relatedName || '-' }}</div>
              <el-tag size="mini" effect="plain" class="type-tag">{{ typeLabel(row.type) }}</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="userName" label="用户" width="100" show-overflow-tooltip />

        <el-table-column label="消耗积分" width="100" align="center">
          <template slot-scope="{ row }">
            <span class="points-text">-{{ row.pointsUsed || 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="收货信息" min-width="200">
          <template slot-scope="{ row }">
            <div v-if="row.receiver || row.phone || row.address" class="ship-info">
              <div v-if="row.receiver"><i class="el-icon-user"></i> {{ row.receiver }}</div>
              <div v-if="row.phone"><i class="el-icon-phone"></i> {{ row.phone }}</div>
              <div v-if="row.address" class="ship-info__addr"><i class="el-icon-location-outline"></i> {{ row.address }}</div>
            </div>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="{ row }">
            <span class="status-badge" :class="'status-badge--' + row.status">
              {{ statusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="兑换时间" width="160" show-overflow-tooltip />

        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template slot-scope="{ row }">
            <div v-if="row.status === 0" class="action-group">
              <el-button type="text" size="small" class="action-btn action-btn--success" @click="handleComplete(row)">
                <i class="el-icon-check"></i> 发货
              </el-button>
              <el-button type="text" size="small" class="action-btn action-btn--danger" @click="handleCancel(row)">
                <i class="el-icon-close"></i> 取消
              </el-button>
            </div>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && !tableData.length" class="empty-state">
        <i class="el-icon-box"></i>
        <p>暂无兑换记录</p>
      </div>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          :page-size="search.pageSize"
          :current-page="search.pageNumber"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :total="total" />
      </div>
    </div>
  </div>
</template>

<script>
import { getSysExchangePage, completeExchange, cancelExchange, getExchangeStats } from '../../../api/api'

export default {
  data() {
    return {
      loading: true,
      search: {
        relatedName: '',
        type: null,
        status: 0,
        pageNumber: 1,
        pageSize: 10
      },
      total: 0,
      tableData: [],
      stats: {}
    }
  },
  computed: {
    totalAll() {
      return (this.stats.pending || 0) + (this.stats.completed || 0) + (this.stats.cancelled || 0)
    }
  },
  methods: {
    typeLabel(type) {
      const map = { 0: '实物商品', 1: '纪念品', 2: '景点门票', 3: '酒店', 4: '会员购买', 5: '优惠券' }
      return map[type] || '其他'
    },
    statusLabel(status) {
      return status === 0 ? '待处理' : status === 1 ? '已完成' : '已取消'
    },
    filterByStatus(status) {
      this.search.status = status
      this.search.pageNumber = 1
      this.query()
    },
    loadStats() {
      getExchangeStats().then(res => {
        if (res.code == 1000) this.stats = res.data || {}
      })
    },
    searchPage() {
      this.search.pageNumber = 1
      this.query()
    },
    query() {
      this.loading = true
      getSysExchangePage(this.search).then(res => {
        if (res.code == 1000) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      }).finally(() => {
        this.loading = false
      })
    },
    refresh() {
      this.search.relatedName = ''
      this.search.type = null
      this.search.status = 0
      this.search.pageNumber = 1
      this.query()
      this.loadStats()
    },
    handleCurrentChange(val) {
      this.search.pageNumber = val
      this.query()
    },
    handleSizeChange(val) {
      this.search.pageSize = val
      this.query()
    },
    handleComplete(row) {
      this.$prompt('可填写发货备注', '确认发货 · ' + row.relatedName, {
        confirmButtonText: '确认发货',
        cancelButtonText: '取消',
        inputPlaceholder: '备注（可选）'
      }).then(({ value }) => {
        completeExchange({ id: row.id, remark: value || '' }).then(res => {
          if (res.code == 1000) {
            this.$message.success('已确认发货')
            this.query()
            this.loadStats()
          } else {
            this.$notify.error({ title: '错误', message: res.message })
          }
        })
      }).catch(() => {})
    },
    handleCancel(row) {
      this.$confirm('取消后将退还用户积分并恢复库存（如适用），确认？', '取消兑换', {
        type: 'warning',
        confirmButtonText: '确认取消',
        cancelButtonText: '返回'
      }).then(() => {
        cancelExchange({ id: row.id }).then(res => {
          if (res.code == 1000) {
            this.$message.success('已取消并退还积分')
            this.query()
            this.loadStats()
          } else {
            this.$notify.error({ title: '错误', message: res.message })
          }
        })
      }).catch(() => {})
    }
  },
  mounted() {
    this.query()
    this.loadStats()
  }
}
</script>

<style lang="scss" scoped>
.exchange-page {
  padding: 4px 0 24px;
}

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-radius: 10px;
  padding: 18px 20px;
  cursor: pointer;
  border: 2px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.1);
  }

  &.active {
    border-color: currentColor;
  }

  &__icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    flex-shrink: 0;
  }

  &__num {
    font-size: 26px;
    font-weight: 700;
    line-height: 1.2;
  }

  &__label {
    font-size: 13px;
    color: #909399;
    margin-top: 2px;
  }

  &--pending {
    color: #e6a23c;
    .stat-card__icon { background: #fdf6ec; color: #e6a23c; }
    .stat-card__num { color: #e6a23c; }
  }

  &--done {
    color: #67c23a;
    .stat-card__icon { background: #f0f9eb; color: #67c23a; }
    .stat-card__num { color: #67c23a; }
  }

  &--cancel {
    color: #909399;
    .stat-card__icon { background: #f4f4f5; color: #909399; }
    .stat-card__num { color: #606266; }
  }

  &--all {
    color: #409eff;
    .stat-card__icon { background: #ecf5ff; color: #409eff; }
    .stat-card__num { color: #409eff; }
  }
}

/* 面板 */
.panel {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
}

.filter-panel {
  padding: 16px 20px 4px;
}

.filter-form ::v-deep .el-form-item {
  margin-bottom: 12px;
}

.table-panel {
  padding: 0 0 12px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f2f5;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;

  i {
    color: #409eff;
    margin-right: 4px;
  }
}

.panel-sub {
  font-size: 13px;
  color: #909399;
}

/* 表格内容 */
.exchange-table {
  width: 100%;
}

.item-cell__name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.type-tag {
  border: none;
  background: #f0f2f5;
  color: #606266;
}

.points-text {
  color: #f56c6c;
  font-weight: 600;
  font-size: 14px;
}

.ship-info {
  font-size: 12px;
  color: #606266;
  line-height: 1.8;

  i {
    color: #909399;
    margin-right: 4px;
  }

  &__addr {
    color: #909399;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 200px;
  }
}

.status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;

  &--0 { background: #fdf6ec; color: #e6a23c; }
  &--1 { background: #f0f9eb; color: #67c23a; }
  &--2 { background: #f4f4f5; color: #909399; }
}

.action-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
}

.action-btn {
  font-size: 13px;
  padding: 4px 6px;

  &--success { color: #67c23a; }
  &--danger { color: #f56c6c; }
}

.text-muted {
  color: #c0c4cc;
  font-size: 13px;
}

.empty-state {
  text-align: center;
  padding: 48px 0;
  color: #c0c4cc;

  i { font-size: 48px; margin-bottom: 12px; display: block; }
  p { font-size: 14px; margin: 0; }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 12px 20px 0;
}

@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
