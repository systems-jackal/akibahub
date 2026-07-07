package com.akibahub.dashboard;

import com.akibahub.shared.dto.ApiResponse;
import com.akibahub.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Object>> getDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true).data(dashboardService.getDashboard(user)).build());
    }
}