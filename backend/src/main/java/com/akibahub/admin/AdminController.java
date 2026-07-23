package com.akibahub.admin;

import com.akibahub.admin.dto.AdminDtos.*;
import com.akibahub.shared.dto.ApiResponse;
import com.akibahub.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Every endpoint here requires the ADMIN role. This is enforced by
 * Spring Security's method security (@PreAuthorize), which was not
 * previously enabled anywhere in the app - see SecurityConfig's
 * @EnableMethodSecurity and JwtAuthenticationFilter's role-based
 * authority grant, both added alongside this controller. A non-admin
 * calling any of these gets a 403, not a 404 - the endpoints aren't
 * hidden, access to them is denied.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<PlatformStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.<PlatformStats>builder()
                .success(true).data(adminService.getStats()).build());
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserSummary>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.<List<AdminUserSummary>>builder()
                .success(true).data(adminService.getAllUsers()).build());
    }

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<AdminGroupSummary>>> getGroups() {
        return ResponseEntity.ok(ApiResponse.<List<AdminGroupSummary>>builder()
                .success(true).data(adminService.getAllGroups()).build());
    }

    @GetMapping("/audit-log")
    public ResponseEntity<ApiResponse<List<AuditLogEntry>>> getAuditLog() {
        return ResponseEntity.ok(ApiResponse.<List<AuditLogEntry>>builder()
                .success(true).data(adminService.getRecentAuditLog()).build());
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<AdminUserSummary>> setUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User actingAdmin) {
        User.AccountStatus newStatus = User.AccountStatus.valueOf(body.get("status").toUpperCase());
        AdminUserSummary updated = adminService.setUserStatus(userId, newStatus, actingAdmin);
        return ResponseEntity.ok(ApiResponse.<AdminUserSummary>builder()
                .success(true).message("User status updated").data(updated).build());
    }
}