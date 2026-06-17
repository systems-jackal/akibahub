package com.akibahub.controller;

import com.akibahub.model.Group;
import com.akibahub.model.GroupMember;
import com.akibahub.service.GroupService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public Group create(@RequestParam Long userId,
                        @RequestParam String name,
                        @RequestParam String description) {
        return groupService.createGroup(userId, name, description);
    }

    @PostMapping("/join")
    public GroupMember join(@RequestParam Long userId,
                            @RequestParam String inviteCode) {
        return groupService.joinGroup(userId, inviteCode);
    }
}