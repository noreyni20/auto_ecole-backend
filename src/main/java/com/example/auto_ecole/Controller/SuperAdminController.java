package com.example.auto_ecole.Controller;

import com.example.auto_ecole.Entity.User;
import com.example.auto_ecole.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}

