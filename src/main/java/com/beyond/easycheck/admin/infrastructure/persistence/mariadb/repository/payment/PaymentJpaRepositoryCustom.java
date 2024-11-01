package com.beyond.easycheck.admin.infrastructure.persistence.mariadb.repository.payment;

import com.beyond.easycheck.payments.infrastructure.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.beyond.easycheck.admin.application.service.AdminReadUseCase.PaymentFindQuery;

public interface PaymentJpaRepositoryCustom {
    List<PaymentEntity> findAllPayments(Long managedAccommodationId, PaymentFindQuery query);
}
