<template>
    <el-aside :width="isCollapse ? '64px' : '240px'">
        <div class="back-icon" :style="{width: isCollapse ? '63px' : '239px'}">
            <img class="logo" style="border-radius:20px" src="../../assets/image/logo.png"/>
        </div>
        <el-menu
            @select="handleSelect"
            :collapse="isCollapse"
            :collapse-transition="false"
            class="el-menu-vertical"
            :unique-opened="true"
            background-color="#ffffff"
            :text-color="textColor"
            :default-active="this.$route.path + '@' + this.$route.name"
            >
                <el-menu-item index="/index@首页">
                    <i class="el-icon-menu"></i>
                    <span slot="title">首页</span>
                </el-menu-item>
                <el-menu-item index="/attractions@景点管理">
                    <i class="el-icon-s-ticket"></i>
                    <span slot="title">景点管理</span>
                </el-menu-item>
                <el-menu-item index="/line@线路管理">
                    <i class="el-icon-help"></i>
                    <span slot="title">线路管理</span>
                </el-menu-item>
                <el-menu-item index="/hotel@酒店管理">
                    <i class="el-icon-s-shop"></i>
                    <span slot="title">酒店管理</span>
                </el-menu-item>
                <el-menu-item index="/forum@资讯管理">
                    <i class="el-icon-picture-outline-round"></i>
                    <span slot="title">资讯管理</span>
                </el-menu-item>
                <el-menu-item index="/order@景点预约">
                    <i class="el-icon-s-promotion"></i>
                    <span slot="title">景点预约</span>
                </el-menu-item>
                <el-menu-item index="/orderHotel@酒店预约">
                    <i class="el-icon-s-home"></i>
                    <span slot="title">酒店预约</span>
                </el-menu-item>
                <el-menu-item index="/rotations@轮播图管理">
                    <i class="el-icon-picture"></i>
                    <span slot="title">轮播图管理</span>
                </el-menu-item>
                <el-menu-item index="/center@个人中心">
                    <i class="el-icon-user"></i>
                    <span slot="title">个人中心</span>
                </el-menu-item>
                <el-submenu index="1 ">
                    <template slot="title">
                        <i class="el-icon-s-platform"></i>
                        <span v-if="!isCollapse">用户管理</span>
                    </template>
                    <el-menu-item index="/admin@管理员管理">
                        <i class="el-icon-s-promotion"></i>
                        <span slot="title">管理员管理</span>
                    </el-menu-item>
                    <el-menu-item index="/user@普通用户管理">
                        <i class="el-icon-coordinate"></i>
                        <span slot="title">普通用户管理</span>
                    </el-menu-item>
                </el-submenu>
        </el-menu>
    </el-aside>
</template>

<script>
  import { mapState } from 'vuex'
  export default {
    name: "asside",
    data() {
      return{
        isCollapse: false,
        textColor: "#606266"
      }
    },
    computed: {
      ...mapState({
          activeMenuArrary: state => state.menu.activeMenuArrary,
          displayMenus: state => state.menu.displayMenus,
      })
    },
    methods: {
        handleSelect(key) {
            var menu = key.split("@")
            this.$router.push({
                path: menu[0],
            })
            this.$store.commit('menu/setActiveMenu', menu[0])
            for(let i = 0;i < this.activeMenuArrary.length;i++) {
                if(this.activeMenuArrary[i].url == menu[0]) {
                    return;
                }
            }
            var param = {
                "name": menu[1],
                "url": menu[0]
            }
            this.$store.commit('menu/addActiveMenu', param)
        }
    },
    created() {

    },
    mounted() {
        // 监听collapse
        this.$bus.$on('collapse', res=>{
            this.isCollapse = res
        })
        this.$store.commit('menu/setActiveMenu', this.$route.path)
        for(let i = 0;i < this.activeMenuArrary.length;i++) {
            if(this.activeMenuArrary[i].url == this.$route.path) {
                return;
            }
        }
        var param = {
            "name": this.$route.name,
            "url": this.$route.path
        }
        this.$store.commit('menu/addActiveMenu', param)
    },
 }
</script>

<style scoped lang="scss">
    .el-aside {
        background-color: #ffffff;
        box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
    }
    .back-icon {
        display: flex;
        height: 100px;
        background-color: #ffffff;
        justify-content: center;
        align-items: center;
        border-bottom: 1px solid #f0f0f0;
        margin-bottom: 10px;
    }
    .logo {
        width: 60px;
        height: 60px;
        border-radius: 15px;
        box-shadow: 0 4px 10px rgba(0,0,0,0.1);
        transition: all 0.3s;
    }
    .logo:hover {
        transform: scale(1.05);
    }
    .logo-text {
        padding-left: 6px;
        color: #333;
        font-family: Mulish;
        font-size: 15px;
        font-weight: 700;
        line-height: 24;
        letter-spacing: 0.4000000059604645px;
    }
    .el-menu {
        height: calc(100vh - 110px);
        overflow-x: hidden;
        font-family: "黑体" !important;
        font-weight: 550 !important;
        border-right: none !important;
        padding: 0 10px;
    }
    .el-menu::-webkit-scrollbar,.el-aside::-webkit-scrollbar {
        width: 0px;
        height: 0px;
    }
    .el-submenu>>>.el-submenu__title {
        height: 56px !important;
        line-height: 56px !important;
        margin: 5px 0;
        border-radius: 8px;
        font-size: 15px;
        padding-left: 20px !important;
        background-color: #ffffff !important;
        transition: all 0.3s;
    }
    .el-submenu>>>.el-submenu__title:hover {
        color: #409EFF !important;
        background-color: #f5f7fa !important;
        transform: translateX(5px);
    } 
    .el-menu-vertical>>>.el-menu-item {
        height: 56px !important;
        line-height: 56px !important;
        margin: 5px 0;
        border-radius: 8px;
        font-size: 15px;
        padding-left: 20px !important;
        transition: all 0.3s;
    }
    .el-menu-vertical>>>.el-menu-item:hover {
        color: #409EFF !important;
        background-color: #f5f7fa !important;
        transform: translateX(5px);
    }
    .el-menu-vertical>>>.el-menu-item.is-active {
        background-color: #ecf5ff !important;
        color: #409EFF !important;
        font-weight: bold;
    }
    .el-menu-vertical>>>.el-menu-item i {
        font-size: 18px;
        margin-right: 5px;
        color: inherit;
    }
    .el-submenu>>>.el-submenu__title i {
        font-size: 18px;
        margin-right: 5px;
        color: inherit;
    }
    .el-aside .el-menu {
        border-right: none !important;
    }
    .el-submenu .el-menu {
        padding: 0;
    }
    .el-submenu .el-menu-item {
        height: 52px !important;
        line-height: 52px !important;
        background-color: #ffffff !important;
        margin: 4px 0;
        padding-left: 50px !important;
        border-radius: 8px;
        font-size: 14px;
    }
</style>