<template>
  <div class="hotel">
    <headers></headers>
    <div class="hotel-container">
      <!-- 左侧：酒店销量排名 -->
      <div class="sales-sidebar">
        <div class="sidebar-title">
          <i class="el-icon-medal"></i> 
          热门酒店排行
          <span v-if="search.attractions" class="location-tag">{{ search.attractions }}</span>
        </div>
        <div class="ranking-list">
          <div v-for="(item, index) in hotHotels" :key="index" class="ranking-item" @click="toInfo(item.id)">
            <div class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
            <img :src="item.images.split(',')[0]" class="rank-img">
            <div class="rank-info">
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-sales">
                <i class="el-icon-tickets"></i> 
                总预订: <span class="sales-count">{{ item.realOrderCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：主要内容 -->
      <div class="hotel-main">
        <div class="attractions2">
          <el-select size="small" style="margin-left:20px" v-model="search.attractions" placeholder="请选择景点">
              <el-option v-for="(item,index) in attractions" :key="index"
              :label="item.name"
              :value="item.name">
              </el-option>
          </el-select>
          <el-input size="small" style="width:300px;margin-left:20px" v-model="search.name" placeholder="请输入酒店名称"></el-input>
          <el-button size="small" style="margin-left:20px" type="primary" plain @click="searchPage">搜索</el-button>
          <el-button size="small" style="margin-left:10px" type="warning" plain @click="resetSearch">重置</el-button>
        </div>

        <div class="attractions3">
          <div class="index5" v-for="(item,index) in tableData" :key="index" style="margin-top:15px">
            <img style="width:100%;height:300px" :src="item.images.split(',')[0]">
            <div class="index6">
              <div class="index7">{{item.name}}</div>
              <div class="index8">{{item.introduce}}</div>
              <div class="index9" style="margin-bottom:10px" @click="toInfo(item.id)">
                预 定
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
    <bottoms></bottoms>
  </div>
</template>

<script>
  import {getSysHotelPage,getSysAttractionsList} from '../../api/api'
  import headers from '@/components/header'
  import bottoms from '@/components/bottom'
  export default {
    data() {
      return{
        search: {
          name: "",
          state: "1",
          attractions: "",
          pageSize: 9,
          pageNumber: 1,
        },
        total: 0,
        tableData: [],
        attractions: [],
        hotHotels: []
      }
    },
    components: {
      headers,
      bottoms
    },
    methods: {
      getSysAttractionsList() {
        getSysAttractionsList().then(res => {
          if (res.code == 1000) {
            this.attractions = res.data
          }
        })
      },
      searchPage() {
        this.search.pageNumber = 1
        this.getSysHotelPage()
      },
      getSysHotelPage() {
        getSysHotelPage(this.search).then(res => {
          if (res.code == 1000) {
            this.tableData = res.data.records
            this.total = res.data.total
            
            // 获取全量订单计算排名（如果是第一页则触发）
            if (this.search.pageNumber === 1) {
              this.getHotelSalesRanking()
            }
          }
        })
      },
      getHotelSalesRanking() {
        // 1. 获取酒店（根据选择的景点筛选）
        const hotelSearchParams = { pageSize: 100, pageNumber: 1, state: "1" }
        // 如果选择了景点，就添加筛选条件
        if (this.search.attractions) {
          hotelSearchParams.attractions = this.search.attractions
        }
        
        getSysHotelPage(hotelSearchParams).then(res => {
          if (res.code == 1000) {
            const allHotels = res.data.records
            
            // 2. 获取所有酒店订单（不传 userId 即可获取全量）
            import('@/api/api').then(api => {
              api.getSysHotelOrderPage({ pageSize: 1000, pageNumber: 1 }).then(orderRes => {
                if (orderRes.code == 1000) {
                  const allOrders = orderRes.data.records
                  
                  // 3. 统计每个酒店的订单量
                  const orderCounts = {}
                  allOrders.forEach(order => {
                    const id = order.hotelId
                    orderCounts[id] = (orderCounts[id] || 0) + 1
                  })
                  
                  // 4. 将订单量合并到酒店数据中并排序
                  this.hotHotels = allHotels.map(hotel => ({
                    ...hotel,
                    realOrderCount: orderCounts[hotel.id] || 0
                  }))
                  .sort((a, b) => b.realOrderCount - a.realOrderCount)
                  .slice(0, 10)
                }
              })
            })
          }
        })
      },
      resetSearch() {
        this.search.name = ''
        this.search.attractions = ''
        this.search.pageNumber = 1
        this.getSysHotelPage()
      },
      toInfo(id) {
        this.$router.push("/hotelInfo?id=" + id)
      },
      handleCurrentChange(val) {
        this.search.pageNumber = val
        this.getSysHotelPage()
      }, 
    },
    mounted() {
      this.getSysAttractionsList()
      this.getSysHotelPage()
    }
 }
</script>

<style scoped>
   @import url('../../assets/css/hotel.css');

   .hotel-container {
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
     flex-wrap: wrap;
   }
   
   .location-tag {
     font-size: 14px;
     font-weight: normal;
     background: linear-gradient(45deg, #fa8c16, #ffa940);
     color: #fff;
     padding: 2px 10px;
     border-radius: 12px;
   }

   .sidebar-title i {
     color: #fa8c16;
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
   .hotel-main {
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
     width: 300px;
     background: #fff;
     border-radius: 8px;
     overflow: hidden;
     box-shadow: 0 2px 12px rgba(0,0,0,0.1);
   }

   .pagination-wrapper {
     display: flex;
     justify-content: center;
     margin-top: 20px;
   }

   @media (max-width: 1200px) {
     .hotel-grid {
       grid-template-columns: repeat(2, 1fr);
     }
   }

   @media (max-width: 992px) {
     .hotel-container {
       flex-direction: column;
     }
     .sales-sidebar {
       width: 100%;
       position: static;
     }
   }
</style>