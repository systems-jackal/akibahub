package com.akibahub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;

    private BigDecimal amount;

    private BigDecimal balanceAfter;

    private String reference;

    // PERSONAL SIDE
    @ManyToOne
    private User user;

    // GROUP SIDE
    @ManyToOne
    private Group group;
}