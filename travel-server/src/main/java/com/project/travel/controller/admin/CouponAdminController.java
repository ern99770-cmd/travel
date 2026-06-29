package com.project.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.travel.domain.Result;
import com.project.travel.domain.SysCoupon;
import com.project.travel.domain.SysCouponUser;
import com.project.travel.enums.ResultCode;
import com.project.travel.service.SysCouponService;
import com.project.travel.service.SysCouponUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("admin/coupon")
public class CouponAdminController {

    @Autowired
    private SysCouponService couponService;

    @Autowired
    private SysCouponUserService couponUserService;

    @PostMapping("getSysCouponPage")
    public Result getSysCouponPage(@RequestBody SysCoupon sysCoupon) {
        Page<SysCoupon> page = new Page<>(sysCoupon.getPageNumber(), sysCoupon.getPageSize());
        QueryWrapper<SysCoupon> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .like(StringUtils.isNotBlank(sysCoupon.getName()), SysCoupon::getName, sysCoupon.getName())
                .eq(sysCoupon.getStatus() != null, SysCoupon::getStatus, sysCoupon.getStatus())
                .orderByAsc(SysCoupon::getSort)
                .orderByDesc(SysCoupon::getCreateTime);
        return Result.success(couponService.page(page, wrapper));
    }

    @GetMapping("getSysCouponById")
    public Result getSysCouponById(@RequestParam("id") String id) {
        return Result.success(couponService.getById(id));
    }

    @PostMapping("saveSysCoupon")
    public Result saveSysCoupon(@RequestBody SysCoupon sysCoupon) {
        if (sysCoupon.getTotalCount() == null || sysCoupon.getTotalCount() < 0) {
            sysCoupon.setTotalCount(0);
        }
        sysCoupon.setRemainCount(sysCoupon.getTotalCount());
        if (sysCoupon.getStatus() == null) {
            sysCoupon.setStatus(1);
        }
        if (sysCoupon.getSort() == null) {
            sysCoupon.setSort(0);
        }
        if (sysCoupon.getRequireLevel() == null) {
            sysCoupon.setRequireLevel(0);
        }
        boolean saved = couponService.save(sysCoupon);
        return saved ? Result.success() : Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
    }

    @PostMapping("editSysCoupon")
    public Result editSysCoupon(@RequestBody SysCoupon sysCoupon) {
        SysCoupon existing = couponService.getById(sysCoupon.getId());
        if (existing == null) {
            return Result.fail("优惠券不存在");
        }

        long claimedCount = couponUserService.count(new QueryWrapper<SysCouponUser>()
                .lambda().eq(SysCouponUser::getCouponId, sysCoupon.getId()));
        if (claimedCount > 0) {
            if (existing.getMinAmount().compareTo(sysCoupon.getMinAmount()) != 0
                    || existing.getDiscountAmount().compareTo(sysCoupon.getDiscountAmount()) != 0) {
                return Result.fail("已有用户领取，不可修改门槛或优惠金额");
            }
        }

        sysCoupon.setTotalCount(existing.getTotalCount());
        sysCoupon.setRemainCount(existing.getRemainCount());
        boolean updated = couponService.updateById(sysCoupon);
        return updated ? Result.success() : Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
    }

    @GetMapping("removeSysCoupon")
    public Result removeSysCoupon(@RequestParam("ids") String ids) {
        if (StringUtils.isBlank(ids)) {
            return Result.fail("请选择要删除的数据");
        }
        for (String id : ids.split(",")) {
            long claimedCount = couponUserService.count(new QueryWrapper<SysCouponUser>()
                    .lambda().eq(SysCouponUser::getCouponId, id));
            if (claimedCount > 0) {
                return Result.fail("存在已被领取的优惠券，无法删除");
            }
            couponService.removeById(id);
        }
        return Result.success();
    }

    @PostMapping("toggleStatus")
    public Result toggleStatus(@RequestBody SysCoupon sysCoupon) {
        SysCoupon existing = couponService.getById(sysCoupon.getId());
        if (existing == null) {
            return Result.fail("优惠券不存在");
        }
        existing.setStatus(sysCoupon.getStatus());
        couponService.updateById(existing);
        return Result.success();
    }

    @PostMapping("adjustStock")
    public Result adjustStock(@RequestBody Map<String, Object> params) {
        String id = (String) params.get("id");
        Integer addCount = params.get("addCount") instanceof Number
                ? ((Number) params.get("addCount")).intValue() : null;
        if (StringUtils.isBlank(id) || addCount == null || addCount <= 0) {
            return Result.fail("请输入有效的补库存数量");
        }
        SysCoupon coupon = couponService.getById(id);
        if (coupon == null) {
            return Result.fail("优惠券不存在");
        }
        coupon.setTotalCount(coupon.getTotalCount() + addCount);
        coupon.setRemainCount(coupon.getRemainCount() + addCount);
        couponService.updateById(coupon);

        Map<String, Object> data = new HashMap<>();
        data.put("totalCount", coupon.getTotalCount());
        data.put("remainCount", coupon.getRemainCount());
        return Result.success(data);
    }
}
