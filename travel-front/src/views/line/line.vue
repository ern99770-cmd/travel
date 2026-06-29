<template>
  <PageLayout>
  <div class="line">
    <div class="line-container page-container">
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
        <SearchPanel title="线路筛选" icon="el-icon-guide" @search="searchPage" @reset="resetSearch">
          <el-input
            v-model="search.name"
            prefix-icon="el-icon-search"
            placeholder="线路名称"
            clearable
            class="search-field">
          </el-input>
          <el-input
            v-model="search.geography"
            prefix-icon="el-icon-location-outline"
            placeholder="地理情况"
            clearable
            class="search-field">
          </el-input>
          <el-input
            v-model="search.temperature"
            prefix-icon="el-icon-sunny"
            placeholder="温度"
            clearable
            class="search-field">
          </el-input>
        </SearchPanel>
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
  </div>
  </PageLayout>
</template>

<script>
  import {getSysLinePage} from '../../api/api'
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
    components: {},
    methods: {
      searchPage() {
        this.search.pageNumber = 1
        this.getSysLinePage()
      },
      resetSearch() {
        this.search.name = ''
        this.search.geography = ''
        this.search.temperature = ''
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
     display: flex;
     gap: 30px;
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