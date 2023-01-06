package com.syberry.bakery.service;

import com.syberry.bakery.dto.EmailDetails;

public interface MailService {
    void sendEmail(EmailDetails emailDetails);
}
