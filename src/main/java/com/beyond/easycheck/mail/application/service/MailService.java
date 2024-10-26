package com.beyond.easycheck.mail.application.service;

import com.beyond.easycheck.reservationrooms.ui.view.ReservationRoomView;
import com.beyond.easycheck.suggestion.ui.requestbody.SuggestionReplyRequestBody;

public interface MailService {

    void sendVerificationCode(String email);

    void verifyEmail(String code);

    void sendReservationConfirmationEmail(String email, ReservationRoomView reservationDetails);

    void send3DaysBeforeReservationReminderEmail(String email, ReservationRoomView reservationDetails);

    void send10DaysBeforeReservationReminderEmail(String email, ReservationRoomView reservationDetails);

    void sendSuggestionReply(SuggestionReplyRequestBody requestBody);
}
