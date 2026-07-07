package com.akibahub.proposal.entity;

import com.akibahub.group.entity.Group;
import com.akibahub.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Proposal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false) private String title;
    private String description;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal amount;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ProposalStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum ProposalStatus { OPEN, APPROVED, REJECTED }
}