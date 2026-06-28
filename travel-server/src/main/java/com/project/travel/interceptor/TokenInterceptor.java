package com.project.travel.interceptor;

import com.project.travel.constant.Constants;
import com.project.travel.domain.User;
import com.project.travel.service.UserService;
import com.project.travel.utils.JwtUtil;
import com.project.travel.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @version 1.0
 * @description: TODO
 * @date 2023/3/18 23:08
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 直接放行静态资源请求
        String requestURI = request.getRequestURI();
        System.out.println("=== 请求路径: " + requestURI);
        
        // 放行静态资源，支持多种路径格式
        if (requestURI.startsWith("/img/") || requestURI.contains("/img/") ||
            requestURI.startsWith("/file/") || requestURI.contains("/file/") ||
            requestURI.startsWith("/video/") || requestURI.contains("/video/")) {
            System.out.println("=== 放行静态资源: " + requestURI);
            return true;
        }
        
        // 是否登录
        boolean isLogin = false;
        // 请求头带上令牌 x_access_token :Bearer token
        String userId = JwtUtil.getUserIdByToken(request);
        System.out.println("=== 从Token解析出userId: " + userId);
        
        if (userId != null) {
            String token = redisUtils.get(Constants.PREFIX_USER_TOKEN + userId);
            System.out.println("=== 从Redis获取token: " + (token != null ? "存在" : "不存在"));
            
            if (null != token) {
                User user = userService.getById(userId);
                System.out.println("=== 查询用户信息: " + (user != null ? user.getUserName() : "用户不存在"));
                
                if (user != null) {
                    // 解析token
                    try {
                        // 校验token有效性
                        System.out.println("=== 开始验证Token...");
                        if (JwtUtil.verify(token, userId, user.getPassword())) {
                            isLogin = true;
                            System.out.println("=== Token验证成功，放行请求");
                        } else {
                            System.out.println("=== Token验证失败");
                        }
                    } catch (Exception e) {
                        System.out.println("=== 解码异常为》》》》" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        
        if (!isLogin) {
            System.out.println("=== 未登录或登录过期，返回1011状态码");
            // 未登录，则响应信息
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(1011);
            response.getWriter().write("登录已过期或未登录，请重新登录");
        }
        // 放行或拒绝
        return isLogin;
    }

}
