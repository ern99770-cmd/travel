package com.project.travel.controller.ai;

import com.project.travel.domain.Result;
import com.project.travel.service.AiUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ai/admin")
public class AiAdminController {

    @Autowired
    private AiUsageService aiUsageService;

    @GetMapping("stats")
    public Result stats() {
        return Result.success(aiUsageService.getStatsOverview());
    }
}
