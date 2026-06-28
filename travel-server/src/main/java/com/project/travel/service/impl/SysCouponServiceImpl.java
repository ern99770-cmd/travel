package com.project.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.travel.domain.SysCoupon;
import com.project.travel.mapper.SysCouponMapper;
import com.project.travel.service.SysCouponService;
import org.springframework.stereotype.Service;

@Service
public class SysCouponServiceImpl extends ServiceImpl<SysCouponMapper, SysCoupon> implements SysCouponService {
}
