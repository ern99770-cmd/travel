package com.project.travel.controller.common;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.travel.domain.Result;
import com.project.travel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @description: TODO
 * @date 2023/8/28 11:35
 */
@Controller
@ResponseBody
@RequestMapping("common")
public class CommonController {

    @Autowired
    private UserService userService;
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    /**
    * @description: 错误转发地址
    * @param: code
    	msg
    * @return:
    * @date: 2023/9/14 15:05
    */
    @GetMapping("/error/{code}/{msg}")
    public Result error (@PathVariable("code")Integer code, @PathVariable("msg") String msg){
        return Result.alert(code,msg);
    }

    /**
     * @description: 上传图片
     * @param: file
     * @return:
     * @date: 2023/10/13 10:44
     */
    @PostMapping("uploadImg")
    public Result uploadImg(@RequestParam("file") MultipartFile img) {
        if(img.isEmpty()){
            return Result.fail("上传的图片不能为空!");
        }
        String coverType = img.getOriginalFilename().substring(img.getOriginalFilename().lastIndexOf(".") + 1).toLowerCase();
        if ("jpeg".equals(coverType)  || "gif".equals(coverType) || "png".equals(coverType) || "bmp".equals(coverType)  || "jpg".equals(coverType)) {
            //文件名=当前时间到毫秒+原来的文件名
            String fileName = System.currentTimeMillis() + img.getOriginalFilename();
            //文件路径
            String filePath = System.getProperty("user.dir")+System.getProperty("file.separator")+"img";
            //如果文件路径不存在，新增该路径
            File file1 = new File(filePath);
            if(!file1.exists()){
                boolean mkdir = file1.mkdir();
            }
            //实际的文件地址
            File dest = new File(filePath + System.getProperty("file.separator") + fileName);
            //存储到数据库里的相对文件地址
            String storeImgPath = "/img/"+fileName;
            try {
                img.transferTo(dest);
                return Result.success(storeImgPath);
            } catch (IOException e) {
                return Result.fail("上传失败");
            }
        } else {
            return Result.fail("请选择正确的图片格式");
        }
    }

    /**
     * @description: 上传视频
     * @param: file
     * @return:
     * @date: 2023/10/13 10:44
     */
    @PostMapping("uploadVideo")
    public Result uploadVideo(@RequestParam("file") MultipartFile file) {
        if(file.isEmpty()){
            return Result.fail("上传的视频不能为空!");
        }
        //文件名=当前时间到毫秒+原来的文件名
        String fileName = System.currentTimeMillis() + file.getOriginalFilename();
        //文件路径
        String filePath = System.getProperty("user.dir")+System.getProperty("file.separator")+"video";
        //如果文件路径不存在，新增该路径
        File file1 = new File(filePath);
        if(!file1.exists()){
            boolean mkdir = file1.mkdir();
        }
        //实际的文件地址
        File dest = new File(filePath + System.getProperty("file.separator") + fileName);
        //存储到数据库里的相对文件地址
        String storeVideoPath = "/video/"+fileName;
        try {
            file.transferTo(dest);
            return Result.success(storeVideoPath);
        } catch (IOException e) {
            return Result.fail("上传失败");
        }
    }

    /**
    * @description: 上传文件
    * @param: file
    * @return:
    * @date: 2023/10/13 10:44
    */
    @PostMapping("uploadFile")
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        if(file.isEmpty()){
            return Result.fail("上传的文件不能为空!");
        }
        //文件名=当前时间到毫秒+原来的文件名
        String fileName = System.currentTimeMillis() + file.getOriginalFilename();
        //文件路径
        String filePath = System.getProperty("user.dir")+System.getProperty("file.separator")+"file";
        //如果文件路径不存在，新增该路径
        File file1 = new File(filePath);
        if(!file1.exists()){
            boolean mkdir = file1.mkdir();
        }
        //实际的文件地址
        File dest = new File(filePath + System.getProperty("file.separator") + fileName);
        //存储到数据库里的相对文件地址
        String storeFilePath = "/file/"+fileName;
        try {
            file.transferTo(dest);
            return Result.success(storeFilePath);
        } catch (IOException e) {
            return Result.fail("上传失败");
        }
    }

    /**
     * @description: 健康检查端点
     * @return: 健康状态
     * @date: 2024
     */
    @GetMapping("/health")
    public Result health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", System.currentTimeMillis());
        
        // 检查数据库连接
        try {
            if (dataSource != null) {
                Connection connection = dataSource.getConnection();
                boolean isValid = connection.isValid(2);
                connection.close();
                healthInfo.put("database", isValid ? "UP" : "DOWN");
            } else {
                healthInfo.put("database", "UNKNOWN");
            }
        } catch (Exception e) {
            healthInfo.put("database", "DOWN");
            healthInfo.put("databaseError", e.getMessage());
        }
        
        // 检查 Redis 连接
        try {
            if (redisTemplate != null) {
                redisTemplate.getConnectionFactory().getConnection().ping();
                healthInfo.put("redis", "UP");
            } else {
                healthInfo.put("redis", "UNKNOWN");
            }
        } catch (Exception e) {
            healthInfo.put("redis", "DOWN");
            healthInfo.put("redisError", e.getMessage());
        }
        
        return Result.success(healthInfo);
    }
    
    /**
     * @description: 根路径健康检查
     * @return: 健康状态
     * @date: 2024
     */
    @GetMapping("/")
    public Result root() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Travel System Backend");
        info.put("status", "running");
        info.put("version", "1.0.0");
        return Result.success(info);
    }

}
