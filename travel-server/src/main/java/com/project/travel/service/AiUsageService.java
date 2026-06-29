package com.project.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.travel.domain.SysAiLog;
import com.project.travel.domain.SysTravelPlan;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiUsageService {

  public static final String TYPE_CHAT = "chat";
  public static final String TYPE_PLAN = "plan";

  @Autowired
  private SysAiLogService sysAiLogService;

  @Autowired
  private SysTravelPlanService sysTravelPlanService;

  public void logChat(String userId, String question) {
    if (StringUtils.isBlank(question)) {
      return;
    }
    SysAiLog log = new SysAiLog();
    log.setUserId(userId);
    log.setType(TYPE_CHAT);
    String content = question.length() > 500 ? question.substring(0, 500) : question;
    log.setContent(content);
    log.setCreateTime(new Date());
    sysAiLogService.save(log);
  }

  public void logPlan(String userId, String destination) {
    SysAiLog log = new SysAiLog();
    log.setUserId(userId);
    log.setType(TYPE_PLAN);
    log.setDestination(destination);
    log.setContent(StringUtils.defaultIfBlank(destination, "行程规划"));
    log.setCreateTime(new Date());
    sysAiLogService.save(log);
  }

  public Map<String, Object> getStatsOverview() {
    Map<String, Object> result = new HashMap<>();

    long chatTotal = sysAiLogService.count(new QueryWrapper<SysAiLog>().lambda().eq(SysAiLog::getType, TYPE_CHAT));
    long planTotal = sysTravelPlanService.count();
    long planLogTotal = sysAiLogService.count(new QueryWrapper<SysAiLog>().lambda().eq(SysAiLog::getType, TYPE_PLAN));

    Date todayStart = getTodayStart();
    long chatToday = sysAiLogService.count(new QueryWrapper<SysAiLog>().lambda()
        .eq(SysAiLog::getType, TYPE_CHAT)
        .ge(SysAiLog::getCreateTime, todayStart));
    long planToday = sysTravelPlanService.count(new QueryWrapper<SysTravelPlan>().lambda()
        .ge(SysTravelPlan::getCreateTime, todayStart));

    result.put("chatTotal", chatTotal);
    result.put("planTotal", planTotal);
    result.put("planLogTotal", planLogTotal);
    result.put("chatToday", chatToday);
    result.put("planToday", planToday);
    result.put("destinationRank", getDestinationRank());
    result.put("dailyTrend", getDailyTrend(7));
    result.put("recentPlans", getRecentPlans(8));
    return result;
  }

  private List<Map<String, Object>> getDestinationRank() {
    List<SysTravelPlan> plans = sysTravelPlanService.list();
    Map<String, Long> grouped = plans.stream()
        .filter(p -> StringUtils.isNotBlank(p.getDestination()))
        .collect(Collectors.groupingBy(SysTravelPlan::getDestination, Collectors.counting()));

    return grouped.entrySet().stream()
        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
        .limit(10)
        .map(e -> {
          Map<String, Object> item = new HashMap<>();
          item.put("destination", e.getKey());
          item.put("count", e.getValue());
          return item;
        })
        .collect(Collectors.toList());
  }

  private List<Map<String, Object>> getDailyTrend(int days) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    List<Map<String, Object>> trend = new ArrayList<>();
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.DAY_OF_MONTH, -(days - 1));

    for (int i = 0; i < days; i++) {
      Date dayStart = cal.getTime();
      cal.add(Calendar.DAY_OF_MONTH, 1);
      Date dayEnd = cal.getTime();

      long chatCount = sysAiLogService.count(new QueryWrapper<SysAiLog>().lambda()
          .eq(SysAiLog::getType, TYPE_CHAT)
          .ge(SysAiLog::getCreateTime, dayStart)
          .lt(SysAiLog::getCreateTime, dayEnd));
      long planCount = sysTravelPlanService.count(new QueryWrapper<SysTravelPlan>().lambda()
          .ge(SysTravelPlan::getCreateTime, dayStart)
          .lt(SysTravelPlan::getCreateTime, dayEnd));

      Map<String, Object> item = new HashMap<>();
      item.put("date", sdf.format(dayStart));
      item.put("chatCount", chatCount);
      item.put("planCount", planCount);
      trend.add(item);
    }
    return trend;
  }

  private List<SysTravelPlan> getRecentPlans(int limit) {
    QueryWrapper<SysTravelPlan> qw = new QueryWrapper<>();
    qw.lambda().orderByDesc(SysTravelPlan::getCreateTime).last("limit " + limit);
    return sysTravelPlanService.list(qw);
  }

  private Date getTodayStart() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }
}
