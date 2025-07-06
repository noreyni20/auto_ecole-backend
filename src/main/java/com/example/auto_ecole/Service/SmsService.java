package com.example.auto_ecole.Service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {


    public void sendSms(String toPhoneNumber, String message) {
        System.out.println("SMS envoyé à " + toPhoneNumber + " :\n" + message);

    }
}

