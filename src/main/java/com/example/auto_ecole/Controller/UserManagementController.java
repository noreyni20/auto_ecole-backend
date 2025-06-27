package com.example.auto_ecole.Controller;

import com.example.auto_ecole.DTO.AdminRequest;
import com.example.auto_ecole.DTO.AuthRequest;
import com.example.auto_ecole.Entity.Role;
import com.example.auto_ecole.Entity.User;
import com.example.auto_ecole.Repository.RoleRepository;
import com.example.auto_ecole.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/superadmin")
@CrossOrigin("*")
public class UserManagementController {

    @Autowired private UserRepository userRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createAdmin(@RequestBody AdminRequest request, Authentication auth) {

        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Ce nom d'utilisateur existe déjà");
        }

        Role role = roleRepo.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_ADMIN")));

        User admin = new User();
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setPhone(request.getPhone());
        admin.setAddress(request.getAddress());
        admin.setDrivingSchoolName(request.getDrivingSchoolName());
        admin.setRegistrationDate(LocalDate.now());
        admin.setRoles(Set.of(role));
        admin.setRole("ADMIN");
        admin.setCandidateQuota(5); // Par défaut

        userRepo.save(admin);

        return ResponseEntity.ok("✔ Compte admin créé avec succès");
    }

}
