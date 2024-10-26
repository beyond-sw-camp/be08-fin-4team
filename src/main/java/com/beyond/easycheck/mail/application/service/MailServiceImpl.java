package com.beyond.easycheck.mail.application.service;

import com.beyond.easycheck.common.exception.CommonMessageType;
import com.beyond.easycheck.common.exception.EasyCheckException;
import com.beyond.easycheck.mail.exception.MailMessageType;
import com.beyond.easycheck.mail.infrastructure.persistence.redis.entity.VerificationCodeEntity;
import com.beyond.easycheck.mail.infrastructure.persistence.redis.entity.VerifiedEmailEntity;
import com.beyond.easycheck.mail.infrastructure.persistence.redis.repository.VerificationCodeRepository;
import com.beyond.easycheck.mail.infrastructure.persistence.redis.repository.VerifiedEmailRepository;
import com.beyond.easycheck.reservationrooms.application.util.ReservationFormatUtil;
import com.beyond.easycheck.reservationrooms.ui.view.ReservationRoomView;
import com.beyond.easycheck.suggestion.exception.SuggestionMessageType;
import com.beyond.easycheck.suggestion.infrastructure.persistence.repository.SuggestionsRepository;
import com.beyond.easycheck.suggestion.ui.requestbody.SuggestionReplyRequestBody;
import com.beyond.easycheck.suggestion.ui.view.SuggestionView;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService{

    private static final String MAIL_SUBJECT = "EasyCheck 이메일 인증코드";
    private static final String MAIL_RESERVATION = "EasyCheck 예약 안내";
    private static final String REMINDER_BEFORE_3DAYS = "EasyCheck 체크인 3일 전";
    private static final String REMINDER_BEFORE_10DAYS = "EasyCheck 체크인 10일 전";

    public static final Long VERIFICATION_EXPIRED_TIME = 300L;

    private final JavaMailSender mailSender;
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerifiedEmailRepository verifiedEmailRepository;
    private final SuggestionsRepository suggestionsRepository;

    @Override
    @Transactional
    public void sendVerificationCode(String email) {

        final String SENDER_EMAIL_ADDRESS = "yonginfren@gmail.com";

        log.info("sender = {}", SENDER_EMAIL_ADDRESS);
        // html 형식으로 내용을 첨부하기 위한 객체
        MimeMessage message = mailSender.createMimeMessage();

        try {
            // 송신자 메일 설정
            message.setFrom(new InternetAddress(SENDER_EMAIL_ADDRESS));
            // 인증 코드 생성
            String verificationCode = generateVerificationCode();

            VerificationCodeEntity verificationCodeEntity = VerificationCodeEntity.createVerificationCode(email, verificationCode, VERIFICATION_EXPIRED_TIME);

            // 현재 이메일과 인증코드 레디스에 저장
            verificationCodeRepository.save(verificationCodeEntity);

            // 메일 내용 html로 생성
            String htmlContent = generateVerificationCodeEmailContent(verificationCode);

            // 수신자 이메일 주소 설정
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            // 메일 제목 설정
            message.setSubject(MAIL_SUBJECT);
            // 메일 내용 설정
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // 메일 전송
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EasyCheckException(CommonMessageType.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String code) {

        VerificationCodeEntity verified = verificationCodeRepository.findByCode(code)
                .orElseThrow(() -> new EasyCheckException(MailMessageType.VERIFICATION_CODE_INVALID));

        // 이메일 인증 성공 했을 경우
        // 인증된 이메일 저장소에 저장
        VerifiedEmailEntity verifiedEmail = VerifiedEmailEntity.createVerifiedEmail(verified.getEmail());
        verifiedEmailRepository.save(verifiedEmail);

        // 인증 코드 삭제
        verificationCodeRepository.delete(verified);

    }

    @Override
    @Transactional
    public void sendReservationConfirmationEmail(String email, ReservationRoomView reservationDetails) {

        MimeMessage message = mailSender.createMimeMessage();

        try {
            // 송신자 메일 설정
            final String SENDER_EMAIL_ADDRESS = "yonginfren@gmail.com";
            message.setFrom(new InternetAddress(SENDER_EMAIL_ADDRESS));

            // 메일 수신자 설정
            message.setRecipients(MimeMessage.RecipientType.TO, email);

            // 메일 제목 설정
            message.setSubject(MAIL_RESERVATION);

            // 메일 본문 내용 생성
            String htmlContent = generateReservationConfirmationEmailContent(reservationDetails);

            // 메일 내용 설정
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // 메일 전송
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send reservation confirmation email", e);
            throw new EasyCheckException(CommonMessageType.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void send3DaysBeforeReservationReminderEmail(String email, ReservationRoomView reservationDetails) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            final String SENDER_EMAIL_ADDRESS = "yonginfren@gmail.com";
            message.setFrom(new InternetAddress(SENDER_EMAIL_ADDRESS));
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject(REMINDER_BEFORE_3DAYS);

            String htmlContent = generateReservation3DaysReminderEmailContent(reservationDetails);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            mailSender.send(message);
            log.info("Reservation reminder email sent to {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send reservation reminder email", e);
            throw new EasyCheckException(CommonMessageType.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void send10DaysBeforeReservationReminderEmail(String email, ReservationRoomView reservationDetails) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            final String SENDER_EMAIL_ADDRESS = "yonginfren@gmail.com";
            message.setFrom(new InternetAddress(SENDER_EMAIL_ADDRESS));
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject(REMINDER_BEFORE_10DAYS);

            String htmlContent = generateReservation10DaysReminderEmailContent(reservationDetails);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            mailSender.send(message);
            log.info("Reservation reminder email sent to {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send reservation reminder email", e);
            throw new EasyCheckException(CommonMessageType.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void sendSuggestionReply(SuggestionReplyRequestBody requestBody) {

        final String SENDER_EMAIL_ADDRESS = "yonginfren@gmail.com";

        log.info("sender = {}", SENDER_EMAIL_ADDRESS);
        // html 형식으로 내용을 첨부하기 위한 객체
        MimeMessage message = mailSender.createMimeMessage();

        try {
            // 송신자 메일 설정
            message.setFrom(new InternetAddress(SENDER_EMAIL_ADDRESS));

            SuggestionView suggestionView = SuggestionView.of(suggestionsRepository.findById(requestBody.getSuggestionId())
                    .orElseThrow(() -> new EasyCheckException(SuggestionMessageType.SUGGESTION_NOT_FOUND)));

            // 메일 내용 html로 생성
            String htmlContent = generateCustomerInquiryResponseContent(suggestionView, requestBody.getReplyContent());

            String email = suggestionView.getEmail();
            // 수신자 이메일 주소 설정
            message.setRecipients(MimeMessage.RecipientType.TO, email);

            // 메일 제목 설정
            message.setSubject(MAIL_SUBJECT);

            // 메일 내용 설정
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // 메일 전송
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EasyCheckException(CommonMessageType.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateVerificationCode() {
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final int CODE_LENGTH = 8;
        final SecureRandom RANDOM = new SecureRandom();

        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();

    }

    private String generateReservationConfirmationEmailContent(ReservationRoomView reservationDetails) {

        String title = "EasyCheck 예약 안내 메일";
        String mainContent = "<div style=\"width: 100%; max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif;\">" +
                "<div style=\"background-color: #f8f8f8; padding: 20px; border-radius: 8px;\">" +
                "<h1 style=\"color: #FF6B35; font-size: 24px; margin-bottom: 15px; text-align: center; font-weight: bold;\">" +
                "EasyCheck 예약 내역</h1>" +
                "<p style=\"font-size: 16px; color: #333333; text-align: center;\">" +
                "안녕하세요, <strong>" + reservationDetails.getUserName() + "</strong>님.</p>" +
                "<p style=\"font-size: 16px; color: #333333; text-align: center; margin-bottom: 20px;\">" +
                "다음과 같이 예약 내역을 확인해주세요.</p>" +
                "<div style=\"background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.1); margin-top: 20px;\">" +
                "<table style=\"width: 100%; border-collapse: collapse; font-size: 16px; color: #333333;\">" +
                "<tr>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd; border-right: 1px solid #ddd;\"><strong>객실 이름</strong></td>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\">" + reservationDetails.getTypeName() + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd; border-right: 1px solid #ddd;\"><strong>체크인 날짜</strong></td>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\">" + ReservationFormatUtil.formatLocalDateTime(reservationDetails.getCheckinDate().atStartOfDay()) + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd; border-right: 1px solid #ddd;\"><strong>체크아웃 날짜</strong></td>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\">" + ReservationFormatUtil.formatLocalDateTime(reservationDetails.getCheckoutDate().atStartOfDay()) + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd; border-right: 1px solid #ddd;\"><strong>예약 상태</strong></td>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\">" + ReservationFormatUtil.formatReservationStatus(reservationDetails.getReservationStatus()) + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd; border-right: 1px solid #ddd;\"><strong>총 가격</strong></td>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\">" + reservationDetails.getTotalPrice() + "원</td>" +
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 10px; border-right: 1px solid #ddd;\"><strong>결제 상태</strong></td>" +
                "<td style=\"padding: 10px;\">" + ReservationFormatUtil.formatPaymentStatus(reservationDetails.getPaymentStatus()) + "</td>" +
                "</tr>" +
                "</table>" +
                "</div>" +
                "</div>" +
                "<p style=\"color: #666666; font-size: 14px; text-align: center; margin-top: 20px;\">" +
                "감사합니다,<br><strong>EasyCheck 팀</strong></p>" +
                "</div>";



        return generateEmailTemplate(title, mainContent);
    }

    private String generateReservation3DaysReminderEmailContent(ReservationRoomView reservationDetails) {
        String title = "체크인 3일 전 예약 안내";
        String mainContent = "<div style=\"width: 100%; max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif;\">" +
                "<div style=\"background-color: #f8f8f8; padding: 20px; border-radius: 8px;\">" +
                "<h1 style=\"color: #FF6B35; font-size: 24px; margin-bottom: 15px; text-align: center; font-weight: bold;\">" +
                "EasyCheck 예약 체크인 안내</h1>" +
                "<p style=\"font-size: 16px; color: #333333; text-align: center;\">" +
                "안녕하세요, <strong>" + reservationDetails.getUserName() + "</strong>님.</p>" +
                "<p style=\"font-size: 16px; color: #333333; text-align: center; margin-bottom: 20px;\">" +
                "예약하신 객실의 체크인 날짜가 3일 남았습니다. 예약 정보를 확인해 주세요.</p>" +
                "<div style=\"background-color: #ffffff; padding: 20px; border-radius: 8px;\">" +
                "<table style=\"width: 100%; border-collapse: collapse; font-size: 16px; color: #333333;\">" +
                "<tr>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\"><strong>체크인 날짜</strong></td>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\">" + ReservationFormatUtil.formatLocalDateTime(reservationDetails.getCheckinDate().atStartOfDay()) + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\"><strong>체크아웃 날짜</strong></td>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\">" + ReservationFormatUtil.formatLocalDateTime(reservationDetails.getCheckoutDate().atStartOfDay()) + "</td>" +
                "</tr>" +
                "</table>" +
                "</div>" +
                "</div>" +
                "<p style=\"color: #666666; font-size: 14px; text-align: center; margin-top: 20px;\">" +
                "감사합니다,<br><strong>EasyCheck 팀</strong></p>" +
                "</div>";

        return generateEmailTemplate(title, mainContent);
    }

    private String generateReservation10DaysReminderEmailContent(ReservationRoomView reservationDetails) {
        String title = "체크인 10일 전 예약 안내";
        String mainContent = "<div style=\"width: 100%; max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif;\">" +
                "<div style=\"background-color: #f8f8f8; padding: 20px; border-radius: 8px;\">" +
                "<h1 style=\"color: #FF6B35; font-size: 24px; margin-bottom: 15px; text-align: center; font-weight: bold;\">" +
                "EasyCheck 예약 체크인 안내</h1>" +
                "<p style=\"font-size: 16px; color: #333333; text-align: center;\">" +
                "안녕하세요, <strong>" + reservationDetails.getUserName() + "</strong>님.</p>" +
                "<p style=\"font-size: 16px; color: #333333; text-align: center; margin-bottom: 20px;\">" +
                "예약하신 객실의 체크인 날짜가 10일 남았습니다. 예약 정보를 확인해 주세요.</p>" +
                "<div style=\"background-color: #ffffff; padding: 20px; border-radius: 8px;\">" +
                "<table style=\"width: 100%; border-collapse: collapse; font-size: 16px; color: #333333;\">" +
                "<tr>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\"><strong>체크인 날짜</strong></td>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\">" + ReservationFormatUtil.formatLocalDateTime(reservationDetails.getCheckinDate().atStartOfDay()) + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\"><strong>체크아웃 날짜</strong></td>" +
                "<td style=\"padding: 10px; border-bottom: 1px solid #ddd;\">" + ReservationFormatUtil.formatLocalDateTime(reservationDetails.getCheckoutDate().atStartOfDay()) + "</td>" +
                "</tr>" +
                "</table>" +
                "</div>" +
                "</div>" +
                "<p style=\"color: #666666; font-size: 14px; text-align: center; margin-top: 20px;\">" +
                "감사합니다,<br><strong>EasyCheck 팀</strong></p>" +
                "</div>";

        return generateEmailTemplate(title, mainContent);
    }

    private String generateCustomerInquiryResponseContent(SuggestionView suggestionView, String replyContent) {
        // 제목 설정
        String title = "EasyCheck 고객 건의사항 답변";

        // 답변 내용을 저장할 변수
        String responseContent = "";

        // 건의사항 주제에 따른 답변 내용 설정
        switch (suggestionView.getSubject()) {
            case "칭찬":
                responseContent = "<p style=\"font-size: 16px; line-height: 1.6;\">" +
                        "귀하의 의견에 감사드리며, 저희는 이를 적극 반영할 계획입니다." +
                        "</p>";
                break;
            case "문의":
                responseContent = "<p style=\"font-size: 16px; line-height: 1.6;\">" +
                        "귀하의 문의에 대해 추가 정보를 제공할 수 있도록 하겠습니다." +
                        "</p>";
                break;
            case "불만":
                responseContent = "<p style=\"font-size: 16px; line-height: 1.6;\">" +
                        "서비스 개선에 대한 귀하의 의견에 감사드리며, 저희는 이를 적극 반영할 계획입니다." +
                        "</p>";
                break;
            default:
                responseContent = "<p style=\"font-size: 16px; line-height: 1.6;\">" +
                        "기타 의견에 대해서도 소중하게 검토하고 있으며, 적절한 조치를 취할 것입니다." +
                        "</p>";
                break;
        }

        // 메인 내용 설정
        String mainContent =
                "<div style=\"font-family: 'Arial', sans-serif; color: #333333;\">" +
                        // 메일 인사말
                        "<p style=\"font-size: 16px; line-height: 1.6; margin-bottom: 20px;\">" +
                        "안녕하세요, <strong>" + suggestionView.getUserName() + "</strong>님,<br><br>" +
                        "고객님의 소중한 의견을 보내주셔서 감사합니다. " + responseContent +
                        "</p>" +

                        // 건의사항 및 답변 섹션
                        "<div style=\"background-color: #ffffff; padding: 20px; border-radius: 8px; margin-bottom: 20px;\">" +
                        "<h2 style=\"font-size: 18px; color: #8da6c5; margin-bottom: 10px;\">건의사항: " + suggestionView.getSubject() + "</h2>" +
                        "<p style=\"font-size: 16px; line-height: 1.6; color: #333333; margin: 0;\">" +
                        suggestionView.getContent() +
                        "</p>" +
                        "</div>" +

                        // 답변 내용
                        "<div style=\"background-color: #ffffff; padding: 20px; border-left: 4px solid #8da6c5; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);\">" +
                        "<p style=\"font-size: 16px; line-height: 1.6; color: #333333; margin: 0;\">" + replyContent +
                        "</p>" +
                        "</div>" +

                        // 추가 안내 사항
                        "<p style=\"font-size: 14px; color: #666666; line-height: 1.6; margin-bottom: 20px;\">" +
                        "추가 문의사항이 있으시면 언제든지 연락해 주시기 바랍니다." +
                        "</p>" +

                        // 마지막 인사말
                        "<p style=\"font-size: 14px; color: #333333; margin: 0; line-height: 1.6;\">" +
                        "감사합니다,<br>" +
                        "<strong>EasyCheck 고객지원팀</strong>" +
                        "</p>" +
                        "</div>";

        // 최종 이메일 템플릿 반환
        return generateEmailTemplate(title, mainContent);
    }



    private String generateVerificationCodeEmailContent(String verificationCode) {
        String title = "EasyCheck 인증 코드";
        String mainContent = "<h1 style=\"color: #FF6B35; font-size: 24px; margin: 0 0 15px 0; text-align: center; font-weight: bold;\">인증 코드 안내</h1>" +
                "<p style=\"color: #333333; font-size: 16px; line-height: 1.4; margin: 0 0 10px 0; text-align: center;\">" +
                "안녕하세요,<br>" +
                "아래의 인증 코드를 입력해 주세요." +
                "</p>" +
                "<div style=\"background-color: #f8f8f8; border-radius: 8px; padding: 15px; text-align: center; margin-bottom: 15px;\">" +
                "<h2 style=\"color: #FF6B35; font-size: 32px; margin: 0; letter-spacing: 5px; font-weight: bold;\">" +
                verificationCode +
                "</h2>" +
                "</div>" +
                "<p style=\"color: #666666; font-size: 14px; line-height: 1.4; margin: 0 0 15px 0; text-align: left;\">" +
                "* 인증 코드는 5분간 유효합니다." +
                "</p>" +
                "<p style=\"color: #333333; font-size: 14px; line-height: 1.4; margin: 0; text-align: left;\">" +
                "감사합니다,<br>" +
                "<strong>EasyCheck 팀</strong>" +
                "</p>";

        return generateEmailTemplate(title, mainContent);
    }

    private String generateEmailTemplate(String title, String mainContent) {
        final String FOOTER_CONTENT = "© 2024 EasyCheck. All rights reserved.<br>" +
                "서울특별시 강남구 테헤란로 123, EasyStay 타워";

        return "<!DOCTYPE html>" +
                "<html lang=\"ko\">" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>EasyCheck 인증 코드</title>" +
                "</head>" +
                "<body style=\"font-family: 'Helvetica Neue', Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;\">" +
                "<table role=\"presentation\" style=\"width: 100%; border-collapse: collapse;\">" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 40px 0;\">" +
                "<table role=\"presentation\" style=\"width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);\">" +
                "<tr>" +
                "<td style=\"padding: 40px 40px 20px 40px; text-align: center;\">" +
                "<img src=\"https://beyond-easycheck.s3.us-east-1.amazonaws.com/logos/email-logo.png\" alt=\"티켓링크 로고\" style=\"max-width: 100%; height: auto;\">" +
                "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 10px 20px;\">" +
                mainContent +
                "</td>" +
                "</tr>" +
                "</table>" +
                "<table role=\"presentation\" style=\"width: 100%; max-width: 600px; border-collapse: collapse;\">" +
                "<tr>" +
                "<td style=\"padding: 20px 0; text-align: center;\">" +
                "<p style=\"color: #999999; font-size: 12px; margin: 0; line-height: 1.4;\">" +
                FOOTER_CONTENT +
                "</p>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</body>" +
                "</html>";
    }




}
