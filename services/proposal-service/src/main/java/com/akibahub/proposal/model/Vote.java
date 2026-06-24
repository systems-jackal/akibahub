package com.akibahub.proposal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "votes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"proposal_id", "voter_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "proposal_id", nullable = false)
    private String proposalId;

    @Column(name = "voter_id", nullable = false)
    private String voterId;

    @Column(name = "voter_email")
    private String voterEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteValue value;

    @CreationTimestamp
    @Column(name = "cast_at", updatable = false)
    private LocalDateTime castAt;
}
