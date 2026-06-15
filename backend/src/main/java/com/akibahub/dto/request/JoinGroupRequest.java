package com.akibahub.dto.request;

import lombok.*;

@Getter
@Setter
public class JoinGroupRequest {

    private Long userId;
    private String inviteCode;
}