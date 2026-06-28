package com.project.travel.config;

import com.project.travel.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 注册拦截器
 * @author AA
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                //拦截所有请求
                .addPathPatterns("/**")
                //排除请求
                .excludePathPatterns(
                        // 静态资源
                        "/file/**",
                        "/video/**",
                        "/img/**",
                        "/img/**/**",
                        // 用户相关
                        "/login",
                        "/common/**",
                        "/user/setUserAvatar/**",
                        "/user/getEmailReg/**",
                        "/user/forgetPassword/**",
                        "/user/saveUser/**",
                        // AI相关
                        "/ai/**",
                        // 旅途印记相关 - 查看接口不需要登录
                        "/share/getSysTravelSharePage",
                        "/share/getSysTravelShareById",
                        "/share/getMySharePage",
                        // 评论相关 - 查看评论不需要登录
                        "/comments/getSysCommentsPage",
                        "/comments/getCommentsByTargetId",
                        "/comments/getSysCommentsById",
                        // 测试使用 - 临时排除点赞和发布，确保功能可用
                        "/share/likeShare",
                        "/share/saveSysTravelShare",
                        "/share/editSysTravelShare",
                        "/share/removeSysTravelShare",
                        "/comments/saveSysComments"
                );
    }
}
