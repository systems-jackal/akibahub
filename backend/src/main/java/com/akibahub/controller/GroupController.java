package com.akibahub.controller;

import com.akibahub.dto.request.GroupRequest;
import com.akibahub.dto.response.ApiResponse;
import com.akibahub.security.JwtUtil;
import com.akibahub.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;
    private final JwtUtil jwtUtil;

    public GroupController(GroupService groupService, JwtUtil jwtUtil) {
        this.groupService = groupService;
        this.jwtUtil = jwtUtil;
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(HttpServletRequest request, @RequestBody GroupRequest req) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Group created",
                groupService.createGroup(req.getName(), req.getDescription(), userId)));
    }

    @PostMapping("/invite/{groupId}")
    public ResponseEntity<?> generateInvite(HttpServletRequest request, @PathVariable Long groupId) {
        Long userId = getUserId(request);
        String code = groupService.generateInviteCode(groupId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Invite code generated", Map.of("code", code)));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinGroup(HttpServletRequest request, @RequestBody Map<String, String> payload) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Joined group",
                groupService.joinGroup(payload.get("code"), userId)));
    }
}