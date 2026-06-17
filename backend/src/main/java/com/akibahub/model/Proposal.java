package com.akibahub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Group group;

    @ManyToOne
    private User createdBy;

    private String title;

    private String description;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

    @OneToMany(mappedBy = "proposal")
    private List<Vote> votes;
}