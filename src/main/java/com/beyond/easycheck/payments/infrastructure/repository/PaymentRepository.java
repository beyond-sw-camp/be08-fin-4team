package com.beyond.easycheck.payments.infrastructure.repository;

import com.beyond.easycheck.payments.infrastructure.entity.CompletionStatus;
import com.beyond.easycheck.payments.infrastructure.entity.PaymentEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    @Query("SELECT DISTINCT p FROM PaymentEntity p " +
            "JOIN FETCH p.reservationRoomEntity r " +
            "JOIN FETCH r.userEntity u " +
            "LEFT JOIN FETCH u.corporate c " +   // Corporate 정보 fetch join
            "LEFT JOIN FETCH u.role " +          // Role 정보 fetch join
            "JOIN FETCH r.roomEntity room " +
            "JOIN FETCH room.roomTypeEntity rt " +
            "JOIN FETCH rt.accommodationEntity a " +
            "WHERE a.id = :accommodationId " +
            "ORDER BY p.paymentDate DESC")
    List<PaymentEntity> findAllByAccommodationId(@Param("accommodationId") Long accommodationId);
    // 결제 상태별로 필터링하는 메서드도 추가
    @Query("SELECT p FROM PaymentEntity p " +
            "JOIN p.reservationRoomEntity r " +
            "JOIN r.roomEntity room " +
            "JOIN room.roomTypeEntity rt " +
            "JOIN rt.accommodationEntity a " +
            "WHERE a.id = :accommodationId " +
            "AND p.completionStatus = :status " +
            "ORDER BY p.paymentDate DESC")
    List<PaymentEntity> findAllByAccommodationIdAndStatus(
            @Param("accommodationId") Long accommodationId,
            @Param("status") CompletionStatus status);

    Optional<PaymentEntity> findByImpUid(String impUid);
}
