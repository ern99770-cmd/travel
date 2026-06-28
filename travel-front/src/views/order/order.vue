<template>
  <div class="attractions">
    <headers></headers>
    <div class="order-container" style="margin-top:20px; padding: 30px; background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%); min-height: 800px;">
      <div class="order-wrapper" style="display: flex; gap: 20px; max-width: 1400px; margin: 0 auto;">
        <!-- 景点订单 -->
        <div class="order-section" style="flex: 1; background: #fff; border-radius: 16px; padding: 25px; box-shadow: 0 8px 30px rgba(0,0,0,0.08);">
          <h2 style="text-align: center; margin: 0 0 25px 0; color: #2c3e50; font-size: 22px; font-weight: 600; border-bottom: 3px solid #3498db; padding-bottom: 12px;">
            <span style="color: #3498db;">🏛️</span> 我的景点预约
          </h2>
          <div v-if="!tableData || tableData.length === 0" style="text-align: center; padding: 40px; color: #999; font-size: 16px; background: #fafafa; border-radius: 8px;">
            暂无景点订单
          </div>
          <div v-else style="display: flex; flex-wrap: wrap; gap: 20px; justify-content: center;">
            <div v-for="(item, index) in tableData" :key="index"
                 style="width: 100%; border: 1px solid #e8e8e8; border-radius: 12px; overflow: hidden; background: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.06); cursor: pointer; transition: transform 0.3s, box-shadow 0.3s;"
                 @click="toOrderInfo(index)"
                 @mouseenter="e => {e.currentTarget.style.transform='translateY(-5px)'; e.currentTarget.style.boxShadow='0 12px 24px rgba(0,0,0,0.1)'}"
                 @mouseleave="e => {e.currentTarget.style.transform=''; e.currentTarget.style.boxShadow=''}">
              <img :src="item.images.split(',')[0]" 
                   style="width: 100%; height: 200px; object-fit: cover;"
                   onerror="this.src='https://via.placeholder.com/400x200?text=暂无图片'">
              <div style="padding: 18px;">
                  <h3 style="margin: 0 0 10px 0; font-size: 18px; color: #2c3e50; font-weight: 600;">{{item.name}}</h3>
                  <p style="margin: 0; color: #666; font-size: 14px; line-height: 1.7; max-height: 70px; overflow: hidden;">
                      {{item.introduce}}
                  </p>
              </div>
            </div>
          </div>
          <el-pagination
              background
              :page-size="search.pageSize"
              layout="prev, pager, next"
              @current-change="handleCurrentChange"
              :total="total"
              style="margin-top: 30px; text-align: center;">
          </el-pagination>
        </div>

        <!-- 酒店订单 -->
        <div class="order-section" style="flex: 1; background: #fff; border-radius: 16px; padding: 25px; box-shadow: 0 8px 30px rgba(0,0,0,0.08);">
          <h2 style="text-align: center; margin: 0 0 25px 0; color: #2c3e50; font-size: 22px; font-weight: 600; border-bottom: 3px solid #e74c3c; padding-bottom: 12px;">
            <span style="color: #e74c3c;">🏨</span> 我的酒店预约
          </h2>
          <div v-if="!tableData1 || tableData1.length === 0" style="text-align: center; color: #999; font-size: 16px; padding: 40px; background: #fafafa; border-radius: 8px;">
            暂无酒店订单
          </div>
          <div v-else style="display: flex; flex-wrap: wrap; gap: 20px; justify-content: center;">
            <div v-for="(item, index) in tableData1" :key="index"
                 style="width: 100%; border: 1px solid #e8e8e8; border-radius: 12px; overflow: hidden; background: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.06); cursor: pointer; transition: transform 0.3s, box-shadow 0.3s;"
                 @click="toOrderInfo1(index)"
                 @mouseenter="e => {e.currentTarget.style.transform='translateY(-5px)'; e.currentTarget.style.boxShadow='0 12px 24px rgba(0,0,0,0.1)'}"
                 @mouseleave="e => {e.currentTarget.style.transform=''; e.currentTarget.style.boxShadow=''}">
              <img :src="item.images.split(',')[0]" 
                   style="width: 100%; height: 200px; object-fit: cover;"
                   onerror="this.src='https://via.placeholder.com/400x200?text=暂无图片'">
              <div style="padding: 18px;">
                  <h3 style="margin: 0 0 10px 0; font-size: 18px; color: #2c3e50; font-weight: 600;">{{item.name}}</h3>
                  <p style="margin: 0; color: #666; font-size: 14px; line-height: 1.7; max-height: 70px; overflow: hidden;">
                      {{item.introduce}}
                  </p>
              </div>
            </div>
          </div>
          <el-pagination
              background
              :page-size="search1.pageSize"
              layout="prev, pager, next"
              @current-change="handleCurrentChange1"
              :total="total1"
              style="margin-top: 30px; text-align: center;">
          </el-pagination>
        </div>
      </div>
    </div>
    <bottoms></bottoms>

    <!-- 景点订单详情弹窗 -->
    <el-dialog title="预约详情" :visible.sync="dialogVisible" width="40%">
      <span>
        <div style="margin-bottom:10px">
          <el-button size="mini" type="primary" plain v-if="state == 0">未确认</el-button>
          <el-button size="mini" type="primary" plain v-if="state == 1">已确认</el-button>
          <el-button size="mini" type="primary" plain v-if="state == 2">取消中</el-button>
          <el-button size="mini" type="primary" plain v-if="state == 3">已取消</el-button>
          <el-button size="mini" type="primary" plain v-if="state == 4">取消失败</el-button>
          <el-button size="mini" type="primary" plain v-if="state == 5">已使用</el-button>
        </div>
        <el-input-number disabled size="small" v-model="num" :min="1" :max="10"></el-input-number>
        <el-date-picker disabled size="small" style="margin-left:20px"
        v-model="date1" type="date" placeholder="选择预约日期">
        </el-date-picker>
        <div v-for="(item,index) in people" style="margin-top: 10px;">
          <el-input size="small" disabled v-model="item.name" placeholder="请输入姓名"></el-input>
          <el-input size="small" disabled style="margin-left:10px" v-model="item.tel" placeholder="请输入电话"></el-input>
          <el-input size="small" disabled style="margin-left:10px" v-model="item.idCard" placeholder="请输入身份证号"></el-input>
        </div>
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="close" size="small">关 闭</el-button>
        <el-button type="primary" v-if="state != 5 && state != 3 && state != 2 && state != 4" @click="editSysAttractionOrder" size="small">取消预约</el-button>
      </span>
    </el-dialog>

    <!-- 酒店订单详情弹窗 -->
    <el-dialog title="预约详情" :visible.sync="dialogVisible1" width="40%">
      <span>
        <div style="margin-bottom:10px">
          <el-button size="mini" type="primary" plain v-if="state == 0">未确认</el-button>
          <el-button size="mini" type="primary" plain v-if="state == 1">已确认</el-button>
          <el-button size="mini" type="primary" plain v-if="state == 2">取消中</el-button>
          <el-button size="mini" type="primary" plain v-if="state == 3">已取消</el-button>
          <el-button size="mini" type="primary" plain v-if="state == 4">取消失败</el-button>
          <el-button size="mini" type="primary" plain v-if="state == 5">已使用</el-button>
        </div>
        <el-input-number disabled size="small" v-model="num" :min="1" :max="10"></el-input-number>
        <el-date-picker disabled size="small" style="margin-left:20px"
        v-model="date1" type="date" placeholder="选择预约日期">
        </el-date-picker>
        <div style="margin-top: 10px;">
          <el-input size="small" disabled v-model="people1.name" placeholder="请输入姓名"></el-input>
          <el-input size="small" disabled style="margin-left:10px" v-model="people1.tel" placeholder="请输入电话"></el-input>
          <el-input size="small" disabled style="margin-left:10px" v-model="people1.idCard" placeholder="请输入身份证号"></el-input>
        </div>
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="close1">关 闭</el-button>
        <el-button type="primary" v-if="state != 5 && state != 3 && state != 2 && state != 4" @click="editSysHotelOrder" size="small">取消预约</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  import {getSysHotelOrderPage,getSysAttractionOrderPage,editSysAttractionOrder,editSysHotelOrder} from '../../api/api'
  import headers from '@/components/header'
  import bottoms from '@/components/bottom'
  export default {
    data() {
      return{
        search: {
          userId: "",
          pageSize: 12,
          pageNumber: 1,
        },
        search1: {
          userId: "",
          pageSize: 12,
          pageNumber: 1,
        },
        total: 0,
        tableData: [],
        total1: 0,
        tableData1: [],
        id: "",
        state: "",
        num: "",
        date1: "",
        people: [],
        people1: {},
        dialogVisible: false,
        dialogVisible1: false
      }
    },
    components: {
      headers,
      bottoms
    },
    methods: {
      editSysAttractionOrder() {
        var param = {
          id: this.id,
          state: 2,
        }
        editSysAttractionOrder(param).then(res => {
          if(res.code == 1000) {
            this.$message({
                message: '取消申请成功,请等待审核通过',
                type: 'success'
            });
            this.close()
            this.getSysAttractionOrderPage()
          }
        })
      },
      editSysHotelOrder() {
        var param = {
          id: this.id,
          state: 2,
        }
        editSysHotelOrder(param).then(res => {
          if(res.code == 1000) {
            this.$message({
                message: '取消申请成功,请等待审核通过',
                type: 'success'
            });
            this.close1()
            this.getSysHotelOrderPage()
          }
        })
      },
      toOrderInfo(index) {
        var data = this.tableData[index]
        this.id = data.id
        this.people = JSON.parse(data.people)
        this.num = data.num
        this.date1 = data.time
        this.state = data.state
        this.dialogVisible = true
      },
      toOrderInfo1(index) {
        var data = this.tableData1[index]
        this.id = data.id
        this.people1 = JSON.parse(data.people)
        this.num = data.num
        this.date1 = data.time
        this.state = data.state
        this.dialogVisible1 = true
      },
      close() {
        this.id = ""
        this.people = []
        this.num = ""
        this.date1 = ""
        this.state = ""
        this.dialogVisible = false
      },
      close1() {
        this.id = ""
        this.people1 = {}
        this.num = ""
        this.date1 = ""
        this.state = ""
        this.dialogVisible1 = false
      },
      getSysAttractionOrderPage() {
        getSysAttractionOrderPage(this.search).then(res => {
          if (res.code == 1000) {
            this.tableData = res.data.records
            this.total = res.data.total
          }
        })
      },
      getSysHotelOrderPage() {
        getSysHotelOrderPage(this.search1).then(res => {
          if (res.code == 1000) {
            this.tableData1 = res.data.records
            this.total1 = res.data.total
          }
        })
      },
      handleCurrentChange(val) {
        this.search.pageNumber = val
        this.getSysAttractionOrderPage()
      },
      handleCurrentChange1(val) {
        this.search1.pageNumber = val
        this.getSysHotelOrderPage()
      }
    },
    mounted() {
      const userInfo = window.localStorage.getItem("user_info")
      if (userInfo) {
        const user = JSON.parse(userInfo)
        this.search.userId = user.id
        this.search1.userId = user.id
        this.getSysAttractionOrderPage()
        this.getSysHotelOrderPage()
      } else {
        console.warn("用户未登录")
      }
    }
  }
</script>

<style scoped>
   @import url('../../assets/css/attractions.css');
   .order-container {
       width: 100%;
   }
   .order-wrapper {
       width: 100%;
   }
</style>
