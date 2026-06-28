<template>
  <div class="attractions">
    <headers></headers>
    <div class="attractions-container">
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
        <div class="search-container">
          <div class="search-box">
            <div class="search-item">
              <i class="el-icon-search"></i>
              <el-input 
                v-model="search.name" 
                placeholder="请输入景点名称"
                clearable>
              </el-input>
            </div>
            <div class="search-item">
              <i class="el-icon-collection"></i>
              <el-select 
                v-model="search.scenicType" 
                placeholder="请选择景点类型" 
                clearable>
                <el-option label="自然风光" value="自然风光"></el-option>
                <el-option label="人文历史" value="人文历史"></el-option>
                <el-option label="美食体验" value="美食体验"></el-option>
                <el-option label="购物娱乐" value="购物娱乐"></el-option>
              </el-select>
            </div>
            <div class="search-item location-item">
              <i class="el-icon-location"></i>
              <el-input 
                v-model="search.location" 
                placeholder="请输入地区（如：北京）"
                clearable>
              </el-input>
              <el-button 
                v-if="userLocation" 
                size="mini" 
                type="text" 
                @click="useMyLocation"
                class="use-my-location-btn"
                title="使用我的位置">
                <i class="el-icon-location-outline"></i> 我的位置
              </el-button>
            </div>
            <el-button type="primary" @click="searchPage">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </div>
        </div>
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
    <bottoms></bottoms>
  </div>
</template>

<script>
  import {getSysAttractionsPage, getSysAttractionsHot, getUser} from '../../api/api'
  import headers from '@/components/header'
  import bottoms from '@/components/bottom'
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
        userLocation: "",
      }
    },
    components: {
      headers,
      bottoms
    },
    computed: {
      currentHotLocation() {
        if (this.search.location && this.search.location.trim()) {
          return this.search.location;
        } else if (this.userLocation) {
          return this.userLocation;
        }
        return '';
      }
    },
    methods: {
      searchPage() {
        this.search.pageNumber = 1
        this.getSysAttractionsPage()
        // 搜索时同时更新销量榜单
        this.getSysAttractionsHot()
      },
      useMyLocation() {
        if (this.userLocation) {
          this.search.location = this.userLocation;
          this.search.pageNumber = 1;
          this.getSysAttractionsPage();
          // 使用我的位置时更新销量榜单
          this.getSysAttractionsHot();
        }
      },
      resetSearch() {
        this.search.name = '';
        this.search.scenicType = '';
        this.search.location = '';
        this.search.pageNumber = 1;
        this.getSysAttractionsPage();
        // 重置时更新销量榜单，强制不筛选
        this.getSysAttractionsHot(true);
      },
      getSysAttractionsPage() {
        console.log('=== 请求景点列表 ===');
        console.log('完整参数对象：', this.search);
        console.log('location 参数值：', this.search.location);
        console.log('location 参数类型：', typeof this.search.location);
        
        getSysAttractionsPage(this.search).then(res => {
          console.log('=== 景点列表响应 ===');
          console.log('完整响应：', res);
          console.log('响应 code：', res.code);
          if (res.code == 1000) {
            this.tableData = res.data.records
            this.total = res.data.total
            console.log('返回景点数量：', this.tableData.length);
            console.log('返回景点数据：', this.tableData);
            // 检查返回的景点是否有 location 字段
            if (this.tableData.length > 0) {
              console.log('第一个景点的 location：', this.tableData[0].location);
            }
          }
        })
      },
      getUserInfo() {
        getUser().then(res => {
          console.log('获取用户信息响应：', res);
          if(res.code == 1000 && res.data) {
            this.userLocation = res.data.location;
            // 获取用户信息后，先加载所有景点
            this.search.location = '';
            this.getSysAttractionsPage();
            // 初始加载销量榜单（强制不筛选
            this.getSysAttractionsHot(true);
            // 如果用户有设置地区，弹出确认弹窗
            if (this.userLocation && this.userLocation.trim()) {
              this.$confirm(`是否切换到当前地区：${this.userLocation}？`, '地区推荐', {
                confirmButtonText: '是',
                cancelButtonText: '否',
                type: 'info'
              }).then(() => {
                // 用户点击"是"，切换到用户地区
                this.search.location = this.userLocation;
                this.search.pageNumber = 1;
                this.getSysAttractionsPage();
                this.getSysAttractionsHot(false);
                this.$message({
                  type: 'success',
                  message: '已切换到当前地区'
                });
              }).catch(() => {
                // 用户点击"否"，保持显示所有景点，清空地区筛选
                this.search.location = '';
                // 销量榜单也保持不筛选
                this.getSysAttractionsHot(true);
                this.$message({
                  type: 'info',
                  message: '已显示所有景点'
                });
              });
            }
          } else {
            // 如果没有用户信息，加载所有景点
            this.getSysAttractionsPage();
            this.getSysAttractionsHot(true);
          }
        }).catch(err => {
          console.error('获取用户信息失败：', err);
          // 即使获取用户信息失败也要加载景点列表
          this.getSysAttractionsPage();
          this.getSysAttractionsHot(true);
        })
      },
      getSysAttractionsHot(forceNoFilter = false) {
        // 根据当前筛选条件获取景点用于榜单
        const hotSearchParams = { 
          pageSize: 100, 
          pageNumber: 1, 
          state: 1 
        };
        
        // 如果不是强制不筛选，则应用地区筛选
        if (!forceNoFilter) {
          // 优先使用搜索框的地区
          if (this.search.location && this.search.location.trim()) {
            hotSearchParams.location = this.search.location;
          } 
          // 只有当搜索框没有地区时，才考虑用户地区
          else if (this.userLocation && this.userLocation.trim()) {
            hotSearchParams.location = this.userLocation;
          }
          // 如果都没有，就不添加 location 参数（不筛选
        }
        
        console.log('销量榜单筛选参数：', hotSearchParams);
        
        // 1. 获取景点（应用地区筛选
        getSysAttractionsPage(hotSearchParams).then(res => {
          if (res.code == 1000) {
            const allAttractions = res.data.records
            
            // 2. 获取所有订单（不传 userId 即可获取全量
            // 使用大 pageSize 获取尽量多的订单数据以计算真实排名
            import('@/api/api').then(api => {
              api.getSysAttractionOrderPage({ pageSize: 1000, pageNumber: 1 }).then(orderRes => {
                if (orderRes.code == 1000) {
                  const allOrders = orderRes.data.records
                  
                  // 3. 统计每个景点的订单量
                  const orderCounts = {}
                  allOrders.forEach(order => {
                    const id = order.attractionsId
                    orderCounts[id] = (orderCounts[id] || 0) + 1
                  })
                  
                  // 4. 将订单量合并到景点数据中并排序
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
      this.getSysAttractionsHot()
    }
 }
</script>

<style scoped>
   @import url('../../assets/css/attractions.css');
   
   .attractions-container {
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
      width: 250px;
    }
    
    .search-item.location-item {
      width: auto;
      min-width: 250px;
    }
    
    .use-my-location-btn {
      margin-left: 8px;
      white-space: nowrap;
      color: #409EFF;
    }
    
    .use-my-location-btn:hover {
      background-color: #ecf5ff;
    }
    
    .search-item i {
      position: absolute;
      left: 15px;
      z-index: 2;
      color: #909399;
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