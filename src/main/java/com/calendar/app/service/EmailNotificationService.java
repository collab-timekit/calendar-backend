package com.calendar.app.service;

import com.calendar.app.port.in.NotificationStrategy;
import com.calendar.domain.ReminderMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;

@Service("EMAIL")
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationStrategy {

    private final JavaMailSender mailSender;

    @Override
    public void execute(String message, String recipient) {
        try {
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(recipient);
            helper.setSubject("Wiadomość o wydarzeniu");
            helper.setText(message, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ReminderMethod getReminderMethod() {
        return ReminderMethod.EMAIL;
    }
}