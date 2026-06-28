package com.project.travel.utils;

import com.project.travel.constant.Constants;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @version 1.0
 * @description: TODO
 * @date 2024/2/26 22:09
 */
public class TokenUtils {

    /**
    * 解析token获取userId
    */
    public static String getUserIdByToken() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                return null;
            }
            HttpServletRequest request = requestAttributes.getRequest();
            String token = request.getHeader(Constants.X_ACCESS_TOKEN);
            if (token == null || token.isEmpty()) {
                return null;
            }
            return JwtUtil.getUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

}
