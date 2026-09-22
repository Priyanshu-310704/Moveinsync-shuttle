package com.moveinsync.shuttle.repository;

import com.moveinsync.shuttle.entity.WaitlistEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {
    List<WaitlistEntry> findByTripIdOrderByCreatedAtAsc(Long tripId);
}
