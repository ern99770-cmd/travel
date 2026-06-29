package com.project.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.travel.domain.SysMember;

public interface SysMemberService extends IService<SysMember> {
    SysMember getOrCreateMember(String userId);
    int signIn(String userId);
    int addPointsFromOrder(String userId, Integer amount);

    /**
     * 一次性行为奖励（同一用户 + 关联ID + 类型仅奖励一次），返回实际获得积分
     */
    int awardPointsOnce(String userId, int points, int type, String description, String relatedId);

    /**
     * 每日限次行为奖励，返回实际获得积分
     */
    int awardPointsDaily(String userId, int points, int type, String description, int dailyLimit);

    boolean adjustPoints(String userId, int changePoints, String reason, String operatorId);
}
