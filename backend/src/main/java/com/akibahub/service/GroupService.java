package com.akibahub.service;

import com.akibahub.dto.request.CreateGroupRequest;
import com.akibahub.dto.request.JoinGroupRequest;
import com.akibahub.dto.response.GroupResponse;

public interface GroupService {

    GroupResponse createGroup(CreateGroupRequest request);

    GroupResponse joinGroup(JoinGroupRequest request);
}