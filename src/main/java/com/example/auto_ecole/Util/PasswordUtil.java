package com.example.auto_ecole.Util;

import java.util.Random;
import java.util.UUID;

public class PasswordUtil {

    // Génère un mot de passe temporaire aléatoire (8 caractères)
    public static String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // Génère un OTP numérique de 6 chiffres
    public static String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
