package com.autoparts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "status_history_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_case_id", nullable = false)
    private RequestCase requestCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus newStatus;

    @Column(nullable = false)
    private LocalDateTime changedDate;

    @PrePersist
    void setChangedDate() {
        if (changedDate == null) {
            changedDate = LocalDateTime.now();
        }
    }
}
