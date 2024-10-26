package com.beyond.easycheck.mail.application.service;

import com.beyond.easycheck.reservationrooms.ui.view.ReservationRoomView;
import com.beyond.easycheck.suggestion.ui.requestbody.SuggestionReplyRequestBody;

public interface MailService {

    void sendVerificationCode(String email);

    void verifyEmail(String code);

    void sendReservationConfirmationEmail(String email, ReservationRoomView reservationDetails);

    void sendReservationReminderEmail(String email, ReservationRoomView reservationDetails);

    void sendSuggestionReply(SuggestionReplyRequestBody requestBody);
}
