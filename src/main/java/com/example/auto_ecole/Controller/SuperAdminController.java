package com.example.auto_ecole.Controller;

import com.example.auto_ecole.Entity.User;
import com.example.auto_ecole.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/superadmin")
public class SuperAdminController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/set-quota/{adminId}")
    public ResponseEntity<?> setQuota(@PathVariable Long adminId, @RequestParam int quota) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));
        admin.setCandidateQuota(quota);
        userRepository.save(admin);
        return ResponseEntity.ok("Quota mis à jour à " + quota);
    }


    @PutMapping("/toggle-admin-status/{adminId}")
    public ResponseEntity<?> toggleAdminStatus(@PathVariable Long adminId) {
        Optional<User> userOpt = userRepository.findById(adminId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User admin = userOpt.get();
        if (!"ADMIN".equals(admin.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ce compte n'est pas un administrateur.");
        }

        admin.setEnabled(!admin.isEnabled());
        userRepository.save(admin);

        String status = admin.isEnabled() ? "activé" : "désactivé";
        return ResponseEntity.ok("Compte administrateur " + status);
    }

}

