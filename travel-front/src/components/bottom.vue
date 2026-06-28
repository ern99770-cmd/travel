<template>
  <footer class="footer">
    <div class="footer-content">
      <!-- Logo 和描述 -->
      <div class="footer-section">
        <img src="../assets/image/Frame 28.png" alt="Logo" class="footer-logo">
        <p class="footer-desc">探索世界的美好，开启您的旅程。</p>
      </div>

      <!-- 快速链接 -->
      <div class="footer-section">
        <h3>快速链接</h3>
        <ul>
          <li v-for="(link, index) in quickLinks" :key="index">
            <a :href="link.url" @click.prevent="toPage(link.path)">{{ link.text }}</a>
          </li>
        </ul>
      </div>

      <!-- 服务支持 -->
      <div class="footer-section">
        <h3>服务支持</h3>
        <ul>
          <li v-for="(link, index) in serviceLinks" :key="index">
            <a :href="link.url" @click.prevent="toPage(link.path)">{{ link.text }}</a>
          </li>
        </ul>
      </div>

      <!-- 联系我们 -->
      <div class="footer-section">
        <h3>联系我们</h3>
        <ul class="contact-info">
          <li><i class="el-icon-phone"></i> 400-123-4567</li>
          <li><i class="el-icon-message"></i> support@travel.com</li>
        </ul>
        <div class="social-links">
          <a href="#" class="social-icon"><i class="el-icon-chat-dot-round"></i></a>
          <a href="#" class="social-icon"><i class="el-icon-share"></i></a>
          <a href="#" class="social-icon"><i class="el-icon-star-on"></i></a>
        </div>
      </div>
    </div>

    <!-- 底部版权信息 -->
    <div class="footer-bottom">
      <span class="copyright">©  2025 个性化旅游规划系统. All rights reserved.</span>
      <div class="footer-links">
        <a href="#">隐私政策</a>
        <a href="#">使用条款</a>
        <a href="#">网站地图</a>
      </div>
    </div>
  </footer>
</template>
<script>
export default {
  name: 'Footer',
  data() {
    return {
      // 快速链接
      quickLinks: [
        { text: '首页', url: '/', path: 'index' },
        { text: '景点信息', url: '/attractions', path: 'attractions' },
        { text: '旅游线路', url: '/line', path: 'line' },
        { text: '景区酒店', url: '/hotel', path: 'hotel' },
        { text: '旅游资讯', url: '/forum', path: 'forum' },
        { text: '旅途印记', url: '/travelShare', path: 'travelShare' }
      ],
      // 服务支持
      serviceLinks: [
        { text: '我的收藏', url: '/favor', path: 'favor' },
        { text: '我的预定', url: '/order', path: 'order' },
        { text: '个人中心', url: '/center', path: 'center' },
        { text: '财经顾问', url: '/ai', path: 'ai' }
      ]
    };
  },
  methods: {
    toPage(name) {
      this.$router.push(`/${name}`);
    }
  }
};
</script>
<style scoped>
/* 页面容器 */
.page-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh; /* 确保页面高度至少占满视口 */
}

/* 主要内容区域 */
.main-content {
  flex: 1; /* 占据剩余空间，将页脚推到页面底部 */
  padding: 20px;
}

/* 页脚样式 */
.footer {
  background-color: #2e8b57;
  color: #fff;
  padding: 40px 0 15px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  position: relative;
  width: 100%;
  margin-top: 40px;
}

.footer-content {
  max-width: 1000px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
  padding: 0 15px;
}

.footer-section h3 {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 5px;
}

.footer-logo {
  max-width: 80px;
  margin-bottom: 5px;
}

.footer-desc {
  font-size: 10px;
  line-height: 1.2;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-section ul li {
  margin-bottom: 3px;
}

.footer-section ul li a {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  transition: all 0.3s;
}

.footer-section ul li a:hover {
  color: #98fb98;
}

.contact-info li {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.9);
}

.social-links {
  display: flex;
  gap: 5px;
  margin-top: 5px;
}

.social-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  text-decoration: none;
  transition: all 0.3s;
}

.social-icon:hover {
  background-color: #98fb98;
  transform: translateY(-1px);
}

.footer-bottom {
  max-width: 1000px;
  margin: 30px auto 0;
  padding: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  position: relative;
}

.footer-links {
  display: flex;
  gap: 5px;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #98fb98;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .footer-bottom {
    flex-direction: column;
    gap: 5px;
    text-align: center;
  }
}
</style>