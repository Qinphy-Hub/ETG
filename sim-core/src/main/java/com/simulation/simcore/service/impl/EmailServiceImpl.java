package com.simulation.simcore.service.impl;

import com.simulation.simcore.entity.EmailProperties;
import com.simulation.simcore.service.EmailService;
import com.simulation.simcore.utils.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private EmailProperties emailProperties;
    @Autowired
    public void setEmailProperties(EmailProperties emailProperties) {
        this.emailProperties = emailProperties;
    }

    @Override
    public boolean sendEmail(String to, String title, String content) {
        return Email.sendEmail(emailProperties, to, title, content);
    }
}
