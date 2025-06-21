package com.example.auto_ecole.Controller;



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
    public ResponseEntity<?> createAdmin(@RequestBody AuthRequest request, Authentication auth) {

        System.out.println("👤 Connecté : " + auth.getName());
        System.out.println("🔐 Rôles : " + auth.getAuthorities());

        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Ce nom d'utilisateur existe déjà");
        }

        Role role = roleRepo.findByName("ROLE_ADMIN");
        if (role == null) {
            role = roleRepo.save(new Role("ROLE_ADMIN"));
        }

        User admin = new User();
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setPhone("772004305");
        admin.setRoles(Set.of(role));
        userRepo.save(admin);

        return ResponseEntity.ok("✔ Compte admin créé avec succès");
    }
}
