package com.project.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.travel.domain.Result;
import com.project.travel.domain.SysExchangeRecord;
import com.project.travel.domain.SysMember;
import com.project.travel.domain.SysProduct;
import com.project.travel.domain.User;
import com.project.travel.service.SysExchangeRecordService;
import com.project.travel.service.SysMemberService;
import com.project.travel.service.SysProductService;
import com.project.travel.service.UserService;
import com.project.travel.utils.TokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("admin/exchange")
public class ExchangeAdminController {

    @Autowired
    private SysExchangeRecordService exchangeRecordService;

    @Autowired
    private SysMemberService memberService;

    @Autowired
    private SysProductService productService;

    @Autowired
    private UserService userService;

    @PostMapping("getSysExchangePage")
    public Result getSysExchangePage(@RequestBody SysExchangeRecord record) {
        Page<SysExchangeRecord> page = new Page<>(record.getPageNumber(), record.getPageSize());
        QueryWrapper<SysExchangeRecord> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(record.getStatus() != null, SysExchangeRecord::getStatus, record.getStatus())
                .eq(record.getType() != null, SysExchangeRecord::getType, record.getType())
                .like(StringUtils.isNotBlank(record.getRelatedName()), SysExchangeRecord::getRelatedName, record.getRelatedName())
                .orderByDesc(SysExchangeRecord::getCreateTime);
        Page<SysExchangeRecord> result = exchangeRecordService.page(page, wrapper);
        for (SysExchangeRecord item : result.getRecords()) {
            if (StringUtils.isNotBlank(item.getUserId())) {
                User user = userService.getById(item.getUserId());
                if (user != null) {
                    item.setUserName(user.getUserName());
                }
            }
        }
        return Result.success(result);
    }

    @GetMapping("getSysExchangeById")
    public Result getSysExchangeById(@RequestParam("id") String id) {
        SysExchangeRecord record = exchangeRecordService.getById(id);
        if (record != null && StringUtils.isNotBlank(record.getUserId())) {
            User user = userService.getById(record.getUserId());
            if (user != null) {
                record.setUserName(user.getUserName());
            }
        }
        return Result.success(record);
    }

    @PostMapping("complete")
    @Transactional
    public Result complete(@RequestBody SysExchangeRecord record) {
        SysExchangeRecord existing = exchangeRecordService.getById(record.getId());
        if (existing == null) {
            return Result.fail("记录不存在");
        }
        if (existing.getStatus() != 0) {
            return Result.fail("仅待处理记录可确认完成");
        }
        existing.setStatus(1);
        if (StringUtils.isNotBlank(record.getRemark())) {
            existing.setRemark(record.getRemark());
        }
        exchangeRecordService.updateById(existing);
        return Result.success();
    }

    @PostMapping("cancel")
    @Transactional
    public Result cancel(@RequestBody SysExchangeRecord record) {
        SysExchangeRecord existing = exchangeRecordService.getById(record.getId());
        if (existing == null) {
            return Result.fail("记录不存在");
        }
        if (existing.getStatus() != 0) {
            return Result.fail("仅待处理记录可取消");
        }

        if (existing.getPointsUsed() != null && existing.getPointsUsed() > 0) {
            String operatorId = TokenUtils.getUserIdByToken();
            memberService.adjustPoints(
                    existing.getUserId(),
                    existing.getPointsUsed(),
                    "取消兑换退还积分：" + existing.getRelatedName(),
                    operatorId);
        }

        if (existing.getType() != null && (existing.getType() == 0 || existing.getType() == 1)
                && StringUtils.isNotBlank(existing.getRelatedId())) {
            SysProduct product = productService.getById(existing.getRelatedId());
            if (product != null) {
                product.setStock(product.getStock() + 1);
                productService.updateById(product);
            }
        }

        existing.setStatus(2);
        if (StringUtils.isNotBlank(record.getRemark())) {
            existing.setRemark(record.getRemark());
        }
        exchangeRecordService.updateById(existing);
        return Result.success();
    }

    @GetMapping("stats")
    public Result stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("pending", exchangeRecordService.count(new QueryWrapper<SysExchangeRecord>().lambda().eq(SysExchangeRecord::getStatus, 0)));
        data.put("completed", exchangeRecordService.count(new QueryWrapper<SysExchangeRecord>().lambda().eq(SysExchangeRecord::getStatus, 1)));
        data.put("cancelled", exchangeRecordService.count(new QueryWrapper<SysExchangeRecord>().lambda().eq(SysExchangeRecord::getStatus, 2)));
        return Result.success(data);
    }
}
