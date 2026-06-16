package com.akibahub.dto.request;

import lombok.Data;

@Data
public class VoteRequest {
    private String decision; // YES, NO, ABSTAIN
}