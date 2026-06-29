package com.project.travel.constant;

/**
 * 积分奖励规则
 */
public final class PointsConstants {

  private PointsConstants() {}

  /** 积分日志类型：互动行为奖励 */
  public static final int TYPE_INTERACTION = 6;

  /** 景点预约奖励 */
  public static final int BOOK_ATTRACTION = 10;
  /** 酒店预约奖励 */
  public static final int BOOK_HOTEL = 10;
  /** AI 对话奖励（每次） */
  public static final int AI_CHAT = 5;
  /** 收藏线路 */
  public static final int FAVOR_LINE = 3;
  /** 点赞旅途印记 */
  public static final int LIKE_SHARE = 2;
  /** 发表评论 */
  public static final int COMMENT = 3;
  /** 发布旅途印记 */
  public static final int PUBLISH_SHARE = 5;

  /** AI 对话每日最多奖励次数 */
  public static final int AI_CHAT_DAILY_LIMIT = 10;

  /** 每满100元消费获得积分（普通用户） */
  public static final int CONSUME_POINTS_PER_100 = 20;
  /** VIP/SVIP 消费积分倍数 */
  public static final int CONSUME_VIP_MULTIPLIER = 2;

  public static final String DESC_BOOK_ATTRACTION = "预约景点奖励";
  public static final String DESC_BOOK_HOTEL = "预约酒店奖励";
  public static final String DESC_AI_CHAT = "AI对话奖励";
  public static final String DESC_FAVOR_LINE = "收藏线路奖励";
  public static final String DESC_LIKE_SHARE = "点赞旅途印记奖励";
  public static final String DESC_COMMENT = "发表评论奖励";
  public static final String DESC_PUBLISH_SHARE = "发布旅途印记奖励";
}
