package com.akibahub.dto.request;

import lombok.*;

@Getter
@Setter
public class CreateGroupRequest {

    private String name;
    private Long createdBy;
}