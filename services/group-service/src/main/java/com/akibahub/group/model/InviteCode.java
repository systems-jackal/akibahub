package com.akibahub.group.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "invite_codes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "max_uses")
    @Builder.Default
    private int maxUses = 10;

    @Column(name = "use_count")
    @Builder.Default
    private int useCount = 0;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public boolean isValid() {
        return active
                && LocalDateTime.now().isBefore(expiresAt)
                && useCount < maxUses;
    }

    public void recordUse() {
        this.useCount++;
        if (this.useCount >= this.maxUses) {
            this.active = false;
        }
    }
}
