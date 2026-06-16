package com.akibahub.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GroupRequest {
    private String name;
    private String description;
    private BigDecimal monthlyContribution;
}