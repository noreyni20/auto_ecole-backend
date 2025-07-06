package com.example.auto_ecole.Controller;

import com.example.auto_ecole.DTO.OtpRequest;
import com.example.auto_ecole.Entity.User;
import com.example.auto_ecole.Repository.UserRepository;
import com.example.auto_ecole.Service.OtpService;
import com.example.auto_ecole.Service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/otp")
@CrossOrigin("*")
public class PasswordResetController {

    @Autowired private OtpService otpService;
    @Autowired private SmsService smsService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestParam String phone) {
        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucun utilisateur trouvé avec ce numéro.");
        }

        String otp = otpService.generateOtp(phone);


        Map<String, String> response = new HashMap<>();
        response.put("message", "Code OTP généré avec succès");
        response.put("otp", otp);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtpAndResetPassword(@RequestBody OtpRequest request) {
        if (!otpService.verifyOtp(request.getPhone(), request.getOtp())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP invalide.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas.");
        }

        Optional<User> userOpt = userRepository.findByPhone(request.getPhone());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable.");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setEnabled(true);
        userRepository.save(user);
        otpService.clearOtp(request.getPhone());

        return ResponseEntity.ok("Mot de passe réinitialisé avec succès.");
    }
}
