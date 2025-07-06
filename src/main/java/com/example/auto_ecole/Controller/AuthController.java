package com.example.auto_ecole.Controller;

import com.example.auto_ecole.Config.JwtUtil;
import com.example.auto_ecole.DTO.AuthRequest;
import com.example.auto_ecole.DTO.AuthResponse;
import com.example.auto_ecole.Entity.User;
import com.example.auto_ecole.Repository.UserRepository;
import com.example.auto_ecole.Service.FakeEmailService;
import com.example.auto_ecole.Service.FakeEmailService;
import com.example.auto_ecole.Service.SmsService;

import com.example.auto_ecole.Service.UserDetailsServiceImpl;
import com.example.auto_ecole.Util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserDetailsServiceImpl userDetailsService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private FakeEmailService emailService;
    @Autowired private SmsService smsService;

    private final Map<String, String> otpStorage = new HashMap<>();

    // 🔐 Connexion standard (admin ou candidat)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Échec d'authentification");
        }

        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");

        if (user.isMustChangePassword()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Mot de passe par défaut. Veuillez le modifier.");
        }

        String token = jwtUtil.generateToken(userDetailsService.loadUserByUsername(request.getUsername()));
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // ✅ ADMIN – Demande de réinitialisation via email
    @PostMapping("/admin/request-reset")
    public ResponseEntity<?> requestAdminPasswordReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Optional<User> userOpt = userRepository.findByUsername(email);

        if (userOpt.isEmpty() || !userOpt.get().hasRole("ADMIN")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin introuvable");
        }

        User admin = userOpt.get();
        String newPassword = PasswordUtil.generateTemporaryPassword();
        admin.setPassword(passwordEncoder.encode(newPassword));
        admin.setMustChangePassword(true);

        userRepository.save(admin);

        emailService.sendEmail(
                email,
                "🔐 Réinitialisation de votre mot de passe",
                "Bonjour,\n\nVoici vos nouveaux identifiants de connexion :\n" +
                        "Email : " + email + "\nMot de passe : " + newPassword + "\n\n" +
                        "Merci de le modifier dès la connexion."
        );

        return ResponseEntity.ok("✔ Nouveau mot de passe envoyé à votre email.");
    }

    // ✅ ADMIN – Modifier son mot de passe par défaut
    @PostMapping("/admin/change-password")
    public ResponseEntity<?> changeAdminPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        User admin = userRepository.findByUsername(username)
                .filter(u -> u.getPassword() != null && passwordEncoder.matches(oldPassword, u.getPassword()))
                .orElse(null);

        if (admin == null || !admin.hasRole("ADMIN")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authentification invalide");
        }

        admin.setPassword(passwordEncoder.encode(newPassword));
        admin.setMustChangePassword(false);
        userRepository.save(admin);

        return ResponseEntity.ok("✔ Mot de passe mis à jour.");
    }

    // ✅ CANDIDAT – Modifier mot de passe par défaut
    @PostMapping("/candidate/change-password")
    public ResponseEntity<?> changeCandidatePassword(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        User candidate = userRepository.findByPhone(phone)
                .filter(u -> passwordEncoder.matches(oldPassword, u.getPassword()))
                .orElse(null);

        if (candidate == null || !candidate.hasRole("CANDIDAT")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Identifiants invalides");
        }

        candidate.setPassword(passwordEncoder.encode(newPassword));
        candidate.setMustChangePassword(false);
        userRepository.save(candidate);

        return ResponseEntity.ok("✔ Mot de passe candidat mis à jour.");
    }

    //  CANDIDAT – Demander OTP
    @PostMapping("/candidate/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");

        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ Candidat non trouvé");
        }

        String otp = PasswordUtil.generateOtp();
        otpStorage.put(phone, otp); // otpStorage est une Map stockée temporairement

        // En mode test : afficher le code dans la réponse (ou console)
        return ResponseEntity.ok("✅ Code OTP (test) : " + otp);
    }


    //  CANDIDAT – Vérifier OTP
    @PostMapping("/candidate/verify-otp")
    public ResponseEntity<?> verifyOtpAndReset(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String otp = body.get("otp");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        String storedOtp = otpStorage.get(phone);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Code OTP invalide ou expiré");
        }

        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(" Les mots de passe ne correspondent pas");
        }

        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(" Utilisateur introuvable");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);

        otpStorage.remove(phone); // Nettoyer l’OTP

        return ResponseEntity.ok(" Nouveau mot de passe enregistré avec succès");
    }


    //  CANDIDAT – Réinitialiser le mot de passe après OTP
    @PostMapping("/candidate/reset-password")
    public ResponseEntity<?> resetCandidatePassword(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String newPassword = body.get("newPassword");

        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Candidat non trouvé");
        }

        User candidate = userOpt.get();
        candidate.setPassword(passwordEncoder.encode(newPassword));
        candidate.setMustChangePassword(false);
        userRepository.save(candidate);

        return ResponseEntity.ok("✔ Nouveau mot de passe enregistré.");
    }

    @PostMapping("/first-login-change-password")
    public ResponseEntity<?> changePasswordAtFirstLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable.");
        }

        User user = userOpt.get();

        // Vérifier si le mot de passe par défaut est correct
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mot de passe par défaut incorrect.");
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("Le mot de passe et la confirmation ne correspondent pas.");
        }

        // Mise à jour du mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);

        return ResponseEntity.ok("✔ Mot de passe mis à jour avec succès.");
    }

}
