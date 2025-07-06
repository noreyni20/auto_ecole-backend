package com.example.auto_ecole.Controller;

import com.example.auto_ecole.Entity.Role;
import com.example.auto_ecole.Entity.User;
import com.example.auto_ecole.Repository.RoleRepository;
import com.example.auto_ecole.Repository.UserRepository;
import com.example.auto_ecole.Service.SmsService;
import com.example.auto_ecole.Util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SmsService smsService;


    @PostMapping("/create-candidate")
    public ResponseEntity<?> createCandidate(@AuthenticationPrincipal UserDetails adminDetails,
                                             @RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String username = body.get("username");

        Optional<User> existing = userRepository.findByPhone(phone);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Candidat avec ce téléphone existe déjà.");
        }

        User admin = userRepository.findByUsername(adminDetails.getUsername()).orElseThrow();
        int quota = Optional.ofNullable(admin.getCandidateQuota()).orElse(0);
        int count = userRepository.findByCreatedBy(admin).size();

        if (count >= quota) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Quota atteint");
        }

        //  Génération du mot de passe temporaire
        String tempPassword = PasswordUtil.generateTemporaryPassword();

        // ✅ Rôle
        Role role = roleRepository.findByName("ROLE_CANDIDAT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_CANDIDAT")));

        // 👤 Création candidat
        User candidate = new User();
        candidate.setUsername(username);
        candidate.setPassword(passwordEncoder.encode(tempPassword));
        candidate.setPhone(phone);
        candidate.setCreatedBy(admin);
        candidate.setRoles(Set.of(role));
        candidate.setRegistrationDate(LocalDate.now());
        candidate.setExpirationDate(LocalDate.now().plusDays(30));
        candidate.setMustChangePassword(true); // 🔁 Forcer changement

        userRepository.save(candidate);

        // 📲 Envoi SMS avec numéro et mot de passe temporaire + lien
        String smsMessage = "Bienvenue sur Auto-École App.\n"
                + "Identifiant: " + phone + "\n"
                + "Mot de passe temporaire: " + tempPassword + "\n"
                + "Changez votre mot de passe ici : https://tonapp.com/reset-password";

        smsService.sendSms(phone, smsMessage);

        return ResponseEntity.ok("Candidat créé et SMS envoyé.");
    }


    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8); // Ex: "a1b2c3d4"
    }

    @GetMapping("/my-candidates")
    public ResponseEntity<?> listMyCandidates(@AuthenticationPrincipal UserDetails adminDetails) {
        // Récupérer l'utilisateur connecté
        User admin = userRepository.findByUsername(adminDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

        // Chercher tous les candidats créés par cet admin
        List<User> candidates = userRepository.findByCreatedBy(admin);

        // Transformer la liste des entités User en liste de DTO ou Map pour ne pas exposer le mot de passe
        List<Map<String, Object>> response = new ArrayList<>();
        for (User candidate : candidates) {
            Map<String, Object> map = new HashMap<>();
            map.put("username", candidate.getUsername());
            map.put("phone", candidate.getPhone());
            map.put("drivingSchoolName", candidate.getDrivingSchoolName());
            map.put("registrationDate", candidate.getRegistrationDate());
            map.put("expirationDate", candidate.getExpirationDate());
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }


    @PutMapping("/toggle-candidate-status/{candidateId}")
    public ResponseEntity<?> toggleCandidateStatus(@PathVariable Long candidateId,
                                                   @AuthenticationPrincipal UserDetails adminDetails) {

        User admin = userRepository.findByUsername(adminDetails.getUsername()).orElseThrow();
        Optional<User> userOpt = userRepository.findById(candidateId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User candidate = userOpt.get();


        if (!candidate.hasRole("CANDIDAT") || candidate.getCreatedBy() == null ||
                !candidate.getCreatedBy().getId().equals(admin.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ce compte ne vous appartient pas.");
        }


        candidate.setEnabled(!candidate.isEnabled());
        userRepository.save(candidate);

        String status = candidate.isEnabled() ? "activé" : "désactivé";
        return ResponseEntity.ok("Compte candidat " + status);
    }


}
