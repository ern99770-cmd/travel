package com.project.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.travel.domain.SysMember;

public interface SysMemberService extends IService<SysMember> {
    SysMember getOrCreateMember(String userId);
    boolean signIn(String userId);
    void addPointsFromOrder(String userId, Integer amount);
}
