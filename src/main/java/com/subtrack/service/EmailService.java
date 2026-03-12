package com.subtrack.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendReminderEmail(
            String to, String serviceName,
            String daysUntil, String cost) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(
                "SubTrack Reminder: " + serviceName
                        + " payment in " + daysUntil + " days");

        message.setText(
                "Hi,\n\n"
                        + "Your " + serviceName + " subscription ("
                        + cost + ") is due in " + daysUntil
                        + " days.\n\n"
                        + "If you want to cancel, do it before "
                        + "the charge date.\n\n"
                        + "- SubTrack");

        mailSender.send(message);
    }
}
