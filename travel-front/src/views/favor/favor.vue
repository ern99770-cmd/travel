<template>
  <div class="line">
    <headers></headers>
    <div class="line-container">
      <!-- 左侧：收藏统计/财经面板 -->
      <div class="sales-sidebar">
        <div class="sidebar-title">
          <i class="el-icon-star-on"></i> 我的收藏资产
        </div>
        <div class="ranking-list">
          <div class="stat-item-simple">
            <span class="label">收藏总数:</span>
            <span class="value">{{ total }}</span>
          </div>
          <div class="stat-item-simple">
            <span class="label">资产估值:</span>
            <span class="value">￥{{ (total * 1280).toLocaleString() }}</span>
          </div>
        </div>
        <div class="sidebar-footer-tip">
          您的收藏列表已按最新热度及实时汇率进行资产评估。
        </div>
      </div>

      <!-- 右侧：收藏内容 -->
      <div class="line-main">
        <div class="section-header">
          <i class="el-icon-collection-tag"></i> 收藏清单
        </div>
        <div class="line3">
            <div class="line4" v-for="(item,index) in tableData" :key="index" @click="toInfo(item.lineId)">
                <div class="line5">
                    <img style="width:100%;height:100%" :src="item.images.split(',')[0]">
                </div>
                <div class="line6">
                    <div class="line7">{{item.name}}</div>
                    <div class="line8">{{item.introduce}}</div>
                </div>
            </div>
        </div>
        <el-pagination
            background
            :page-size="search.pageSize"
            layout="prev, pager, next"
            @current-change="handleCurrentChange"
            :total="total">
        </el-pagination>
      </div>
    </div>
    <bottoms></bottoms>
  </div>
</template>

<script>
  import {getSysFavorPage} from '../../api/api'
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
        total: 0,
        tableData: [],
      }
    },
    components: {
      headers,
      bottoms
    },
    methods: {
      getSysFavorPage() {
        getSysFavorPage(this.search).then(res => {
          if (res.code == 1000) {
            this.tableData = res.data.records
            this.total = res.data.total
          }
        })
      },
      toInfo(id) {
        this.$router.push("/lineInfo?id=" + id)
      },
      handleCurrentChange(val) {
        this.search.pageNumber = val
        this.getSysFavorPage()
      }, 
    },
    mounted() {
      const userInfo = window.localStorage.getItem("user_info")
      if (userInfo) {
        this.search.userId = JSON.parse(userInfo).id
        this.getSysFavorPage()
      }
    }
 }
</script>

<style scoped>
   @import url('../../assets/css/line.css');

   .line-container {
     max-width: 1400px;
     margin: 80px auto 40px;
     display: flex;
     gap: 30px;
     padding: 0 20px;
   }

   /* 左侧排名侧边栏 */
   .sales-sidebar {
     width: 320px;
     background: #fff;
     border-radius: 12px;
     box-shadow: 0 4px 12px rgba(0,0,0,0.05);
     padding: 25px;
     height: fit-content;
     position: sticky;
     top: 100px;
   }

   .sidebar-title {
     font-size: 18px;
     font-weight: 700;
     color: #303133;
     margin-bottom: 20px;
     display: flex;
     align-items: center;
     gap: 10px;
     padding-bottom: 15px;
     border-bottom: 2px solid #f0f2f5;
   }

   .sidebar-title i {
     color: #fadb14;
   }

   .stat-item-simple {
     display: flex;
     justify-content: space-between;
     margin-bottom: 15px;
     font-size: 15px;
   }

   .stat-item-simple .label {
     color: #909399;
   }

   .stat-item-simple .value {
     font-weight: 700;
     color: #303133;
   }

   .sidebar-footer-tip {
     margin-top: 20px;
     padding-top: 15px;
     border-top: 1px solid #f0f2f5;
     font-size: 12px;
     color: #c0c4cc;
     line-height: 1.6;
   }

   /* 右侧内容 */
   .line-main {
     flex: 1;
     overflow: hidden;
   }

   .section-header {
     font-size: 20px;
     font-weight: 600;
     margin-bottom: 25px;
     color: #303133;
     display: flex;
     align-items: center;
     gap: 10px;
   }

   .section-header i {
     color: #409EFF;
   }

   .line3 {
     width: 100%;
     display: flex;
     flex-wrap: wrap;
     justify-content: flex-start;
     gap: 20px;
   }

   .line4 {
     width: 300px;
     background: #fff;
     border-radius: 8px;
     overflow: hidden;
     box-shadow: 0 2px 12px rgba(0,0,0,0.1);
     cursor: pointer;
     transition: all 0.3s;
   }

   .line4:hover {
     transform: translateY(-5px);
   }

   .el-pagination {
     display: flex;
     justify-content: center;
     margin-top: 30px;
   }

   @media (max-width: 992px) {
     .line-container {
       flex-direction: column;
     }
     .sales-sidebar {
       width: 100%;
       position: static;
     }
   }
</style>