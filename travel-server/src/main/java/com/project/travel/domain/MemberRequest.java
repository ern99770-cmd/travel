package com.project.travel.domain;

import lombok.Data;

/**
 * 通用请求参数
 */
@Data
public class MemberRequest {
    private String userId;
    private Integer pageNumber;
    private Integer pageSize;
    private String couponId;
    private String couponUserId;
    private String orderId;
    private Integer orderType;
    private Integer userLevel;
    private String productId;
    private String address;
    private String phone;
    private String receiver;
    private String remark;
    private Integer level;
    private Integer pointsToUse;
    private String relatedId;
    private String relatedName;
    private Integer type;
}
