<template>
  <div class="login">
    <div class="login1">
      <div class="login-bg-content">
        <h1>探索世界的美好</h1>
        <p>让每一次旅行都成为难忘的回忆</p>
      </div>
    </div>
    <div class="login4">
        <div class="login5">
            <img src="../../../assets/image/logo.png" class="logo-img">
            <div class="login6">欢迎使用个性化旅游规划系统</div>
            <div class="login3">
                珍藏每一刻的美好，旅行让生活变得更加有意义！
            </div>
            <div class="input-group">
              <el-input 
                prefix-icon="el-icon-user" 
                v-model="loginAccount" 
                placeholder="请输入登录账号"
                class="custom-input">
              </el-input>
              <el-input 
                prefix-icon="el-icon-lock" 
                type="password" 
                v-model="password" 
                placeholder="请输入用户密码"
                class="custom-input">
              </el-input>
            </div>
            <div class="login8" @click="login">
                登 录
            </div>
        </div>
    </div>
  </div>
</template>

<script>
  import {login,getUser} from '../../../api/api' 
  export default {
    data() {
      return{
        loginAccount: '',
        password: ''
      }
    },
    methods: {
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
                    this.$store.commit('user/setToken', token)
                    this.getUserInfo()
                    setTimeout(function() {
                        that.$router.push("/index")
                    },500)
                } else {
                    this.$message.error(res.message);
                }
            })
        },
        getUserInfo() {
            getUser().then(res => {
                if(res.code == 1000) {
                    this.$store.commit('user/setUser', JSON.stringify(res.data))
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
.login {
    width: 100%;
    height: 100vh;
    font-family: '黑体';
    display: flex;
    background: #f5f7fa;
}
.login1 {
    width: 60%;
    height: 100%;
    background-image: url('../../../assets/image/image 2.png');
    background-size: cover;
    background-position: center;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
}
.login1::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.3);
}
.login-bg-content {
    position: relative;
    z-index: 1;
    color: #fff;
    text-align: center;
    padding: 20px;
}
.login-bg-content h1 {
    font-size: 48px;
    margin-bottom: 20px;
    text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}
.login-bg-content p {
    font-size: 24px;
    text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
}
.login2 {
    font-size: 35px;
    font-weight: bold;
}
.login3 {
    margin-top: 20px;
    letter-spacing: 2px;
    font-size: 18px;
    color: #666;
    text-align: center;
}
.login4 {
    width: 40%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background: #fff;
}
.login5 {
    width: 80%;
    height: 80%;
    flex-direction: column;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 40px;
    background: #fff;
    border-radius: 20px;
    box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}
.logo-img {
    width: 80px;
    height: 80px;
    margin-bottom: 30px;
    border-radius: 50%;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.login6 {
    font-size: 28px;
    font-weight: bold;
    color: #333;
    margin-bottom: 20px;
    text-align: center;
}
.login7 {
    width: 100%;
    text-align: right;
    cursor: pointer;
}
.input-group {
    width: 100%;
    max-width: 320px;
    margin: 30px 0;
}
.custom-input {
    margin-bottom: 20px;
}
.custom-input >>> .el-input__inner {
    height: 50px;
    line-height: 50px;
    border-radius: 25px;
    padding-left: 45px;
    font-size: 16px;
    border: 1px solid #dcdfe6;
    transition: all 0.3s;
}
.custom-input >>> .el-input__inner:focus {
    border-color: #409EFF;
    box-shadow: 0 0 0 2px rgba(64,158,255,0.2);
}
.custom-input >>> .el-input__prefix {
    left: 15px;
}
.login8 {
    display: flex;
    justify-content: center;
    align-items: center;
    color: #ffffff;
    width: 100%;
    max-width: 320px;
    height: 50px;
    cursor: pointer;
    border-radius: 25px;
    background: linear-gradient(135deg, #409EFF, #3E78F3);
    font-size: 18px;
    font-weight: 500;
    transition: all 0.3s;
    box-shadow: 0 4px 12px rgba(62,120,243,0.3);
}
.login8:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(62,120,243,0.4);
}
.login8:active {
    transform: translateY(0);
}
</style>