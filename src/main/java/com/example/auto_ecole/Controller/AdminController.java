package com.example.auto_ecole.Controller;

import com.example.auto_ecole.Entity.Role;
import com.example.auto_ecole.Entity.User;
import com.example.auto_ecole.Repository.RoleRepository;
import com.example.auto_ecole.Repository.UserRepository;
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

    @PostMapping("/create-candidate")
    public ResponseEntity<?> createCandidate(@AuthenticationPrincipal UserDetails adminDetails,
                                             @RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String username = body.get("username");
        String password = body.getOrDefault("password", generateRandomPassword());

        if (phone == null || username == null) {
            return ResponseEntity.badRequest().body("Le téléphone et le nom d'utilisateur sont obligatoires.");
        }

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

        Role role = roleRepository.findByName("ROLE_CANDIDAT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_CANDIDAT")));

        User candidate = new User();
        candidate.setUsername(username);
        candidate.setPassword(passwordEncoder.encode(password));
        candidate.setPhone(phone);
        candidate.setCreatedBy(admin);
        candidate.setRoles(Set.of(role));
        candidate.setRegistrationDate(LocalDate.now());
        candidate.setExpirationDate(LocalDate.now().plusDays(30));

        // Hériter du nom de l'auto-école de l'admin
        candidate.setDrivingSchoolName(admin.getDrivingSchoolName());

        userRepository.save(candidate);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Candidat créé avec succès.");
        response.put("mot_de_passe", password); // retourner le mot de passe généré
        return ResponseEntity.ok(response);
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

}
