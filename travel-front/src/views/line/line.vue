<template>
  <div class="line">
    <headers></headers>
    <div class="line-container" style="background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%); padding: 30px 0; min-height: 100vh;">
      <!-- 左侧：销量排名 -->
      <div class="sales-sidebar">
        <div class="sidebar-title">
          <i class="el-icon-guide"></i> 热门路线榜单
        </div>
        <div class="ranking-list">
          <div v-for="(item, index) in hotLines" :key="index" class="ranking-item" @click="toInfo(item.id)">
            <div class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
            <img :src="item.images.split(',')[0]" class="rank-img">
            <div class="rank-info">
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-sales">
                <i class="el-icon-position"></i> 
                总热度: <span class="sales-count">{{ item.temperature || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：主要内容 -->
      <div class="line-main">
        <div class="search-container" style="background: #fff; border-radius: 16px; padding: 25px; box-shadow: 0 8px 30px rgba(0,0,0,0.08);">
            <div class="search-box">
                <div class="search-item">
                    <i class="el-icon-search"></i>
                    <el-input 
                        v-model="search.name" 
                        placeholder="     请输入旅游线路名称"
                        clearable>
                    </el-input>
                </div>
                <div class="search-item">
                    <i class="el-icon-location"></i>
                    <el-input 
                        v-model="search.geography" 
                        placeholder="     输入地理情况"
                        clearable>
                    </el-input>
                </div>
                <div class="search-item">
                    <i class="el-icon-sunny"></i>
                    <el-input 
                        v-model="search.temperature" 
                        placeholder="     请输入温度"
                        clearable>
                    </el-input>
                </div>
                <el-button type="primary" @click="searchPage" style="border-radius: 20px; padding: 10px 25px;">搜索</el-button>
            </div>
        </div>
        <div class="line3">
            <div v-for="(item,index) in tableData" :key="index" 
                 style="width: 31%; border: 1px solid #e8e8e8; border-radius: 12px; overflow: hidden; background: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.06); cursor: pointer; transition: transform 0.3s, box-shadow 0.3s; margin-bottom: 20px;"
                 @click="toInfo(item.id)"
                 @mouseenter="e => {e.currentTarget.style.transform='translateY(-5px)'; e.currentTarget.style.boxShadow='0 12px 24px rgba(0,0,0,0.1)'}"
                 @mouseleave="e => {e.currentTarget.style.transform=''; e.currentTarget.style.boxShadow=''}">
                <img :src="item.images.split(',')[0]" 
                     style="width: 100%; height: 200px; object-fit: cover;"
                     onerror="this.src='https://via.placeholder.com/400x200?text=暂无图片'">
                <div style="padding: 18px;">
                    <h3 style="margin: 0 0 10px 0; font-size: 18px; color: #2c3e50; font-weight: 600;">{{item.name}}</h3>
                    <div style="display: flex; align-items: center; margin-bottom: 8px; color: #409EFF; font-size: 14px;">
                        <i class="el-icon-fire" style="margin-right: 5px;"></i>
                        热度: {{ item.temperature || 0 }}
                    </div>
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
            :total="total">
        </el-pagination>
      </div>
    </div>
    <bottoms></bottoms>
  </div>
</template>

<script>
  import {getSysLinePage} from '../../api/api'
  import headers from '@/components/header'
  import bottoms from '@/components/bottom'
  export default {
    data() {
      return{
        search: {
          name: "",
          geography: "",
          temperature: "",
          pageSize: 12,
          pageNumber: 1,
        },
        total: 0,
        tableData: [],
        hotLines: []
      }
    },
    components: {
      headers,
      bottoms
    },
    methods: {
      searchPage() {
        this.search.pageNumber = 1
        this.getSysLinePage()
      },
      getSysLinePage() {
        getSysLinePage(this.search).then(res => {
          if (res.code == 1000) {
            this.tableData = res.data.records
            this.total = res.data.total
            // 更新热门路线（如果是第一页）
            if (this.search.pageNumber === 1 && this.tableData.length > 0) {
              this.hotLines = [...this.tableData].slice(0, 10)
            }
          }
        })
      },
      toInfo(id) {
        this.$router.push("/lineInfo?id=" + id)
      },
      handleCurrentChange(val) {
        this.search.pageNumber = val
        this.getSysLinePage()
      }, 
    },
    mounted() {
      this.getSysLinePage()
    }
 }
</script>

<style scoped>
   @import url('../../assets/css/line.css');
   
   .line-container {
     max-width: 1400px;
     margin: -10px auto 40px;
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
     padding: 20px;
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
     color: #409EFF;
   }

   .ranking-list {
     display: flex;
     flex-direction: column;
     gap: 15px;
   }

   .ranking-item {
     display: flex;
     gap: 12px;
     padding: 10px;
     border-radius: 8px;
     transition: all 0.3s;
     cursor: pointer;
   }

   .ranking-item:hover {
     background-color: #f5f7fa;
     transform: translateX(5px);
   }

   .rank-badge {
     width: 24px;
     height: 24px;
     background: #f0f2f5;
     color: #909399;
     border-radius: 4px;
     display: flex;
     align-items: center;
     justify-content: center;
     font-size: 12px;
     font-weight: 700;
     flex-shrink: 0;
   }

   .rank-1 { background: #f5222d; color: #fff; }
   .rank-2 { background: #fa8c16; color: #fff; }
   .rank-3 { background: #fadb14; color: #fff; }

   .rank-img {
     width: 60px;
     height: 60px;
     border-radius: 6px;
     object-fit: cover;
     flex-shrink: 0;
   }

   .rank-info {
     flex: 1;
     overflow: hidden;
   }

   .rank-name {
     font-size: 14px;
     font-weight: 600;
     color: #303133;
     margin-bottom: 6px;
     white-space: nowrap;
     overflow: hidden;
     text-overflow: ellipsis;
   }

   .rank-sales {
     font-size: 12px;
     color: #909399;
     display: flex;
     align-items: center;
     gap: 4px;
   }

   .sales-count {
     color: #f56c6c;
     font-weight: 700;
   }

   /* 右侧主内容 */
   .line-main {
     flex: 1;
     overflow: hidden;
   }

   .search-container {
     width: 100%;
     padding: 20px 0;
     background-color: #fff;
     border-radius: 8px;
     box-shadow: 0 2px 12px rgba(0,0,0,0.1);
     margin-bottom: 20px;
   }
   
   .search-box {
     display: flex;
     align-items: center;
     justify-content: flex-start;
     gap: 20px;
     padding: 0 20px;
   }
   
   .search-item {
     position: relative;
     display: flex;
     align-items: center;
     width: 220px;
   }
   
   .search-item i {
     position: absolute;
     left: 15px;
     z-index: 2;
     color: #909399;
   }
   
   .search-item .el-input {
     width: 100%;
   }
   
   ::v-deep .el-input__inner {
     padding-left: 40px !important;
     border-radius: 20px;
   }
   
   .line3 {
     width: 100%;
     display: flex;
     flex-wrap: wrap;
     justify-content: flex-start;
     gap: 20px;
     margin-top: 20px;
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