package com.akibahub.group.controller;

import com.akibahub.group.dto.request.*;
import com.akibahub.group.dto.response.*;
import com.akibahub.group.model.InviteCode;
import com.akibahub.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                GroupResponse.from(groupService.createGroup(
                        userId, userEmail, request.getName(), request.getDescription())));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable String groupId,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
                GroupResponse.from(groupService.getGroup(groupId, userId)));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GroupResponse>> getMyGroups(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
                groupService.getUserGroups(userId).stream()
                        .map(GroupResponse::from)
                        .toList());
    }

    @PostMapping("/join")
    public ResponseEntity<GroupMemberResponse> joinGroup(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody JoinGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                GroupMemberResponse.from(groupService.joinGroup(
                        userId, userEmail,
                        request.getDisplayName(),
                        request.getInviteCode())));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getMembers(
            @PathVariable String groupId,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
                groupService.getMembers(groupId, userId).stream()
                        .map(GroupMemberResponse::from)
                        .toList());
    }

    @PostMapping("/{groupId}/invite")
    public ResponseEntity<Map<String, Object>> generateInvite(
            @PathVariable String groupId,
            @RequestHeader("X-User-Id") String userId) {
        InviteCode invite = groupService.generateInviteCode(groupId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "code", invite.getCode(),
                "expiresAt", invite.getExpiresAt(),
                "maxUses", invite.getMaxUses()
        ));
    }

    @PutMapping("/{groupId}/members/role")
    public ResponseEntity<Void> changeMemberRole(
            @PathVariable String groupId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ChangeRoleRequest request) {
        groupService.changeMemberRole(groupId, request.getTargetUserId(),
                request.getNewRole(), userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/members/{targetUserId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String groupId,
            @PathVariable String targetUserId,
            @RequestHeader("X-User-Id") String userId) {
        groupService.removeMember(groupId, targetUserId, userId);
        return ResponseEntity.ok().build();
    }

    // Internal — called by proposal-service
    @GetMapping("/internal/{groupId}/is-member/{userId}")
    public ResponseEntity<Map<String, Boolean>> isMember(
            @PathVariable String groupId,
            @PathVariable String userId,
            @RequestHeader("X-Service-Key") String serviceKey) {
        return ResponseEntity.ok(Map.of("member", groupService.isMember(groupId, userId)));
    }
}
