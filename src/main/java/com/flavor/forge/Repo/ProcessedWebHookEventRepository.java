package com.flavor.forge.Repo;

import com.flavor.forge.Model.ProcessedWebHookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessedWebHookEventRepository extends JpaRepository<ProcessedWebHookEvent, Long> {
    Optional<ProcessedWebHookEvent> findByEventId(String eventId);
}
