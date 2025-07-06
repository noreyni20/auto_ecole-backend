package com.example.auto_ecole.Service;



import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    private Map<String, String> otpStorage = new HashMap<>();

    public String generateOtp(String phone) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(phone, otp);
        return otp;
    }

    public boolean verifyOtp(String phone, String otp) {
        return otpStorage.containsKey(phone) && otpStorage.get(phone).equals(otp);
    }

    public void clearOtp(String phone) {
        otpStorage.remove(phone);
    }
}

