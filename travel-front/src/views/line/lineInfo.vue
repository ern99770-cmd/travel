<template>
  <PageLayout>
  <div class="forumInfo">
    <div class="forumInfo1" v-if="info && info.images">
        <div class="forumInfo2">
          <el-carousel height="500px">
            <el-carousel-item v-for="(item,index) in (info.images ? info.images.split(',') : [])" :key="index">
              <img style="width:100%;height:100%" :src="item">
            </el-carousel-item>
          </el-carousel>
        </div>
    </div>
    <div class="forumInfo1">
        <div class="forumInfo2">
            <div class="forumInfo3">
                {{info.name || ''}}
                <el-button v-if="!flag" size="small" type="success" icon="el-icon-star-on" circle @click="saveSysFavor"></el-button>
                <el-button v-if="flag" size="small" type="warning" icon="el-icon-star-off" circle @click="removeSysFavor"></el-button>
            </div>
            <div class="forumInfo4" v-html="info.content || ''">
                
            </div>
        </div>
    </div>
  </div>
  </PageLayout>
</template>

<script>
  import {getSysLineById,getSysFavor,saveSysFavor,removeSysFavor,addLineHits} from '../../api/api'
  import { showPointsEarned, extractPointsEarned } from '@/utils/pointsToast'
  export default {
    data() {
      return{
        id: "",
        info: {
          content: "",
          name: "",
          images: ""
        },
        favor: {},
        flag: false,
      }
    },
    components: {},
    methods: {
      removeSysFavor(id) {
        removeSysFavor({ids: this.favor.id}).then(res => {
          if (res.code == 1000) {
            this.getSysFavor()
          }
        })
      },
      saveSysFavor() {
        var param = {
          lineId: this.id
        }
        saveSysFavor(param).then(res => {
          if (res.code == 1000) {
            showPointsEarned(extractPointsEarned(res), '收藏线路奖励')
            this.$message({
                message: '收藏成功',
                type: 'success'
            });
            this.getSysFavor()
          }
        })
      },
      getSysLineById() {
        getSysLineById({id: this.id}).then(res => {
          if (res.code == 1000) {
            this.info = res.data
          }
        }).catch(err => {
          console.error("获取路线详情失败:", err)
        })
      },
      getSysFavor() {
        getSysFavor({id:this.id}).then(res => {
          if (res.code == 1000) {
            this.favor = res.data
            this.flag = true
          } else {
            this.flag = false
          }
        }).catch(err => {
          console.error("获取收藏状态失败:", err)
        })
      },
    },
    created() {
     
    },
    mounted() {
      this.id = this.$route.query.id
      console.log("当前路线ID:", this.id)
      // 增加点击量
      if (this.id) {
        addLineHits({id: this.id}).then(res => {
          console.log("热度增加成功, 响应:", res)
        }).catch(err => {
          console.error("热度增加失败:", err)
        })
      } else {
        console.warn("没有获取到路线ID")
      }
      this.getSysFavor()
      this.getSysLineById()
    }
 }
</script>

<style scoped>
  @import url('../../assets/css/forumInfo.css');
  ::v-deep img {
    max-width: 100% !important;
  }
</style>