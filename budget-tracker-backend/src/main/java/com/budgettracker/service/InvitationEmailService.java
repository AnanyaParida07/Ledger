package com.budgettracker.service;

import com.budgettracker.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvitationEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public void sendInvitation(User user, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("You're invited to Ledger");
        message.setText("""
                Hello %s,

                You have been invited to Ledger. Sign in with the credentials below:

                Email: %s
                Temporary password: %s

                For your security, you will be required to choose a new password immediately after signing in.
                """.formatted(user.getFullName(), user.getEmail(), temporaryPassword));
        mailSender.send(message);
    }
}
