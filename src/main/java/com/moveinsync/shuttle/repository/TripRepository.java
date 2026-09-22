package com.moveinsync.shuttle.repository;

import com.moveinsync.shuttle.entity.Trip;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trip t where t.id = :id")
    Optional<Trip> findByIdForUpdate(@Param("id") Long id);

    @Query("select distinct t from Trip t join fetch t.route r left join fetch r.stops where t.id = :id")
    Optional<Trip> findByIdWithStops(@Param("id") Long id);
}
