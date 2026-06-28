package com.project.travel.controller.share;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.travel.domain.Result;
import com.project.travel.domain.SysTravelShare;
import com.project.travel.domain.SysTravelShareLike;
import com.project.travel.domain.User;
import com.project.travel.domain.SysComments;
import com.project.travel.enums.ResultCode;
import com.project.travel.service.SysCommentsService;
import com.project.travel.service.SysTravelShareLikeService;
import com.project.travel.service.SysTravelShareService;
import com.project.travel.service.UserService;
import com.project.travel.utils.TokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @description: 旅游分享Controller
 */
@Controller
@ResponseBody
@RequestMapping("share")
public class SysTravelShareController {

    @Autowired
    private SysTravelShareService sysTravelShareService;

    @Autowired
    private SysTravelShareLikeService sysTravelShareLikeService;

    @Autowired
    private UserService userService;

    @Autowired
    private SysCommentsService sysCommentsService;

    /**
     * 分页查询旅游分享
     */
    @PostMapping("getSysTravelSharePage")
    public Result getSysTravelSharePage(@RequestBody SysTravelShare sysTravelShare) {
        Page<SysTravelShare> page = new Page<>(sysTravelShare.getPageNumber(), sysTravelShare.getPageSize());
        QueryWrapper<SysTravelShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(sysTravelShare.getTitle()), SysTravelShare::getTitle, sysTravelShare.getTitle())
                .like(StringUtils.isNotBlank(sysTravelShare.getLocation()), SysTravelShare::getLocation, sysTravelShare.getLocation())
                .eq(sysTravelShare.getType() != null, SysTravelShare::getType, sysTravelShare.getType())
                .orderByDesc(SysTravelShare::getCreateTime);
        Page<SysTravelShare> sharePage = sysTravelShareService.page(page, queryWrapper);

        String currentUserId = null;
        try {
            currentUserId = TokenUtils.getUserIdByToken();
        } catch (Exception e) {
            // 用户未登录
        }

        List<SysTravelShare> records = sharePage.getRecords();
        for (SysTravelShare share : records) {
            // 1. 处理用户信息
            if (StringUtils.isNotBlank(share.getUserId())) {
                User user = userService.getById(share.getUserId());
                if (user != null) {
                    share.setUserName(user.getUserName());
                    if (StringUtils.isBlank(share.getAvatar())) {
                        share.setAvatar(user.getAvatar());
                    }
                }
            }

            // 2. 确保 likes 不为 null
            if (share.getLikes() == null) {
                share.setLikes(0);
            }

            // 3. 判断当前用户是否点赞
            if (StringUtils.isNotBlank(currentUserId)) {
                QueryWrapper<SysTravelShareLike> likeWrapper = new QueryWrapper<>();
                likeWrapper.lambda()
                        .eq(SysTravelShareLike::getShareId, share.getId())
                        .eq(SysTravelShareLike::getUserId, currentUserId);
                SysTravelShareLike like = sysTravelShareLikeService.getOne(likeWrapper);
                share.setIsLiked(like != null);
            } else {
                share.setIsLiked(false);
            }

            // 4. 统计评论数
            QueryWrapper<SysComments> commentWrapper = new QueryWrapper<>();
            commentWrapper.lambda()
                    .eq(SysComments::getTargetId, share.getId())
                    .eq(SysComments::getTargetType, 2);
            long count = sysCommentsService.count(commentWrapper);
            share.setCommentCount((int) count);
        }

        return Result.success(sharePage);
    }

    /**
     * 根据ID查询旅游分享详情
     */
    @GetMapping("getSysTravelShareById")
    public Result getSysTravelShareById(@RequestParam("id") String id) {
        SysTravelShare share = sysTravelShareService.getById(id);

        if (share == null) {
            return Result.fail("分享不存在");
        }

        String currentUserId = null;
        try {
            currentUserId = TokenUtils.getUserIdByToken();
        } catch (Exception e) {
            // 用户未登录
        }

        // 1. 确保 likes 不为 null
        if (share.getLikes() == null) {
            share.setLikes(0);
        }

        // 2. 处理用户信息
        if (StringUtils.isNotBlank(share.getUserId())) {
            User user = userService.getById(share.getUserId());
            if (user != null) {
                share.setUserName(user.getUserName());
                if (StringUtils.isBlank(share.getAvatar())) {
                    share.setAvatar(user.getAvatar());
                }
            }
        }

        // 3. 判断当前用户是否点赞
        if (StringUtils.isNotBlank(currentUserId)) {
            QueryWrapper<SysTravelShareLike> likeWrapper = new QueryWrapper<>();
            likeWrapper.lambda()
                    .eq(SysTravelShareLike::getShareId, share.getId())
                    .eq(SysTravelShareLike::getUserId, currentUserId);
            SysTravelShareLike like = sysTravelShareLikeService.getOne(likeWrapper);
            share.setIsLiked(like != null);
        } else {
            share.setIsLiked(false);
        }

        return Result.success(share);
    }

    /**
     * 新增旅游分享
     */
    @PostMapping("saveSysTravelShare")
    public Result saveSysTravelShare(@RequestBody SysTravelShare sysTravelShare) {
        try {
            String userId = TokenUtils.getUserIdByToken();
            User user = userService.getById(userId);

            if (user == null) {
                return Result.fail("用户不存在，请重新登录");
            }

            sysTravelShare.setUserId(userId);
            sysTravelShare.setAvatar(user.getAvatar());
            sysTravelShare.setLikes(0);
            sysTravelShare.setCreateTime(new Date());
            sysTravelShare.setUpdateTime(new Date());

            boolean save = sysTravelShareService.save(sysTravelShare);
            if (save) {
                return Result.success();
            } else {
                return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("请先登录后再发布分享");
        }
    }

    /**
     * 编辑旅游分享
     */
    @PostMapping("editSysTravelShare")
    public Result editSysTravelShare(@RequestBody SysTravelShare sysTravelShare) {
        try {
            String userId = TokenUtils.getUserIdByToken();
            SysTravelShare oldShare = sysTravelShareService.getById(sysTravelShare.getId());

            if (oldShare == null) {
                return Result.fail("分享不存在");
            }

            if (!oldShare.getUserId().equals(userId)) {
                return Result.fail("只能编辑自己的分享");
            }

            sysTravelShare.setUpdateTime(new Date());
            boolean save = sysTravelShareService.updateById(sysTravelShare);
            if (save) {
                return Result.success();
            } else {
                return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("请先登录后再编辑分享");
        }
    }

    /**
     * 删除旅游分享
     */
    @GetMapping("removeSysTravelShare")
    @Transactional(rollbackFor = Exception.class)
    public Result removeSysTravelShare(@RequestParam("ids") String ids) {
        try {
            String userId = TokenUtils.getUserIdByToken();

            if (StringUtils.isBlank(ids)) {
                return Result.fail("分享id不能为空！");
            }

            String[] asList = ids.split(",");
            for (String id : asList) {
                SysTravelShare share = sysTravelShareService.getById(id);
                if (share == null) {
                    continue;
                }
                if (!share.getUserId().equals(userId)) {
                    return Result.fail("只能删除自己的分享");
                }
                // 删除分享
                sysTravelShareService.removeById(id);

                // 删除该分享的所有点赞记录
                QueryWrapper<SysTravelShareLike> likeWrapper = new QueryWrapper<>();
                likeWrapper.lambda().eq(SysTravelShareLike::getShareId, id);
                sysTravelShareLikeService.remove(likeWrapper);

                // 删除该分享的所有评论
                QueryWrapper<SysComments> commentWrapper = new QueryWrapper<>();
                commentWrapper.lambda()
                        .eq(SysComments::getTargetId, id)
                        .eq(SysComments::getTargetType, 2);
                sysCommentsService.remove(commentWrapper);
            }
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail("删除失败，请稍后重试");
        }
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("likeShare")
    @Transactional(rollbackFor = Exception.class)
    public Result likeShare(@RequestParam("shareId") String shareId) {
        try {
            String userId = TokenUtils.getUserIdByToken();

            // 1. 查询分享是否存在
            SysTravelShare share = sysTravelShareService.getById(shareId);
            if (share == null) {
                return Result.fail("分享不存在");
            }

            // 2. 确保 likes 不为 null
            if (share.getLikes() == null) {
                share.setLikes(0);
            }

            // 3. 查询是否已点赞
            QueryWrapper<SysTravelShareLike> likeWrapper = new QueryWrapper<>();
            likeWrapper.lambda()
                    .eq(SysTravelShareLike::getShareId, shareId)
                    .eq(SysTravelShareLike::getUserId, userId);
            SysTravelShareLike existingLike = sysTravelShareLikeService.getOne(likeWrapper);

            if (existingLike != null) {
                // 取消点赞
                boolean removed = sysTravelShareLikeService.removeById(existingLike.getId());
                if (!removed) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return Result.fail("取消点赞失败");
                }

                // 更新点赞数（确保不小于0）
                int newLikes = Math.max(0, share.getLikes() - 1);
                share.setLikes(newLikes);
                boolean updated = sysTravelShareService.updateById(share);
                if (!updated) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return Result.fail("更新点赞数失败");
                }

                return Result.success("取消点赞成功");
            } else {
                // 点赞
                SysTravelShareLike like = new SysTravelShareLike();
                like.setShareId(shareId);
                like.setUserId(userId);
                like.setCreateTime(new Date());
                boolean saved = sysTravelShareLikeService.save(like);
                if (!saved) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return Result.fail("点赞失败");
                }

                // 更新点赞数
                share.setLikes(share.getLikes() + 1);
                boolean updated = sysTravelShareService.updateById(share);
                if (!updated) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return Result.fail("更新点赞数失败");
                }

                return Result.success("点赞成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail("操作失败，请稍后重试");
        }
    }

    /**
     * 获取我的分享列表
     */
    @PostMapping("getMySharePage")
    public Result getMySharePage(@RequestBody SysTravelShare sysTravelShare) {
        try {
            String userId = TokenUtils.getUserIdByToken();

            Page<SysTravelShare> page = new Page<>(sysTravelShare.getPageNumber(), sysTravelShare.getPageSize());
            QueryWrapper<SysTravelShare> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(SysTravelShare::getUserId, userId)
                    .orderByDesc(SysTravelShare::getCreateTime);
            Page<SysTravelShare> sharePage = sysTravelShareService.page(page, queryWrapper);

            User user = userService.getById(userId);
            List<SysTravelShare> records = sharePage.getRecords();
            for (SysTravelShare share : records) {
                share.setUserName(user != null ? user.getUserName() : "未知用户");
                share.setIsLiked(false);

                if (StringUtils.isBlank(share.getAvatar()) && user != null) {
                    share.setAvatar(user.getAvatar());
                }

                if (share.getLikes() == null) {
                    share.setLikes(0);
                }

                // 统计评论数
                QueryWrapper<SysComments> commentWrapper = new QueryWrapper<>();
                commentWrapper.lambda()
                        .eq(SysComments::getTargetId, share.getId())
                        .eq(SysComments::getTargetType, 2);
                long count = sysCommentsService.count(commentWrapper);
                share.setCommentCount((int) count);
            }

            return Result.success(sharePage);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("请先登录");
        }
    }
}