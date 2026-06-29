<template>
  <div class="_member-list">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding:10px">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">用户名:</span>
            <el-input style="margin-top:10px" size="mini" placeholder="请输入用户名" v-model="search.userName"></el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">等级:</span>
            <el-select style="margin-top:10px;width:100%" size="mini" v-model="search.level" clearable placeholder="全部">
              <el-option label="普通用户" :value="0"></el-option>
              <el-option label="VIP" :value="1"></el-option>
              <el-option label="SVIP" :value="2"></el-option>
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <el-button style="margin-top:10px" size="mini" icon="el-icon-search" type="primary" @click="searchPage">查询</el-button>
            <el-button style="margin-top:10px" size="mini" icon="el-icon-refresh" @click="refresh">重置</el-button>
          </el-col>
        </el-row>
        <el-row style="padding:0 10px 10px">
          <el-tag>会员总数 {{ memberStats.total || 0 }}</el-tag>
          <el-tag type="warning" style="margin-left:8px">VIP {{ memberStats.vip || 0 }}</el-tag>
          <el-tag type="danger" style="margin-left:8px">SVIP {{ memberStats.svip || 0 }}</el-tag>
        </el-row>
      </div>
      <div class="table">
        <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
          <el-table-column prop="userName" label="用户名" width="120"></el-table-column>
          <el-table-column label="等级" width="90">
            <template slot-scope="scope">
              {{ scope.row.level === 2 ? 'SVIP' : scope.row.level === 1 ? 'VIP' : '普通' }}
            </template>
          </el-table-column>
          <el-table-column prop="points" label="积分余额" width="100"></el-table-column>
          <el-table-column prop="signInCount" label="连续签到" width="90"></el-table-column>
          <el-table-column prop="vipExpireTime" label="VIP到期" min-width="150"></el-table-column>
          <el-table-column prop="lastSignInTime" label="最后签到" min-width="150"></el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template slot-scope="scope">
              <el-button size="mini" type="primary" @click="viewLogs(scope.row)">积分流水</el-button>
              <el-button size="mini" type="warning" @click="adjustPoints(scope.row)">调整积分</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          :page-size="search.pageSize"
          :current-page="search.pageNumber"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
    </div>

    <el-dialog title="积分流水" :visible.sync="logVisible" width="600px">
      <el-table :data="pointsLogs" size="small" max-height="400">
        <el-table-column prop="description" label="说明" min-width="180"></el-table-column>
        <el-table-column prop="changePoints" label="变动" width="80">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.changePoints > 0 ? '#67c23a' : '#f56c6c' }">
              {{ scope.row.changePoints > 0 ? '+' : '' }}{{ scope.row.changePoints }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="afterPoints" label="余额" width="80"></el-table-column>
        <el-table-column prop="createTime" label="时间" min-width="150"></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { getSysMemberPage, getMemberPointsLog, adjustMemberPoints, getMemberStats } from '../../../api/api'

export default {
  data() {
    return {
      loading: true,
      search: {
        userName: '',
        level: null,
        pageNumber: 1,
        pageSize: 10
      },
      total: 0,
      tableData: [],
      memberStats: {},
      logVisible: false,
      pointsLogs: [],
      currentUserId: ''
    }
  },
  methods: {
    loadStats() {
      getMemberStats().then(res => {
        if (res.code == 1000) this.memberStats = res.data || {}
      })
    },
    searchPage() {
      this.search.pageNumber = 1
      this.query()
    },
    query() {
      getSysMemberPage(this.search).then(res => {
        if (res.code == 1000) {
          this.tableData = res.data.records
          this.total = res.data.total
          this.loading = false
        }
      })
    },
    refresh() {
      this.search.userName = ''
      this.search.level = null
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
    viewLogs(row) {
      this.currentUserId = row.userId
      getMemberPointsLog({ userId: row.userId, pageNumber: 1, pageSize: 50 }).then(res => {
        if (res.code == 1000) {
          this.pointsLogs = res.data.records || []
          this.logVisible = true
        }
      })
    },
    adjustPoints(row) {
      this.$prompt('正数为增加，负数为扣减', '调整积分 - ' + row.userName, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^-?\d+$/,
        inputErrorMessage: '请输入整数'
      }).then(({ value }) => {
        const changePoints = Number(value)
        this.$prompt('请输入调整原因', '备注', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '如：活动补偿'
        }).then(({ value: reason }) => {
          adjustMemberPoints({
            userId: row.userId,
            changePoints,
            reason: reason || '管理员调整积分'
          }).then(res => {
            if (res.code == 1000) {
              this.$message.success('调整成功')
              this.query()
            } else {
              this.$notify.error({ title: '错误', message: res.message })
            }
          })
        }).catch(() => {})
      }).catch(() => {})
    }
  },
  mounted() {
    this.query()
    this.loadStats()
  }
}
</script>

<style lang=scss scoped>
.search-table { width: 100%; }
.search {
  background: #fff;
  border-radius: 7px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}
.table {
  background: #fff;
  border-radius: 7px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
  margin-top: 10px;
}
.el-col {
  display: flex;
  flex-direction: row;
  align-items: center;
}
.search-title {
  font-family: '黑体';
  white-space: nowrap;
  font-size: 14px;
  margin-top: 10px;
  width: 63px;
  text-align: right;
}
.el-table { padding: 10px; }
</style>
