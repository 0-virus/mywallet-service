package com.ragvirus.policy.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "policy_calendar_event")
public class PolicyCalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    private String eventType;
    private LocalDate eventDate;
    private String title;
    private Instant createdAt = Instant.now();

    protected PolicyCalendarEvent() {
    }

    public PolicyCalendarEvent(Long memberId, Policy policy, String eventType, LocalDate eventDate, String title) {
        this.memberId = memberId;
        this.policy = policy;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Policy getPolicy() {
        return policy;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public String getTitle() {
        return title;
    }
}
