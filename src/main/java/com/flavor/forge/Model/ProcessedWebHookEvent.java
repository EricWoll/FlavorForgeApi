package com.flavor.forge.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "processed_webhook_events", uniqueConstraints = @UniqueConstraint(columnNames = "eventId"))
public class ProcessedWebHookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @lombok.Setter
    @Column(nullable = false, unique = true)
    private String eventId;

    // Constructors, getters, and setters

    public ProcessedWebHookEvent() {}

    public ProcessedWebHookEvent(String eventId) {
        this.eventId = eventId;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

}
