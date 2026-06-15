package com.akibahub.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
public class GroupResponse {

    private Long id;
    private String name;
    private String inviteCode;
    private double totalBalance;
    private Long createdBy;
}