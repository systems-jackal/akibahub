package com.akibahub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long groupId;

    private Long createdBy;

    private String title;

    private String description;

    private double amount; // optional (for financial proposals)

    @Enumerated(EnumType.STRING)
    private ProposalType type;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

    private int requiredApprovals;

    private int currentApprovals;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = ProposalStatus.PENDING;
        this.currentApprovals = 0;
    }
}