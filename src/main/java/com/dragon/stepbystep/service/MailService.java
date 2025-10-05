package com.dragon.stepbystep.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@stepbystep.com}")
    private String fromAddress;

    @Value("${app.mail.temp-password-subject:성큼성큼 임시 비밀번호 안내}")
    private String temporaryPasswordSubject;

    public void sendTemporaryPasswordEmail(String to, String temporaryPassword, long expirationMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(fromAddress);
        message.setSubject(temporaryPasswordSubject);
        message.setText(buildTemporaryPasswordBody(temporaryPassword, expirationMinutes));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send temporary password email to {}", to, ex);
            throw ex;
        }
    }

    private String buildTemporaryPasswordBody(String temporaryPassword, long expirationMinutes) {
        StringBuilder body = new StringBuilder();
        body.append("안녕하세요, 성큼성큼 입니다.\n\n")
                .append("요청하신 임시 비밀번호는 다음과 같습니다.\n")
                .append("임시 비밀번호: ")
                .append(temporaryPassword)
                .append("\n\n")
                .append("임시 비밀번호는 발급 후 ")
                .append(expirationMinutes)
                .append("분 동안만 유효합니다. 해당 시간 안에 로그인하여 비밀번호를 변경해주세요.\n\n")
                .append("감사합니다.");
        return body.toString();
    }
}
