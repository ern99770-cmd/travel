<template>
  <PageLayout>
  <div class="index">
    
    <!-- 调试信息 -->
    <div v-if="error" class="debug-info">
      <h3>错误信息</h3>
      <p>{{error}}</p>
      <el-button size="mini" type="danger" @click="retryLoading">重试加载</el-button>
    </div>
    
    <el-carousel height="500px">
      <el-carousel-item v-for="(item,index) in rotations" :key="index">
        <img style="width:100%;height:100%" :src="$store.state.HOST + item.image">
      </el-carousel-item>
    </el-carousel>
    <div class="index1">
      <div class="index2">
        <div class="index3">
          推荐景点
        </div>
        <div class="index4">
          <template v-if="attractions && attractions.length > 0">
            <div class="index5" v-for="(item,index) in attractions" :key="index" >
              <img style="width:100%;height:220px" :src="item.images.split(',')[0]">
              <div class="index6">
                <div class="index7">{{item.name}}</div>
                <div class="index8">{{item.introduce}}</div>
              </div>
              <div class="index9" style="margin-top:10px" @click="toInfo(item.id)">
                  预 约
                </div>
            </div>
          </template>
          <div v-else class="no-data">暂无推荐景点数据</div>
        </div>
        <!-- <el-button style="margin-top:10px" size="small" type="primary" plain @click="toAttraction">查看更多</el-button> -->
      </div>
    </div>
    
    <div class="index1">
      <div class="index2">
        <div class="index3">
          热门景点
        </div>
        <div class="index4">
          <template v-if="hotAttractions && hotAttractions.length > 0">
            <div class="index5" v-for="(item,index) in hotAttractions" :key="index" >
              <img style="width:100%;height:220px" :src="item.images.split(',')[0]">
              <div class="index6">
                <div class="index7">{{item.name}}</div>
                <div class="index8">{{item.introduce}}</div>
              </div>
              <div class="index9" style="margin-top:10px" @click="toInfo(item.id)">
                  预 约
                </div>
            </div>
          </template>
          <div v-else class="no-data">暂无热门景点数据</div>
        </div>
        <!-- <el-button style="margin-top:10px" size="small" type="primary" plain @click="toAttraction">查看更多</el-button> -->
      </div>
    </div>
    <div class="index10">
      <div class="image-collage">
        <div class="image-item">
          <img src="@/assets/image/lvyou/alley-4191906_1920.jpg" alt="古巷风情">
          <div class="image-overlay">
            <div class="image-title">古巷风情</div>
            <div class="image-description">探索历史文化的魅力</div>
          </div>
        </div>
        <div class="image-item">
          <img src="@/assets/image/lvyou/girl-5160131_1920.jpg" alt="自然风光">
          <div class="image-overlay">
            <div class="image-title">自然风光</div>
            <div class="image-description">感受大自然的壮丽</div>
          </div>
        </div>
        <div class="image-item">
          <img src="@/assets/image/lvyou/sunset-4509879.jpg" alt="日落美景">
          <div class="image-overlay">
            <div class="image-title">日落美景</div>
            <div class="image-description">欣赏绝美日落时刻</div>
          </div>
        </div>
        <div class="image-item">
          <img src="@/assets/image/lvyou/woman-6961929_1920.jpg" alt="人文风情">
          <div class="image-overlay">
            <div class="image-title">人文风情</div>
            <div class="image-description">体验当地文化特色</div>
          </div>
        </div>
        <div class="image-item">
          <img src="@/assets/image/lvyou/photographer-407068_1920.jpg" alt="摄影之旅">
          <div class="image-overlay">
            <div class="image-title">摄影之旅</div>
            <div class="image-description">记录精彩瞬间</div>
          </div>
        </div>
        <div class="image-item">
          <img src="@/assets/image/lvyou/tram-7524963_1920.jpg" alt="城市探索">
          <div class="image-overlay">
            <div class="image-title">城市探索</div>
            <div class="image-description">感受都市魅力</div>
          </div>
        </div>
        <div class="image-item">
          <img src="@/assets/image/lvyou/car-6603726_1920.jpg" alt="自驾游">
          <div class="image-overlay">
            <div class="image-title">自驾游</div>
            <div class="image-description">自由探索的乐趣</div>
          </div>
        </div>
        <div class="image-item">
          <img src="@/assets/image/lvyou/journey-1130732_1920.jpg" alt="旅程">
          <div class="image-overlay">
            <div class="image-title">精彩旅程</div>
            <div class="image-description">开启难忘之旅</div>
          </div>
        </div>
        <div class="image-item">
          <img src="@/assets/image/lvyou/hot-air-balloon-8869138_1920.jpg" alt="热气球">
          <div class="image-overlay">
            <div class="image-title">热气球之旅</div>
            <div class="image-description">俯瞰壮丽景色</div>
          </div>
        </div>
      </div>
    </div>
    <div class="index1">
      <div class="index2">
        <div class="index3">
          旅游路线
        </div>
        <div class="index4">
          <template v-if="line && line.length > 0">
            <div class="index5" v-for="(item,index) in line" :key="index">
              <img style="width:100%;height:220px" :src="item.images.split(',')[0]">
              <div class="index6">
                <div class="index7">{{item.name}}</div>
                <div class="index8">{{item.introduce}}</div>
                <div class="index9" @click="toLineInfo(item.id)">
                  查 看
                </div>
              </div>
            </div>
          </template>
          <div v-else class="no-data">暂无旅游路线数据</div>
        </div>
        <!-- <el-button style="margin-top:10px" size="small" type="primary" plain @click="toLine">查看更多</el-button> -->
      </div>
    </div>
  </div>
  </PageLayout>
</template>

<script>
  import {getSysRotationsList, getSysAttractionsIndex, getUserCount, getSysLineIndex, getSysAttractionsHot, getSysAttractionsRecommend} from '../../api/api'
  export default {
    data() {
      return{
        rotations: [],
        attractions: [],
        hotAttractions: [],
        count: 0,
        line: [],
        loading: true,
        error: null
      }
    },
    components: {
    },
    methods: {
      toAttraction() {
        this.$router.push("/attractions")
      },
      toLine() {
        this.$router.push("/line")
      },
      toInfo(id) {
        this.$router.push("/attractionsInfo?id=" + id)
      },
      toLineInfo(id) {
        this.$router.push("/lineInfo?id=" + id)
      },
      getSysRotationsList() {
        this.loading = true;
        getSysRotationsList().then(res => {
          if (res.code == 1000) {
            this.rotations = res.data
          }
          this.loading = false;
        }).catch(err => {
          console.error("获取轮播图失败:", err);
          this.error = "获取数据失败，请刷新页面重试";
          this.loading = false;
        })
      },
      getSysAttractionsIndex() {
        // 获取当前登录用户的喜好
        let scenicType = "";
        const userInfoStr = window.localStorage.getItem("user_info");
        
        if (userInfoStr) {
          try {
            const userInfo = JSON.parse(userInfoStr);
            if (userInfo && userInfo.scenicType) {
              // 用户景点类型偏好已经是以逗号分隔的字符串，直接使用
              scenicType = userInfo.scenicType;
            }
          } catch (e) {
            console.error("解析用户信息出错:", e);
          }
        }
        
        console.log("用户喜好的景点类型:", scenicType);
        
        // 根据用户喜好获取推荐景点
        getSysAttractionsRecommend({scenicType: scenicType}).then(res => {
          if (res.code == 1000) {
            console.log("推荐景点数据:", res.data);
            this.attractions = res.data;
          } else {
            console.error("获取推荐景点失败:", res.message);
          }
        }).catch(err => {
          console.error("获取推荐景点异常:", err);
        });
      },
      getSysAttractionsHot() {
        // 获取热门景点
        getSysAttractionsHot().then(res => {
          if (res.code == 1000) {
            console.log("热门景点数据:", res.data);
            this.hotAttractions = res.data;
          } else {
            console.error("获取热门景点失败:", res.message);
          }
        }).catch(err => {
          console.error("获取热门景点异常:", err);
        });
      },
      getUserCount() {
        getUserCount().then(res => {
          if (res.code == 1000) {
            this.count = res.data
          }
        })
      },
      getSysLineIndex() {
        getSysLineIndex().then(res => {
          if (res.code == 1000) {
            console.log("旅游路线数据:", res.data);
            this.line = res.data;
          } else {
            console.error("获取旅游路线失败:", res.message);
          }
        }).catch(err => {
          console.error("获取旅游路线异常:", err);
        });
      },
      // 重试加载数据
      retryLoading() {
        this.error = null;
        this.getSysRotationsList();
        this.getSysAttractionsIndex();
        this.getSysAttractionsHot();
        this.getSysLineIndex();
      }
    },
    created() {
      // 检查API是否正确定义
      console.log("API检查:", {
        getSysAttractionsHot: typeof getSysAttractionsHot,
        getSysAttractionsRecommend: typeof getSysAttractionsRecommend
      });
      
      // 保存事件处理函数引用，以便后续移除
      this._handlePreferenceUpdate = (scenicType) => {
        console.log("收到用户喜好更新事件，重新获取推荐景点", scenicType);
        this.getSysAttractionsIndex();
      };
      
      this._handleStorageChange = (e) => {
        if (e.key === 'preferenceUpdatedAt' || e.key === 'user_info') {
          console.log("检测到用户信息在其他页面更新，重新获取推荐景点");
          this.getSysAttractionsIndex();
        }
      };
      
      // 监听用户喜好更新事件
      this.$bus.$on('userPreferenceUpdated', this._handlePreferenceUpdate);
      
      // 监听localStorage变化，处理跨页面通信
      window.addEventListener('storage', this._handleStorageChange);
    },
    mounted() {
      this.getSysRotationsList()
      this.getSysAttractionsIndex()
      this.getSysAttractionsHot()
      this.getUserCount()
      this.getSysLineIndex()
      
      // 延迟检查数据
      setTimeout(() => {
        console.log("数据状态检查:", {
          attractions: this.attractions.length,
          hotAttractions: this.hotAttractions.length,
          line: this.line.length
        });
      }, 2000);
    },
    beforeDestroy() {
      // 移除事件监听 - 必须传和添加时相同的处理函数
      if (this._handlePreferenceUpdate) {
        this.$bus.$off('userPreferenceUpdated', this._handlePreferenceUpdate);
      }
      if (this._handleStorageChange) {
        window.removeEventListener('storage', this._handleStorageChange);
      }
    }
 }
</script>

<style scoped>
   @import url('../../assets/css/index.css');
   
   .no-data {
     text-align: center;
     color: #909399;
     font-size: 14px;
     padding: 20px 0;
   }
   
   .debug-info {
     position: fixed;
     bottom: 20px;
     right: 20px;
     background-color: rgba(255, 255, 255, 0.9);
     box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
     padding: 15px;
     border-radius: 5px;
     z-index: 1000;
     max-width: 300px;
     font-size: 14px;
   }
</style>