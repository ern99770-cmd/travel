package com.project.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.travel.domain.SysTravelPlan;
import com.project.travel.mapper.SysTravelPlanMapper;
import com.project.travel.service.SysTravelPlanService;
import org.springframework.stereotype.Service;

@Service
public class SysTravelPlanServiceImpl extends ServiceImpl<SysTravelPlanMapper, SysTravelPlan> implements SysTravelPlanService {
}
