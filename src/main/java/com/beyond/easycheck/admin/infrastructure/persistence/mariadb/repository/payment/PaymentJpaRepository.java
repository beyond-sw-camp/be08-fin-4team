package com.beyond.easycheck.admin.infrastructure.persistence.mariadb.repository.payment;

import com.beyond.easycheck.payments.infrastructure.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long>, PaymentJpaRepositoryCustom {
}
