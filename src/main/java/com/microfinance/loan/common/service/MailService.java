package com.microfinance.loan.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String smtpUsername;

    public MailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                       @Value("${app.mail.from:}") String fromAddress,
                       @Value("${spring.mail.username:}") String smtpUsername) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.fromAddress = fromAddress;
        this.smtpUsername = smtpUsername;
    }

    public void sendStaffCredentials(String toEmail,
                                     String fullName,
                                     String role,
                                     String loginCode,
                                     String rawPassword) {
        String subject = "Your " + role + " account credentials";
        String body = "Hello " + fullName + ",\n\n"
                + "Your account has been created.\n"
                + "Role: " + role + "\n"
                + "Login Code: " + loginCode + "\n"
                + "Password: " + rawPassword + "\n\n"
                + "Please change your password after first login.\n"
                + "- Loan Management System";

        sendPlainText(toEmail, subject, body);
    }

    public void sendCashDisbursalOtp(String toEmail, String fullName, String otp, String applicationNumber) {
        String subject = "Cash disbursal OTP for loan " + applicationNumber;
        String body = "Hello " + fullName + ",\n\n"
                + "Your cash disbursal OTP is: " + otp + "\n"
                + "Loan Application: " + applicationNumber + "\n"
                + "This OTP is valid for 10 minutes and should be shared only with assigned agent at handover.\n\n"
                + "- Loan Management System";
        sendPlainText(toEmail, subject, body);
    }

    private void sendPlainText(String toEmail, String subject, String body) {
        if (mailSender == null) {
            log.warn("JavaMailSender not configured. Skipping email to {} with subject '{}'.", toEmail, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            String effectiveFrom = StringUtils.hasText(fromAddress) ? fromAddress : smtpUsername;
            if (StringUtils.hasText(effectiveFrom)) {
                helper.setFrom(effectiveFrom);
            }
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            throw new IllegalStateException("Unable to send email to " + toEmail + ". Please verify SMTP host/port/username/password and app password settings.", ex);
        }
    }
}
