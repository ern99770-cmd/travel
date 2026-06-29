<template>
  <div class="_coupon">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding:10px">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">名称:</span>
            <el-input style="margin-top:10px" size="mini" placeholder="请输入名称" v-model="search.name"></el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">状态:</span>
            <el-select style="margin-top:10px;width:100%" size="mini" v-model="search.status" clearable placeholder="全部">
              <el-option label="上架" :value="1"></el-option>
              <el-option label="下架" :value="0"></el-option>
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <el-button style="margin-top:10px" size="mini" icon="el-icon-search" type="primary" @click="searchPage">查询</el-button>
            <el-button style="margin-top:10px" size="mini" icon="el-icon-refresh" @click="refresh">重置</el-button>
          </el-col>
        </el-row>
      </div>
      <div class="table">
        <el-row style="padding-top:10px;margin-left:10px">
          <el-button type="primary" size="mini" icon="el-icon-plus" plain @click="add">新增</el-button>
          <el-button type="success" :disabled="update.length != 1" size="mini" icon="el-icon-edit" plain @click="updateDataBtn">修改</el-button>
          <el-button type="danger" :disabled="update.length <= 0" size="mini" icon="el-icon-delete" plain @click="deleteDataBtn">删除</el-button>
        </el-row>
        <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" stripe style="width: 100%">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="name" label="名称" min-width="120"></el-table-column>
          <el-table-column label="优惠规则" min-width="130">
            <template slot-scope="scope">满{{ scope.row.minAmount }}减{{ scope.row.discountAmount }}</template>
          </el-table-column>
          <el-table-column prop="pointsRequired" label="所需积分" width="90"></el-table-column>
          <el-table-column label="会员等级" width="90">
            <template slot-scope="scope">
              {{ scope.row.requireLevel === 1 ? 'VIP' : scope.row.requireLevel === 2 ? 'SVIP' : '普通' }}
            </template>
          </el-table-column>
          <el-table-column label="库存" width="100">
            <template slot-scope="scope">
              <span :class="{ 'low-stock': scope.row.remainCount <= 10 }">{{ scope.row.remainCount }}/{{ scope.row.totalCount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="validDays" label="有效天数" width="90"></el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.status == 0">下架</el-tag>
              <el-tag v-if="scope.row.status == 1" type="success">上架</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="150"></el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template slot-scope="scope">
              <el-button size="mini" type="info" v-if="scope.row.status == 0" @click="toggleStatus(scope.row, 1)">上架</el-button>
              <el-button size="mini" type="info" v-if="scope.row.status == 1" @click="toggleStatus(scope.row, 0)">下架</el-button>
              <el-button size="mini" type="warning" @click="adjustStock(scope.row)">补库存</el-button>
              <el-button size="mini" type="success" @click="updateData(scope.row.id)">修改</el-button>
              <el-popconfirm
                style="margin-left:5px"
                confirm-button-text="确认"
                cancel-button-text="取消"
                icon="el-icon-info"
                icon-color="red"
                title="确认删除该优惠券？"
                @confirm="deleteDate(scope.row.id)">
                <el-button size="mini" slot="reference" type="danger">删除</el-button>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="search.pageSize"
          :current-page="search.pageNumber"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
    </div>
    <add @addFalse="addFalse" :addVisible="addVisible"></add>
    <update @updateFalse="updateFalse" :updateId="updateId" :updateVisible="updateVisible"></update>
  </div>
</template>

<script>
import { getSysCouponPage, removeSysCoupon, toggleCouponStatus, adjustCouponStock } from '../../../api/api'
import add from '../../../components/system/coupon/addCoupon'
import update from '../../../components/system/coupon/updateCoupon'

export default {
  data() {
    return {
      loading: true,
      update: [],
      remove: [],
      updateId: '',
      addVisible: false,
      updateVisible: false,
      search: {
        name: '',
        status: null,
        pageNumber: 1,
        pageSize: 10
      },
      total: 0,
      tableData: []
    }
  },
  components: { add, update },
  methods: {
    searchPage() {
      this.search.pageNumber = 1
      this.query()
    },
    query() {
      getSysCouponPage(this.search).then(res => {
        if (res.code == 1000) {
          this.tableData = res.data.records
          this.total = res.data.total
          this.loading = false
        } else {
          this.$notify.error({ title: '错误', message: res.message })
        }
      })
    },
    refresh() {
      this.search.name = ''
      this.search.status = null
      this.search.pageNumber = 1
      this.query()
    },
    handleCurrentChange(val) {
      this.search.pageNumber = val
      this.query()
    },
    handleSizeChange(val) {
      this.search.pageSize = val
      this.query()
    },
    handleSelectionChange(val) {
      this.update = val.map(item => item.id)
      this.remove = this.update.slice()
    },
    add() {
      this.addVisible = true
    },
    addFalse() {
      this.addVisible = false
      this.query()
    },
    updateFalse() {
      this.updateId = ''
      this.updateVisible = false
      this.query()
    },
    updateData(id) {
      this.updateId = id
      this.updateVisible = true
    },
    updateDataBtn() {
      this.updateData(this.update[0])
    },
    toggleStatus(row, status) {
      toggleCouponStatus({ id: row.id, status }).then(res => {
        if (res.code == 1000) {
          this.query()
        } else {
          this.$notify.error({ title: '错误', message: res.message })
        }
      })
    },
    adjustStock(row) {
      this.$prompt('请输入补库存数量', '补库存 - ' + row.name, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^[1-9]\d*$/,
        inputErrorMessage: '请输入大于0的整数'
      }).then(({ value }) => {
        adjustCouponStock({ id: row.id, addCount: Number(value) }).then(res => {
          if (res.code == 1000) {
            this.$message.success('补库存成功')
            this.query()
          } else {
            this.$notify.error({ title: '错误', message: res.message })
          }
        })
      }).catch(() => {})
    },
    deleteDataBtn() {
      this.$confirm('确定删除选中的 ' + this.remove.length + ' 条数据?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.deleteDate(this.remove.join(','))
      }).catch(() => {})
    },
    deleteDate(ids) {
      removeSysCoupon({ ids }).then(res => {
        if (res.code == 1000) {
          this.$message.success('删除成功')
          this.query()
        } else {
          this.$notify.error({ title: '错误', message: res.message })
        }
      })
    }
  },
  mounted() {
    this.query()
  }
}
</script>

<style lang=scss scoped>
.search-table { width: 100%; }
.search {
  background: #ffffff;
  border-radius: 7px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}
.table {
  background: #ffffff;
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
  float: right;
  white-space: nowrap;
  font-size: 14px;
  margin-top: 10px;
  width: 63px;
  text-align: right;
}
.el-table { padding: 10px; }
.low-stock { color: #f56c6c; font-weight: 600; }
</style>
