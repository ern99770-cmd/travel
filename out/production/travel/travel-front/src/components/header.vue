<template>
  <div class="header">
    <div class="header1">
      <img style="height:90%" src="../assets/image/logo.png">
      <div class="header4">
        <div class="menu-list">
            <div @click="toPage('index')" :class="$route.path == '/'?'menu_item':''">系统主页</div>
            <div @click="toPage('attractions')" :class="$route.path == '/attractions'?'menu_item':''">景点信息</div>
            <div @click="toPage('line')" :class="$route.path == '/line'?'menu_item':''">旅游线路</div>
            <div @click="toPage('hotel')" :class="$route.path == '/hotel'?'menu_item':''">景区酒店</div>
            <div @click="toPage('forum')" :class="$route.path == '/forum'?'menu_item':''">旅游资讯</div>
            <div @click="toPage('favor')" :class="$route.path == '/favor'?'menu_item':''">我的收藏</div>
            <div @click="toPage('order')" :class="$route.path == '/order'?'menu_item':''">我的预定</div>
            <!-- <div @click="toPage('manage')" :class="$route.path == '/manage'?'menu_item':''">后台管理</div> -->
            <div @click="toPage('ai')" :class="$route.path == '/ai'?'menu_item':''">智游向导</div>
        </div>
      </div>
      <div class="header5">
        <div class="avatar-container" @click="toCenter">
          <img v-if="!userInfo" class="user-avatar" src="../assets/image/image 2.png">
          <img v-else class="user-avatar" :src="$store.state.HOST + userInfo.avatar">
          <span v-if="userInfo" class="user-name">{{userInfo.userName || userInfo.loginAccount}}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    data() {
      return{
        name: "",
        userInfo: null
      }
    },
    methods: {
      toCenter() {
        this.$router.push("/center")
      },
      toPage(name) {
        if (name == "index") {
          this.$router.push("/")
        } else if (name == "attractions") {
          this.$router.push("/attractions")
        } else if (name == "line") {
          this.$router.push("/line")
        } else if (name == "hotel") {
          this.$router.push("/hotel")
        } else if (name == "forum") {
          this.$router.push("/forum")
        } else if (name == "manage") {
          window.open("http://localhost:3000")
        } else if (name == "order") {
          this.$router.push("/order")
        } else if (name == "favor") {
          this.$router.push("/favor")
        } else if (name == "order") {
          this.$router.push("/order")
        } else if (name == "ai") {
          this.$router.push("/ai")
        }
      },
      loginOut() {
        this.$store.dispatch('logout').then(() => {
          window.localStorage.removeItem("user_info")
          window.localStorage.removeItem("user_token")
          this.userInfo = null
          this.$message({
              message: '退出成功',
              type: 'success'
          });
          setTimeout(function(){
              window.location.reload()
          },1000)
        })
      },
      getUserInfo() {
        const userInfoStr = window.localStorage.getItem("user_info")
        if (userInfoStr) {
          this.userInfo = JSON.parse(userInfoStr)
        }
      }
    },
    created() {
      this.getUserInfo()
    },
    mounted() {
      // 监听用户信息更新
      window.addEventListener('storage', (e) => {
        if (e.key === 'user_info') {
          this.getUserInfo()
        }
      })
    },
    beforeDestroy() {
      // 移除事件监听
      window.removeEventListener('storage', this.getUserInfo)
    }
 }
</script>

<style scoped>
  .header {
      width: 100%;
      height: 80px;
      display: flex;
      flex-direction: column;
      align-items: center;
      background-color: #fff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
      position: fixed;
      top: 0;
      z-index: 1000;
  }
  .header1 {
    width: 80%;
    height: 80px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .header4 {
    width: 80%;
    height: 60px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .menu-list {
    display: flex;
    color: #2c3e50;
    gap: 20px;
  }
  .menu-list>div{
    font-size: 16px;
    padding: 8px 16px;
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.3s;
    position: relative;
  }
  .menu-list>div:hover {
    color: #409EFF;
    background-color: rgba(64, 158, 255, 0.1);
  }
  .menu_item {
    color: #409EFF;
    font-weight: 500;
  }
  .menu_item::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 4px;
    height: 4px;
    background-color: #409EFF;
    border-radius: 50%;
  }
  .header5 {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  .avatar-container {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 20px;
    transition: all 0.3s;
  }
  .avatar-container:hover {
    background-color: rgba(64, 158, 255, 0.1);
  }
  .user-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
    box-shadow: 0 2px 6px rgba(0,0,0,0.1);
    border: 2px solid #fff;
  }
  .user-name {
    font-size: 14px;
    color: #2c3e50;
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
</style>