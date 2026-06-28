package com.project.travel.controller.hotel;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.travel.domain.Result;
import com.project.travel.domain.SysHotel;
import com.project.travel.domain.SysHotelItem;
import com.project.travel.domain.SysHotelOrder;
import com.project.travel.enums.ResultCode;
import com.project.travel.service.SysHotelItemService;
import com.project.travel.service.SysHotelOrderService;
import com.project.travel.service.SysHotelService;
import com.project.travel.utils.TokenUtils;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 酒店预约controller
 * @date 2024/12/07 11:01
 */
@Controller
@ResponseBody
@RequestMapping("order")
public class SysHotelOrderController {

    @Autowired
    private SysHotelOrderService sysHotelOrderService;
    @Autowired
    private SysHotelService sysHotelService;
    @Autowired
    private SysHotelItemService sysHotelItemService;
    @Autowired
    private com.project.travel.service.SysMemberService sysMemberService;

    /** 分页获取酒店预约 */
    @PostMapping("getSysHotelOrderPage")
    public Result getSysHotelOrderPage(@RequestBody SysHotelOrder sysHotelOrder) {
        System.out.println("【酒店订单查询开始，参数：" + sysHotelOrder);
        System.out.println("查询用户ID：" + sysHotelOrder.getUserId());
        Page<SysHotelOrder> page = new Page<>(sysHotelOrder.getPageNumber(),sysHotelOrder.getPageSize());
        QueryWrapper<SysHotelOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(sysHotelOrder.getHotelId()),SysHotelOrder::getHotelId,sysHotelOrder.getHotelId())
                .like(StringUtils.isNotBlank(sysHotelOrder.getName()),SysHotelOrder::getName,sysHotelOrder.getName())
                .eq(sysHotelOrder.getState() != null,SysHotelOrder::getState,sysHotelOrder.getState())
                .eq(StringUtils.isNotBlank(sysHotelOrder.getUserId()),SysHotelOrder::getUserId,sysHotelOrder.getUserId())
                .eq(StringUtils.isNotBlank(sysHotelOrder.getItemId()),SysHotelOrder::getItemId,sysHotelOrder.getItemId())
                .like(StringUtils.isNotBlank(sysHotelOrder.getCreateBy()),SysHotelOrder::getCreateBy,sysHotelOrder.getCreateBy())
                .eq(sysHotelOrder.getCreateTime() != null,SysHotelOrder::getCreateTime,sysHotelOrder.getCreateTime())
                .orderByDesc(SysHotelOrder::getCreateTime);
        System.out.println("执行查询...");
        Page<SysHotelOrder> sysHotelOrderPage = sysHotelOrderService.page(page, queryWrapper);
        System.out.println("查询结果条数：" + (sysHotelOrderPage.getRecords() != null ? sysHotelOrderPage.getRecords().size() : 0));
        
        // 补充酒店信息，确保即使是旧订单也能正常显示
        if (sysHotelOrderPage.getRecords() != null && !sysHotelOrderPage.getRecords().isEmpty()) {
            for (SysHotelOrder order : sysHotelOrderPage.getRecords()) {
                System.out.println("处理订单：" + order);
                if (order.getHotelId() != null && (order.getName() == null || order.getImages() == null)) {
                    System.out.println("需要补充酒店信息，hotelId：" + order.getHotelId());
                    SysHotel hotel = sysHotelService.getById(order.getHotelId());
                    if (hotel != null) {
                        System.out.println("查到酒店信息：" + hotel);
                        order.setName(hotel.getName());
                        order.setIntroduce(hotel.getIntroduce());
                        order.setImages(hotel.getImages());
                    } else {
                        System.out.println("未找到对应的酒店信息");
                    }
                }
            }
        } else {
            System.out.println("未查询到酒店订单数据！");
        }
        
        return Result.success(sysHotelOrderPage);
    }

    /** 根据id获取酒店预约 */
    @GetMapping("getSysHotelOrderById")
    public Result getSysHotelOrderById(@RequestParam("id")String id) {
        SysHotelOrder sysHotelOrder = sysHotelOrderService.getById(id);
        return Result.success(sysHotelOrder);
    }

    /** 保存酒店预约 */
    @PostMapping("saveSysHotelOrder")
    @Transactional(rollbackFor = Exception.class)
    public Result saveSysHotelOrder(@RequestBody SysHotelOrder sysHotelOrder) {
        String userId = TokenUtils.getUserIdByToken();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        SysHotel hotel = sysHotelService.getById(sysHotelOrder.getHotelId());
        if (hotel == null) {
            return Result.fail("酒店不存在");
        }
        sysHotelOrder.setName(hotel.getName());
        sysHotelOrder.setIntroduce(hotel.getIntroduce());
        sysHotelOrder.setImages(hotel.getImages());
        SysHotelItem item = sysHotelItemService.getById(sysHotelOrder.getItemId());
        if (item == null) {
            return Result.fail("房型不存在");
        }
        if (item.getNum() - 1 < 0) {
            return Result.fail("库存不足");
        }
        item.setNum(item.getNum() - 1);
        sysHotelItemService.updateById(item);
        sysHotelOrder.setItemName(item.getName());
        sysHotelOrder.setPrice(item.getPrice());
        sysHotelOrder.setUserId(userId);
        boolean save = sysHotelOrderService.save(sysHotelOrder);
        if (save) {
            // 计算订单金额并添加积分
            float price = item.getPrice() != null ? item.getPrice() : 0;
            int orderAmount = Math.round(price * (sysHotelOrder.getNum() != null ? sysHotelOrder.getNum() : 1));
            if (orderAmount > 0) {
                sysMemberService.addPointsFromOrder(userId, orderAmount);
            }
            return Result.success(sysHotelOrder);
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑酒店预约 */
    @PostMapping("editSysHotelOrder")
    public Result editSysHotelOrder(@RequestBody SysHotelOrder sysHotelOrder) {
        boolean save = sysHotelOrderService.updateById(sysHotelOrder);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除酒店预约 */
    @GetMapping("removeSysHotelOrder")
    public Result removeSysHotelOrder(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysHotelOrderService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("酒店预约id不能为空！");
        }
    }

}
