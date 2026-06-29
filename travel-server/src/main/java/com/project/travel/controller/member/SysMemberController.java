package com.project.travel.controller.member;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.travel.domain.Result;
import com.project.travel.domain.SysCoupon;
import com.project.travel.domain.SysCouponUser;
import com.project.travel.domain.SysExchangeRecord;
import com.project.travel.domain.SysMember;
import com.project.travel.domain.SysPointsLog;
import com.project.travel.domain.SysProduct;
import com.project.travel.domain.MemberRequest;
import com.project.travel.domain.User;
import com.project.travel.service.SysCouponService;
import com.project.travel.service.SysCouponUserService;
import com.project.travel.service.SysExchangeRecordService;
import com.project.travel.service.SysMemberService;
import com.project.travel.service.SysPointsLogService;
import com.project.travel.service.SysProductService;
import com.project.travel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/member")
public class SysMemberController {

    @Autowired
    private SysMemberService memberService;

    @Autowired
    private SysPointsLogService pointsLogService;

    @Autowired
    private SysCouponService couponService;

    @Autowired
    private SysCouponUserService couponUserService;

    @Autowired
    private SysProductService productService;

    @Autowired
    private SysExchangeRecordService exchangeRecordService;

    @Autowired
    private UserService userService;

    // 获取会员信息
    @GetMapping("/info")
    public Result getMemberInfo(@RequestParam String userId) {
        SysMember member = memberService.getOrCreateMember(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("member", member);
        
        // 检查今天是否已签到
        boolean signedToday = false;
        if (member.getLastSignInTime() != null) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(member.getLastSignInTime());
            
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(new Date());
            
            signedToday = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) 
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) 
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
        }
        data.put("signedToday", signedToday);
        
        return Result.success(data);
    }

    // 签到
    @PostMapping("/signin")
    public Result signIn(@RequestBody MemberRequest request) {
        int points = memberService.signIn(request.getUserId());
        if (points > 0) {
            Map<String, Object> data = new HashMap<>();
            data.put("pointsEarned", points);
            return Result.success(data);
        } else {
            return Result.fail("今天已签到");
        }
    }

    // 获取积分记录
    @GetMapping("/points/log")
    public Result getPointsLog(@RequestParam String userId,
                                @RequestParam(defaultValue = "1") Integer pageNumber,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysPointsLog> page = new Page<>(pageNumber, pageSize);
        QueryWrapper<SysPointsLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        Page<SysPointsLog> result = pointsLogService.page(page, wrapper);
        return Result.success(result);
    }

    // 获取优惠券列表
    @GetMapping("/coupons")
    public Result getCoupons(@RequestParam(required = false) Integer userLevel) {
        QueryWrapper<SysCoupon> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByAsc("sort");
        if (userLevel != null) {
            wrapper.le("require_level", userLevel);
        }
        List<SysCoupon> list = couponService.list(wrapper);
        return Result.success(list);
    }

    // 兑换优惠券
    @PostMapping("/coupon/exchange")
    @Transactional
    public Result exchangeCoupon(@RequestBody MemberRequest request) {
        SysMember member = memberService.getOrCreateMember(request.getUserId());
        SysCoupon coupon = couponService.getById(request.getCouponId());
        
        if (coupon == null || coupon.getStatus() != 1) {
            return Result.fail("优惠券不存在或已下架");
        }
        if (coupon.getRemainCount() <= 0) {
            return Result.fail("优惠券已领完");
        }
        if (coupon.getRequireLevel() > member.getLevel()) {
            return Result.fail("您的会员等级不足");
        }
        if (member.getPoints() < coupon.getPointsRequired()) {
            return Result.fail("积分不足");
        }
        
        // 检查是否在有效期内
        Date now = new Date();
        if (coupon.getStartTime() != null && now.before(coupon.getStartTime())) {
            return Result.fail("活动未开始");
        }
        if (coupon.getEndTime() != null && now.after(coupon.getEndTime())) {
            return Result.fail("活动已结束");
        }
        
        // 扣积分
        int beforePoints = member.getPoints();
        int afterPoints = beforePoints - coupon.getPointsRequired();
        member.setPoints(afterPoints);
        memberService.updateById(member);
        
        // 减库存
        coupon.setRemainCount(coupon.getRemainCount() - 1);
        couponService.updateById(coupon);
        
        // 发放优惠券
        Date expireTime = null;
        if (coupon.getValidDays() != null && coupon.getValidDays() > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            cal.add(Calendar.DAY_OF_MONTH, coupon.getValidDays());
            expireTime = cal.getTime();
        }
        
        SysCouponUser couponUser = new SysCouponUser();
        couponUser.setUserId(request.getUserId());
        couponUser.setCouponId(request.getCouponId());
        couponUser.setCouponName(coupon.getName());
        couponUser.setMinAmount(coupon.getMinAmount());
        couponUser.setDiscountAmount(coupon.getDiscountAmount());
        couponUser.setStatus(0);
        couponUser.setReceiveTime(now);
        couponUser.setExpireTime(expireTime);
        couponUserService.save(couponUser);

        // 创建兑换记录（优惠券即时完成，无需发货）
        SysExchangeRecord record = new SysExchangeRecord();
        record.setUserId(request.getUserId());
        record.setType(5);
        record.setRelatedId(couponUser.getId());
        record.setRelatedName(coupon.getName());
        record.setPointsUsed(coupon.getPointsRequired());
        record.setAmount(coupon.getDiscountAmount());
        record.setStatus(1);
        exchangeRecordService.save(record);
        
        // 记录积分日志
        SysPointsLog log = new SysPointsLog();
        log.setUserId(request.getUserId());
        log.setChangePoints(-coupon.getPointsRequired());
        log.setBeforePoints(beforePoints);
        log.setAfterPoints(afterPoints);
        log.setType(3);
        log.setDescription("兑换优惠券：" + coupon.getName());
        log.setRelatedId(record.getId());
        pointsLogService.save(log);
        
        return Result.success("兑换成功");
    }

    // 获取我的优惠券
    @GetMapping("/coupons/my")
    public Result getMyCoupons(@RequestParam String userId,
                               @RequestParam(required = false) Integer status) {
        QueryWrapper<SysCouponUser> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        List<SysCouponUser> list = couponUserService.list(wrapper);
        return Result.success(list);
    }

    // 使用优惠券
    @PostMapping("/coupon/use")
    @Transactional
    public Result useCoupon(@RequestBody MemberRequest request) {
        SysCouponUser couponUser = couponUserService.getById(request.getCouponUserId());
        
        if (couponUser == null) {
            return Result.fail("优惠券不存在");
        }
        if (couponUser.getStatus() != 0) {
            return Result.fail("优惠券已使用或已过期");
        }
        // 检查是否过期
        Date now = new Date();
        if (couponUser.getExpireTime() != null && now.after(couponUser.getExpireTime())) {
            couponUser.setStatus(2);
            couponUserService.updateById(couponUser);
            return Result.fail("优惠券已过期");
        }
        
        // 使用优惠券
        couponUser.setStatus(1);
        couponUser.setUseTime(now);
        couponUser.setUseOrderId(request.getOrderId());
        couponUserService.updateById(couponUser);
        
        return Result.success("使用成功");
    }

    // 获取商品列表
    @GetMapping("/products")
    public Result getProducts(@RequestParam(defaultValue = "1") Integer pageNumber,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) Integer type) {
        Page<SysProduct> page = new Page<>(pageNumber, pageSize);
        QueryWrapper<SysProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        if (type != null) {
            wrapper.eq("type", type);
        }
        wrapper.orderByAsc("sort").orderByDesc("create_time");
        Page<SysProduct> result = productService.page(page, wrapper);
        return Result.success(result);
    }

    // 兑换商品
    @PostMapping("/product/exchange")
    @Transactional
    public Result exchangeProduct(@RequestBody MemberRequest request) {
        SysMember member = memberService.getOrCreateMember(request.getUserId());
        SysProduct product = productService.getById(request.getProductId());
        
        if (product == null || product.getStatus() != 1) {
            return Result.fail("商品不存在或已下架");
        }
        if (product.getStock() <= 0) {
            return Result.fail("商品库存不足");
        }
        if (member.getPoints() < product.getPointsRequired()) {
            return Result.fail("积分不足");
        }
        
        // 如果是实物商品，需要地址信息
        if (product.getType() == 0 || product.getType() == 1) {
            if (request.getAddress() == null || request.getPhone() == null || request.getReceiver() == null) {
                return Result.fail("请填写收货信息");
            }
        }
        
        // 扣积分
        int beforePoints = member.getPoints();
        int afterPoints = beforePoints - product.getPointsRequired();
        member.setPoints(afterPoints);
        memberService.updateById(member);
        
        // 减库存
        product.setStock(product.getStock() - 1);
        productService.updateById(product);
        
        // 创建兑换记录
        SysExchangeRecord record = new SysExchangeRecord();
        record.setUserId(request.getUserId());
        record.setType(product.getType());
        record.setRelatedId(request.getProductId());
        record.setRelatedName(product.getName());
        record.setPointsUsed(product.getPointsRequired());
        record.setStatus(0);
        record.setAddress(request.getAddress());
        record.setPhone(request.getPhone());
        record.setReceiver(request.getReceiver());
        record.setRemark(request.getRemark());
        exchangeRecordService.save(record);
        
        // 记录积分日志
        SysPointsLog log = new SysPointsLog();
        log.setUserId(request.getUserId());
        log.setChangePoints(-product.getPointsRequired());
        log.setBeforePoints(beforePoints);
        log.setAfterPoints(afterPoints);
        log.setType(2);
        log.setDescription("兑换商品：" + product.getName());
        log.setRelatedId(record.getId());
        pointsLogService.save(log);
        
        return Result.success("兑换成功");
    }

    // 获取我的兑换记录
    @GetMapping("/exchange/records")
    public Result getExchangeRecords(@RequestParam String userId,
                                     @RequestParam(defaultValue = "1") Integer pageNumber,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysExchangeRecord> page = new Page<>(pageNumber, pageSize);
        QueryWrapper<SysExchangeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        Page<SysExchangeRecord> result = exchangeRecordService.page(page, wrapper);
        return Result.success(result);
    }

    // 购买会员
    @PostMapping("/purchase")
    @Transactional
    public Result purchaseMember(@RequestBody MemberRequest request) {
        if (request.getLevel() != 1 && request.getLevel() != 2) {
            return Result.fail("无效的会员等级");
        }
        
        SysMember member = memberService.getOrCreateMember(request.getUserId());
        Date now = new Date();
        
        // 保存原会员等级（注意要在修改之前保存
        Integer originalLevel = member.getLevel();
        
        // 计算到期时间
        Calendar cal = Calendar.getInstance();
        if (member.getVipExpireTime() != null && member.getVipExpireTime().after(now)) {
            cal.setTime(member.getVipExpireTime());
        } else {
            cal.setTime(now);
        }
        cal.add(Calendar.MONTH, 1);
        
        member.setLevel(request.getLevel());
        member.setVipExpireTime(cal.getTime());
        memberService.updateById(member);
        
        // 设置会员价格：VIP 99元，SVIP 199元
        BigDecimal memberPrice;
        String memberName;
        
        // 判断是否是升级（原先是VIP，现在升级到SVIP）
        if (originalLevel != null && originalLevel == 1 && request.getLevel() == 2) {
            // 升级只需要补差价：199 - 99 = 100
            memberPrice = new BigDecimal("100.00");
            memberName = "升级SVIP会员(补差价)";
        } else {
            // 正常购买
            memberPrice = request.getLevel() == 1 ? new BigDecimal("99.00") : new BigDecimal("199.00");
            memberName = request.getLevel() == 1 ? "VIP会员(1个月)" : "SVIP会员(1个月)";
        }
        
        // 添加会员购买记录到兑换记录中
        SysExchangeRecord exchangeRecord = new SysExchangeRecord();
        exchangeRecord.setUserId(request.getUserId());
        exchangeRecord.setType(4); // 4表示会员购买
        exchangeRecord.setRelatedId(member.getId());
        exchangeRecord.setRelatedName(memberName);
        exchangeRecord.setPointsUsed(0);
        exchangeRecord.setAmount(memberPrice);
        exchangeRecord.setStatus(1); // 已完成
        exchangeRecordService.save(exchangeRecord);
        
        // 赠送积分
        int bonusPoints = request.getLevel() == 1 ? 100 : 200; // VIP送100，SVIP送200
        int beforePoints = member.getPoints();
        int afterPoints = beforePoints + bonusPoints;
        member.setPoints(afterPoints);
        memberService.updateById(member);
        
        // 记录积分日志
        SysPointsLog log = new SysPointsLog();
        log.setUserId(request.getUserId());
        log.setChangePoints(bonusPoints);
        log.setBeforePoints(beforePoints);
        log.setAfterPoints(afterPoints);
        log.setType(5);
        log.setDescription("开通" + (request.getLevel() == 1 ? "VIP" : "SVIP") + "赠送积分");
        pointsLogService.save(log);
        
        return Result.success("开通成功");
    }

    // 使用积分抵扣门票/酒店
    @PostMapping("/deduction")
    @Transactional
    public Result deductionWithPoints(@RequestBody MemberRequest request) {
        SysMember member = memberService.getOrCreateMember(request.getUserId());
        
        if (member.getPoints() < request.getPointsToUse()) {
            return Result.fail("积分不足");
        }
        
        // 1积分抵扣1元
        BigDecimal deductionAmount = new BigDecimal(request.getPointsToUse());
        
        // 扣积分
        int beforePoints = member.getPoints();
        int afterPoints = beforePoints - request.getPointsToUse();
        member.setPoints(afterPoints);
        memberService.updateById(member);
        
        // 创建兑换记录
        SysExchangeRecord record = new SysExchangeRecord();
        record.setUserId(request.getUserId());
        record.setType(request.getType()); // 2:景点, 3:酒店
        record.setRelatedId(request.getRelatedId());
        record.setRelatedName(request.getRelatedName());
        record.setPointsUsed(request.getPointsToUse());
        record.setAmount(deductionAmount);
        record.setStatus(1);
        exchangeRecordService.save(record);
        
        // 记录积分日志
        String typeName = request.getType() == 2 ? "景点门票" : "酒店";
        SysPointsLog log = new SysPointsLog();
        log.setUserId(request.getUserId());
        log.setChangePoints(-request.getPointsToUse());
        log.setBeforePoints(beforePoints);
        log.setAfterPoints(afterPoints);
        log.setType(4);
        log.setDescription("积分抵扣" + typeName + "：" + request.getRelatedName());
        log.setRelatedId(record.getId());
        pointsLogService.save(log);
        
        return Result.success(deductionAmount);
    }
}
