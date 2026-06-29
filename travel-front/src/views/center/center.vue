<template>
  <PageLayout>
  <div class="centerPage page-container page-container--narrow">
    <div class="centerPage-content">
      <el-card class="box-card">
        <div slot="header" class="card-header">
          <span class="header-title">个人信息管理</span>
        </div>
        <div class="profile-header">
          <div class="avatar-wrap">
            <el-image
              class="avatar-img"
              :src="$store.state.HOST + user.avatar"
              :preview-src-list="avatar"
              fit="cover">
            </el-image>
          </div>
          <div class="profile-meta">
            <h3 class="profile-name">{{ user.userName || '用户' }}</h3>
            <p class="profile-account">账号：{{ user.loginAccount }}</p>
            <p v-if="user.pwdUpdateDate" class="profile-pwd-time">上次修改密码：{{ user.pwdUpdateDate }}</p>
            <div class="profile-btns">
              <el-upload
                ref="upload"
                :action="uploadAvatarUrl() + '/' + user.id"
                :show-file-list="false"
                :before-upload="beforeAvatorUpload"
                :on-success="handleAvatorSuccess"
                accept="image/*">
                <el-button size="small" type="primary" plain icon="el-icon-picture-outline-round">修改头像</el-button>
              </el-upload>
              <el-button size="small" type="warning" plain icon="el-icon-key" @click="changePassword">修改密码</el-button>
            </div>
          </div>
        </div>

        <el-form class="profile-form" :model="user" :rules="rules" ref="ruleForm" label-width="100px">
          <div class="form-grid">
            <el-form-item label="用户名" prop="userName">
              <el-input v-model="user.userName" placeholder="请输入用户名"></el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="user.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>
            <el-form-item label="联系电话" prop="tel">
              <el-input v-model="user.tel" placeholder="请输入联系电话"></el-input>
            </el-form-item>
            <el-form-item label="所在地区" prop="location">
              <el-input v-model="user.location" placeholder="如：北京市海淀区"></el-input>
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="user.sex">
                <el-radio label="0">男</el-radio>
                <el-radio label="1">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="景点偏好" class="form-item--full">
              <el-checkbox-group v-model="scenicTypeList" class="scenic-type-group">
                <el-checkbox label="自然风光">自然风光</el-checkbox>
                <el-checkbox label="人文历史">人文历史</el-checkbox>
                <el-checkbox label="美食体验">美食体验</el-checkbox>
                <el-checkbox label="购物娱乐">购物娱乐</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </div>
          <div class="form-actions">
            <el-button type="primary" icon="el-icon-check" @click="submit">保存</el-button>
            <el-button type="danger" plain icon="el-icon-switch-button" @click="logout">退出登录</el-button>
          </div>
        </el-form>
      </el-card>

      <!-- 财经数据分析报表 -->
      <el-card class="box-card financial-card">
        <div slot="header" class="card-header">
          <span class="header-title"><i class="el-icon-data-analysis"></i> 个人旅行财务报表</span>
          <el-tag type="success" size="small">实时汇率结算</el-tag>
        </div>
        <div class="financial-content">
          <div class="stat-summary">
            <div class="stat-item">
              <div class="stat-label">总旅行支出 (CNY)</div>
              <div class="stat-value">￥{{ totalSpending.toFixed(2) }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">本月新增支出</div>
              <div class="stat-value">￥{{ monthSpending.toFixed(2) }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">财务健康评分</div>
              <div class="stat-value score">{{ financialScore }}</div>
            </div>
          </div>
          <div class="charts-container">
            <div id="categoryChart" class="chart-box"></div>
            <div id="trendChart" class="chart-box"></div>
          </div>
        </div>
      </el-card>
    </div>
    
    <el-dialog
        title="修改密码"
        :visible.sync="passwordDialogVisible"
        width="30%"
        :before-close="handlePasswordClose">
        <span>请输入{{user.userName}}的旧密码：</span>
        <el-input style="margin-top:10px" show-password v-model="oldPassword" size="mini" autocomplete="off"></el-input>
        <span>请输入{{user.userName}}的新密码：</span>
        <el-input style="margin-top:10px" show-password v-model="newPassword" size="mini" autocomplete="off"></el-input>
        <span slot="footer" class="dialog-footer">
          <el-button size="mini" @click="handlePasswordClose">取 消</el-button>
          <el-button size="mini" type="primary" @click="passwordSubmit">确 定</el-button>
        </span>
    </el-dialog>
  </div>
  </PageLayout>
</template>

<script>
  import {mixin} from "../../minix";
  import * as echarts from 'echarts'
  import {getUser,setUserInfo,setUserAvatar,changePassword, getSysAttractionOrderPage, getSysHotelOrderPage, getSysAttractionsList, getExchangeRecords} from '../../api/api' 
  export default {
    mixins: [mixin],
    data() {
      var checkPhone = (rule, value, callback) => {
          if (!value) {
              return callback(new Error('请输入联系电话'));
          } else {
              const reg = /^1[3|4|5|7|8][0-9]\d{8}$/
              if (reg.test(value)) {
                  callback();
              } else {
                  return callback(new Error('请输入正确的联系电话'));
              }
          }
      };
      return{
        school: [],
        major: [],
        user: {},
        avatar: [],
        scenicTypeList: [],
        rules: {
          userName: [
            { required: true, message: '请输入用户名', trigger: 'blur' },
          ],
          email: [
            { required: true, message: '请输入邮箱地址', trigger: 'blur' },
            { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
          ],
          tel: [
            { required: true, validator:checkPhone, message: '请输入正确的联系电话', trigger: 'blur' },
          ],
        },
        oldPassword: "",
        newPassword: "",
        passwordDialogVisible: false,
        totalSpending: 0,
        monthSpending: 0,
        financialScore: 0,
        attractionOrders: [],
        hotelOrders: [],
        attractions: [],
        exchangeRecords: [],
        categoryChart: null,
        trendChart: null
      }
    },
    components: {
    },
    methods: {
      handlePasswordClose() {
        this.$bus.$emit('password', false)
      },
      passwordSubmit() {
        var param = {
          id: this.user.id,
          password: this.oldPassword,
          newPassword: this.newPassword
        }
        changePassword(param).then(res => {
          if(res.code == 1000) {
            this.$notify.success({
              title: '成功',
              message: "密码修改成功"
            });
            this.$bus.$emit('password', false)
          } else {
            this.$notify.error({
              title: '错误',
              message: res.message
            });
          }
        })
      },
      submit() {
        this.$refs["ruleForm"].validate((valid) => {
          if (valid) {
            // 确保scenicTypeList不为undefined或null
            if(!this.scenicTypeList) {
              this.scenicTypeList = [];
            }
            // 处理喜欢的景点类型，将数组转为逗号分隔的字符串
            this.user.scenicType = this.scenicTypeList.join(',');
            
            console.log("提交的用户信息:", this.user); // 添加日志检查提交的数据
            
            setUserInfo(this.user).then(res => {
              if(res.code == 1000) {
                this.$message({
                  type: 'success',
                  message: '保存成功!'
                });
                this.getUserInfo();
                
                // 更新本地存储的用户信息
                window.localStorage.setItem("user_info", JSON.stringify(this.user));
                
                // 广播用户喜好更新事件，通知其他页面
                this.$bus.$emit('userPreferenceUpdated', this.user.scenicType);
                
                // 如果首页在其他选项卡中打开，也要更新其存储
                window.localStorage.setItem("preferenceUpdatedAt", new Date().getTime());
              } else {
                this.$notify.error({
                  title: '错误',
                  message: res.message
                });
              }
            })
          } else {
            return false;
          }
        });
      },
      getUserInfo() {
        getUser().then(res => {
            if(res.code == 1000) {
              this.user = res.data
              this.user.sex = res.data.sex + ""
              this.avatar[0] = this.$store.state.HOST + this.user.avatar
              if(this.user.scenicType) {
                this.scenicTypeList = this.user.scenicType.split(',')
              } else {
                this.scenicTypeList = []
              }
              // 获取个人信息后加载财务数据
              this.fetchFinancialData();
            } else {
              this.$notify.error({
                title: '错误',
                message: res.message
              });
            }
        })
      },
      async fetchFinancialData() {
        if (!this.user.id) return;

        try {
          // 1. 获取所有景点，用于价格匹配
          const attractionsRes = await getSysAttractionsList();
          if (attractionsRes.code === 1000) {
            this.attractions = attractionsRes.data;
          }

          // 2. 获取景点订单
          const attractionOrdersRes = await getSysAttractionOrderPage({
            userId: this.user.id,
            pageSize: 100,
            pageNumber: 1
          });
          if (attractionOrdersRes.code === 1000) {
            this.attractionOrders = attractionOrdersRes.data.records;
          }

          // 3. 获取酒店订单
          const hotelOrdersRes = await getSysHotelOrderPage({
            userId: this.user.id,
            pageSize: 100,
            pageNumber: 1
          });
          if (hotelOrdersRes.code === 1000) {
            this.hotelOrders = hotelOrdersRes.data.records;
          }

          // 4. 获取兑换记录（包含会员购买记录）
          const exchangeRecordsRes = await getExchangeRecords({
            userId: this.user.id,
            pageSize: 100,
            pageNumber: 1
          });
          if (exchangeRecordsRes.code === 1000) {
            this.exchangeRecords = exchangeRecordsRes.data.records;
          }

          // 5. 计算统计数据
          this.calculateStatistics();
          
          // 6. 初始化图表
          this.$nextTick(() => {
            this.initCharts();
          });
        } catch (error) {
          console.error("加载财务数据失败:", error);
        }
      },
      calculateStatistics() {
        const attractionsPriceMap = {};
        this.attractions.forEach(item => {
          attractionsPriceMap[item.id] = item.price;
        });

        let totalAttractionSpending = 0;
        let totalHotelSpending = 0;
        let totalMemberSpending = 0;
        let currentMonthSpending = 0;
        const now = new Date();
        const currentMonth = now.getMonth();
        const currentYear = now.getFullYear();

        // 景点订单支出
        this.attractionOrders.forEach(order => {
          const price = attractionsPriceMap[order.attractionsId] || 0;
          const cost = price * (order.num || 1);
          totalAttractionSpending += cost;

          const orderDate = new Date(order.createTime);
          if (orderDate.getMonth() === currentMonth && orderDate.getFullYear() === currentYear) {
            currentMonthSpending += cost;
          }
        });

        // 酒店订单支出
        this.hotelOrders.forEach(order => {
          const cost = order.price || 0;
          totalHotelSpending += cost;

          const orderDate = new Date(order.createTime);
          if (orderDate.getMonth() === currentMonth && orderDate.getFullYear() === currentYear) {
            currentMonthSpending += cost;
          }
        });

        // 会员购买支出
        this.exchangeRecords.forEach(record => {
          // type 4表示会员购买
          if (record.type === 4) {
            const cost = record.amount ? parseFloat(record.amount) : 0;
            totalMemberSpending += cost;

            const recordDate = new Date(record.createTime);
            if (recordDate.getMonth() === currentMonth && recordDate.getFullYear() === currentYear) {
              currentMonthSpending += cost;
            }
          }
        });

        this.totalSpending = totalAttractionSpending + totalHotelSpending + totalMemberSpending;
        this.monthSpending = currentMonthSpending;
        
        // 财务评分逻辑 (示例: 越高支出比例月度占比越低越健康)
        this.financialScore = Math.min(100, Math.max(0, 85 + (totalAttractionSpending > 0 ? 5 : 0) - (this.monthSpending / 1000)));
      },
      initCharts() {
        const categoryEl = document.getElementById('categoryChart');
        const trendEl = document.getElementById('trendChart');
        if (!categoryEl || !trendEl) return;

        if (this.categoryChart) this.categoryChart.dispose();
        if (this.trendChart) this.trendChart.dispose();

        this.categoryChart = echarts.init(categoryEl);
        this.trendChart = echarts.init(trendEl);

        const attractionCost = this.attractionOrders.reduce((sum, order) => {
          const attr = this.attractions.find(a => a.id === order.attractionsId);
          return sum + (attr ? attr.price * order.num : 0);
        }, 0);
        const hotelCost = this.hotelOrders.reduce((sum, order) => sum + (order.price || 0), 0);
        const memberCost = this.exchangeRecords
          .filter(r => r.type === 4)
          .reduce((sum, record) => sum + (record.amount ? parseFloat(record.amount) : 0), 0);

        const pieData = [
          { value: attractionCost, name: '景点门票' },
          { value: hotelCost, name: '酒店住宿' },
          { value: memberCost, name: '会员购买' }
        ].filter(item => item.value > 0);

        this.categoryChart.setOption({
          title: { text: '支出占比分析', left: 'center', textStyle: { fontSize: 15, fontWeight: 600 } },
          tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
          legend: { bottom: 0, left: 'center' },
          color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C'],
          series: [{
            name: '支出类别',
            type: 'pie',
            radius: ['35%', '60%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: true,
            label: { show: pieData.length > 0, formatter: '{b}\n{d}%' },
            data: pieData.length > 0 ? pieData : [{ value: 1, name: '暂无消费记录', itemStyle: { color: '#dcdfe6' } }]
          }]
        });

        const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
        const monthlyData = new Array(12).fill(0);

        [...this.attractionOrders, ...this.hotelOrders].forEach(order => {
          const date = new Date(order.createTime);
          if (date.getFullYear() === new Date().getFullYear()) {
            const month = date.getMonth();
            const cost = order.price || (this.attractions.find(a => a.id === order.attractionsId)?.price * order.num) || 0;
            monthlyData[month] += cost;
          }
        });

        this.exchangeRecords.forEach(record => {
          if (record.type === 4) {
            const date = new Date(record.createTime);
            if (date.getFullYear() === new Date().getFullYear()) {
              const month = date.getMonth();
              const cost = record.amount ? parseFloat(record.amount) : 0;
              monthlyData[month] += cost;
            }
          }
        });

        this.trendChart.setOption({
          title: { text: '年度旅行消费趋势', left: 'center', textStyle: { fontSize: 15, fontWeight: 600 } },
          grid: { left: 50, right: 20, top: 50, bottom: 30 },
          xAxis: { type: 'category', data: months, boundaryGap: false },
          yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
          tooltip: { trigger: 'axis', valueFormatter: val => '¥' + (val || 0).toFixed(2) },
          series: [{
            data: monthlyData,
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: { width: 2, color: '#409EFF' },
            itemStyle: { color: '#409EFF' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(64, 158, 255, 0.35)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
              ])
            }
          }]
        });
      },
      handleChartResize() {
        if (this.categoryChart) this.categoryChart.resize();
        if (this.trendChart) this.trendChart.resize();
      },
      changePassword() {
        //修改密码
        this.$bus.$emit('password', true)
      },
      handleAvatorSuccess(res){
        let _this = this;
        if(res.code == 1000){
            _this.$message({
              type: 'success',
              message: '上传成功!'
            });
        }else{
          _this.$notify.error({
            title: '错误',
            message: res.message
          });
        }
      },
      logout() {
        this.$store.dispatch('logout').then(() => {
          window.localStorage.removeItem("user_info")
          window.localStorage.removeItem("user_token")
          this.$message({
            message: '退出成功',
            type: 'success'
          });
          setTimeout(() => {
            this.$router.push('/logIn');
          }, 1000);
        })
      }
    },
    created() {
     
    },
    mounted() {
      this.$bus.$on('password', res => {
        this.passwordDialogVisible = res
      })
      window.addEventListener('resize', this.handleChartResize)
      this.getUserInfo()
    },
    beforeDestroy() {
      window.removeEventListener('resize', this.handleChartResize)
      if (this.categoryChart) this.categoryChart.dispose()
      if (this.trendChart) this.trendChart.dispose()
    }
 }
</script>

<style scoped>
  .centerPage {
    width: 100%;
    min-height: 100vh;
    background-color: #f5f7fa;
  }

  .centerPage-content {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    gap: 24px;
    padding-bottom: 40px;
  }

  .box-card {
    width: 100%;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border-radius: 12px;
    border: none;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-title {
    font-size: 17px;
    font-weight: 600;
    color: #303133;
  }

  .header-title i {
    margin-right: 4px;
    color: #409EFF;
  }

  /* 个人信息头部 */
  .profile-header {
    display: flex;
    align-items: center;
    gap: 24px;
    padding: 8px 0 24px;
    margin-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
  }

  .avatar-wrap {
    flex-shrink: 0;
  }

  .avatar-img {
    width: 96px;
    height: 96px;
    border-radius: 50%;
    overflow: hidden;
    border: 3px solid #fff;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }

  .profile-meta {
    flex: 1;
    min-width: 0;
  }

  .profile-name {
    margin: 0 0 6px;
    font-size: 20px;
    font-weight: 600;
    color: #303133;
  }

  .profile-account,
  .profile-pwd-time {
    margin: 0 0 4px;
    font-size: 13px;
    color: #909399;
  }

  .profile-btns {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 12px;
  }

  /* 表单网格 */
  .profile-form {
    padding-top: 8px;
  }

  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0 32px;
  }

  .form-item--full {
    grid-column: 1 / -1;
  }

  .scenic-type-group {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 20px;
  }

  .scenic-type-group >>> .el-checkbox {
    margin-right: 0;
  }

  .form-actions {
    display: flex;
    gap: 12px;
    margin-top: 8px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
  }

  /* 财务报表 */
  .financial-content {
    padding: 4px 0;
  }

  .stat-summary {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-bottom: 24px;
  }

  .stat-item {
    text-align: center;
    background: #f8f9fb;
    padding: 20px 16px;
    border-radius: 10px;
  }

  .stat-label {
    font-size: 13px;
    color: #909399;
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 22px;
    font-weight: 600;
    color: #303133;
  }

  .stat-value.score {
    color: #67C23A;
  }

  .charts-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }

  .chart-box {
    height: 320px;
    background: #fafbfc;
    border-radius: 10px;
    padding: 8px;
    box-sizing: border-box;
  }

  @media screen and (max-width: 768px) {
    .profile-header {
      flex-direction: column;
      text-align: center;
    }

    .profile-btns {
      justify-content: center;
    }

    .form-grid {
      grid-template-columns: 1fr;
      gap: 0;
    }

    .stat-summary {
      grid-template-columns: 1fr;
    }

    .charts-container {
      grid-template-columns: 1fr;
    }

    .chart-box {
      height: 280px;
    }
  }
</style>