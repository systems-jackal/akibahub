package com.akibahub.controller;

import com.akibahub.dto.request.CreateGroupRequest;
import com.akibahub.dto.request.JoinGroupRequest;
import com.akibahub.dto.response.GroupResponse;
import com.akibahub.service.GroupService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public GroupResponse createGroup(@RequestBody CreateGroupRequest request) {
        return groupService.createGroup(request);
    }

    @PostMapping("/join")
    public GroupResponse joinGroup(@RequestBody JoinGroupRequest request) {
        return groupService.joinGroup(request);
    }
}