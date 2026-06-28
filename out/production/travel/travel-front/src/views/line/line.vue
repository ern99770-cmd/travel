<template>
  <div class="line">
    <headers></headers>
    <div class="line1">
        <div class="search-container">
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
                <el-button type="primary" @click="searchPage">搜索</el-button>
            </div>
        </div>
        <div class="line3">
            <div class="line4" v-for="(item,index) in tableData" @click="toInfo(item.id)">
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
        total:100,
        tableData: [],
      }
    },
    components: {
      headers,
      bottoms
    },
    methods: {
      searchPage() {
        this.pageNumber = 1
        this.getSysLinePage()
      },
      getSysLinePage() {
        getSysLinePage(this.search).then(res => {
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
        this.getSysLinePage()
      }, 
    },
    created() {
     
    },
    mounted() {
      this.getSysLinePage()
    }
 }
</script>

<style scoped>
   @import url('../../assets/css/line.css');
   
   .search-container {
     width: 100%;
     padding: 20px 0;
     background-color: #fff;
     margin-bottom: 20px;
     position: relative;
     z-index: 10;
   }
   
   .search-box {
     max-width: 1200px;
     margin: 0 auto;
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
   
   .search-item i {
     position: absolute;
     left: 15px;
     top: 50%;
     transform: translateY(-50%);
     color: #909399;
     font-size: 16px;
     z-index: 2;
     pointer-events: none;
   }
   
   .search-item .el-input {
     width: 100%;
   }
   
   .search-item .el-input__inner {
     padding-left: 40px;
     border-radius: 20px;
     height: 40px;
     line-height: 40px;
     border: 1px solid #DCDFE6;
     background-color: #fff;
   }
   
   .search-box .el-button {
     height: 40px;
     padding: 0 30px;
     border-radius: 20px;
     font-size: 14px;
     background: linear-gradient(45deg, #409EFF, #66b1ff);
     border: none;
     transition: all 0.3s ease;
   }
   
   .search-box .el-button:hover {
     transform: translateY(-2px);
     box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
   }
   
   .line3 {
     max-width: 1200px;
     margin: 0 auto;
     padding: 0 20px;
     position: relative;
     z-index: 1;
   }
</style>