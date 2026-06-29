package com.project.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.travel.constant.PointsConstants;
import com.project.travel.domain.SysMember;
import com.project.travel.domain.SysPointsLog;
import com.project.travel.mapper.SysMemberMapper;
import com.project.travel.service.SysMemberService;
import com.project.travel.service.SysPointsLogService;
import org.apache.commons.lang3.StringUtils;
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
    public int signIn(String userId) {
        SysMember member = getOrCreateMember(userId);
        Date now = new Date();

        if (member.getLastSignInTime() != null) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(member.getLastSignInTime());

            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(now);

            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                    && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                    && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) {
                return 0;
            }
        }

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

        int totalPoints = 1;
        if (member.getLevel() != null && member.getLevel() >= 1) {
            totalPoints += 1;
        }

        int beforePoints = member.getPoints();
        int afterPoints = beforePoints + totalPoints;

        member.setPoints(afterPoints);
        member.setLastSignInTime(now);
        member.setSignInCount(signInCount);
        this.updateById(member);

        SysPointsLog log = new SysPointsLog();
        log.setUserId(userId);
        log.setChangePoints(totalPoints);
        log.setBeforePoints(beforePoints);
        log.setAfterPoints(afterPoints);
        log.setType(0);
        log.setDescription((member.getLevel() != null && member.getLevel() >= 1)
                ? "签到获得" + totalPoints + "积分（含VIP加成）" : "签到获得1积分");
        pointsLogService.save(log);

        return totalPoints;
    }

    @Override
    @Transactional
    public int addPointsFromOrder(String userId, Integer amount) {
        SysMember member = getOrCreateMember(userId);

        int units = amount / 100;
        if (units <= 0) {
            return 0;
        }

        int multiplier = (member.getLevel() != null && member.getLevel() >= 1)
                ? PointsConstants.CONSUME_VIP_MULTIPLIER
                : 1;
        int pointsEarned = units * PointsConstants.CONSUME_POINTS_PER_100 * multiplier;

        if (pointsEarned > 0) {
            addPoints(userId, pointsEarned, 1, "消费获得积分（" + amount + "元）", null);
        }
        return pointsEarned;
    }

    @Override
    @Transactional
    public int awardPointsOnce(String userId, int points, int type, String description, String relatedId) {
        if (StringUtils.isBlank(userId) || points <= 0) {
            return 0;
        }
        QueryWrapper<SysPointsLog> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(SysPointsLog::getUserId, userId)
                .eq(SysPointsLog::getType, type);
        if (StringUtils.isNotBlank(relatedId)) {
            wrapper.lambda().eq(SysPointsLog::getRelatedId, relatedId);
        } else {
            wrapper.lambda().eq(SysPointsLog::getDescription, description);
        }
        if (pointsLogService.count(wrapper) > 0) {
            return 0;
        }
        addPoints(userId, points, type, description, relatedId);
        return points;
    }

    @Override
    @Transactional
    public int awardPointsDaily(String userId, int points, int type, String description, int dailyLimit) {
        if (StringUtils.isBlank(userId) || points <= 0 || dailyLimit <= 0) {
            return 0;
        }
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        QueryWrapper<SysPointsLog> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(SysPointsLog::getUserId, userId)
                .eq(SysPointsLog::getType, type)
                .eq(SysPointsLog::getDescription, description)
                .ge(SysPointsLog::getCreateTime, start.getTime());
        if (pointsLogService.count(wrapper) >= dailyLimit) {
            return 0;
        }
        addPoints(userId, points, type, description, null);
        return points;
    }

    @Override
    @Transactional
    public boolean adjustPoints(String userId, int changePoints, String reason, String operatorId) {
        if (StringUtils.isBlank(userId) || changePoints == 0) {
            return false;
        }
        SysMember member = getOrCreateMember(userId);
        int beforePoints = member.getPoints() != null ? member.getPoints() : 0;
        int afterPoints = beforePoints + changePoints;
        if (afterPoints < 0) {
            return false;
        }
        member.setPoints(afterPoints);
        this.updateById(member);

        SysPointsLog log = new SysPointsLog();
        log.setUserId(userId);
        log.setChangePoints(changePoints);
        log.setBeforePoints(beforePoints);
        log.setAfterPoints(afterPoints);
        log.setType(5);
        log.setDescription(StringUtils.isNotBlank(reason) ? reason : "管理员调整积分");
        log.setRelatedId(operatorId);
        pointsLogService.save(log);
        return true;
    }

    private void addPoints(String userId, int points, int type, String description, String relatedId) {
        SysMember member = getOrCreateMember(userId);
        int beforePoints = member.getPoints() != null ? member.getPoints() : 0;
        int afterPoints = beforePoints + points;
        member.setPoints(afterPoints);
        this.updateById(member);

        SysPointsLog log = new SysPointsLog();
        log.setUserId(userId);
        log.setChangePoints(points);
        log.setBeforePoints(beforePoints);
        log.setAfterPoints(afterPoints);
        log.setType(type);
        log.setDescription(description);
        log.setRelatedId(relatedId);
        pointsLogService.save(log);
    }
}
