package com.beyond.easycheck.reservationrooms.infrastructure.repository;

import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRoomRepository extends JpaRepository<ReservationRoomEntity, Long> {

//    List<ReservationRoomEntity> findByReservationDateBeforeAndReminderSentFalse(LocalDateTime dateTime);

    @Query("SELECT r FROM ReservationRoomEntity r WHERE r.checkinDate = :checkinDate")
    List<ReservationRoomEntity> findByCheckinDate(@Param("checkinDate") LocalDate checkinDate);
}
