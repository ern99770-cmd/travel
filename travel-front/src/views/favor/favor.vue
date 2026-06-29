<template>
  <PageLayout>
  <div class="favor-page">
    <div class="favor-container page-container">
      <div class="favor-sidebar">
        <div class="sidebar-title">
          <i class="el-icon-star-on"></i> 我的收藏
        </div>
        <div class="stat-block">
          <div class="stat-number">{{ total }}</div>
          <div class="stat-label">已收藏路线</div>
        </div>
        <div class="sidebar-tip">
          <i class="el-icon-info"></i>
          收藏心仪的路线，方便下次快速查看与预订。
        </div>
        <el-button type="primary" plain size="small" class="browse-btn" @click="$router.push('/line')">
          去发现更多路线
        </el-button>
      </div>

      <div class="favor-main">
        <div class="section-header">
          <i class="el-icon-collection-tag"></i> 收藏清单
        </div>

        <div v-if="tableData.length > 0" class="favor-grid">
          <div class="favor-card" v-for="(item, index) in tableData" :key="index" @click="toInfo(item.lineId)">
            <div class="favor-card-img">
              <img :src="item.images.split(',')[0]" alt="">
            </div>
            <div class="favor-card-body">
              <div class="favor-card-title">{{ item.name }}</div>
              <div class="favor-card-desc">{{ item.introduce }}</div>
            </div>
          </div>
        </div>

        <el-empty v-else description="还没有收藏任何路线" :image-size="120">
          <el-button type="primary" size="small" @click="$router.push('/line')">去看看旅游线路</el-button>
        </el-empty>

        <el-pagination
          v-if="total > search.pageSize"
          background
          :page-size="search.pageSize"
          layout="prev, pager, next"
          :current-page="search.pageNumber"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
    </div>
  </div>
  </PageLayout>
</template>

<script>
  import {getSysFavorPage} from '../../api/api'
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
   .favor-page {
     width: 100%;
     background-color: #f5f7fa;
     padding-bottom: 40px;
   }

   .favor-container {
     display: flex;
     gap: 24px;
   }

   .favor-sidebar {
     width: 280px;
     background: #fff;
     border-radius: 12px;
     box-shadow: 0 4px 12px rgba(0,0,0,0.05);
     padding: 24px;
     height: fit-content;
     position: sticky;
     top: var(--page-sticky-top, 104px);
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

   .stat-block {
     text-align: center;
     padding: 16px 0;
   }

   .stat-number {
     font-size: 36px;
     font-weight: 700;
     color: #409EFF;
     line-height: 1.2;
   }

   .stat-label {
     margin-top: 6px;
     font-size: 14px;
     color: #909399;
   }

   .sidebar-tip {
     margin-top: 16px;
     padding: 12px;
     background: #f5f7fa;
     border-radius: 8px;
     font-size: 13px;
     color: #909399;
     line-height: 1.6;
   }

   .sidebar-tip i {
     margin-right: 4px;
     color: #409EFF;
   }

   .browse-btn {
     width: 100%;
     margin-top: 16px;
   }

   .favor-main {
     flex: 1;
     min-width: 0;
   }

   .section-header {
     font-size: 20px;
     font-weight: 600;
     margin-bottom: 20px;
     color: #303133;
     display: flex;
     align-items: center;
     gap: 10px;
   }

   .section-header i {
     color: #409EFF;
   }

   .favor-grid {
     display: flex;
     flex-wrap: wrap;
     gap: 20px;
   }

   .favor-card {
     width: calc(33.333% - 14px);
     background: #fff;
     border-radius: 12px;
     overflow: hidden;
     box-shadow: 0 2px 12px rgba(0,0,0,0.08);
     cursor: pointer;
     transition: all 0.3s;
   }

   .favor-card:hover {
     transform: translateY(-4px);
     box-shadow: 0 8px 20px rgba(0,0,0,0.12);
   }

   .favor-card-img {
     height: 180px;
     overflow: hidden;
   }

   .favor-card-img img {
     width: 100%;
     height: 100%;
     object-fit: cover;
     transition: transform 0.3s;
   }

   .favor-card:hover .favor-card-img img {
     transform: scale(1.05);
   }

   .favor-card-body {
     padding: 16px;
   }

   .favor-card-title {
     font-size: 16px;
     font-weight: 600;
     color: #303133;
     margin-bottom: 8px;
   }

   .favor-card-desc {
     font-size: 13px;
     color: #909399;
     line-height: 1.6;
     display: -webkit-box;
     -webkit-line-clamp: 2;
     -webkit-box-orient: vertical;
     overflow: hidden;
   }

   .el-pagination {
     display: flex;
     justify-content: center;
     margin-top: 24px;
   }

   @media (max-width: 992px) {
     .favor-container {
       flex-direction: column;
     }
     .favor-sidebar {
       width: 100%;
       position: static;
     }
     .favor-card {
       width: calc(50% - 10px);
     }
   }

   @media (max-width: 576px) {
     .favor-card {
       width: 100%;
     }
   }
</style>
