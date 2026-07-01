package com.badminton.repository;

import com.badminton.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn(
            Long courtId, LocalDate bookingDate, Long timeSlotId, Collection<String> statuses);

    List<Booking> findByCustomerId(Long customerId);
}
