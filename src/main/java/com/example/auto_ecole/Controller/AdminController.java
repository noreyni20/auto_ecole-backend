package com.example.auto_ecole.Controller;

import com.example.auto_ecole.Entity.Role;
import com.example.auto_ecole.Entity.User;
import com.example.auto_ecole.Repository.RoleRepository;
import com.example.auto_ecole.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/create-candidate")
    public ResponseEntity<?> createCandidate(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");

        if (userRepo.findByUsername(phone).isPresent()) {
            return ResponseEntity.badRequest().body("Candidat existe déjà");
        }

        Role role = roleRepo.findByName("ROLE_CANDIDAT");
        if (role == null) {
            role = roleRepo.save(new Role("ROLE_CANDIDAT"));
        }

        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);

        User candidate = new User();
        candidate.setUsername(phone);
        candidate.setPassword(passwordEncoder.encode(generatedPassword));
        candidate.setPhone(phone);
        candidate.setRoles(Set.of(role));
        userRepo.save(candidate);

        return ResponseEntity.ok(Map.of(
                "phone", phone,
                "generatedPassword", generatedPassword
        ));
    }
}

