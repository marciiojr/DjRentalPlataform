package com.locadj.repository;

import com.locadj.model.Reservation;
import com.locadj.model.Kit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByKitAndStartDateTimeLessThanEqualAndEndDateTimeGreaterThanEqual(
            Kit kit, LocalDateTime endDateTime, LocalDateTime startDateTime);
}
