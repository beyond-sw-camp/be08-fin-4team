package com.beyond.easycheck.sms.ui.controller;

import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.sms.exception.SmsMessageType;
import com.beyond.easycheck.sms.infrastructure.persistence.redis.entity.SmsVerificationCode;
import com.beyond.easycheck.sms.infrastructure.persistence.redis.entity.VerifiedPhone;
import com.beyond.easycheck.sms.infrastructure.persistence.redis.repository.SmsVerificationCodeRepository;
import com.beyond.easycheck.sms.infrastructure.persistence.redis.repository.SmsVerifiedPhoneRepository;
import com.beyond.easycheck.sms.ui.requestbody.SmsCodeVerifyRequest;
import com.beyond.easycheck.sms.ui.requestbody.SmsVerificationCodeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;

@Slf4j
@RestController
@RequestMapping("/api/v1/sms")
@Tag(name = "Sms", description = "휴대폰 인증 API")
public class SmsController {

    @Value("${coolsms.easycheck.representative-number}")
    private String senderPhoneNumber;

    private final Long SMS_VERIFICATION_EXPIRED_TIME = 300L;

    private DefaultMessageService defaultMessageService;

    private SmsVerifiedPhoneRepository smsVerifiedPhoneRepository;

    private SmsVerificationCodeRepository smsVerificationCodeRepository;


    @Autowired
    public SmsController(@Value("${coolsms.easycheck.apikey}") String apiKey,
                         @Value("${coolsms.easycheck.apisecret}") String apiSecret,
                         @Value("${coolsms.easycheck.representative-number}") String senderPhoneNumber,
                         SmsVerificationCodeRepository smsVerificationCodeRepository,
                         SmsVerifiedPhoneRepository smsVerifiedPhoneRepository,
                         DefaultMessageService defaultMessageService) {
        this.senderPhoneNumber = senderPhoneNumber;
        this.smsVerificationCodeRepository = smsVerificationCodeRepository;
        this.smsVerifiedPhoneRepository = smsVerifiedPhoneRepository;
        this.defaultMessageService = defaultMessageService;
    }

    @PostMapping("/code")
    @Operation(summary = "인증번호 받기")
    public ResponseEntity<Void> getVerificationCode(@RequestBody @Validated SmsVerificationCodeRequest request) {

        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom(senderPhoneNumber);
        message.setTo(request.receivingPhoneNumber());
        message.setSubject("[EasyCheck] 휴대폰 인증");

        String verificationCode = generateVerificationCode();
        SmsVerificationCode smsVerificationCode = SmsVerificationCode.createSmsVerificationCode(request.receivingPhoneNumber(), verificationCode, SMS_VERIFICATION_EXPIRED_TIME);

        smsVerificationCodeRepository.save(smsVerificationCode);
        message.setText("인증번호: " + verificationCode);

        SingleMessageSentResponse response = this.defaultMessageService.sendOne(new SingleMessageSendingRequest(message));

        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }

    @Transactional
    @PostMapping("/verify")
    @Operation(summary = "인증번호 확인하기")
    public ResponseEntity<Void> verifyCode(@RequestBody @Validated SmsCodeVerifyRequest request) {

        log.info("[SmsController - verifyCode] request = {}", request);
        SmsVerificationCode smsVerificationCode = smsVerificationCodeRepository.findById(request.code())
                .orElseThrow(() -> new EasyCheckException(SmsMessageType.INVALID_VERIFICATION_CODE));

        log.info("[SmsController - verifyCode] smsVerificationCode = {}", smsVerificationCode);
        if (!request.phone().equals(smsVerificationCode.getPhone())) {
            throw new EasyCheckException(SmsMessageType.SMS_VERIFICATION_CODE_NOT_MATCHED);
        }

        VerifiedPhone verifiedPhone = VerifiedPhone.createVerifiedPhone(request.phone());
        log.info("[SmsController - verifyCode] verifiedPhone = {}", verifiedPhone);

        // 인증된 핸드폰 번호 추가
        smsVerifiedPhoneRepository.save(verifiedPhone);
        // 인증 완료 후 코드 제거
        smsVerificationCodeRepository.delete(smsVerificationCode);

        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }

    public static String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        final int CODE_LENGTH = 6;
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

}
