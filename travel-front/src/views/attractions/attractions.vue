<template>
  <PageLayout>
  <div class="attractions">
    <div class="attractions-container page-container">
      <!-- 左侧：销量排名 -->
      <div class="sales-sidebar">
        <div class="sidebar-title">
          <i class="el-icon-trophy"></i> 
          景点销量榜单
          <span v-if="currentHotLocation" class="location-tag">{{ currentHotLocation }}</span>
        </div>
        <div class="ranking-list">
          <div v-for="(item, index) in hotAttractions" :key="index" class="ranking-item" @click="toInfo(item.id)">
            <div class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
            <img :src="item.images.split(',')[0]" class="rank-img">
            <div class="rank-info">
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-sales">
                <i class="el-icon-shopping-cart-2"></i> 
                总预约: <span class="sales-count">{{ item.realOrderCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：主要内容 -->
      <div class="attractions-main">
        <SearchPanel title="景点筛选" @search="searchPage" @reset="resetSearch">
          <el-input
            v-model="search.name"
            prefix-icon="el-icon-search"
            placeholder="景点名称"
            clearable
            class="search-field">
          </el-input>
          <el-select
            v-model="search.scenicType"
            placeholder="景点类型"
            clearable
            class="search-field">
            <el-option label="自然风光" value="自然风光"></el-option>
            <el-option label="人文历史" value="人文历史"></el-option>
            <el-option label="美食体验" value="美食体验"></el-option>
            <el-option label="购物娱乐" value="购物娱乐"></el-option>
          </el-select>
          <el-input
            v-model="search.location"
            prefix-icon="el-icon-location-outline"
            placeholder="所在地区，如：北京"
            clearable
            class="search-field">
          </el-input>
          <template #quick>
            <span class="quick-label">快捷筛选</span>
            <el-button
              type="text"
              size="small"
              :loading="locating"
              @click="detectLocation">
              <i class="el-icon-aim"></i> 当前定位
            </el-button>
            <el-button
              v-if="profileLocation"
              type="text"
              size="small"
              @click="useProfileLocation">
              <i class="el-icon-user"></i> 资料地区（{{ profileLocation }}）
            </el-button>
          </template>
        </SearchPanel>
        <div class="attractions3">
          <div class="index5" v-for="(item,index) in tableData" :key="index" style="margin-top:15px">
            <img style="width:100%;height:300px" :src="item.images.split(',')[0]">
            <div class="index6">
              <div class="index7">{{item.name}}</div>
              <div class="index8">{{item.introduce}}</div>
              <div class="index9" @click="toInfo(item.id)" style="margin-bottom:10px;cursor: pointer;margin-top:10px">
                预 约
              </div>
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
  import {getSysAttractionsPage, getUser} from '../../api/api'
  import { detectBrowserLocation } from '@/utils/geoLocation'
  export default {
    data() {
      return{
        search: {
          name: "",
          scenicType: "",
          location: "",
          state: 1,
          pageSize: 9,
          pageNumber: 1,
        },
        total: 0,
        tableData: [],
        hotAttractions: [],
        profileLocation: "",
        detectedLocation: "",
        locating: false,
      }
    },
    components: {},
    computed: {
      currentHotLocation() {
        return (this.search.location && this.search.location.trim()) ? this.search.location : ''
      }
    },
    methods: {
      searchPage() {
        this.search.pageNumber = 1
        this.getSysAttractionsPage()
        this.getSysAttractionsHot()
      },
      useProfileLocation() {
        if (!this.profileLocation) return
        this.search.location = this.profileLocation
        this.search.pageNumber = 1
        this.getSysAttractionsPage()
        this.getSysAttractionsHot()
        this.$message.success(`已按资料地区「${this.profileLocation}」筛选`)
      },
      detectLocation() {
        this.locating = true
        detectBrowserLocation()
          .then(location => {
            this.detectedLocation = location
            this.search.location = location
            this.search.pageNumber = 1
            this.getSysAttractionsPage()
            this.getSysAttractionsHot()
            this.$message.success(`定位成功：${location}`)
          })
          .catch(err => {
            this.$message.warning(err.message || '定位失败')
          })
          .finally(() => {
            this.locating = false
          })
      },
      resetSearch() {
        this.search.name = ''
        this.search.scenicType = ''
        this.search.location = ''
        this.search.pageNumber = 1
        this.getSysAttractionsPage()
        this.getSysAttractionsHot(true)
      },
      getSysAttractionsPage() {
        getSysAttractionsPage(this.search).then(res => {
          if (res.code == 1000) {
            this.tableData = res.data.records
            this.total = res.data.total
          }
        })
      },
      getUserInfo() {
        this.applyRouteQuery()
        getUser().then(res => {
          if (res.code == 1000 && res.data) {
            this.profileLocation = (res.data.location || '').trim()
          }
          const hasLocationFilter = this.search.location && this.search.location.trim()
          this.getSysAttractionsPage()
          this.getSysAttractionsHot(!hasLocationFilter)
        }).catch(() => {
          this.getSysAttractionsPage()
          this.getSysAttractionsHot(!(this.search.location && this.search.location.trim()))
        })
      },
      applyRouteQuery() {
        const { location, fromPlan } = this.$route.query
        if (location) {
          this.search.location = location
          if (fromPlan === '1') {
            this.$nextTick(() => {
              this.$message.success(`已按行程目的地「${location}」筛选景点`)
            })
          }
        }
      },
      getSysAttractionsHot(forceNoFilter = false) {
        const hotSearchParams = {
          pageSize: 100,
          pageNumber: 1,
          state: 1
        }

        if (!forceNoFilter && this.search.location && this.search.location.trim()) {
          hotSearchParams.location = this.search.location
        }

        getSysAttractionsPage(hotSearchParams).then(res => {
          if (res.code == 1000) {
            const allAttractions = res.data.records
            import('@/api/api').then(api => {
              api.getSysAttractionOrderPage({ pageSize: 1000, pageNumber: 1 }).then(orderRes => {
                if (orderRes.code == 1000) {
                  const allOrders = orderRes.data.records
                  const orderCounts = {}
                  allOrders.forEach(order => {
                    const id = order.attractionsId
                    orderCounts[id] = (orderCounts[id] || 0) + 1
                  })
                  this.hotAttractions = allAttractions.map(attr => ({
                    ...attr,
                    realOrderCount: orderCounts[attr.id] || 0
                  }))
                  .sort((a, b) => b.realOrderCount - a.realOrderCount)
                  .slice(0, 10)
                }
              })
            })
          }
        })
      },
      toInfo(id) {
        this.$router.push("/attractionsInfo?id=" + id)
      },
      handleCurrentChange(val) {
        this.search.pageNumber = val
        this.getSysAttractionsPage()
      }, 
    },
    mounted() {
      this.getUserInfo()
    }
 }
</script>

<style scoped>
   @import url('../../assets/css/attractions.css');
   
   .attractions-container {
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
      flex-wrap: wrap;
    }
    
    .location-tag {
      font-size: 14px;
      font-weight: normal;
      background: linear-gradient(45deg, #409EFF, #66b1ff);
      color: white;
      padding: 2px 10px;
      border-radius: 12px;
    }
    
    .sidebar-title i {
      color: #fadb14;
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
     position: relative;
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
   .attractions-main {
     flex: 1;
     overflow: hidden;
   }

   .attractions3 {
     width: 100%;
     display: flex;
     flex-wrap: wrap;
     justify-content: flex-start;
     gap: 20px;
     margin-top: 20px;
   }

   .index5 {
     width: 300px; /* 恢复原始的大致宽度 */
     background: #fff;
     border-radius: 8px;
     overflow: hidden;
     box-shadow: 0 2px 12px rgba(0,0,0,0.1);
   }

   .el-pagination {
     display: flex;
     justify-content: center;
     margin-top: 20px;
   }

   @media (max-width: 1200px) {
     .attractions3 {
       grid-template-columns: repeat(2, 1fr);
     }
   }

   @media (max-width: 992px) {
     .attractions-container {
       flex-direction: column;
     }
     .sales-sidebar {
       width: 100%;
       position: static;
     }
     .attractions3 {
       grid-template-columns: repeat(2, 1fr);
     }
   }
</style>