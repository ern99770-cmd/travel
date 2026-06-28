package com.project.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.travel.domain.SysMember;
import com.project.travel.domain.SysPointsLog;
import com.project.travel.mapper.SysMemberMapper;
import com.project.travel.service.SysMemberService;
import com.project.travel.service.SysPointsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

@Service
public class SysMemberServiceImpl extends ServiceImpl<SysMemberMapper, SysMember> implements SysMemberService {

    @Autowired
    private SysPointsLogService pointsLogService;

    @Override
    public SysMember getOrCreateMember(String userId) {
        QueryWrapper<SysMember> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        SysMember member = this.getOne(wrapper);
        
        if (member == null) {
            member = new SysMember();
            member.setUserId(userId);
            member.setLevel(0);
            member.setPoints(0);
            member.setSignInCount(0);
            this.save(member);
        }
        
        // 检查VIP是否过期
        if (member.getLevel() > 0 && member.getVipExpireTime() != null) {
            if (member.getVipExpireTime().before(new Date())) {
                member.setLevel(0);
                member.setVipExpireTime(null);
                this.updateById(member);
            }
        }
        
        return member;
    }

    @Override
    @Transactional
    public boolean signIn(String userId) {
        SysMember member = getOrCreateMember(userId);
        Date now = new Date();
        
        // 检查是否今天已签到
        if (member.getLastSignInTime() != null) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(member.getLastSignInTime());
            
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(now);
            
            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) 
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) 
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) {
                return false; // 今天已签到
            }
        }
        
        // 计算连续签到天数
        int signInCount = 1;
        if (member.getLastSignInTime() != null) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(member.getLastSignInTime());
            cal1.add(Calendar.DAY_OF_MONTH, 1);
            
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(now);
            
            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) 
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) 
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) {
                signInCount = member.getSignInCount() + 1;
            }
        }
        
        // 固定获得1积分
        int totalPoints = 1;
        
        int beforePoints = member.getPoints();
        int afterPoints = beforePoints + totalPoints;
        
        // 更新会员信息
        member.setPoints(afterPoints);
        member.setLastSignInTime(now);
        member.setSignInCount(signInCount);
        this.updateById(member);
        
        // 记录积分日志
        SysPointsLog log = new SysPointsLog();
        log.setUserId(userId);
        log.setChangePoints(totalPoints);
        log.setBeforePoints(beforePoints);
        log.setAfterPoints(afterPoints);
        log.setType(0);
        log.setDescription("签到获得1积分");
        pointsLogService.save(log);
        
        return true;
    }

    @Override
    @Transactional
    public void addPointsFromOrder(String userId, Integer amount) {
        SysMember member = getOrCreateMember(userId);
        
        // 根据等级计算积分：VIP1 100元1积分，SVIP 100元2积分
        int pointsEarned;
        if (member.getLevel() == 1) {
            pointsEarned = amount / 100;
        } else if (member.getLevel() == 2) {
            pointsEarned = (amount / 100) * 2;
        } else {
            pointsEarned = amount / 100; // 普通用户也有基础积分
        }
        
        if (pointsEarned > 0) {
            int beforePoints = member.getPoints();
            int afterPoints = beforePoints + pointsEarned;
            
            member.setPoints(afterPoints);
            this.updateById(member);
            
            // 记录积分日志
            SysPointsLog log = new SysPointsLog();
            log.setUserId(userId);
            log.setChangePoints(pointsEarned);
            log.setBeforePoints(beforePoints);
            log.setAfterPoints(afterPoints);
            log.setType(1);
            log.setDescription("消费获得积分（" + amount + "元）");
            pointsLogService.save(log);
        }
    }
}
