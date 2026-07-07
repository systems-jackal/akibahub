package com.akibahub.group;

import com.akibahub.group.entity.Group;
import com.akibahub.group.entity.GroupMember;
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
    public ResponseEntity<Group> createGroup(@RequestBody Map<String, String> body,
                                             @AuthenticationPrincipal User user) {
        Group group = groupService.createGroup(body.get("name"), body.get("description"), user);
        return ResponseEntity.ok(group);
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(@PathVariable Long groupId, @AuthenticationPrincipal User user) {
        groupService.joinGroup(groupId, user);
        return ResponseEntity.ok(Map.of("message", "Joined group"));
    }

    @GetMapping
    public ResponseEntity<List<Group>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getMembers(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupMembers(groupId));
    }
}