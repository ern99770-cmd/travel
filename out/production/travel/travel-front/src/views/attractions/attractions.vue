<template>
  <div class="attractions">
    <headers></headers>
    <div class="attractions1">
      <div class="search-container">
        <div class="search-box">
          <div class="search-item">
            <i class="el-icon-search"></i>
            <el-input 
              v-model="search.name" 
              placeholder="     请输入景点名称"
              clearable>
            </el-input>
          </div>
          <div class="search-item">
            <i class="el-icon-collection"></i>
            <el-select 
              v-model="search.scenicType" 
              placeholder="     请选择景点类型" 
              clearable>
              <el-option label="     自然风光" value="自然风光"></el-option>
              <el-option label="     人文历史" value="人文历史"></el-option>
              <el-option label="     美食体验" value="美食体验"></el-option>
              <el-option label="     购物娱乐" value="购物娱乐"></el-option>
            </el-select>
          </div>
          <el-button type="primary" @click="searchPage">搜索</el-button>
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
    <bottoms></bottoms>
  </div>
</template>

<script>
  import {getSysAttractionsPage} from '../../api/api'
  import headers from '@/components/header'
  import bottoms from '@/components/bottom'
  export default {
    data() {
      return{
        search: {
          name: "",
          scenicType: "",
          state: 1,
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
        this.getSysAttractionsPage()
      },
      getSysAttractionsPage() {
        getSysAttractionsPage(this.search).then(res => {
          if (res.code == 1000) {
            this.tableData = res.data.records
            this.total = res.data.total
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
    created() {
     
    },
    mounted() {
      this.getSysAttractionsPage()
    }
 }
</script>

<style scoped>
   @import url('../../assets/css/attractions.css');
   
   .search-container {
     width: 100%;
     padding: 20px 0;
     background-color: #fff;
     margin-bottom: 20px;
     position: relative;
     z-index: 10;
     margin-top: 60px;
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
   
   .search-item .el-input,
   .search-item .el-select {
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
   
   .search-item .el-select .el-input__inner {
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
   
   .attractions3 {
     max-width: 1200px;
     margin: 0 auto;
     padding: 0 20px;
     position: relative;
     z-index: 1;
     margin-top: 20px;
   }
</style>