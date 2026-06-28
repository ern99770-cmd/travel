<template>
  <div class="member-center">
    <headers></headers>
    <div class="container">
      <div class="member-header">
        <div class="member-info">
          <div class="avatar">
            <img v-if="userInfo && userInfo.avatar" :src="$store.state.HOST + userInfo.avatar" alt="">
            <img v-else src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" alt="">
          </div>
          <div class="info">
            <h2>{{ userInfo ? userInfo.username : '用户' }}</h2>
            <div class="level-badge" :class="'level-' + member.level">
              {{ member.level === 0 ? '普通用户' : member.level === 1 ? 'VIP' : 'SVIP' }}
            </div>
            <div class="points">
              <span class="points-label">我的积分：</span>
              <span class="points-value">{{ member.points || 0 }}</span>
            </div>
            <div v-if="member.level > 0 && member.vipExpireTime" class="expire-time">
              VIP到期时间：{{ formatTime(member.vipExpireTime) }}
            </div>
          </div>
          <div class="member-actions">
            <el-button type="primary" @click="openPurchaseDialog" :disabled="member.level === 2">
              {{ member.level === 0 ? '开通会员' : member.level === 1 ? '升级SVIP' : '已开通SVIP' }}
            </el-button>
            <el-button type="success" @click="handleSignIn" :disabled="signedToday">
              {{ signedToday ? '今日已签到' : '立即签到' }}
            </el-button>
          </div>
        </div>
      </div>

      <div class="tabs">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="积分商城" name="mall"></el-tab-pane>
          <el-tab-pane label="优惠券" name="coupon"></el-tab-pane>
          <el-tab-pane label="积分记录" name="points"></el-tab-pane>
          <el-tab-pane label="兑换记录" name="exchange"></el-tab-pane>
        </el-tabs>
      </div>

      <div class="tab-content">
        <!-- 积分商城 -->
        <div v-if="activeTab === 'mall'" class="mall-section">
          <div class="filter-bar">
            <el-radio-group v-model="productType" @change="getProducts">
              <el-radio-button :label="null">全部</el-radio-button>
              <el-radio-button :label="0">实物商品</el-radio-button>
              <el-radio-button :label="1">纪念品</el-radio-button>
              <el-radio-button :label="2">虚拟商品</el-radio-button>
            </el-radio-group>
          </div>
          <div class="product-list">
            <div v-for="product in products" :key="product.id" class="product-card">
              <img :src="product.images ? product.images.split(',')[0] : ''" class="product-img">
              <div class="product-info">
                <h3>{{ product.name }}</h3>
                <p class="desc">{{ product.description }}</p>
                <div class="price">
                  <span class="points">{{ product.pointsRequired }}积分</span>
                  <span v-if="product.price" class="money">¥{{ product.price }}</span>
                </div>
                <div class="stock">库存：{{ product.stock }}</div>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="exchangeProduct(product)"
                  :disabled="product.stock <= 0 || member.points < product.pointsRequired">
                  立即兑换
                </el-button>
              </div>
            </div>
          </div>
          <el-pagination
            class="pagination"
            background
            :page-size="search.pageSize"
            :current-page="search.pageNumber"
            :total="total"
            @current-change="handlePageChange"
          ></el-pagination>
        </div>

        <!-- 优惠券 -->
        <div v-if="activeTab === 'coupon'" class="coupon-section">
          <div class="coupon-tabs">
            <el-radio-group v-model="couponTab" @change="getMyCoupons">
              <el-radio-button :label="null">全部</el-radio-button>
              <el-radio-button :label="0">未使用</el-radio-button>
              <el-radio-button :label="1">已使用</el-radio-button>
              <el-radio-button :label="2">已过期</el-radio-button>
            </el-radio-group>
          </div>
          <div class="coupon-list">
            <div v-for="coupon in myCoupons" :key="coupon.id" class="coupon-card" :class="'status-' + coupon.status">
              <div class="coupon-left">
                <div class="discount">¥{{ coupon.discountAmount }}</div>
                <div class="condition">满{{ coupon.minAmount }}可用</div>
              </div>
              <div class="coupon-right">
                <div class="name">{{ coupon.couponName }}</div>
                <div class="time">有效期至：{{ formatTime(coupon.expireTime) }}</div>
                <div class="status-text">
                  {{ coupon.status === 0 ? '未使用' : coupon.status === 1 ? '已使用' : '已过期' }}
                </div>
              </div>
            </div>
            <div v-if="myCoupons.length === 0" class="empty">
              暂无优惠券
            </div>
          </div>
          <h3 style="margin-top: 30px; margin-bottom: 15px;">可兑换优惠券</h3>
          <div class="coupon-list">
            <div v-for="coupon in coupons" :key="coupon.id" class="coupon-card available">
              <div class="coupon-left">
                <div class="discount">¥{{ coupon.discountAmount }}</div>
                <div class="condition">满{{ coupon.minAmount }}可用</div>
              </div>
              <div class="coupon-right">
                <div class="name">{{ coupon.name }}</div>
                <div class="level-require">
                  需要：{{ coupon.requireLevel === 0 ? '普通用户' : coupon.requireLevel === 1 ? 'VIP' : 'SVIP' }}
                </div>
                <div class="points-require">{{ coupon.pointsRequired }}积分兑换</div>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="exchangeCoupon(coupon)"
                  :disabled="coupon.remainCount <= 0 || member.points < coupon.pointsRequired || coupon.requireLevel > member.level">
                  立即兑换
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 积分记录 -->
        <div v-if="activeTab === 'points'" class="points-section">
          <div class="points-list">
            <div v-for="log in pointsLogs" :key="log.id" class="points-item">
              <div class="points-info">
                <div class="desc">{{ log.description }}</div>
                <div class="time">{{ formatTime(log.createTime) }}</div>
              </div>
              <div class="points-change" :class="log.changePoints > 0 ? 'positive' : 'negative'">
                {{ log.changePoints > 0 ? '+' : '' }}{{ log.changePoints }}
              </div>
            </div>
          </div>
          <el-pagination
            class="pagination"
            background
            :page-size="search.pageSize"
            :current-page="search.pageNumber"
            :total="total"
            @current-change="handlePointsPageChange"
          ></el-pagination>
        </div>

        <!-- 兑换记录 -->
        <div v-if="activeTab === 'exchange'" class="exchange-section">
          <div class="exchange-list">
            <div v-for="record in exchangeRecords" :key="record.id" class="exchange-item">
              <div class="exchange-info">
                <div class="name">{{ record.relatedName }}</div>
                <div class="type">
                  {{ record.type === 0 ? '实物商品' : record.type === 1 ? '纪念品' : record.type === 2 ? '景点门票' : '酒店' }}
                </div>
                <div class="points">消耗{{ record.pointsUsed }}积分</div>
                <div v-if="record.amount" class="amount">抵扣¥{{ record.amount }}</div>
                <div class="time">{{ formatTime(record.createTime) }}</div>
                <div class="status">
                  {{ record.status === 0 ? '待处理' : record.status === 1 ? '已完成' : '已取消' }}
                </div>
              </div>
            </div>
          </div>
          <el-pagination
            class="pagination"
            background
            :page-size="search.pageSize"
            :current-page="search.pageNumber"
            :total="total"
            @current-change="handleExchangePageChange"
          ></el-pagination>
        </div>
      </div>
    </div>

    <!-- 购买会员弹窗 -->
    <el-dialog :title="member.level === 1 ? '升级SVIP会员' : '开通会员'" :visible.sync="purchaseDialogVisible" width="500px">
      <div class="purchase-options">
        <div 
          v-if="member.level !== 1" 
          class="option-card" 
          :class="{ selected: selectedLevel === 1 }" 
          @click="selectedLevel = 1"
        >
          <div class="option-title">VIP会员</div>
          <div class="option-price">¥99/月</div>
          <div class="option-benefits">
            <p>✓ 100元消费 = 1积分</p>
            <p>✓ 签到额外+1积分</p>
            <p>✓ 可兑换满100-20优惠券</p>
            <p>✓ 专属客服</p>
          </div>
        </div>
        <div 
          class="option-card" 
          :class="{ selected: selectedLevel === 2 }" 
          @click="selectedLevel = 2"
        >
          <div class="option-title">SVIP会员</div>
          <div class="option-price" v-if="member.level === 1">
            <span class="original-price">¥199</span>
            <span class="upgrade-price">¥100</span>
            <span class="price-desc">补差价</span>
          </div>
          <div class="option-price" v-else>¥199/月</div>
          <div class="option-benefits">
            <p>✓ 100元消费 = 2积分</p>
            <p>✓ 签到额外+1积分</p>
            <p>✓ 可兑换满100-30优惠券</p>
            <p>✓ 专属客服</p>
            <p>✓ 优先体验新功能</p>
          </div>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="purchaseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePurchase">{{ member.level === 1 ? '立即升级' : '立即开通' }}</el-button>
      </div>
    </el-dialog>

    <!-- 兑换商品弹窗 -->
    <el-dialog title="兑换商品" :visible.sync="exchangeDialogVisible" width="500px">
      <div v-if="currentProduct" class="exchange-form">
        <div class="product-preview">
          <img :src="currentProduct.images ? currentProduct.images.split(',')[0] : ''" class="preview-img">
          <div class="preview-info">
            <h3>{{ currentProduct.name }}</h3>
            <p>{{ currentProduct.description }}</p>
            <p class="points-cost">消耗：{{ currentProduct.pointsRequired }}积分</p>
          </div>
        </div>
        <div v-if="currentProduct.type === 0 || currentProduct.type === 1" class="address-form">
          <el-form label-width="80px">
            <el-form-item label="收货人">
              <el-input v-model="exchangeForm.receiver"></el-input>
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="exchangeForm.phone"></el-input>
            </el-form-item>
            <el-form-item label="收货地址">
              <el-input type="textarea" v-model="exchangeForm.address"></el-input>
            </el-form-item>
            <el-form-item label="备注">
              <el-input type="textarea" v-model="exchangeForm.remark"></el-input>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="exchangeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmExchange">确认兑换</el-button>
      </div>
    </el-dialog>

    <bottoms></bottoms>
  </div>
</template>

<script>
import headers from '@/components/header'
import bottoms from '@/components/bottom'
import { getUser, getMemberInfo, signIn, getPointsLog, getCoupons, exchangeCoupon, getMyCoupons, getProducts, exchangeProduct, getExchangeRecords, purchaseMember } from '@/api/api'

export default {
  components: { headers, bottoms },
  data() {
    return {
      userInfo: null,
      member: { level: 0, points: 0 }, // 添加默认值
      signedToday: false,
      activeTab: 'mall',
      productType: null,
      products: [],
      coupons: [],
      myCoupons: [],
      pointsLogs: [],
      exchangeRecords: [],
      search: {
        pageNumber: 1,
        pageSize: 10
      },
      total: 0,
      couponTab: null,
      purchaseDialogVisible: false,
      selectedLevel: 1,
      exchangeDialogVisible: false,
      currentProduct: null,
      exchangeForm: {
        receiver: '',
        phone: '',
        address: '',
        remark: ''
      }
    }
  },
  mounted() {
    this.loadUserInfo()
  },
  methods: {
    loadUserInfo() {
      getUser().then(res => {
        if (res.code === 1000) {
          this.userInfo = res.data
          // 用户信息加载完成后再加载会员信息
          this.loadMemberInfo()
        }
      })
      this.getProducts()
    },
    loadMemberInfo() {
      if (!this.userInfo) return
      getMemberInfo({ userId: this.userInfo.id }).then(res => {
        if (res.code === 1000) {
          this.member = res.data.member
          this.signedToday = res.data.signedToday
          // 会员信息加载完成后再获取优惠券
          this.getCoupons()
        }
      })
    },
    handleSignIn() {
      if (!this.userInfo) return
      signIn({ userId: this.userInfo.id }).then(res => {
        if (res.code === 1000) {
          this.$message.success('签到成功')
          this.loadMemberInfo()
        } else {
          this.$message.error(res.message)
        }
      })
    },
    handleTabClick() {
      this.search.pageNumber = 1
      if (this.activeTab === 'mall') {
        this.getProducts()
      } else if (this.activeTab === 'coupon') {
        this.getMyCoupons()
      } else if (this.activeTab === 'points') {
        this.getPointsLog()
      } else if (this.activeTab === 'exchange') {
        this.getExchangeRecords()
      }
    },
    getProducts() {
      const params = {
        pageNumber: this.search.pageNumber,
        pageSize: this.search.pageSize
      }
      if (this.productType !== null) {
        params.type = this.productType
      }
      getProducts(params).then(res => {
        if (res.code === 1000) {
          this.products = res.data.records
          this.total = res.data.total
        }
      })
    },
    getCoupons() {
      if (!this.userInfo) return
      console.log('获取优惠券，用户等级:', this.member.level)
      getCoupons({ userLevel: this.member.level }).then(res => {
        console.log('获取优惠券响应:', res)
        if (res.code === 1000) {
          this.coupons = res.data
          console.log('优惠券列表:', this.coupons)
        }
      }).catch(err => {
        console.error('获取优惠券失败:', err)
      })
    },
    exchangeCoupon(coupon) {
      this.$confirm(`确定要花费${coupon.pointsRequired}积分兑换此优惠券吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        exchangeCoupon({ userId: this.userInfo.id, couponId: coupon.id }).then(res => {
          if (res.code === 1000) {
            this.$message.success('兑换成功')
            this.loadMemberInfo()
            this.getCoupons()
            this.getMyCoupons()
          } else {
            this.$message.error(res.message)
          }
        })
      }).catch(() => {})
    },
    getMyCoupons() {
      if (!this.userInfo) return
      const params = { userId: this.userInfo.id }
      if (this.couponTab !== null) {
        params.status = this.couponTab
      }
      getMyCoupons(params).then(res => {
        if (res.code === 1000) {
          this.myCoupons = res.data
        }
      })
    },
    getPointsLog() {
      if (!this.userInfo) return
      getPointsLog({
        userId: this.userInfo.id,
        pageNumber: this.search.pageNumber,
        pageSize: this.search.pageSize
      }).then(res => {
        if (res.code === 1000) {
          this.pointsLogs = res.data.records
          this.total = res.data.total
        }
      })
    },
    getExchangeRecords() {
      if (!this.userInfo) return
      getExchangeRecords({
        userId: this.userInfo.id,
        pageNumber: this.search.pageNumber,
        pageSize: this.search.pageSize
      }).then(res => {
        if (res.code === 1000) {
          this.exchangeRecords = res.data.records
          this.total = res.data.total
        }
      })
    },
    exchangeProduct(product) {
      this.currentProduct = product
      this.exchangeForm = {
        receiver: '',
        phone: '',
        address: '',
        remark: ''
      }
      this.exchangeDialogVisible = true
    },
    confirmExchange() {
      if (!this.currentProduct || !this.userInfo) return
      
      const params = {
        userId: this.userInfo.id,
        productId: this.currentProduct.id
      }
      
      if (this.currentProduct.type === 0 || this.currentProduct.type === 1) {
        if (!this.exchangeForm.receiver || !this.exchangeForm.phone || !this.exchangeForm.address) {
          this.$message.error('请填写收货信息')
          return
        }
        params.address = this.exchangeForm.address
        params.phone = this.exchangeForm.phone
        params.receiver = this.exchangeForm.receiver
        params.remark = this.exchangeForm.remark
      }
      
      exchangeProduct(params).then(res => {
        if (res.code === 1000) {
          this.$message.success('兑换成功')
          this.exchangeDialogVisible = false
          this.loadMemberInfo()
          this.getProducts()
        } else {
          this.$message.error(res.message)
        }
      })
    },
    openPurchaseDialog() {
      this.selectedLevel = this.member.level === 0 ? 1 : 2
      this.purchaseDialogVisible = true
    },
    handlePurchase() {
      if (!this.userInfo) return
      purchaseMember({ userId: this.userInfo.id, level: this.selectedLevel }).then(res => {
        if (res.code === 1000) {
          this.$message.success('开通成功')
          this.purchaseDialogVisible = false
          this.loadMemberInfo()
        } else {
          this.$message.error(res.message)
        }
      })
    },
    handlePageChange(page) {
      this.search.pageNumber = page
      this.getProducts()
    },
    handlePointsPageChange(page) {
      this.search.pageNumber = page
      this.getPointsLog()
    },
    handleExchangePageChange(page) {
      this.search.pageNumber = page
      this.getExchangeRecords()
    },
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      const h = String(date.getHours()).padStart(2, '0')
      const i = String(date.getMinutes()).padStart(2, '0')
      const s = String(date.getSeconds()).padStart(2, '0')
      return `${y}-${m}-${d} ${h}:${i}:${s}`
    }
  }
}
</script>

<style scoped>
.member-center {
  min-height: 100vh;
  background: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 20px 40px;
}

.member-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 30px;
  color: white;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 30px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid rgba(255,255,255,0.3);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info h2 {
  margin: 0 0 10px;
  font-size: 28px;
}

.level-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 14px;
  margin-bottom: 10px;
}

.level-badge.level-0 {
  background: rgba(255,255,255,0.3);
}

.level-badge.level-1 {
  background: #ff9800;
}

.level-badge.level-2 {
  background: linear-gradient(135deg, #ffd700, #ff8c00);
}

.points {
  font-size: 18px;
  margin-bottom: 10px;
}

.points-label {
  opacity: 0.9;
}

.points-value {
  font-size: 24px;
  font-weight: bold;
  margin-left: 10px;
}

.expire-time {
  font-size: 14px;
  opacity: 0.9;
}

.member-actions {
  margin-left: auto;
  display: flex;
  gap: 15px;
}

.tabs {
  background: white;
  border-radius: 12px;
  padding: 0 20px;
  margin-bottom: 20px;
}

.tab-content {
  background: white;
  border-radius: 12px;
  padding: 30px;
  min-height: 400px;
}

.filter-bar {
  margin-bottom: 20px;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.product-card {
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.product-card:hover {
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  transform: translateY(-5px);
}

.product-img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-info h3 {
  margin: 0 0 10px;
  font-size: 16px;
}

.desc {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  margin-bottom: 10px;
}

.points {
  color: #ff6b6b;
  font-weight: bold;
  font-size: 18px;
}

.money {
  color: #909399;
  text-decoration: line-through;
  margin-left: 10px;
  font-size: 14px;
}

.stock {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.coupon-tabs {
  margin-bottom: 20px;
}

.coupon-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.coupon-card {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.coupon-card.status-0 {
  background: linear-gradient(135deg, #fff5f5, #fff);
}

.coupon-card.status-1,
.coupon-card.status-2 {
  background: #f5f7fa;
  opacity: 0.6;
}

.coupon-card.available {
  background: linear-gradient(135deg, #fff5f5, #fff);
}

.coupon-left {
  background: linear-gradient(135deg, #ff6b6b, #ff8e53);
  padding: 20px;
  min-width: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
}

.coupon-card.status-1 .coupon-left,
.coupon-card.status-2 .coupon-left {
  background: #909399;
}

.discount {
  font-size: 32px;
  font-weight: bold;
}

.condition {
  font-size: 12px;
  margin-top: 5px;
}

.coupon-right {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.coupon-right .name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
}

.coupon-right .time,
.coupon-right .level-require,
.coupon-right .points-require {
  font-size: 13px;
  color: #909399;
  margin-bottom: 5px;
}

.status-text {
  font-size: 14px;
  color: #67c23a;
  margin-top: 10px;
}

.points-list,
.exchange-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.points-item,
.exchange-item {
  padding: 20px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points-info,
.exchange-info {
  flex: 1;
}

.points-info .desc,
.exchange-info .name {
  font-size: 16px;
  margin-bottom: 8px;
}

.points-info .time,
.exchange-info .time {
  color: #909399;
  font-size: 14px;
}

.points-change {
  font-size: 24px;
  font-weight: bold;
}

.points-change.positive {
  color: #67c23a;
}

.points-change.negative {
  color: #ff6b6b;
}

.exchange-info .type,
.exchange-info .points,
.exchange-info .amount,
.exchange-info .status {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.pagination {
  margin-top: 30px;
  text-align: center;
}

.empty {
  text-align: center;
  color: #909399;
  padding: 60px 0;
}

.purchase-options {
  display: flex;
  gap: 20px;
}

.option-card {
  flex: 1;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  padding: 30px;
  cursor: pointer;
  transition: all 0.3s;
}

.option-card:hover,
.option-card.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.option-title {
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 15px;
}

.option-price {
  font-size: 28px;
  color: #ff6b6b;
  font-weight: bold;
  text-align: center;
  margin-bottom: 20px;
}

.original-price {
  text-decoration: line-through;
  color: #909399;
  font-size: 18px;
  margin-right: 10px;
}

.upgrade-price {
  color: #ff6b6b;
  font-size: 32px;
}

.price-desc {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.option-benefits p {
  margin: 8px 0;
  color: #606266;
}

.exchange-form .product-preview {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.preview-img {
  width: 150px;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
}

.preview-info h3 {
  margin: 0 0 10px;
}

.preview-info p {
  margin: 5px 0;
  color: #606266;
}

.points-cost {
  color: #ff6b6b;
  font-weight: bold;
  font-size: 18px;
}
</style>
