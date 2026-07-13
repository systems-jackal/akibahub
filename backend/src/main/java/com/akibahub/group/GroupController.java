package com.akibahub.group;

import com.akibahub.group.entity.Group;
import com.akibahub.group.entity.GroupMember;
import com.akibahub.shared.dto.ApiResponse;
import com.akibahub.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Group>> createGroup(@RequestBody Map<String, String> body,
                                                          @AuthenticationPrincipal User user) {
        Group group = groupService.createGroup(
                body.get("name"),
                body.get("description"),
                body.get("rules"),
                user
        );
        return ResponseEntity.ok(ApiResponse.<Group>builder()
                .success(true).message("Group created").data(group).build());
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<String>> joinGroup(@RequestBody Map<String, String> body,
                                                         @AuthenticationPrincipal User user) {
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            throw new RuntimeException("Invite code required");
        }
        groupService.joinGroup(code.trim(), user);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true).message("Joined group").build());
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Group>>> getMyGroups(@AuthenticationPrincipal User user) {
        List<Group> groups = groupService.getMyGroups(user);
        return ResponseEntity.ok(ApiResponse.<List<Group>>builder()
                .success(true).data(groups).build());
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Group>> getGroup(@PathVariable Long groupId,
                                                        @AuthenticationPrincipal User user) {
        Group group = groupService.getGroup(groupId, user);
        return ResponseEntity.ok(ApiResponse.<Group>builder()
                .success(true).data(group).build());
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Group>> updateGroup(@PathVariable Long groupId,
                                                          @RequestBody Map<String, String> body,
                                                          @AuthenticationPrincipal User user) {
        Group updated = groupService.updateGroup(
                groupId, 
                body.get("name"), 
                body.get("description"), 
                body.get("rules"), 
                user
        );
        return ResponseEntity.ok(ApiResponse.<Group>builder()
                .success(true).message("Group updated").data(updated).build());
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long groupId,
                                                         @AuthenticationPrincipal User user) {
        groupService.deleteGroup(groupId, user);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Group deleted").build());
    }

    @PostMapping("/{groupId}/invite")
    public ResponseEntity<ApiResponse<String>> generateInvite(@PathVariable Long groupId,
                                                              @AuthenticationPrincipal User user) {
        String code = groupService.generateInviteCode(groupId, user);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true).data(code).build());
    }

    @GetMapping("/{groupId}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGroupStats(@PathVariable Long groupId,
                                                                          @AuthenticationPrincipal User user) {
        Map<String, Object> stats = groupService.getGroupStats(groupId, user);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true).data(stats).build());
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMember>>> getMembers(@PathVariable Long groupId,
                                                                      @AuthenticationPrincipal User user) {
        List<GroupMember> members = groupService.getGroupMembers(groupId, user);
        return ResponseEntity.ok(ApiResponse.<List<GroupMember>>builder()
                .success(true).data(members).build());
    }
}