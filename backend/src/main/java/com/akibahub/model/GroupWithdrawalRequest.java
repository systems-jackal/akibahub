package com.akibahub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "group_withdrawal_requests")
@Getter
@Setter
public class GroupWithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private String reason;

    @ManyToOne
    private Group group;

    @ManyToOne
    private User requestedBy;

    private boolean approved;

    private boolean executed;
}