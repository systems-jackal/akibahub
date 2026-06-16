package com.akibahub.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProposalRequest {
    private Long groupId;
    private String title;
    private String description;
    private BigDecimal amount;
}