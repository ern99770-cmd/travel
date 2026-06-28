package com.project.travel.controller.comments;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.travel.domain.Result;
import com.project.travel.domain.SysComments;
import com.project.travel.domain.User;
import com.project.travel.enums.ResultCode;
import com.project.travel.service.SysCommentsService;
import com.project.travel.service.UserService;
import com.project.travel.utils.TokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 评论controller(支持景点、酒店、帖子、旅游分享评论)
 * @date 2024/12/07 10:23
 */
@Controller
@ResponseBody
@RequestMapping("comments")
public class SysCommentsController {

    @Autowired
    private SysCommentsService sysCommentsService;

    @Autowired
    private UserService userService;

    /** 分页获取评论 */
    @PostMapping("getSysCommentsPage")
    public Result getSysCommentsPage(@RequestBody SysComments sysComments) {
        Page<SysComments> page = new Page<>(sysComments.getPageNumber(), sysComments.getPageSize());
        QueryWrapper<SysComments> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(sysComments.getTargetId()), SysComments::getTargetId, sysComments.getTargetId())
                .eq(sysComments.getTargetType() != null, SysComments::getTargetType, sysComments.getTargetType())
                .orderByDesc(SysComments::getCreateTime);
        Page<SysComments> sysCommentsPage = sysCommentsService.page(page, queryWrapper);
        
        // 添加用户信息
        List<SysComments> records = sysCommentsPage.getRecords();
        for (SysComments comment : records) {
            if (StringUtils.isNotBlank(comment.getUserId())) {
                User user = userService.getById(comment.getUserId());
                if (user != null) {
                    comment.setUserName(user.getUserName());
                }
            }
        }
        
        return Result.success(sysCommentsPage);
    }

    /** 根据对象id获取评论列表(景点、酒店、帖子、旅游分享) */
    @GetMapping("getCommentsByTargetId")
    public Result getCommentsByTargetId(@RequestParam("targetId") String targetId,
                                        @RequestParam("targetType") Integer targetType,
                                        @RequestParam(value = "pageNumber", defaultValue = "1") Integer pageNumber,
                                        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<SysComments> page = new Page<>(pageNumber, pageSize);
        QueryWrapper<SysComments> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(SysComments::getTargetId, targetId)
                .eq(SysComments::getTargetType, targetType)
                .orderByDesc(SysComments::getCreateTime);
        Page<SysComments> sysCommentsPage = sysCommentsService.page(page, queryWrapper);
        
        // 添加用户信息
        List<SysComments> records = sysCommentsPage.getRecords();
        for (SysComments comment : records) {
            if (StringUtils.isNotBlank(comment.getUserId())) {
                User user = userService.getById(comment.getUserId());
                if (user != null) {
                    comment.setUserName(user.getUserName());
                }
            }
        }
        
        return Result.success(sysCommentsPage);
    }

    /** 根据id获取评论 */
    @GetMapping("getSysCommentsById")
    public Result getSysCommentsById(@RequestParam("id") String id) {
        SysComments sysComments = sysCommentsService.getById(id);
        
        // 添加用户信息
        if (sysComments != null && StringUtils.isNotBlank(sysComments.getUserId())) {
            User user = userService.getById(sysComments.getUserId());
            if (user != null) {
                sysComments.setUserName(user.getUserName());
            }
        }
        
        return Result.success(sysComments);
    }

    /** 保存评论 */
    @PostMapping("saveSysComments")
    public Result saveSysComments(@RequestBody SysComments sysComments) {
        try {
            String userId = TokenUtils.getUserIdByToken();
            User user = userService.getById(userId);
            
            sysComments.setUserId(userId);
            sysComments.setAvatar(user.getAvatar());
            
            boolean save = sysCommentsService.save(sysComments);
            if (save) {
                return Result.success();
            } else {
                return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
            }
        } catch (Exception e) {
            return Result.fail("请先登录后再评论");
        }
    }

    /** 编辑评论 */
    @PostMapping("editSysComments")
    public Result editSysComments(@RequestBody SysComments sysComments) {
        try {
            String userId = TokenUtils.getUserIdByToken();
            SysComments oldComment = sysCommentsService.getById(sysComments.getId());
            
            // 只允许本人编辑
            if (!oldComment.getUserId().equals(userId)) {
                return Result.fail("只能编辑自己的评论");
            }
            
            boolean save = sysCommentsService.updateById(sysComments);
            if (save) {
                return Result.success();
            } else {
                return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
            }
        } catch (Exception e) {
            return Result.fail("请先登录后再编辑评论");
        }
    }

    /** 删除评论 */
    @GetMapping("removeSysComments")
    public Result removeSysComments(@RequestParam("ids") String ids) {
        try {
            String userId = TokenUtils.getUserIdByToken();
            
            if (StringUtils.isNotBlank(ids)) {
                String[] asList = ids.split(",");
                for (String id : asList) {
                    SysComments comment = sysCommentsService.getById(id);
                    // 只允许本人删除
                    if (!comment.getUserId().equals(userId)) {
                        return Result.fail("只能删除自己的评论");
                    }
                    sysCommentsService.removeById(id);
                }
                return Result.success();
            } else {
                return Result.fail("评论id不能为空！");
            }
        } catch (Exception e) {
            return Result.fail("请先登录后再删除评论");
        }
    }

}