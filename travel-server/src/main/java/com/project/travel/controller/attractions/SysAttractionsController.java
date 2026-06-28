package com.project.travel.controller.attractions;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.travel.domain.Result;
import com.project.travel.domain.SysAttractionOrder;
import com.project.travel.domain.SysAttractions;
import com.project.travel.domain.SysComments;
import com.project.travel.enums.ResultCode;
import com.project.travel.service.SysAttractionOrderService;
import com.project.travel.service.SysAttractionsService;
import com.project.travel.service.SysCommentsService;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 景点controller
 * @date 2024/12/07 11:27
 */
@Slf4j
@RestController
@RequestMapping("attractions")
public class SysAttractionsController {

    @Autowired
    private SysAttractionsService sysAttractionsService;
    @Autowired
    private SysAttractionOrderService sysAttractionOrderService;
    @Autowired
    private SysCommentsService sysCommentsService;

    /** 分页获取景点 */
    @PostMapping("getSysAttractionsPage")
    public Result getSysAttractionsPage(@RequestBody SysAttractions sysAttractions) {
        log.info("查询景点参数：{}", sysAttractions);
        log.info("location 参数值：{}", sysAttractions.getLocation());
        
        Page<SysAttractions> page = new Page<>(sysAttractions.getPageNumber(),sysAttractions.getPageSize());
        QueryWrapper<SysAttractions> queryWrapper = new QueryWrapper<>();
        
        // 基础条件
        queryWrapper.lambda().eq(sysAttractions.getState() != null, SysAttractions::getState, sysAttractions.getState());
        
        // 名称模糊查询
        if (StringUtils.isNotBlank(sysAttractions.getName())) {
            queryWrapper.lambda().like(SysAttractions::getName, sysAttractions.getName());
        }
        
        // 类型精确查询
        if (StringUtils.isNotBlank(sysAttractions.getScenicType())) {
            queryWrapper.lambda().eq(SysAttractions::getScenicType, sysAttractions.getScenicType());
        }
        
        // 地区模糊查询 - 关键修复点
        if (StringUtils.isNotBlank(sysAttractions.getLocation())) {
            log.info("应用地区筛选：{}", sysAttractions.getLocation());
            queryWrapper.lambda().like(SysAttractions::getLocation, sysAttractions.getLocation());
        }
        
        // 排序
        queryWrapper.lambda().orderByDesc(SysAttractions::getCreateTime);
        
        log.info("最终查询条件 SQL：{}", queryWrapper.getSqlSegment());
        Page<SysAttractions> sysAttractionsPage = sysAttractionsService.page(page, queryWrapper);
        log.info("查询结果数量：{}", sysAttractionsPage.getRecords().size());
        return Result.success(sysAttractionsPage);
    }

    @GetMapping("getSysAttractionsList")
    public Result getSysAttractionsList() {
        List<SysAttractions> attractionsList = sysAttractionsService.list();
        return Result.success(attractionsList);
    }

    @GetMapping("getSysAttractionsIndex")
    public Result getSysAttractionsIndex() {
        QueryWrapper<SysAttractions> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAttractions::getState,1).last("limit 3");
        List<SysAttractions> attractionsList = sysAttractionsService.list(queryWrapper);
        return Result.success(attractionsList);
    }

    /** 获取热门景点(根据访问量排序) */
    @GetMapping("getSysAttractionsHot")
    public Result getSysAttractionsHot() {
        QueryWrapper<SysAttractions> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(SysAttractions::getState, 1)
                .orderByDesc(SysAttractions::getPv)
                .last("limit 3");
        List<SysAttractions> attractionsList = sysAttractionsService.list(queryWrapper);
        return Result.success(attractionsList);
    }

    /** 根据用户喜欢的风景类型获取推荐景点 */
    @GetMapping("getSysAttractionsRecommend")
    public Result getSysAttractionsRecommend(@RequestParam(value = "scenicType", required = false) String scenicType) {
        QueryWrapper<SysAttractions> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(SysAttractions::getState, 1);
        
        if (StringUtils.isNotBlank(scenicType)) {
            // 分割用户喜好的多个类型
            String[] types = scenicType.split(",");
            if (types.length > 0) {
                queryWrapper.lambda().and(wrapper -> {
                    for (int i = 0; i < types.length; i++) {
                        String type = types[i];
                        if (StringUtils.isNotBlank(type)) {
                            if (i == 0) {
                                wrapper.eq(SysAttractions::getScenicType, type.trim());
                            } else {
                                wrapper.or().eq(SysAttractions::getScenicType, type.trim());
                            }
                        }
                    }
                });
            }
        }
        
        // 先按照类型筛选，再按照访问量降序排序
        queryWrapper.lambda().orderByDesc(SysAttractions::getPv).last("limit 3");
        List<SysAttractions> attractionsList = sysAttractionsService.list(queryWrapper);
        return Result.success(attractionsList);
    }

    /** 根据id获取景点 */
    @GetMapping("getSysAttractionsById")
    public Result getSysAttractionsById(@RequestParam("id")String id) {
        SysAttractions sysAttractions = sysAttractionsService.getById(id);
        // 增加访问量计数
        if (sysAttractions != null) {
            // 如果pv为null则设置为1，否则加1
            Integer pv = sysAttractions.getPv();
            if (pv == null) {
                pv = 1;
            } else {
                pv = pv + 1;
            }
            sysAttractions.setPv(pv);
            sysAttractionsService.updateById(sysAttractions);
        }
        return Result.success(sysAttractions);
    }

    /** 保存景点 */
    @PostMapping("saveSysAttractions")
    public Result saveSysAttractions(@RequestBody SysAttractions sysAttractions) {
        boolean save = sysAttractionsService.save(sysAttractions);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑景点 */
    @PostMapping("editSysAttractions")
    public Result editSysAttractions(@RequestBody SysAttractions sysAttractions) {
        boolean save = sysAttractionsService.updateById(sysAttractions);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除景点 */
    @GetMapping("removeSysAttractions")
    @Transactional(rollbackFor = Exception.class)
    public Result removeSysAttractions(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysAttractionsService.removeById(id);
                QueryWrapper<SysAttractionOrder> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(SysAttractionOrder::getAttractionsId,id);
                sysAttractionOrderService.remove(queryWrapper);
                QueryWrapper<SysComments> wrapper = new QueryWrapper<>();
                wrapper.lambda().eq(SysComments::getAttractionsId,id);
                sysCommentsService.remove(wrapper);
            }
            return Result.success();
        } else {
            return Result.fail("景点id不能为空！");
        }
    }

    // TODO 根据名称获取景点
    @GetMapping("getSysAttractionsByName")
    public Result getSysAttractionsByName(@RequestParam("name")String name) {
        QueryWrapper<SysAttractions> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().like(SysAttractions::getName,name);
        List<SysAttractions> attractionsList = sysAttractionsService.list(queryWrapper);
        log.info("景点信息:{}",attractionsList);
        return Result.success(attractionsList);
    }

}
