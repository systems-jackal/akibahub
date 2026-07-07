package com.akibahub.user;

import com.akibahub.shared.dto.ApiResponse;
import com.akibahub.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<User.UserDto>> updateProfile(@AuthenticationPrincipal User user,
                                                                   @RequestBody Map<String, String> body) {
        User updated = userService.updateProfile(user, body);
        return ResponseEntity.ok(ApiResponse.<User.UserDto>builder()
                .success(true).message("Profile updated").data(updated.toDto()).build());
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal User user,
                                                            @RequestBody Map<String, String> body) {
        userService.changePassword(user, body.get("currentPassword"), body.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Password changed").build());
    }
}