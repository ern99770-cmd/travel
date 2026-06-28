<template>
  <div class="login">
    <div class="login1">
      <div class="login2">探索世界的每一个角落</div>
      <div class="login3">开启您的个性化旅行之旅</div>
    </div>
    <div class="login4">
        <div class="login5">
            <img src="../../assets/image/logo.png" alt="旅游平台">
            <div class="login6">欢迎使用个性化旅游规划系统</div>
            <div class="login3">
                珍藏每一刻的美好，旅行让生活变得更加有意义！
            </div>
            <el-input 
              prefix-icon="el-icon-user" 
              v-model="loginAccount" 
              placeholder="请输入登录账号"
              clearable>
            </el-input>
            <el-input 
              prefix-icon="el-icon-star-off" 
              type="password" 
              v-model="password" 
              placeholder="请输入用户密码"
              show-password>
            </el-input>
            <div class="login7" @click="toForget">
                忘记密码？
            </div>
            <div class="login8" @click="login">
                登 录
            </div>
            <div class="login8" style="background-color:#28a745;" @click="toRegister">
                注 册
            </div>
        </div>
    </div>
  </div>
</template>

<script>
  import {login,getUser} from '../../api/api'
  export default {
    data() {
      return{
        loginAccount: "",
        password: "",
      }
    },
    methods: {
        toRegister() {
          this.$router.push("/register")
        },
        toForget() {
          this.$router.push("/forget")
        },
        login() {
            if(!this.loginAccount) {
                this.$message({
                    message: '请输入用户名',
                    type: 'warning'
                });
                return;
            }
            if(!this.password) {
                this.$message({
                    message: '请输入密码',
                    type: 'warning'
                });
                return;
            }
            var params = {
                loginAccount: this.loginAccount,
                password: this.password
            }
            login(params).then(res => {
                if(res.code == 1000) {
                    this.$message({
                        message: '登陆成功',
                        type: 'success'
                    });
                    var that = this
                    var token = res.data.token
                    window.localStorage.setItem("user_token",token)
                    this.getUserInfo()
                    setTimeout(function() {
                        that.$router.push("/")
                    },500)
                } else {
                    this.$message.error(res.message);
                }
            })
        },
        getUserInfo() {
            getUser().then(res => {
                if(res.code == 1000) {
                    window.localStorage.setItem("user_info",JSON.stringify(res.data))
                }
            })
        },
    },
    created() {
     
    },
    mounted() {
      
    }
 }
</script>

<style scoped>
  @import url('../../assets/css/login.css');
</style>