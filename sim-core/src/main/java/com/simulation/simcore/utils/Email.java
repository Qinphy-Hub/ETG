package com.simulation.simcore.utils;

import com.simulation.simcore.entity.EmailProperties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class Email {
    public static boolean sendEmail(EmailProperties emailProperties, String to, String title, String content){
        MimeMessage message = null;
        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", emailProperties.getHost());
            properties.put("mail.smtp.auth",emailProperties.isAuth());
            properties.put("mail.user", emailProperties.getUser());
            properties.put("mail.password", emailProperties.getCode());
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailProperties.getUser(), emailProperties.getCode());
                }
            };
            Session mailSession = Session.getInstance(properties, authenticator);
            message = new MimeMessage(mailSession);
        }catch (Exception e){
            log.error("exception message:", e);
        }
        if (message==null){
            return false;
        }

        try {
            InternetAddress form = new InternetAddress(emailProperties.getUser());
            message.setFrom(form);
            InternetAddress toAddress = new InternetAddress(to);
            message.setRecipient(Message.RecipientType.TO, toAddress);
            message.setSubject(title);
            message.setContent(content, "text/html;charset=UTF-8");
            Transport.send(message);
        }catch (Exception e){
            log.error("exception message:", e);
        }
        return true;
    }
}
